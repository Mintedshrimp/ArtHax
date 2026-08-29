package com.example.ui.screens

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.CalibrationBounds
import com.example.model.ExecutionState
import com.example.model.PuterAuthState
import com.example.ui.components.CyberCanvas
import com.example.ui.components.DraggableCutoutBox
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanDark
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val overlayGranted by viewModel.isOverlayPermissionGranted.collectAsState()
    val accessibilityEnabled by viewModel.isAccessibilityServiceEnabled.collectAsState()
    val overlayServiceRunning by viewModel.isOverlayServiceRunning.collectAsState()
    val sdkReady by viewModel.puterBridge.isSdkReady.collectAsState()
    val bridgeLog by viewModel.puterBridge.lastLog.collectAsState()
    val puterAuthState by viewModel.puterAuthState.collectAsState()

    val promptText by viewModel.promptText.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val instructionSet by viewModel.currentInstructionSet.collectAsState()
    val execState by viewModel.executionState.collectAsState()
    val settings by viewModel.drawingSettings.collectAsState()
    val bounds by viewModel.calibrationBounds.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val heroGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_glow"
    )

    var showCalibrateSliders by remember { mutableStateOf(false) }
    var isCutoutInteractiveMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CyberBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // HEADER BAR & TITLE
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .shadow(8.dp, CircleShape, spotColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ArtHax // NEON CORE",
                            color = NeonCyan,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "AI Floating Drawing Assistant • Sekai Engine",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshServiceStatus() },
                    modifier = Modifier.testTag("refresh_status_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Status",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // STATUS CHIPS (Overlay, Accessibility, Puter.js)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Floating Overlay Permission Chip
                StatusBadgeChip(
                    title = "FLOATING OVERLAY",
                    status = if (overlayGranted) "READY" else "PERMISSION REQ",
                    isActive = overlayGranted,
                    activeColor = NeonGreen,
                    inactiveColor = NeonPink,
                    onClick = { viewModel.openOverlaySettings() }
                )

                // Accessibility Service Chip
                StatusBadgeChip(
                    title = "ACCESSIBILITY GESTURE",
                    status = if (accessibilityEnabled) "ONLINE" else "TAP TO ENABLE",
                    isActive = accessibilityEnabled,
                    activeColor = NeonCyan,
                    inactiveColor = NeonYellow,
                    onClick = { viewModel.openAccessibilitySettings() }
                )

                // Puter.js AI Bridge Chip
                StatusBadgeChip(
                    title = "PUTER.JS AI",
                    status = if (sdkReady) "CONNECTED" else "CONNECTING",
                    isActive = sdkReady,
                    activeColor = NeonPurple,
                    inactiveColor = TextMuted,
                    onClick = {}
                )
            }

            // Quick Setup Alert if permissions missing
            if (!overlayGranted || !accessibilityEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                PermissionSetupCard(
                    overlayGranted = overlayGranted,
                    accessibilityEnabled = accessibilityEnabled,
                    onGrantOverlay = { viewModel.openOverlaySettings() },
                    onGrantAccessibility = { viewModel.openAccessibilitySettings() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // FLOATING OVERLAY LAUNCHER BAR
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            if (overlayServiceRunning) listOf(NeonPink, NeonPurple)
                            else listOf(NeonCyan, NeonPink)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("floating_launcher_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (overlayServiceRunning) "FLOATING OVERLAY DECK ACTIVE" else "ACTIVATE FLOATING OVERLAY",
                            color = if (overlayServiceRunning) NeonPink else NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (overlayServiceRunning) "Tap bubble to draw over other apps (Sekai)" else "Overlays floating bubble & HUD on other apps",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleOverlayService() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (overlayServiceRunning) NeonPink else NeonCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("launch_overlay_btn")
                    ) {
                        Icon(
                            imageVector = if (overlayServiceRunning) Icons.Default.Stop else Icons.Default.Layers,
                            contentDescription = null,
                            tint = CyberBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (overlayServiceRunning) "STOP" else "LAUNCH",
                            color = CyberBlack,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // INTERACTIVE CYBER SANDBOX CANVAS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SANDBOX CANVAS & STROKE PREVIEW",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (instructionSet != null) {
                    Text(
                        text = "${instructionSet?.strokes?.size} STROKES • ${instructionSet?.totalEstimatedPoints} PTS",
                        color = NeonPink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Display Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = NeonCyan)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (isCutoutInteractiveMode) {
                    DraggableCutoutBox(
                        bounds = bounds,
                        onBoundsChange = { viewModel.setCalibrationBounds(it) },
                        onConfirmAndDraw = {
                            isCutoutInteractiveMode = false
                            viewModel.startSandboxSimulation()
                        },
                        onClose = {
                            isCutoutInteractiveMode = false
                        },
                        instructionSet = instructionSet,
                        executionState = execState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CyberCanvas(
                        instructionSet = instructionSet,
                        executionState = execState,
                        bounds = if (showCalibrateSliders) bounds else null,
                        showGrid = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulation & Test Gesture Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (execState is ExecutionState.Drawing) {
                    Button(
                        onClick = { viewModel.abortSimulation() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("sandbox_abort_btn"),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = CyberBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ABORT DRAWING",
                            color = CyberBlack,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.startSandboxSimulation() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, NeonCyan, RoundedCornerShape(22.dp))
                            .testTag("sandbox_sim_btn"),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardBackgroundElevated),
                        enabled = instructionSet != null && instructionSet!!.strokes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SIMULATE IN SANDBOX",
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Interactive Drag Cutout Box Button
                Button(
                    onClick = { isCutoutInteractiveMode = !isCutoutInteractiveMode },
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, if (isCutoutInteractiveMode) NeonPink else NeonCyan, RoundedCornerShape(22.dp))
                        .testTag("toggle_cutout_interactive_btn"),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCutoutInteractiveMode) NeonPink.copy(alpha = 0.25f) else CardBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = null,
                        tint = if (isCutoutInteractiveMode) NeonPink else NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCutoutInteractiveMode) "DONE" else "DRAG CUTOUT",
                        color = if (isCutoutInteractiveMode) NeonPink else NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Calibrate canvas frame toggle button
                Button(
                    onClick = { showCalibrateSliders = !showCalibrateSliders },
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, if (showCalibrateSliders) NeonPink else BorderGlass, RoundedCornerShape(22.dp))
                        .testTag("toggle_calibration_ui_btn"),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showCalibrateSliders) NeonPink.copy(alpha = 0.2f) else CardBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (showCalibrateSliders) NeonPink else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SLIDERS",
                        color = if (showCalibrateSliders) NeonPink else TextWhite,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Calibrate Sliders Section
            AnimatedVisibility(visible = showCalibrateSliders) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBackground)
                        .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "SEKAI CANVAS BOUNDS CALIBRATION",
                        color = NeonPink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Align this box to the exact target drawing canvas in Sekai",
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Top Offset: ${(bounds.top * 100).toInt()}%", color = TextWhite, fontSize = 11.sp)
                    Slider(
                        value = bounds.top,
                        onValueChange = { viewModel.updateCalibrationBounds(bounds.copy(top = it)) },
                        valueRange = 0.05f..0.50f,
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                    )

                    Text("Bottom Offset: ${(bounds.bottom * 100).toInt()}%", color = TextWhite, fontSize = 11.sp)
                    Slider(
                        value = bounds.bottom,
                        onValueChange = { viewModel.updateCalibrationBounds(bounds.copy(bottom = it)) },
                        valueRange = 0.50f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // PROMPT INPUT & AI MODEL SELECTOR
            // ==========================================
            Text(
                text = "AI DRAWING INSTRUCTION GENERATOR",
                color = NeonCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Prompt Field with Neon Underline
            TextField(
                value = promptText,
                onValueChange = { viewModel.setPrompt(it) },
                placeholder = {
                    Text("Enter drawing prompt or character name...", color = TextMuted, fontSize = 13.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                    .testTag("main_prompt_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackgroundElevated,
                    unfocusedContainerColor = CardBackground,
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
                    viewModel.generateStrokes()
                }),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.generateStrokes()
                        },
                        modifier = Modifier.testTag("main_generate_btn")
                    ) {
                        if (execState is ExecutionState.Generating) {
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

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // PUTER.JS AUTHENTICATION & FREE MODELS CARD
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (puterAuthState.isSignedIn) NeonGreen.copy(alpha = 0.5f) else BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("puter_auth_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (puterAuthState.isSignedIn) NeonGreen else NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (puterAuthState.isSignedIn) "PUTER.JS // AUTHENTICATED" else "PUTER.JS // FREE TIER MODE",
                                    color = if (puterAuthState.isSignedIn) NeonGreen else NeonCyan,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (puterAuthState.isSignedIn) {
                                        "User: @${puterAuthState.username ?: "Puter User"} • Cloud Sync Active"
                                    } else {
                                        "Guest Mode • All Free Models (Claude, Gemini, DeepSeek) Ready"
                                    },
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (puterAuthState.isSignedIn) {
                            Button(
                                onClick = { viewModel.logoutFromPuter() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink.copy(alpha = 0.25f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("puter_logout_btn")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = NeonPink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LOGOUT", color = NeonPink, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.loginToPuter() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("puter_login_btn")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LOGIN", color = CyberBlack, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Selection Chips
            Text(
                text = "AVAILABLE FREE & CLOUD MODELS",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.availableModels.forEach { model ->
                    val isSelected = selectedModel == model.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardBackground)
                            .border(1.dp, if (isSelected) NeonCyan else BorderGlass, RoundedCornerShape(18.dp))
                            .clickable { viewModel.selectModel(model.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("model_${model.id}")
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
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // SEKAI QUICK PRESET LIBRARY
            // ==========================================
            Text(
                text = "SEKAI & CYBER ART PRESETS",
                color = NeonCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectPreset(preset) }
                            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                            .testTag("preset_card_${preset.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = preset.previewIcon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = preset.title,
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
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
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // SPEED & DELAY TUNING
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STROKE EXECUTION SPEED",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1fx", settings.speedMultiplier)} SPEED",
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = settings.speedMultiplier,
                        onValueChange = { viewModel.updateSettings(settings.copy(speedMultiplier = it)) },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CardBackgroundElevated
                        ),
                        modifier = Modifier.testTag("main_speed_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusBadgeChip(
    title: String,
    status: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, if (isActive) activeColor.copy(alpha = 0.4f) else inactiveColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("status_chip_${title.replace(" ", "_")}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else inactiveColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = status,
                    color = if (isActive) activeColor else inactiveColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PermissionSetupCard(
    overlayGranted: Boolean,
    accessibilityEnabled: Boolean,
    onGrantOverlay: () -> Unit,
    onGrantAccessibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonYellow.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .testTag("permission_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SETUP REQUIRED FOR DRAWING OVER APPS",
                    color = NeonYellow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!overlayGranted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1. Floating Overlay Permission",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = onGrantOverlay,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!accessibilityEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "2. Accessibility Gesture Service",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = onGrantAccessibility,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enable", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
