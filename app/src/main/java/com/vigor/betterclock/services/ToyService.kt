package com.vigor.betterclock.services

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        backgroundScope.launch {
            while (isActive && !aod) {
                uiScope.launch {
                    val (battery_level, battery_charging) = get_battery_info()
                    glyphMatrixManager.setMatrixFrame(frame(time(blink=false), battery_level, battery_charging))
                }
                // wait a bit
                delay(100)
            }
        }
    }

    override fun onAODEvent(glyphMatrixManager: GlyphMatrixManager?) {
        aod = true
        backgroundScope.launch {
            uiScope.launch {
                val (battery_level, _) = get_battery_info()
                glyphMatrixManager?.setMatrixFrame(frame(time(blink=false), battery_level, false))
            }
        }
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

    fun frame(time: String, battery_level: Int, charging: Boolean): IntArray? {
        val clock_builder = GlyphMatrixObject.Builder()
            .setText(time)
            .setTextStyle("tall")
            .setPosition(2, 9)
            .setScale(100)
            .setBrightness(255)

        val clock = clock_builder.build()
        val charge_icon = if (charging)
            Graphics.copy(src=lightning, dst=IntArray(WIDTH*HEIGHT), pos=Pair(11, 1), dimensions=Pair(3, 7), multiplier=255)
        else
            IntArray(WIDTH*HEIGHT)
        val battery_bar = Graphics.fill_bar(battery_level, pos=Pair(3, 18), width=19, colors=Pair(512, 40))

        val frameBuilder = GlyphMatrixFrame.Builder()
            .addTop(clock)
            .addMid(charge_icon)
            .addLow(battery_bar)

        val frame = frameBuilder.build(this)

        return frame.render()
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private companion object {
        private const val WIDTH = 25
        private const val HEIGHT = 25
        private val lightning = intArrayOf(
            0, 0, 1,
            0, 1, 0,
            1, 0, 0,
            0, 1, 0,
            0, 0, 1,
            0, 1, 0,
            1, 0, 0
        )
    }
}