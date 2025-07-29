package com.vigor.betterclock.utils

import kotlin.math.*
import kotlin.math.roundToInt
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceAtMost
import kotlin.ranges.until
import kotlin.to

class Graphics {
    companion object {
        fun rotateGridArbitrary(
            input: IntArray,
            width: Int,
            height: Int,
            degrees: Double
        ): IntArray {
            require(input.size == width * height) { "Grid size does not match dimensions." }

            val output = IntArray(input.size) { 0 }  // default to black or empty
            val radians = Math.toRadians(degrees)
            val cosTheta = cos(radians)
            val sinTheta = sin(radians)

            val cx = (width / 2).toFloat()
            val cy = (height / 2).toFloat()

            for (y in 0 until height) {
                for (x in 0 until width) {
                    // translate to center
                    val dx = x - cx
                    val dy = y - cy

                    // rotate
                    val srcX =  dx * cosTheta + dy * sinTheta + cx
                    val srcY = -dx * sinTheta + dy * cosTheta + cy

                    // nearest neighbor
                    val srcXi = srcX.roundToInt()
                    val srcYi = srcY.roundToInt()

                    // bounds check
                    if (srcXi in 0 until width && srcYi in 0 until height) {
                        val destIndex = x + y * width
                        val srcIndex = srcXi + srcYi * width
                        output[destIndex] = input[srcIndex]
                    }
                }
            }

            return output
        }
        /**
         * Sets a single pixel if the (x,y) coordinate lies inside the canvas bounds.
         */
        fun setPixel(canvas: Array<Int>, width: Int, height: Int,
                            x: Int, y: Int, brightness: Int = 0) {
            if (x in 0 until width && y in 0 until height) {
                canvas[y * width + x] = brightness
            }
        }

        /**
         * Draws a filled (solid) circle with centre (cx,cy) and given radius.
         *
         * @param canvas  Mutable 1-D IntArray holding the pixels (row-major order).
         * @param width   Canvas width in pixels.
         * @param height  Canvas height in pixels.
         * @param cx      X coordinate of circle centre.
         * @param cy      Y coordinate of circle centre.
         * @param radius  Circle radius in pixels (≥ 0).
         * @param color   Pixel value to write (default 1).
         */
        fun drawCircle(canvas: Array<Int>,
                       width: Int, height: Int,
                       cx: Int, cy: Int,
                       radius: Int,
                       brightness: Int = 0) {

            if (radius <= 0) {
                setPixel(canvas, width, height, cx, cy, brightness)
                return
            }

            var x = radius
            var y = 0
            var decision = 1 - radius     // Bresenham / midpoint circle variable

            // Outline points, mirrored into all eight octants;
            // then fill each scan line horizontally.
            val ySpans = ArrayList<IntArray>()    // collect min/max x for each y

            while (y <= x) {
                // Store x-ranges for symmetric y rows
                for ((yy, xx) in listOf(
                    cy + y to x,
                    cy - y to x,
                    cy + x to y,
                    cy - x to y
                )) {
                    if (yy !in 0 until height) continue
                    val span = intArrayOf(cx - xx, cx + xx)
                    ySpans.add(intArrayOf(yy, span[0], span[1]))
                }

                y++
                decision += if (decision <= 0) 2 * y + 1 else {
                    x--
                    2 * (y - x) + 1
                }
            }

            // Fill horizontal segments collected above
            for ((yy, xMin, xMax) in ySpans) {
                val clampedMin = xMin.coerceAtLeast(0)
                val clampedMax = xMax.coerceAtMost(width - 1)
                for (xx in clampedMin..clampedMax) {
                    canvas[yy * width + xx] = brightness
                }
            }
        }

        /**
         * Draws a straight line from (x0,y0) to (x1,y1) using Bresenham's algorithm.
         *
         * @param canvas  Mutable 1-D IntArray holding the pixels (row-major order).
         * @param width   Canvas width in pixels.
         * @param height  Canvas height in pixels.
         * @param x0,y0   Starting point.
         * @param x1,y1   End point.
         * @param color   Pixel value to write (default 1).
         */
        fun drawLine(canvas: Array<Int>,
                     width: Int, height: Int,
                     x0: Int, y0: Int,
                     x1: Int, y1: Int,
                     brightness: Int = 0) {

            var x0m = x0
            var y0m = y0
            var x1m = x1
            var y1m = y1

            val dx = abs(x1m - x0m)
            val sx = if (x0m < x1m) 1 else -1
            val dy = -abs(y1m - y0m)
            val sy = if (y0m < y1m) 1 else -1
            var err = dx + dy        // error value e₂ in the Wikipedia article

            while (true) {
                setPixel(canvas, width, height, x0m, y0m, brightness)
                if (x0m == x1m && y0m == y1m) break
                val e2 = 2 * err
                if (e2 >= dy) {               // e₂ ≥ e_y
                    err += dy
                    x0m += sx
                }
                if (e2 <= dx) {               // e₂ ≤ e_x
                    err += dx
                    y0m += sy
                }
            }
        }

        /**
         * Draws a sun with eight rays.
         *
         * @param canvas     pixel buffer (Array<Int>)
         * @param w,h        canvas dimensions
         * @param cx,cy      sun centre
         * @param r          radius of the sun’s disk
         * @param rayLen     length of each ray
         * @param rayGap     gap between disk edge and the start of a ray
         * @param brightDisk brightness value for the disk
         * @param brightRay  brightness value for the ray
         */
        fun drawSun(
            canvas: Array<Int>, w: Int, h: Int,
            cx: Int, cy: Int,
            r: Int,
            rayLen: Int,
            rayGap: Int = 0,
            brightDisk: Int = 255,
            brightRay: Int = 255
        ) {
            // 1. Draw the solar disk
            drawCircle(canvas, w, h, cx, cy, r, brightDisk)

            // 2. Draw rays in 8 directions (every 45°)
            val startDist = r + rayGap          // where the ray begins
            val endDist   = startDist + rayLen  // where the ray ends
            val anglesDeg = intArrayOf(0, 45, 90, 135, 180, 225, 270, 315)

            for (a in anglesDeg) {
                val rad = Math.toRadians(a.toDouble())
                val x0 = cx + (startDist * cos(rad)).roundToInt()
                val y0 = cy + (startDist * sin(rad)).roundToInt()
                val x1 = cx + (endDist   * cos(rad)).roundToInt()
                val y1 = cy + (endDist   * sin(rad)).roundToInt()

                drawLine(canvas, w, h, x0, y0, x1, y1, brightRay)
            }
        }

        fun minx(y: Int): Int {
            if (y < 0 || y > 24) return -1
            // We solve (x - 12)**2 + (y - 12)**2 <= (12.5)**2
            // Smallest x that satisfies this is:
            // x_min = |12 - sqrt(12.5**2 - (y-12)**2)|
            val R: Double = 25.0/2.0
            val C = 12.0;
            val dy: Double = y.toDouble() - C
            val dx = sqrt(R*R - dy*dy)
            return ceil(C - dx).toInt()
        }

        fun fill_circle(i: Int, brightness: Int = 255): IntArray {
            val arr = IntArray(25*25)
            var sum: Int = ((489.0*i)/100).toInt() // 489 pixels on the matrix circle
            var x_min: Int
            var width: Int
            for (y in 24 downTo 0) {
                if (sum <= 0)
                    break // No more pixels to fill
                x_min = minx(y)
                width = 25-x_min*2
                sum -= width
                // If we go past the sum, the entire row should not be filled
                if (sum < 0)
                    width += sum
                // Fill width amount of pixels
                for (x in 0..<width)
                    arr[x_min + x + y*25] = brightness
            }
            return arr
        }
        fun fill(arr: IntArray, pos: Pair<Int, Int>, dimensions: Pair<Int, Int>, brightness: Int): IntArray {
            for (y in 0..<dimensions.second) {
                for (x in 0..<dimensions.first) {
                    arr[x + pos.first + (y + pos.second)*25] = brightness
                }
            }
            return arr
        }
        fun fill_bar(progress: Int, pos: Pair<Int, Int>, width: Int, colors: Pair<Int, Int> = Pair(255,0)): IntArray {
            val arr = IntArray(25*25)
            val filled_width = (width * progress)/100
            for (x in pos.first..<(pos.first + width))
                arr[x + pos.second*25] = if (pos.first + filled_width >= x) colors.first else colors.second
            return arr
        }
        // We assume src always fit where we are writing inside dst and dimensions are correct
        fun copy(src: IntArray, dst: IntArray, pos: Pair<Int, Int>, dimensions: Pair<Int, Int>, multiplier: Int = 1): IntArray {
            for (y in 0..<dimensions.second) {
                for (x in 0..<dimensions.first) {
                    dst[x + pos.first + (y + pos.second)*25] = src[x + y * dimensions.first] * multiplier
                }
            }
            return dst
        }
    }
}