package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.ExecutionState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.roundToInt

/**
 * Interactive high-performance overlay with ultra-smooth draggable and resizable cutout box.
 * Allows artists to calibrate drawing boundaries with zero lag, presets, fine-tune nudge tools,
 * and 4-edge resize handles.
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
    val currentBounds by rememberUpdatedState(bounds)
    val currentOnBoundsChange by rememberUpdatedState(onBoundsChange)

    var showNudgeControls by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "cutout_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
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

        val boxLeftPx = bounds.left * totalWidthPx
        val boxTopPx = bounds.top * totalHeightPx
        val boxRightPx = bounds.right * totalWidthPx
        val boxBottomPx = bounds.bottom * totalHeightPx
        val boxWidthPx = (boxRightPx - boxLeftPx).coerceAtLeast(80f)
        val boxHeightPx = (boxBottomPx - boxTopPx).coerceAtLeast(80f)

        // 1. Scrim background drawing (Dark outside, crisp clear view inside)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw dimmed outer overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.65f),
                size = size
            )

            // Clear the cutout box area
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(boxLeftPx, boxTopPx),
                size = Size(boxWidthPx, boxHeightPx),
                blendMode = BlendMode.Clear
            )

            // Draw subtle rule-of-thirds grid inside cutout
            val oneThirdW = boxWidthPx / 3f
            val oneThirdH = boxHeightPx / 3f
            for (i in 1..2) {
                // Vertical grid lines
                drawLine(
                    color = NeonCyan.copy(alpha = 0.15f),
                    start = Offset(boxLeftPx + oneThirdW * i, boxTopPx),
                    end = Offset(boxLeftPx + oneThirdW * i, boxBottomPx),
                    strokeWidth = 1.dp.toPx()
                )
                // Horizontal grid lines
                drawLine(
                    color = NeonCyan.copy(alpha = 0.15f),
                    start = Offset(boxLeftPx, boxTopPx + oneThirdH * i),
                    end = Offset(boxRightPx, boxTopPx + oneThirdH * i),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Bounding Border with Neon Glow
            drawRect(
                color = NeonCyan.copy(alpha = pulseAlpha),
                topLeft = Offset(boxLeftPx, boxTopPx),
                size = Size(boxWidthPx, boxHeightPx),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Draw Corner Brackets (L-shapes)
            val cornerLen = 22.dp.toPx().coerceAtMost(boxWidthPx / 3)
            val cornerStroke = 3.5.dp.toPx()
            val cornerColor = NeonCyan

            // Top-Left
            drawLine(cornerColor, Offset(boxLeftPx, boxTopPx), Offset(boxLeftPx + cornerLen, boxTopPx), cornerStroke, StrokeCap.Square)
            drawLine(cornerColor, Offset(boxLeftPx, boxTopPx), Offset(boxLeftPx, boxTopPx + cornerLen), cornerStroke, StrokeCap.Square)

            // Top-Right
            drawLine(cornerColor, Offset(boxRightPx, boxTopPx), Offset(boxRightPx - cornerLen, boxTopPx), cornerStroke, StrokeCap.Square)
            drawLine(cornerColor, Offset(boxRightPx, boxTopPx), Offset(boxRightPx, boxTopPx + cornerLen), cornerStroke, StrokeCap.Square)

            // Bottom-Left
            drawLine(cornerColor, Offset(boxLeftPx, boxBottomPx), Offset(boxLeftPx + cornerLen, boxBottomPx), cornerStroke, StrokeCap.Square)
            drawLine(cornerColor, Offset(boxLeftPx, boxBottomPx), Offset(boxLeftPx, boxBottomPx - cornerLen), cornerStroke, StrokeCap.Square)

            // Bottom-Right
            drawLine(cornerColor, Offset(boxRightPx, boxBottomPx), Offset(boxRightPx - cornerLen, boxBottomPx), cornerStroke, StrokeCap.Square)
            drawLine(cornerColor, Offset(boxRightPx, boxBottomPx), Offset(boxRightPx, boxBottomPx - cornerLen), cornerStroke, StrokeCap.Square)
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
                            color = stroke.parseColor().copy(alpha = 0.85f),
                            style = Stroke(width = stroke.strokeWidth.coerceAtLeast(1.5f), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // 3. ULTRA-SMOOTH CENTER PANNING AREA (Moves the entire cutout with zero lag)
        Box(
            modifier = Modifier
                .offset { IntOffset(boxLeftPx.roundToInt(), boxTopPx.roundToInt()) }
                .size(
                    width = with(density) { boxWidthPx.toDp() },
                    height = with(density) { boxHeightPx.toDp() }
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x / totalWidthPx
                        val dy = dragAmount.y / totalHeightPx
                        val b = currentBounds
                        val currentW = b.right - b.left
                        val currentH = b.bottom - b.top

                        val newLeft = (b.left + dx).coerceIn(0.0f, (1.0f - currentW).coerceAtLeast(0f))
                        val newTop = (b.top + dy).coerceIn(0.0f, (1.0f - currentH).coerceAtLeast(0f))
                        val newRight = (newLeft + currentW).coerceAtMost(1.0f)
                        val newBottom = (newTop + currentH).coerceAtMost(1.0f)

                        currentOnBoundsChange(
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
            // Drag guide indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackgroundElevated.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenWith,
                        contentDescription = "Pan canvas",
                        tint = NeonCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${(boxWidthPx).roundToInt()} × ${(boxHeightPx).roundToInt()} px",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 4. EDGE RESIZE BARS (TOP, BOTTOM, LEFT, RIGHT)
        val edgeBarThicknessDp = 32.dp

        // Top Edge Bar
        EdgeResizeBar(
            modifier = Modifier
                .offset { IntOffset(boxLeftPx.roundToInt(), (boxTopPx - with(density) { 16.dp.toPx() }).roundToInt()) }
                .size(width = with(density) { boxWidthPx.toDp() }, height = edgeBarThicknessDp)
                .testTag("edge_resize_top"),
            onDrag = { _, dyPx ->
                val b = currentBounds
                val newTop = ((boxTopPx + dyPx) / totalHeightPx).coerceIn(0.0f, b.bottom - 0.05f)
                currentOnBoundsChange(b.copy(top = newTop))
            }
        )

        // Bottom Edge Bar
        EdgeResizeBar(
            modifier = Modifier
                .offset { IntOffset(boxLeftPx.roundToInt(), (boxBottomPx - with(density) { 16.dp.toPx() }).roundToInt()) }
                .size(width = with(density) { boxWidthPx.toDp() }, height = edgeBarThicknessDp)
                .testTag("edge_resize_bottom"),
            onDrag = { _, dyPx ->
                val b = currentBounds
                val newBottom = ((boxBottomPx + dyPx) / totalHeightPx).coerceIn(b.top + 0.05f, 1.0f)
                currentOnBoundsChange(b.copy(bottom = newBottom))
            }
        )

        // Left Edge Bar
        EdgeResizeBar(
            modifier = Modifier
                .offset { IntOffset((boxLeftPx - with(density) { 16.dp.toPx() }).roundToInt(), boxTopPx.roundToInt()) }
                .size(width = edgeBarThicknessDp, height = with(density) { boxHeightPx.toDp() })
                .testTag("edge_resize_left"),
            onDrag = { dxPx, _ ->
                val b = currentBounds
                val newLeft = ((boxLeftPx + dxPx) / totalWidthPx).coerceIn(0.0f, b.right - 0.05f)
                currentOnBoundsChange(b.copy(left = newLeft))
            }
        )

        // Right Edge Bar
        EdgeResizeBar(
            modifier = Modifier
                .offset { IntOffset((boxRightPx - with(density) { 16.dp.toPx() }).roundToInt(), boxTopPx.roundToInt()) }
                .size(width = edgeBarThicknessDp, height = with(density) { boxHeightPx.toDp() })
                .testTag("edge_resize_right"),
            onDrag = { dxPx, _ ->
                val b = currentBounds
                val newRight = ((boxRightPx + dxPx) / totalWidthPx).coerceIn(b.left + 0.05f, 1.0f)
                currentOnBoundsChange(b.copy(right = newRight))
            }
        )

        // 5. CORNER RESIZE HANDLES (Enlarged touch slop with crisp glowing handles)
        val handleTouchSizeDp = 48.dp
        val handleHalfDp = 24.dp

        // Top-Left Handle
        CornerResizeHandle(
            xPx = boxLeftPx,
            yPx = boxTopPx,
            handleTouchSizeDp = handleTouchSizeDp,
            handleHalfDp = handleHalfDp,
            testTag = "handle_top_left",
            onDrag = { dxPx, dyPx ->
                val b = currentBounds
                val newLeft = ((boxLeftPx + dxPx) / totalWidthPx).coerceIn(0.0f, b.right - 0.05f)
                val newTop = ((boxTopPx + dyPx) / totalHeightPx).coerceIn(0.0f, b.bottom - 0.05f)
                currentOnBoundsChange(b.copy(left = newLeft, top = newTop))
            }
        )

        // Top-Right Handle
        CornerResizeHandle(
            xPx = boxRightPx,
            yPx = boxTopPx,
            handleTouchSizeDp = handleTouchSizeDp,
            handleHalfDp = handleHalfDp,
            testTag = "handle_top_right",
            onDrag = { dxPx, dyPx ->
                val b = currentBounds
                val newRight = ((boxRightPx + dxPx) / totalWidthPx).coerceIn(b.left + 0.05f, 1.0f)
                val newTop = ((boxTopPx + dyPx) / totalHeightPx).coerceIn(0.0f, b.bottom - 0.05f)
                currentOnBoundsChange(b.copy(right = newRight, top = newTop))
            }
        )

        // Bottom-Left Handle
        CornerResizeHandle(
            xPx = boxLeftPx,
            yPx = boxBottomPx,
            handleTouchSizeDp = handleTouchSizeDp,
            handleHalfDp = handleHalfDp,
            testTag = "handle_bottom_left",
            onDrag = { dxPx, dyPx ->
                val b = currentBounds
                val newLeft = ((boxLeftPx + dxPx) / totalWidthPx).coerceIn(0.0f, b.right - 0.05f)
                val newBottom = ((boxBottomPx + dyPx) / totalHeightPx).coerceIn(b.top + 0.05f, 1.0f)
                currentOnBoundsChange(b.copy(left = newLeft, bottom = newBottom))
            }
        )

        // Bottom-Right Handle
        CornerResizeHandle(
            xPx = boxRightPx,
            yPx = boxBottomPx,
            handleTouchSizeDp = handleTouchSizeDp,
            handleHalfDp = handleHalfDp,
            testTag = "handle_bottom_right",
            onDrag = { dxPx, dyPx ->
                val b = currentBounds
                val newRight = ((boxRightPx + dxPx) / totalWidthPx).coerceIn(b.left + 0.05f, 1.0f)
                val newBottom = ((boxBottomPx + dyPx) / totalHeightPx).coerceIn(b.top + 0.05f, 1.0f)
                currentOnBoundsChange(b.copy(right = newRight, bottom = newBottom))
            }
        )

        // 6. FLOATING CONTROL PANEL (Auto-positions safely above or below box)
        val actionBarY = if (boxTopPx > 180f) {
            (boxTopPx - 70f).coerceAtLeast(16f)
        } else {
            (boxBottomPx + 16f).coerceAtMost(totalHeightPx - 160f)
        }

        Column(
            modifier = Modifier
                .offset { IntOffset(boxLeftPx.coerceIn(12f, totalWidthPx - 340f).roundToInt(), actionBarY.roundToInt()) }
                .shadow(14.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackgroundElevated)
                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                .padding(8.dp)
                .testTag("cutout_action_bar")
        ) {
            // Main Action Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START DRAWING BUTTON
                Button(
                    onClick = onConfirmAndDraw,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.testTag("cutout_draw_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Draw Here",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Toggle Nudge & Presets
                IconButton(
                    onClick = { showNudgeControls = !showNudgeControls },
                    modifier = Modifier.size(36.dp).testTag("cutout_toggle_nudge_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Fine-tune & Aspect Ratios",
                        tint = if (showNudgeControls) NeonCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset Bounds Button
                IconButton(
                    onClick = {
                        currentOnBoundsChange(CalibrationBounds(left = 0.08f, top = 0.20f, right = 0.92f, bottom = 0.80f))
                    },
                    modifier = Modifier.size(36.dp).testTag("cutout_reset_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Canvas Size",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
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
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Nudge & Aspect Ratio Toolbar
            AnimatedVisibility(visible = showNudgeControls) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Quick Aspect Ratio Chips
                    Text(
                        text = "CANVAS RATIOS",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AspectRatioChip("Full") {
                            currentOnBoundsChange(CalibrationBounds(left = 0.02f, top = 0.08f, right = 0.98f, bottom = 0.92f))
                        }
                        AspectRatioChip("1:1 Sq") {
                            val size = 0.70f
                            val left = (1.0f - size) / 2f
                            val top = 0.25f
                            currentOnBoundsChange(CalibrationBounds(left = left, top = top, right = left + size, bottom = top + size))
                        }
                        AspectRatioChip("16:9") {
                            val w = 0.88f
                            val h = w * 0.5625f
                            val left = (1.0f - w) / 2f
                            val top = 0.30f
                            currentOnBoundsChange(CalibrationBounds(left = left, top = top, right = left + w, bottom = top + h))
                        }
                        AspectRatioChip("9:16") {
                            val h = 0.75f
                            val w = h * 0.5625f
                            val left = (1.0f - w) / 2f
                            val top = 0.15f
                            currentOnBoundsChange(CalibrationBounds(left = left, top = top, right = left + w, bottom = top + h))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // D-Pad Nudge & Zoom Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "NUDGE POSITION",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Left
                            NudgeButton(Icons.Default.KeyboardArrowLeft) {
                                val b = currentBounds
                                val w = b.right - b.left
                                val newLeft = (b.left - 0.02f).coerceAtLeast(0f)
                                currentOnBoundsChange(b.copy(left = newLeft, right = newLeft + w))
                            }
                            // Right
                            NudgeButton(Icons.Default.KeyboardArrowRight) {
                                val b = currentBounds
                                val w = b.right - b.left
                                val newLeft = (b.left + 0.02f).coerceIn(0f, 1f - w)
                                currentOnBoundsChange(b.copy(left = newLeft, right = newLeft + w))
                            }
                            // Up
                            NudgeButton(Icons.Default.KeyboardArrowUp) {
                                val b = currentBounds
                                val h = b.bottom - b.top
                                val newTop = (b.top - 0.02f).coerceAtLeast(0f)
                                currentOnBoundsChange(b.copy(top = newTop, bottom = newTop + h))
                            }
                            // Down
                            NudgeButton(Icons.Default.KeyboardArrowDown) {
                                val b = currentBounds
                                val h = b.bottom - b.top
                                val newTop = (b.top + 0.02f).coerceIn(0f, 1f - h)
                                currentOnBoundsChange(b.copy(top = newTop, bottom = newTop + h))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectRatioChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = CardBackgroundElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = NeonCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun NudgeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EdgeResizeBar(
    modifier: Modifier,
    onDrag: (Float, Float) -> Unit
) {
    val currentOnDrag by rememberUpdatedState(onDrag)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle grip line indicator
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NeonCyan.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun CornerResizeHandle(
    xPx: Float,
    yPx: Float,
    handleTouchSizeDp: androidx.compose.ui.unit.Dp,
    handleHalfDp: androidx.compose.ui.unit.Dp,
    testTag: String,
    onDrag: (Float, Float) -> Unit
) {
    val density = LocalDensity.current
    val offsetPx = with(density) { handleHalfDp.toPx() }
    val currentOnDrag by rememberUpdatedState(onDrag)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (xPx - offsetPx).roundToInt(),
                    (yPx - offsetPx).roundToInt()
                )
            }
            .size(handleTouchSizeDp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnDrag(dragAmount.x, dragAmount.y)
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Glowing handle pill
        Box(
            modifier = Modifier
                .size(20.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(NeonCyan)
                .border(2.dp, TextWhite, CircleShape)
        )
    }
}

