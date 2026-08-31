package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.DrawingPoint
import com.example.model.DrawingSettings
import com.example.model.DrawingStroke
import com.example.model.ExecutionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * High-performance Accessibility Service that translates ArtHax vector drawing instructions
 * into real hardware touch gestures on target applications (like Sekai canvas).
 */
class ArtHaxAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var activeDrawingJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        currentInstance = this
        _isServiceActive.value = true
        Log.i(TAG, "ArtHax Accessibility Service connected successfully.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op - we perform gesture execution on demand
    }

    override fun onInterrupt() {
        abortCurrentDrawing()
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentInstance == this) {
            currentInstance = null
        }
        _isServiceActive.value = false
    }

    /**
     * Executes drawing instruction set via real gestures on screen.
     */
    fun startDrawing(
        instructionSet: ArtHaxInstructionSet,
        bounds: CalibrationBounds,
        settings: DrawingSettings,
        screenWidth: Int,
        screenHeight: Int,
        onStateUpdate: (ExecutionState) -> Unit
    ) {
        abortCurrentDrawing()

        activeDrawingJob = serviceScope.launch {
            try {
                val strokesToDraw = instructionSet.activeStrokes
                val totalStrokes = strokesToDraw.size
                val totalPoints = strokesToDraw.sumOf { it.points.size }.coerceAtLeast(1)
                var drawnPoints = 0
                val startTime = System.currentTimeMillis()

                val canvasLeft = bounds.left * screenWidth
                val canvasTop = bounds.top * screenHeight
                val canvasWidth = bounds.width * screenWidth
                val canvasHeight = bounds.height * screenHeight

                for (strokeIdx in 0 until totalStrokes) {
                    val stroke = strokesToDraw[strokeIdx]
                    if (stroke.points.isEmpty()) continue

                    // Build smooth gesture path with Spline/Quadratic interpolation
                    val path = Path()
                    val pts = stroke.points

                    if (settings.executionProfile == com.example.model.ExecutionProfile.ORGANIC_HUMAN && pts.size >= 3) {
                        // Smooth Catmull-Rom / Bezier spline interpolation for natural human hand strokes
                        val firstX = canvasLeft + (pts[0].x * canvasWidth)
                        val firstY = canvasTop + (pts[0].y * canvasHeight)
                        path.moveTo(firstX, firstY)

                        for (i in 1 until pts.size) {
                            val prev = pts[i - 1]
                            val curr = pts[i]
                            val px = canvasLeft + (prev.x * canvasWidth)
                            val py = canvasTop + (prev.y * canvasHeight)
                            val cx = canvasLeft + (curr.x * canvasWidth)
                            val cy = canvasTop + (curr.y * canvasHeight)
                            val midX = (px + cx) * 0.5f
                            val midY = (py + cy) * 0.5f
                            path.quadTo(px, py, midX, midY)
                        }
                        val lastPt = pts.last()
                        path.lineTo(canvasLeft + (lastPt.x * canvasWidth), canvasTop + (lastPt.y * canvasHeight))
                    } else {
                        // Linear direct path (Cyber Turbo)
                        var isFirst = true
                        for (pt in pts) {
                            val sx = canvasLeft + (pt.x * canvasWidth)
                            val sy = canvasTop + (pt.y * canvasHeight)
                            if (isFirst) {
                                path.moveTo(sx, sy)
                                isFirst = false
                            } else {
                                path.lineTo(sx, sy)
                            }
                        }
                    }

                    drawnPoints += pts.size
                    val progress = drawnPoints.toFloat() / totalPoints.coerceAtLeast(1)

                    withContext(Dispatchers.Main) {
                        onStateUpdate(
                            ExecutionState.Drawing(
                                currentStrokeIndex = strokeIdx + 1,
                                totalStrokes = totalStrokes,
                                currentPointIndex = drawnPoints,
                                totalPoints = totalPoints,
                                progress = progress,
                                activePoint = pts.lastOrNull(),
                                activeColorHex = stroke.colorHex
                            )
                        )
                    }

                    if (stroke.isClosed) {
                        path.close()
                    }

                    // Dynamic duration based on ExecutionProfile and PenType
                    val pointCount = stroke.points.size
                    val baseDuration = when (settings.executionProfile) {
                        com.example.model.ExecutionProfile.CYBER_TURBO -> (pointCount * 8L).coerceIn(25L, 120L)
                        com.example.model.ExecutionProfile.ORGANIC_HUMAN -> (pointCount * 22L).coerceIn(60L, 400L)
                    }
                    val adjustedDuration = (baseDuration / settings.speedMultiplier).toLong().coerceAtLeast(20L)

                    // Dispatch gesture
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val strokeDesc = GestureDescription.StrokeDescription(path, 0, adjustedDuration)
                        val gestureBuilder = GestureDescription.Builder().addStroke(strokeDesc)
                        val gesture = gestureBuilder.build()

                        val success = suspendCoroutine<Boolean> { cont ->
                            dispatchGesture(gesture, object : GestureResultCallback() {
                                override fun onCompleted(gestureDescription: GestureDescription?) {
                                    cont.resume(true)
                                }

                                override fun onCancelled(gestureDescription: GestureDescription?) {
                                    cont.resume(false)
                                }
                            }, null)
                        }

                        if (!success) {
                            Log.w(TAG, "Stroke $strokeIdx gesture was cancelled by system.")
                        }
                    }

                    // Delay between individual strokes (Turbo = 5ms, Organic = natural micro-pause)
                    val baseDelay = when (settings.executionProfile) {
                        com.example.model.ExecutionProfile.CYBER_TURBO -> 5L
                        com.example.model.ExecutionProfile.ORGANIC_HUMAN -> settings.strokeDelayMs.coerceAtLeast(15L)
                    }
                    val strokeDelay = (baseDelay / settings.speedMultiplier).toLong().coerceAtLeast(4L)
                    delay(strokeDelay)
                }

                val duration = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    onStateUpdate(ExecutionState.Completed(totalStrokes, duration))
                }
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    onStateUpdate(ExecutionState.Idle)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing gestures", e)
                withContext(Dispatchers.Main) {
                    onStateUpdate(ExecutionState.Error("Execution error: ${e.message}"))
                }
            }
        }
    }

    fun abortCurrentDrawing() {
        activeDrawingJob?.cancel()
        activeDrawingJob = null
    }

    companion object {
        private const val TAG = "ArtHaxAccessibility"

        private var currentInstance: ArtHaxAccessibilityService? = null
        val instance: ArtHaxAccessibilityService? get() = currentInstance

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        /**
         * Checks if the accessibility service is actively running and granted.
         */
        fun isRunning(): Boolean = currentInstance != null
    }
}
