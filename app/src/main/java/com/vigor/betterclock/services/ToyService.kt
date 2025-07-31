package com.vigor.betterclock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import com.vigor.betterclock.utils.Graphics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ToyService : GlyphMatrixService("BetterClock") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var aod = false
    private var battery_level: Int = 0
    private var battery_charging: Boolean = false
    private var animate_percent: Int = 110

    private val batteryFilter = IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
    }
    private val battery_receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            battery_level = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                0
            }
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            battery_charging = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_FULL -> true
                else -> false
            }
            if (battery_charging && !handler.hasCallbacks(charge_animation_runnable)) {
                handler.removeCallbacks(charge_animation_runnable)
                handler.post(charge_animation_runnable)
            } else if (!battery_charging) {
                handler.removeCallbacks(charge_animation_runnable)
            }
            refresh()
        }
    }

    private val timeFilter = IntentFilter(Intent.ACTION_TIME_TICK)
    private val time_receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_TIME_TICK) {
                if (aod) refresh()
            }
        }
    }

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        registerReceiver(battery_receiver, batteryFilter, RECEIVER_EXPORTED)
        registerReceiver(time_receiver, timeFilter, RECEIVER_EXPORTED)

        val p = get_battery_info()
        battery_level = p.first
        battery_charging = p.second

        backgroundScope.launch {
            // We don't want to stop mid animation
            while (isActive && (!aod || animate_percent <= 100)) {
                uiScope.launch {
                    refresh()
                    if (animate_percent <= 100) {
                        animate_percent += 10
                    }
                }
                // wait a bit
                delay(100)
            }
        }
    }

    fun charge_animation() {
        animate_percent = 0
        if (!aod) {
            return
        }
        backgroundScope.launch {
            while (isActive && animate_percent <= 100) {
                uiScope.launch {
                    refresh()
                    animate_percent += 10
                }
                // wait a bit
                delay(100)
            }
            uiScope.launch {
                refresh()
            }
        }
    }

    override fun onAODEvent(glyphMatrixManager: GlyphMatrixManager?) {
        aod = true
    }

    fun refresh() {
        glyphMatrixManager?.setMatrixFrame(frame(time(blink=!aod), battery_level, battery_charging))
    }

    fun time(blink: Boolean): String {
        val ms = System.currentTimeMillis()
        val date = Date()
        val formatter = SimpleDateFormat("HH${if ((ms/1000).toInt()%2 == 0 || !blink) ":" else " "}mm", Locale.getDefault())
        return formatter.format(date)
    }

    fun get_battery_info(): Pair<Int, Boolean> {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = registerReceiver(null, intentFilter)

        batteryStatus?.let {
            // Get battery level and scale
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = (level * 100) / scale

            // Charging status
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            return Pair(batteryPct, isCharging)
        }

        return Pair(-1, false)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val charge_animation_runnable = object : Runnable {
        override fun run() {
            charge_animation()
            // re-schedule
            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    fun frame(time: String, battery_level: Int, charging: Boolean): IntArray? {
        val clock_builder = GlyphMatrixObject.Builder()
            .setText(time)
            .setTextStyle("tall")
            .setPosition(2, 9)
            .setScale(100)
            .setBrightness(255)

        val clock = clock_builder.build()
        val charge_icon = if (charging)
            Graphics.copy(src=lightning, dst=IntArray(WIDTH*HEIGHT), pos=Pair(9, 20), dimensions=Pair(7, 3), multiplier=255)
        else
            IntArray(WIDTH*HEIGHT)
        var battery_bar = Graphics.fill_bar(battery_level, pos=Pair(3, 18), width=19, colors=Pair(512, 40))
        if (animate_percent <= 100) {
            val x_pos = 3 + ((19 * battery_level * animate_percent) / 10000)
            battery_bar = Graphics.set_pixel(battery_bar, WIDTH, HEIGHT, x_pos, 18, 1024)
        }

        val frameBuilder = GlyphMatrixFrame.Builder()
            .addTop(clock)
            .addMid(charge_icon)
            .addLow(battery_bar)

        val frame = frameBuilder.build(this)

        return frame.render()
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
        unregisterReceiver(battery_receiver)
        unregisterReceiver(time_receiver)
        handler.removeCallbacks(charge_animation_runnable)
    }

    private companion object {
        private const val WIDTH = 25
        private const val HEIGHT = 25
        private const val INTERVAL_MS = 10_000L
        private val lightning = intArrayOf(
            1, 0, 0, 0, 1, 0, 0,
            0, 1, 0, 1, 0, 1, 0,
            0, 0, 1, 0, 0, 0, 1,
        )
    }
}