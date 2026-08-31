package com.example.ai

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.model.ArtHaxInstructionSet
import com.example.model.DrawingLayer
import com.example.model.DrawingPoint
import com.example.model.DrawingStroke
import com.example.model.StrokeType
import java.util.ArrayDeque
import java.util.UUID
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance, on-device AI-Less Image Contour and Tap-to-Segment Vectorizer.
 * Extracts vector stroke paths directly from photos, character sketches, plants, and tattoos
 * without requiring any network connection or cloud API tokens.
 */
object ImageTraceEngine {

    /**
     * Traces the whole image using multi-threshold edge gradients into silhouette & detail layers.
     */
    fun traceFullImageContours(
        bitmap: Bitmap,
        title: String = "Image Auto-Trace",
        contrastSensitivity: Float = 0.35f,
        colorHex: String = "#00F0FF"
    ): ArtHaxInstructionSet {
        val scaledBitmap = scaleBitmapToWorkingSize(bitmap, maxDimension = 320)
        val width = scaledBitmap.width
        val height = scaledBitmap.height

        val luminance = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = scaledBitmap.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)
                // Standard perceptual luminance
                luminance[y][x] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }
        }

        // 1. Sobel Edge Gradient Filter
        val edges = Array(height) { BooleanArray(width) }
        val threshold = (contrastSensitivity * 255f).toInt().coerceIn(20, 180)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val gx = (-1 * luminance[y - 1][x - 1]) + (1 * luminance[y - 1][x + 1]) +
                        (-2 * luminance[y][x - 1]) + (2 * luminance[y][x + 1]) +
                        (-1 * luminance[y + 1][x - 1]) + (1 * luminance[y + 1][x + 1])

                val gy = (-1 * luminance[y - 1][x - 1]) + (-2 * luminance[y - 1][x]) + (-1 * luminance[y - 1][x + 1]) +
                        (1 * luminance[y + 1][x - 1]) + (2 * luminance[y + 1][x]) + (1 * luminance[y + 1][x + 1])

                val magnitude = hypot(gx.toDouble(), gy.toDouble()).toInt()
                if (magnitude >= threshold) {
                    edges[y][x] = true
                }
            }
        }

        // 2. Extract continuous vector chains
        val rawStrokes = extractVectorChains(edges, width, height, colorHex)

        // 3. Separate into Main Outline Layer and Shading Detail Layer
        val outlineStrokes = mutableListOf<DrawingStroke>()
        val detailStrokes = mutableListOf<DrawingStroke>()

        rawStrokes.forEach { stroke ->
            if (stroke.points.size >= 8) {
                outlineStrokes.add(stroke.copy(strokeWidth = 4.0f, colorHex = "#00F0FF"))
            } else if (stroke.points.size >= 3) {
                detailStrokes.add(stroke.copy(strokeWidth = 2.5f, colorHex = "#FF00E5"))
            }
        }

        val layers = listOf(
            DrawingLayer(
                id = "layer_img_outline",
                name = "Layer 1 - Silhouette Outlines",
                colorTagHex = "#00F0FF",
                strokes = outlineStrokes
            ),
            DrawingLayer(
                id = "layer_img_details",
                name = "Layer 2 - Inner Contours & Details",
                colorTagHex = "#FF00E5",
                strokes = detailStrokes
            )
        )

        return ArtHaxInstructionSet(
            id = UUID.randomUUID().toString(),
            title = title,
            prompt = "Image Trace: $title",
            model = "AI-Less Vector Engine (Local On-Device)",
            strokes = outlineStrokes + detailStrokes,
            layers = layers,
            canvasAspectRatio = width.toFloat() / height.toFloat().coerceAtLeast(1f)
        )
    }

    /**
     * Tap-To-Segment Mode: Given a user tap at (tapNormX, tapNormY) on the image (e.g. character, plant, logo),
     * isolates the connected region using color/luminance thresholding and builds focused vector outline strokes.
     */
    fun traceTappedSubject(
        bitmap: Bitmap,
        tapNormX: Float,
        tapNormY: Float,
        tolerance: Float = 0.28f,
        subjectLabel: String = "Tapped Subject"
    ): ArtHaxInstructionSet {
        val scaledBitmap = scaleBitmapToWorkingSize(bitmap, maxDimension = 320)
        val width = scaledBitmap.width
        val height = scaledBitmap.height

        val seedX = (tapNormX * width).toInt().coerceIn(0, width - 1)
        val seedY = (tapNormY * height).toInt().coerceIn(0, height - 1)

        val targetPixel = scaledBitmap.getPixel(seedX, seedY)
        val targetR = AndroidColor.red(targetPixel)
        val targetG = AndroidColor.green(targetPixel)
        val targetB = AndroidColor.blue(targetPixel)
        val maxDistSq = (tolerance * 255f) * (tolerance * 255f) * 3f

        // Flood Fill / Connected Component Mask
        val mask = Array(height) { BooleanArray(width) }
        val visited = Array(height) { BooleanArray(width) }
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(Pair(seedX, seedY))
        visited[seedY][seedX] = true

        val dx = intArrayOf(0, 1, 0, -1, 1, 1, -1, -1)
        val dy = intArrayOf(-1, 0, 1, 0, -1, 1, 1, -1)

        var count = 0
        val maxPixels = (width * height * 0.85f).toInt()

        while (queue.isNotEmpty() && count < maxPixels) {
            val (cx, cy) = queue.removeFirst()
            mask[cy][cx] = true
            count++

            for (d in 0 until 8) {
                val nx = cx + dx[d]
                val ny = cy + dy[d]

                if (nx in 0 until width && ny in 0 until height && !visited[ny][nx]) {
                    visited[ny][nx] = true
                    val p = scaledBitmap.getPixel(nx, ny)
                    val dr = AndroidColor.red(p) - targetR
                    val dg = AndroidColor.green(p) - targetG
                    val db = AndroidColor.blue(p) - targetB
                    val distSq = (dr * dr + dg * dg + db * db).toFloat()

                    if (distSq <= maxDistSq) {
                        queue.add(Pair(nx, ny))
                    }
                }
            }
        }

        // Find boundary edges of the connected mask
        val boundaryEdges = Array(height) { BooleanArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (mask[y][x]) {
                    var isBorder = false
                    for (d in 0 until 4) {
                        val nx = x + dx[d]
                        val ny = y + dy[d]
                        if (nx !in 0 until width || ny !in 0 until height || !mask[ny][nx]) {
                            isBorder = true
                            break
                        }
                    }
                    if (isBorder) {
                        boundaryEdges[y][x] = true
                    }
                }
            }
        }

        val subjectStrokes = extractVectorChains(boundaryEdges, width, height, colorHex = "#00FF88")

        val layers = listOf(
            DrawingLayer(
                id = "layer_subject_boundary",
                name = "Subject: $subjectLabel",
                colorTagHex = "#00FF88",
                strokes = subjectStrokes
            )
        )

        return ArtHaxInstructionSet(
            id = UUID.randomUUID().toString(),
            title = "Segment: $subjectLabel",
            prompt = "Tap Segment ($subjectLabel)",
            model = "On-Device Tap-To-Segment",
            strokes = subjectStrokes,
            layers = layers,
            canvasAspectRatio = width.toFloat() / height.toFloat().coerceAtLeast(1f)
        )
    }

    private fun extractVectorChains(
        edges: Array<BooleanArray>,
        width: Int,
        height: Int,
        colorHex: String
    ): List<DrawingStroke> {
        val visited = Array(height) { BooleanArray(width) }
        val strokeList = mutableListOf<DrawingStroke>()

        val dx = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
        val dy = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (edges[y][x] && !visited[y][x]) {
                    val rawPoints = mutableListOf<DrawingPoint>()
                    var currX = x
                    var currY = y

                    while (true) {
                        visited[currY][currX] = true
                        rawPoints.add(
                            DrawingPoint(
                                x = (currX.toFloat() / width).coerceIn(0f, 1f),
                                y = (currY.toFloat() / height).coerceIn(0f, 1f)
                            )
                        )

                        var nextX = -1
                        var nextY = -1
                        for (d in 0 until 8) {
                            val nx = currX + dx[d]
                            val ny = currY + dy[d]
                            if (nx in 0 until width && ny in 0 until height && edges[ny][nx] && !visited[ny][nx]) {
                                nextX = nx
                                nextY = ny
                                break
                            }
                        }

                        if (nextX != -1) {
                            currX = nextX
                            currY = nextY
                        } else {
                            break
                        }
                    }

                    // Douglas-Peucker Simplification to keep vector points smooth and compact
                    if (rawPoints.size >= 3) {
                        val simplified = douglasPeuckerSimplify(rawPoints, epsilon = 0.004f)
                        if (simplified.size >= 2) {
                            strokeList.add(
                                DrawingStroke(
                                    id = UUID.randomUUID().toString(),
                                    points = simplified,
                                    colorHex = colorHex,
                                    strokeWidth = 3.5f,
                                    strokeType = StrokeType.CURVE
                                )
                            )
                        }
                    }
                }
            }
        }

        return strokeList
    }

    /**
     * Classic Douglas-Peucker line simplification algorithm
     */
    private fun douglasPeuckerSimplify(points: List<DrawingPoint>, epsilon: Float): List<DrawingPoint> {
        if (points.size <= 2) return points

        var dmax = 0.0f
        var index = 0
        val end = points.size - 1

        for (i in 1 until end) {
            val d = perpendicularDistance(points[i], points[0], points[end])
            if (d > dmax) {
                index = i
                dmax = d
            }
        }

        return if (dmax > epsilon) {
            val recResults1 = douglasPeuckerSimplify(points.subList(0, index + 1), epsilon)
            val recResults2 = douglasPeuckerSimplify(points.subList(index, points.size), epsilon)
            recResults1.dropLast(1) + recResults2
        } else {
            listOf(points[0], points[end])
        }
    }

    private fun perpendicularDistance(p: DrawingPoint, p1: DrawingPoint, p2: DrawingPoint): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val mag = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (mag == 0.0f) {
            return hypot((p.x - p1.x).toDouble(), (p.y - p1.y).toDouble()).toFloat()
        }
        val num = abs(dy * p.x - dx * p.y + p2.x * p1.y - p2.y * p1.x)
        return num / mag
    }

    private fun scaleBitmapToWorkingSize(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDimension && h <= maxDimension) return bitmap

        val ratio = w.toFloat() / h.toFloat()
        val targetW: Int
        val targetH: Int
        if (w > h) {
            targetW = maxDimension
            targetH = (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            targetH = maxDimension
            targetW = (maxDimension * ratio).toInt().coerceAtLeast(1)
        }

        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}
