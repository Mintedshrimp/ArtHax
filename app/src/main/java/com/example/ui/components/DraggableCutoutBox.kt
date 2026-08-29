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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.ExecutionState
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.roundToInt

/**
 * Interactive full-screen overlay with a freely draggable and resizable "Cutout Box"
 * allowing the user to define the exact drawing canvas area on top of other apps (like Sekai).
 */
@Composable
fun DraggableCutoutBox(
    bounds: CalibrationBounds,
    onBoundsChange: (CalibrationBounds) -> Unit,
    onConfirmAndDraw: () -> Unit,
    onClose: () -> Unit,
    instructionSet: ArtHaxInstructionSet? = null,
    executionState: ExecutionState = ExecutionState.Idle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "cutout_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("draggable_cutout_container")
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(100f)
        val totalHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(100f)

        // Pixel bounds of the cutout box
        val boxLeftPx = bounds.left * totalWidthPx
        val boxTopPx = bounds.top * totalHeightPx
        val boxRightPx = bounds.right * totalWidthPx
        val boxBottomPx = bounds.bottom * totalHeightPx
        val boxWidthPx = (boxRightPx - boxLeftPx).coerceAtLeast(120f)
        val boxHeightPx = (boxBottomPx - boxTopPx).coerceAtLeast(120f)

        // 1. Scrim background drawing (dark outside, clear inside cutout)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw dimmed outer overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.55f),
                size = size
            )

            // Clear the cutout box area
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(boxLeftPx, boxTopPx),
                size = Size(boxWidthPx, boxHeightPx),
                blendMode = BlendMode.Clear
            )

            // Draw glowing cyber grid inside cutout
            val gridSpacing = 36f
            var x = boxLeftPx + gridSpacing
            while (x < boxRightPx) {
                drawLine(
                    color = NeonCyan.copy(alpha = 0.12f),
                    start = Offset(x, boxTopPx),
                    end = Offset(x, boxBottomPx),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            var y = boxTopPx + gridSpacing
            while (y < boxBottomPx) {
                drawLine(
                    color = NeonCyan.copy(alpha = 0.12f),
                    start = Offset(boxLeftPx, y),
                    end = Offset(boxRightPx, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // Draw Neon Bounding Border
            drawRect(
                color = NeonCyan.copy(alpha = pulseAlpha),
                topLeft = Offset(boxLeftPx, boxTopPx),
                size = Size(boxWidthPx, boxHeightPx),
                style = Stroke(width = 2.5f)
            )

            // Draw Corner Cyber Accents (L-shapes)
            val cornerLen = 24f
            val cornerStroke = 4f
            val cornerColor = NeonPink

            // Top-Left
            drawLine(cornerColor, Offset(boxLeftPx, boxTopPx), Offset(boxLeftPx + cornerLen, boxTopPx), cornerStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxLeftPx, boxTopPx), Offset(boxLeftPx, boxTopPx + cornerLen), cornerStroke, StrokeCap.Round)

            // Top-Right
            drawLine(cornerColor, Offset(boxRightPx, boxTopPx), Offset(boxRightPx - cornerLen, boxTopPx), cornerStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxRightPx, boxTopPx), Offset(boxRightPx, boxTopPx + cornerLen), cornerStroke, StrokeCap.Round)

            // Bottom-Left
            drawLine(cornerColor, Offset(boxLeftPx, boxBottomPx), Offset(boxLeftPx + cornerLen, boxBottomPx), cornerStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxLeftPx, boxBottomPx), Offset(boxLeftPx, boxBottomPx - cornerLen), cornerStroke, StrokeCap.Round)

            // Bottom-Right
            drawLine(cornerColor, Offset(boxRightPx, boxBottomPx), Offset(boxRightPx - cornerLen, boxBottomPx), cornerStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxRightPx, boxBottomPx), Offset(boxRightPx, boxBottomPx - cornerLen), cornerStroke, StrokeCap.Round)

            // Draw center crosshair
            val cx = (boxLeftPx + boxRightPx) / 2f
            val cy = (boxTopPx + boxBottomPx) / 2f
            val chLen = 14f
            drawLine(NeonCyan.copy(alpha = 0.5f), Offset(cx - chLen, cy), Offset(cx + chLen, cy), 1.5f)
            drawLine(NeonCyan.copy(alpha = 0.5f), Offset(cx, cy - chLen), Offset(cx, cy + chLen), 1.5f)
        }

        // 2. Render vector stroke preview inside cutout if available
        if (instructionSet != null && instructionSet.strokes.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .offset { IntOffset(boxLeftPx.roundToInt(), boxTopPx.roundToInt()) }
                    .size(
                        width = with(density) { boxWidthPx.toDp() },
                        height = with(density) { boxHeightPx.toDp() }
                    )
            ) {
                instructionSet.strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path()
                        path.moveTo(stroke.points[0].x * size.width, stroke.points[0].y * size.height)
                        for (i in 1 until stroke.points.size) {
                            path.lineTo(stroke.points[i].x * size.width, stroke.points[i].y * size.height)
                        }
                        if (stroke.isClosed) path.close()

                        drawPath(
                            path = path,
                            color = stroke.parseColor().copy(alpha = 0.7f),
                            style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // 3. Center Draggable Area (Moves the entire cutout box)
        Box(
            modifier = Modifier
                .offset { IntOffset(boxLeftPx.roundToInt(), boxTopPx.roundToInt()) }
                .size(
                    width = with(density) { boxWidthPx.toDp() },
                    height = with(density) { boxHeightPx.toDp() }
                )
                .pointerInput(totalWidthPx, totalHeightPx, bounds) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x / totalWidthPx
                        val dy = dragAmount.y / totalHeightPx
                        val currentW = bounds.right - bounds.left
                        val currentH = bounds.bottom - bounds.top

                        var newLeft = (bounds.left + dx).coerceIn(0.0f, 1.0f - currentW)
                        var newTop = (bounds.top + dy).coerceIn(0.0f, 1.0f - currentH)
                        var newRight = newLeft + currentW
                        var newBottom = newTop + currentH

                        onBoundsChange(
                            CalibrationBounds(
                                left = newLeft,
                                top = newTop,
                                right = newRight,
                                bottom = newBottom
                            )
                        )
                    }
                }
                .testTag("cutout_center_drag_area"),
            contentAlignment = Alignment.Center
        ) {
            // Drag icon badge in center
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CyberBlack.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenWith,
                        contentDescription = "Drag to reposition canvas",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DRAG TO MOVE CANVAS",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Corner Handles for Freely Resizing the Cutout Box
        val handleSizeDp = 44.dp
        val handleOffsetDp = 22.dp

        // Top-Left Handle
        CornerResizeHandle(
            xPx = boxLeftPx,
            yPx = boxTopPx,
            handleOffsetDp = handleOffsetDp,
            handleSizeDp = handleSizeDp,
            testTag = "handle_top_left",
            onDrag = { dxPx, dyPx ->
                val newLeft = ((boxLeftPx + dxPx) / totalWidthPx).coerceIn(0.0f, bounds.right - 0.12f)
                val newTop = ((boxTopPx + dyPx) / totalHeightPx).coerceIn(0.0f, bounds.bottom - 0.12f)
                onBoundsChange(bounds.copy(left = newLeft, top = newTop))
            }
        )

        // Top-Right Handle
        CornerResizeHandle(
            xPx = boxRightPx,
            yPx = boxTopPx,
            handleOffsetDp = handleOffsetDp,
            handleSizeDp = handleSizeDp,
            testTag = "handle_top_right",
            onDrag = { dxPx, dyPx ->
                val newRight = ((boxRightPx + dxPx) / totalWidthPx).coerceIn(bounds.left + 0.12f, 1.0f)
                val newTop = ((boxTopPx + dyPx) / totalHeightPx).coerceIn(0.0f, bounds.bottom - 0.12f)
                onBoundsChange(bounds.copy(right = newRight, top = newTop))
            }
        )

        // Bottom-Left Handle
        CornerResizeHandle(
            xPx = boxLeftPx,
            yPx = boxBottomPx,
            handleOffsetDp = handleOffsetDp,
            handleSizeDp = handleSizeDp,
            testTag = "handle_bottom_left",
            onDrag = { dxPx, dyPx ->
                val newLeft = ((boxLeftPx + dxPx) / totalWidthPx).coerceIn(0.0f, bounds.right - 0.12f)
                val newBottom = ((boxBottomPx + dyPx) / totalHeightPx).coerceIn(bounds.top + 0.12f, 1.0f)
                onBoundsChange(bounds.copy(left = newLeft, bottom = newBottom))
            }
        )

        // Bottom-Right Handle
        CornerResizeHandle(
            xPx = boxRightPx,
            yPx = boxBottomPx,
            handleOffsetDp = handleOffsetDp,
            handleSizeDp = handleSizeDp,
            testTag = "handle_bottom_right",
            onDrag = { dxPx, dyPx ->
                val newRight = ((boxRightPx + dxPx) / totalWidthPx).coerceIn(bounds.left + 0.12f, 1.0f)
                val newBottom = ((boxBottomPx + dyPx) / totalHeightPx).coerceIn(bounds.top + 0.12f, 1.0f)
                onBoundsChange(bounds.copy(right = newRight, bottom = newBottom))
            }
        )

        // 5. Floating Action Bar (Positioned above or below the box)
        val actionBarY = if (boxTopPx > 160f) {
            boxTopPx - 60f
        } else {
            (boxBottomPx + 16f).coerceAtMost(totalHeightPx - 90f)
        }

        Surface(
            modifier = Modifier
                .offset { IntOffset((boxLeftPx.coerceAtLeast(16f)).roundToInt(), actionBarY.roundToInt()) }
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = NeonPink)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NeonPink.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .testTag("cutout_action_bar"),
            color = CyberBlack.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START DRAWING BUTTON
                Button(
                    onClick = onConfirmAndDraw,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.testTag("cutout_draw_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyberBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "START DRAWING",
                        color = CyberBlack,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Reset Bounds Button
                IconButton(
                    onClick = {
                        onBoundsChange(CalibrationBounds(left = 0.08f, top = 0.22f, right = 0.92f, bottom = 0.78f))
                    },
                    modifier = Modifier.size(36.dp).testTag("cutout_reset_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Canvas Size",
                        tint = NeonCyan
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp).testTag("cutout_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Cutout Selector",
                        tint = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun CornerResizeHandle(
    xPx: Float,
    yPx: Float,
    handleOffsetDp: androidx.compose.ui.unit.Dp,
    handleSizeDp: androidx.compose.ui.unit.Dp,
    testTag: String,
    onDrag: (Float, Float) -> Unit
) {
    val density = LocalDensity.current
    val offsetPx = with(density) { handleOffsetDp.toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (xPx - offsetPx).roundToInt(),
                    (yPx - offsetPx).roundToInt()
                )
            }
            .size(handleSizeDp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Glowing handle circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .shadow(8.dp, CircleShape, spotColor = NeonPink)
                .clip(CircleShape)
                .background(NeonPink)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}
