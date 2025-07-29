package com.vigor.betterclock.services

import android.content.Context
import com.example.matrix.services.GlyphMatrixService
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

class ToyService : GlyphMatrixService("BetterClock") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        backgroundScope.launch {
            while (isActive) {
                uiScope.launch {
                    glyphMatrixManager.setMatrixFrame(frame())
                }
                // wait a bit
                delay(100)
            }
        }
    }

    fun frame(): IntArray? {
        val clock_builder = GlyphMatrixObject.Builder()
            .setText("21:50")
            .setTextStyle("tall")
            .setPosition(2, 9)
            .setScale(100)
            .setBrightness(255)

        val clock = clock_builder.build()

        val frameBuilder = GlyphMatrixFrame.Builder()
            .addTop(clock)
            .addMid(Graphics.copy(src=lightning, dst=IntArray(WIDTH*HEIGHT), pos=Pair(11, 1), dimensions=Pair(3, 7), multiplier=255))
            .addLow(Graphics.fill_bar(60, pos=Pair(3, 18), width=19, colors=Pair(512, 40)))

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