package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelOption
import com.example.model.ArtHaxInstructionSet
import com.example.model.CalibrationBounds
import com.example.model.DrawingSettings
import com.example.model.ExecutionState
import com.example.model.SekaiPreset
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundGlass
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanDark
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * ArtHax Cyber Overlay HUD Panel.
 * Supports Peek (30%), Expanded (70%), and Collapsed state with glowing neon elements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayHudSheet(
    isExpanded: Boolean,
    prompt: String,
    onPromptChange: (String) -> Unit,
    selectedModel: String,
    onModelSelect: (String) -> Unit,
    availableModels: List<AiModelOption>,
    presets: List<SekaiPreset>,
    onSelectPreset: (SekaiPreset) -> Unit,
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    settings: DrawingSettings,
    onUpdateSettings: (DrawingSettings) -> Unit,
    onGenerate: () -> Unit,
    onExecuteDraw: () -> Unit,
    onAbortDraw: () -> Unit,
    onToggleExpand: () -> Unit,
    onCloseOverlay: () -> Unit,
    onToggleCalibrationMode: () -> Unit,
    isCalibrationMode: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val infiniteTransition = rememberInfiniteTransition(label = "hud_glow")
    val executeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "execute_glow"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(24.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), spotColor = NeonCyan)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .animateContentSize()
            .testTag("overlay_hud_surface"),
        color = CardBackgroundGlass
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Drag handle & Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (executionState is ExecutionState.Drawing) NeonPink else NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ArtHax // NEON DECK",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Peek/Expand toggle icon
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Peek HUD" else "Expand HUD",
                            tint = NeonCyan
                        )
                    }

                    // Minimize/Close HUD button
                    IconButton(
                        onClick = onCloseOverlay,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close HUD",
                            tint = TextMuted
                        )
                    }
                }
            }

            // Status bar
            LiveExecutionStatusBar(executionState = executionState)

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // COMPACT PEEK VIEW (Always visible in HUD)
            // ==========================================

            // Prompt Input with Neon Underline
            TextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = {
                    Text(
                        text = "Prompt: e.g. Chibi Miku, Cyber Skull, Dragon...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                    .testTag("overlay_prompt_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CyberBlack,
                    focusedIndicatorColor = NeonCyan,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = NeonPink
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onGenerate()
                }),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onGenerate()
                        },
                        modifier = Modifier.testTag("overlay_generate_btn")
                    ) {
                        if (executionState is ExecutionState.Generating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = NeonPink,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate Vector Strokes",
                                tint = NeonPink
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // EXECUTE / DRAW ACTION BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (executionState is ExecutionState.Drawing) {
                    Button(
                        onClick = onAbortDraw,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("overlay_abort_btn"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = CyberBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ABORT DRAWING",
                            color = CyberBlack,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val hasStrokes = instructionSet != null && instructionSet.strokes.isNotEmpty()
                    Button(
                        onClick = {
                            if (!hasStrokes) {
                                onGenerate()
                            } else {
                                onExecuteDraw()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                                RoundedCornerShape(24.dp)
                            )
                            .testTag("overlay_execute_btn"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasStrokes) NeonCyan.copy(alpha = 0.9f) else CardBackground
                        )
                    ) {
                        Icon(
                            imageVector = if (hasStrokes) Icons.Default.PlayArrow else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (hasStrokes) CyberBlack else NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasStrokes) "EXECUTE // DRAW (${instructionSet?.strokes?.size} STROKES)" else "GENERATE BLUEPRINT",
                            color = if (hasStrokes) CyberBlack else NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ==========================================
            // EXPANDED 70% VIEW DETAILS
            // ==========================================
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Model Selector
                    Text(
                        text = "AI MODEL ENGINE (PUTER.JS)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableModels.forEach { model ->
                            val isSelected = selectedModel == model.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardBackground)
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else BorderGlass,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onModelSelect(model.id) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("overlay_model_${model.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.name,
                                        color = if (isSelected) NeonCyan else TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (model.isFree) "FREE" else model.badge,
                                        color = if (model.isFree) NeonGreen else if (isSelected) NeonPink else TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sekai Quick Presets
                    Text(
                        text = "QUICK SEKAI & CYBER PRESETS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardBackground)
                                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                                    .clickable { onSelectPreset(preset) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("preset_${preset.id}")
                            ) {
                                Column {
                                    Text(
                                        text = "${preset.previewIcon} ${preset.title}",
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${preset.strokeCountApprox} strokes • ${preset.category}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed & Execution Tuning
                    Text(
                        text = "EXECUTION SPEED: ${String.format("%.1fx", settings.speedMultiplier)}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = settings.speedMultiplier,
                        onValueChange = { onUpdateSettings(settings.copy(speedMultiplier = it)) },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CardBackground
                        ),
                        modifier = Modifier.testTag("speed_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Control action row: Canvas Calibrator & Preview Overlay toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sekai Canvas Cutout Box Drag Toggle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCalibrationMode) NeonPink.copy(alpha = 0.25f) else CardBackground)
                                .border(1.dp, if (isCalibrationMode) NeonPink else NeonCyan, RoundedCornerShape(12.dp))
                                .clickable { onToggleCalibrationMode() }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("calibration_toggle_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FitScreen,
                                    contentDescription = null,
                                    tint = if (isCalibrationMode) NeonPink else NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCalibrationMode) "DONE CUTOUT" else "DRAG CUTOUT BOX",
                                    color = if (isCalibrationMode) NeonPink else NeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Preview overlay toggle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                                .clickable {
                                    onUpdateSettings(settings.copy(showPreviewOverlay = !settings.showPreviewOverlay))
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("preview_toggle_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (settings.showPreviewOverlay) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (settings.showPreviewOverlay) NeonGreen else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (settings.showPreviewOverlay) "PREVIEW ON" else "PREVIEW OFF",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun LiveExecutionStatusBar(executionState: ExecutionState) {
    when (executionState) {
        is ExecutionState.Drawing -> {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DRAWING STROKE ${executionState.currentStrokeIndex}/${executionState.totalStrokes}",
                        color = NeonPink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(executionState.progress * 100).toInt()}%",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { executionState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonPink,
                    trackColor = CardBackground
                )
            }
        }
        is ExecutionState.Generating -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = NeonYellow,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = executionState.message,
                    color = NeonYellow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        is ExecutionState.Completed -> {
            Text(
                text = "DRAWING COMPLETED // ${executionState.totalStrokesDrawn} STROKES IN ${executionState.durationMs / 1000}s",
                color = NeonGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        is ExecutionState.Error -> {
            Text(
                text = "STATUS: ${executionState.message}",
                color = NeonPink,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        else -> {
            Text(
                text = "STATUS: READY // IDLE",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
