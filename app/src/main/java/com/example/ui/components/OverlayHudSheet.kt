package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.model.DrawingSettings
import com.example.model.ExecutionState
import com.example.model.PuterAuthState
import com.example.model.SekaiPreset
import com.example.ui.theme.CarbonElevated
import com.example.ui.theme.CarbonInteractive
import com.example.ui.theme.CobaltBeam
import com.example.ui.theme.HairlineAmber
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineCobalt
import com.example.ui.theme.LaserCrimson
import com.example.ui.theme.MatteCarbon
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TungstenAmber

/**
 * Neo-Precision HUD instrument controller for the Overlay Assistant.
 * Provides AI prompt box, model picker, one-tap canvas crop toggle, presets, and drawing execution controls.
 */
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
    isPuterSdkReady: Boolean = true,
    puterAuthState: PuterAuthState? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, HairlineBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .animateContentSize()
            .testTag("overlay_hud_surface"),
        color = CarbonElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row & Minimize Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = (if (executionState is ExecutionState.Drawing) LaserCrimson else TungstenAmber).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (executionState is ExecutionState.Drawing) LaserCrimson.copy(alpha = 0.4f) else HairlineAmber),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (executionState is ExecutionState.Drawing) LaserCrimson else TungstenAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SYNTHESIS CONTROLLER",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isPuterSdkReady) SignalEmerald else TungstenAmber)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPuterSdkReady) {
                                    if (puterAuthState?.isSignedIn == true) "@${puterAuthState.username ?: "USER"} (LINKED)" else "PUTER.JS ENGINE ONLINE"
                                } else "INITIALIZING ENGINE...",
                                color = if (isPuterSdkReady) SignalEmerald else TungstenAmber,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp).testTag("hud_expand_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextMuted
                        )
                    }

                    IconButton(
                        onClick = onCloseOverlay,
                        modifier = Modifier.size(32.dp).testTag("hud_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Minimize Hub",
                            tint = TextMuted
                        )
                    }
                }
            }

            // Status indicator when generating or drawing
            if (executionState is ExecutionState.Generating) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SYNTHESIZING STROKES...", color = TungstenAmber, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(executionState.message, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { executionState.progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = TungstenAmber,
                        trackColor = MatteCarbon
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else if (executionState is ExecutionState.Drawing) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("EXECUTING STROKE ${executionState.currentStrokeIndex} / ${executionState.totalStrokes}", color = SignalEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("${(executionState.progress * 100).toInt()}%", color = SignalEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { executionState.progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = SignalEmerald,
                        trackColor = MatteCarbon
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // PRIMARY AI PROMPT BOX
            TextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = {
                    Text("Enter prompt to synthesize strokes...", color = TextMuted, fontSize = 12.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .testTag("overlay_prompt_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MatteCarbon,
                    unfocusedContainerColor = MatteCarbon,
                    focusedIndicatorColor = TungstenAmber,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
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
                                modifier = Modifier.size(18.dp),
                                color = TungstenAmber,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate",
                                tint = TungstenAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PRIMARY ACTION BUTTONS: [Toggle Crop Area] & [Draw / Abort]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TOGGLE CANVAS CROP BUTTON
                OutlinedButton(
                    onClick = onToggleCalibrationMode,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isCalibrationMode) TungstenAmber.copy(alpha = 0.15f) else MatteCarbon,
                        contentColor = if (isCalibrationMode) TungstenAmber else TextWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCalibrationMode) HairlineAmber else HairlineBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("calibration_toggle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = if (isCalibrationMode) TungstenAmber else TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCalibrationMode) "LOCK VIEW" else "CROP CANVAS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // DRAW ON SCREEN / ABORT BUTTON
                if (executionState is ExecutionState.Drawing) {
                    Button(
                        onClick = onAbortDraw,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("overlay_abort_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LaserCrimson,
                            contentColor = TextWhite
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STOP DRAWING",
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
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
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("overlay_execute_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasStrokes) TungstenAmber else MatteCarbon,
                            contentColor = if (hasStrokes) ObsidianBlack else TextWhite
                        )
                    ) {
                        Icon(
                            imageVector = if (hasStrokes) Icons.Default.PlayArrow else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (hasStrokes) ObsidianBlack else TungstenAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasStrokes) "DRAW (${instructionSet?.strokes?.size} STROKES)" else "SYNTHESIZE",
                            color = if (hasStrokes) ObsidianBlack else TungstenAmber,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // EXPANDED ADVANCED CONTROLS (Models, Presets, Speed Slider)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // AI Model Selector Chips
                    Text(
                        text = "NEURAL MODEL PROVIDER",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableModels.forEach { model ->
                            val isSelected = selectedModel == model.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) TungstenAmber.copy(alpha = 0.15f) else MatteCarbon,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) HairlineAmber else HairlineBorder
                                ),
                                modifier = Modifier
                                    .clickable { onModelSelect(model.id) }
                                    .testTag("overlay_model_${model.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = model.name.uppercase(),
                                        color = if (isSelected) TungstenAmber else TextWhite,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (model.isFree) "FREE" else model.badge.uppercase(),
                                        color = if (model.isFree) SignalEmerald else TextMuted,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // One-Tap Presets
                    Text(
                        text = "QUICK BLUEPRINT PRESETS",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MatteCarbon,
                                border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
                                modifier = Modifier
                                    .clickable { onSelectPreset(preset) }
                                    .testTag("preset_${preset.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset.previewIcon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = preset.title,
                                            color = TextWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${preset.strokeCountApprox} STROKES",
                                            color = TextMuted,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Drawing Speed Multiplier Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TungstenAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STROKE RATE (${settings.executionProfile.displayName.uppercase()})",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${String.format("%.1fx", settings.speedMultiplier)}",
                            color = TungstenAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = settings.speedMultiplier,
                        onValueChange = { onUpdateSettings(settings.copy(speedMultiplier = it)) },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = TungstenAmber,
                            activeTrackColor = TungstenAmber,
                            inactiveTrackColor = MatteCarbon
                        ),
                        modifier = Modifier.testTag("speed_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // PEN TYPE BUBBLES
                    Text(
                        text = "INSTRUMENT EMULATION",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.model.PenType.values().forEach { pen ->
                            val isSel = settings.penType == pen
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) TungstenAmber.copy(alpha = 0.2f) else MatteCarbon,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) HairlineAmber else HairlineBorder),
                                modifier = Modifier.clickable { onUpdateSettings(settings.copy(penType = pen)) }
                            ) {
                                Text(
                                    text = pen.displayName.uppercase(),
                                    color = if (isSel) TungstenAmber else TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // GHOST TRACING AR OVERLAY TOGGLE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "GHOST TRACING (AR LIGHT TABLE)",
                                color = if (settings.ghostTracingMode) TungstenAmber else TextWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Project stroke blueprint for manual tracing",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = settings.ghostTracingMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(ghostTracingMode = it)) },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = TungstenAmber
                            )
                        )
                    }
                }
            }
        }
    }
}

