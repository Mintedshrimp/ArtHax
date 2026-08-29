package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.DrawingPoint
import com.example.model.DrawingStroke
import com.example.model.ExecutionState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.GridLine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted

/**
 * Interactive cyber canvas that renders vector art strokes, neon glow effects,
 * coordinate grids, calibration guides, and animated live drawing pointers.
 */
@Composable
fun CyberCanvas(
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    modifier: Modifier = Modifier,
    bounds: CalibrationBounds? = null,
    showGrid: Boolean = true,
    showStrokeIndices: Boolean = false,
    interactiveGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBackground)
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .testTag("cyber_canvas_box"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cyber_canvas_viewport")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw Cyber Matrix Grid
            if (showGrid) {
                drawCyberGrid(canvasW, canvasH)
            }

            // 2. Draw Sekai Calibration Box (if supplied)
            if (bounds != null) {
                drawCalibrationGuides(bounds, canvasW, canvasH)
            }

            // 3. Draw Planned or Completed Vector Strokes
            if (instructionSet != null && instructionSet.strokes.isNotEmpty()) {
                val activeDrawing = executionState as? ExecutionState.Drawing

                instructionSet.strokes.forEachIndexed { sIdx, stroke ->
                    val isCurrentStroke = activeDrawing?.currentStrokeIndex == (sIdx + 1)
                    val isDrawnStroke = activeDrawing == null || sIdx < (activeDrawing.currentStrokeIndex - 1)

                    if (isDrawnStroke || isCurrentStroke) {
                        drawNeonStroke(
                            stroke = stroke,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            glowAlpha = if (interactiveGlow) glowAlpha else 0.5f,
                            maxPointsToDraw = if (isCurrentStroke) activeDrawing.currentPointIndex else stroke.points.size
                        )
                    }
                }
            }

            // 4. Draw Active Stylus / Laser Pointer when drawing is in progress
            if (executionState is ExecutionState.Drawing && executionState.activePoint != null) {
                val pt = executionState.activePoint
                val posX = pt.x * canvasW
                val posY = pt.y * canvasH

                // Outer pulsing radar ring
                drawCircle(
                    color = NeonCyan.copy(alpha = glowAlpha),
                    radius = 24f,
                    center = Offset(posX, posY),
                    style = Stroke(width = 2.5f)
                )

                // Hot pink core target
                drawCircle(
                    color = NeonPink,
                    radius = 8f,
                    center = Offset(posX, posY)
                )

                // White laser center
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(posX, posY)
                )
            }
        }

        // Empty state overlay
        if (instructionSet == null && executionState is ExecutionState.Idle) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AWAITING AI DRAWING BLUEPRINT\nSELECT A PRESET OR ENTER PROMPT",
                    color = TextMuted.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

private fun DrawScope.drawCyberGrid(w: Float, h: Float) {
    val step = 36f
    var x = step
    while (x < w) {
        drawLine(
            color = GridLine,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += step
    }

    var y = step
    while (y < h) {
        drawLine(
            color = GridLine,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawCalibrationGuides(bounds: CalibrationBounds, w: Float, h: Float) {
    val left = bounds.left * w
    val top = bounds.top * h
    val width = bounds.width * w
    val height = bounds.height * h

    // Dashed bounding box
    drawRect(
        color = NeonPink.copy(alpha = 0.6f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
        )
    )

    // Corner targeting marks
    val cornerLen = 16f
    // Top-left
    drawLine(NeonCyan, Offset(left, top), Offset(left + cornerLen, top), 3f)
    drawLine(NeonCyan, Offset(left, top), Offset(left, top + cornerLen), 3f)
    // Top-right
    drawLine(NeonCyan, Offset(left + width, top), Offset(left + width - cornerLen, top), 3f)
    drawLine(NeonCyan, Offset(left + width, top), Offset(left + width, top + cornerLen), 3f)
    // Bottom-left
    drawLine(NeonCyan, Offset(left, top + height), Offset(left + cornerLen, top + height), 3f)
    drawLine(NeonCyan, Offset(left, top + height), Offset(left, top + height - cornerLen), 3f)
    // Bottom-right
    drawLine(NeonCyan, Offset(left + width, top + height), Offset(left + width - cornerLen, top + height), 3f)
    drawLine(NeonCyan, Offset(left + width, top + height), Offset(left + width, top + height - cornerLen), 3f)
}

private fun DrawScope.drawNeonStroke(
    stroke: DrawingStroke,
    canvasW: Float,
    canvasH: Float,
    glowAlpha: Float,
    maxPointsToDraw: Int
) {
    if (stroke.points.size < 2) return
    val pts = stroke.points.take(maxPointsToDraw)
    if (pts.size < 2) return

    val strokeColor = stroke.parseColor()
    val path = Path()
    path.moveTo(pts[0].x * canvasW, pts[0].y * canvasH)

    for (i in 1 until pts.size) {
        val prev = pts[i - 1]
        val curr = pts[i]
        val midX = (prev.x + curr.x) * 0.5f * canvasW
        val midY = (prev.y + curr.y) * 0.5f * canvasH
        path.quadraticTo(prev.x * canvasW, prev.y * canvasH, midX, midY)
    }
    path.lineTo(pts.last().x * canvasW, pts.last().y * canvasH)

    if (stroke.isClosed) {
        path.close()
    }

    // Outer Neon Glow Layer
    drawPath(
        path = path,
        color = strokeColor.copy(alpha = 0.25f * glowAlpha),
        style = Stroke(
            width = stroke.strokeWidth * 3.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Mid Glow Aura
    drawPath(
        path = path,
        color = strokeColor.copy(alpha = 0.6f * glowAlpha),
        style = Stroke(
            width = stroke.strokeWidth * 1.8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Sharp Core Vector
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = stroke.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Center Hot White Core for cyber vibrancy
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(
            width = (stroke.strokeWidth * 0.35f).coerceAtLeast(1f),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
