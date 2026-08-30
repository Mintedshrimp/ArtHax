package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.DrawingStroke
import com.example.model.ExecutionState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.GridLine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Clean, formal vector drawing canvas that displays vector strokes,
 * subtle coordinate grids, calibration guides, and active drawing indicators.
 */
@Composable
fun DrawingCanvas(
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    modifier: Modifier = Modifier,
    bounds: CalibrationBounds? = null,
    showGrid: Boolean = true,
    showStrokeIndices: Boolean = false,
    interactiveGlow: Boolean = false,
    transparentBackground: Boolean = false
) {
    val boxModifier = if (transparentBackground) {
        modifier
            .background(Color.Transparent)
            .testTag("drawing_canvas_box")
    } else {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBackground)
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .testTag("drawing_canvas_box")
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("drawing_canvas_viewport")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw subtle guide grid (only if not in transparent overlay mode)
            if (showGrid && !transparentBackground) {
                drawBlueprintGrid(canvasW, canvasH)
            }

            // 2. Draw Calibration Box Guides (if active)
            if (bounds != null) {
                drawCalibrationGuides(bounds, canvasW, canvasH)
            }

            // 3. Draw Planned or In-Progress Vector Strokes
            if (instructionSet != null && instructionSet.strokes.isNotEmpty()) {
                val activeDrawing = executionState as? ExecutionState.Drawing

                instructionSet.strokes.forEachIndexed { sIdx, stroke ->
                    val isCurrentStroke = activeDrawing?.currentStrokeIndex == (sIdx + 1)
                    val isDrawnStroke = activeDrawing == null || sIdx < (activeDrawing.currentStrokeIndex - 1)

                    if (isDrawnStroke || isCurrentStroke) {
                        drawCleanStroke(
                            stroke = stroke,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            maxPointsToDraw = if (isCurrentStroke) activeDrawing.currentPointIndex else stroke.points.size
                        )
                    }
                }
            }

            // 4. Draw Stylus indicator when drawing is actively running
            if (executionState is ExecutionState.Drawing && executionState.activePoint != null) {
                val pt = executionState.activePoint
                val posX = pt.x * canvasW
                val posY = pt.y * canvasH

                // Outer focus ring
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.4f),
                    radius = 18f,
                    center = Offset(posX, posY),
                    style = Stroke(width = 2f)
                )

                // Core indicator
                drawCircle(
                    color = NeonCyan,
                    radius = 6f,
                    center = Offset(posX, posY)
                )

                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(posX, posY)
                )
            }
        }

        // Empty state overlay (only in app, not in transparent overlay mode)
        if (!transparentBackground && instructionSet == null && executionState is ExecutionState.Idle) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No Drawing Loaded",
                    color = TextWhite,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a preset sample or use the prompt bar below to generate strokes.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Backward compatibility alias for CyberCanvas
 */
@Composable
fun CyberCanvas(
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    modifier: Modifier = Modifier,
    bounds: CalibrationBounds? = null,
    showGrid: Boolean = true,
    showStrokeIndices: Boolean = false,
    interactiveGlow: Boolean = false,
    transparentBackground: Boolean = false
) {
    DrawingCanvas(
        instructionSet = instructionSet,
        executionState = executionState,
        modifier = modifier,
        bounds = bounds,
        showGrid = showGrid,
        showStrokeIndices = showStrokeIndices,
        interactiveGlow = interactiveGlow,
        transparentBackground = transparentBackground
    )
}

private fun DrawScope.drawBlueprintGrid(w: Float, h: Float) {
    val step = 40f
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
        color = NeonCyan.copy(alpha = 0.5f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
    )

    // Corner targeting marks
    val cornerLen = 14f
    // Top-left
    drawLine(NeonCyan, Offset(left, top), Offset(left + cornerLen, top), 2.5f)
    drawLine(NeonCyan, Offset(left, top), Offset(left, top + cornerLen), 2.5f)
    // Top-right
    drawLine(NeonCyan, Offset(left + width, top), Offset(left + width - cornerLen, top), 2.5f)
    drawLine(NeonCyan, Offset(left + width, top), Offset(left + width, top + cornerLen), 2.5f)
    // Bottom-left
    drawLine(NeonCyan, Offset(left, top + height), Offset(left + cornerLen, top + height), 2.5f)
    drawLine(NeonCyan, Offset(left, top + height), Offset(left + top + height, top + height), 2.5f)
    // Bottom-right
    drawLine(NeonCyan, Offset(left + width, top + height), Offset(left + width - cornerLen, top + height), 2.5f)
    drawLine(NeonCyan, Offset(left + width, top + height), Offset(left + width, top + height - cornerLen), 2.5f)
}

private fun DrawScope.drawCleanStroke(
    stroke: DrawingStroke,
    canvasW: Float,
    canvasH: Float,
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

    // Subtle soft background stroke for anti-aliasing depth
    drawPath(
        path = path,
        color = strokeColor.copy(alpha = 0.2f),
        style = Stroke(
            width = stroke.strokeWidth + 2f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Solid clean vector stroke
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = stroke.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
