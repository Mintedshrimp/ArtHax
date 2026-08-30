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
import androidx.compose.material.icons.filled.SmartToy
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
import com.example.ui.components.CompactFloatingChatWindow
import com.example.ui.components.CyberCanvas
import com.example.ui.components.DraggableCutoutBox
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.components.PuterAuthSheet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanDark
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PuterAuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    puterAuthViewModel: PuterAuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
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
    val chatMessages by viewModel.chatMessages.collectAsState()

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
    var showFloatingChatWindow by remember { mutableStateOf(true) }
    var showPuterAuthSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CyberBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Art Assistant",
                                color = TextWhite,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Automated vector drawing and floating overlay assistant",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshServiceStatus() },
                    modifier = Modifier.testTag("refresh_status_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Status",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    title = "Floating Overlay",
                    status = if (overlayGranted) "Active" else "Action Required",
                    isActive = overlayGranted,
                    activeColor = NeonGreen,
                    inactiveColor = NeonYellow,
                    onClick = { viewModel.openOverlaySettings() }
                )

                // Accessibility Service Chip
                StatusBadgeChip(
                    title = "Gesture Service",
                    status = if (accessibilityEnabled) "Connected" else "Tap to Enable",
                    isActive = accessibilityEnabled,
                    activeColor = NeonGreen,
                    inactiveColor = NeonYellow,
                    onClick = { viewModel.openAccessibilitySettings() }
                )

                // Puter.js AI Bridge Chip
                StatusBadgeChip(
                    title = "AI Cloud Engine",
                    status = if (sdkReady) (if (puterAuthState.isSignedIn) "Signed In" else "Ready (Guest)") else "Connecting...",
                    isActive = sdkReady,
                    activeColor = NeonCyan,
                    inactiveColor = TextMuted,
                    onClick = { showPuterAuthSheet = true }
                )
            }

            // Quick Setup Alert if permissions missing
            if (!overlayGranted || !accessibilityEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
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
                        width = 1.dp,
                        color = if (overlayServiceRunning) NeonPink.copy(alpha = 0.5f) else BorderGlass,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("floating_launcher_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (overlayServiceRunning) "Floating Overlay Active" else "Floating Drawing Assistant",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (overlayServiceRunning) "Tap the floating bubble over canvas apps to draw" else "Show floating bubble to trigger drawings over other apps",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { viewModel.toggleOverlayService() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (overlayServiceRunning) CardBackground else NeonCyan
                        ),
                        border = if (overlayServiceRunning) androidx.compose.foundation.BorderStroke(1.dp, NeonPink) else null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("launch_overlay_btn")
                    ) {
                        Icon(
                            imageVector = if (overlayServiceRunning) Icons.Default.Stop else Icons.Default.Layers,
                            contentDescription = null,
                            tint = if (overlayServiceRunning) NeonPink else TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (overlayServiceRunning) "Stop" else "Launch",
                            color = if (overlayServiceRunning) NeonPink else TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // INTERACTIVE CANVAS & STROKE PREVIEW
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Canvas Preview & Simulation",
                        color = TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Real-time preview of vector drawing paths",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (instructionSet != null) {
                    Surface(
                        color = CardBackgroundElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass)
                    ) {
                        Text(
                            text = "${instructionSet?.strokes?.size} strokes • ${instructionSet?.totalEstimatedPoints} points",
                            color = TextCyan,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Display Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation & Test Gesture Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (execState is ExecutionState.Drawing) {
                    Button(
                        onClick = { viewModel.abortSimulation() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("sandbox_abort_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonYellow)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stop Drawing",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.startSandboxSimulation() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("sandbox_sim_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        enabled = instructionSet != null && instructionSet!!.strokes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulate Drawing",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Interactive Drag Cutout Box Button
                Button(
                    onClick = { isCutoutInteractiveMode = !isCutoutInteractiveMode },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("toggle_cutout_interactive_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCutoutInteractiveMode) NeonPink else BorderGlass),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCutoutInteractiveMode) CardBackgroundElevated else CardBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = null,
                        tint = if (isCutoutInteractiveMode) NeonPink else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCutoutInteractiveMode) "Done" else "Adjust Bounds",
                        color = if (isCutoutInteractiveMode) NeonPink else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Calibrate canvas frame toggle button
                Button(
                    onClick = { showCalibrateSliders = !showCalibrateSliders },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("toggle_calibration_ui_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (showCalibrateSliders) NeonCyan else BorderGlass),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showCalibrateSliders) CardBackgroundElevated else CardBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (showCalibrateSliders) NeonCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sliders",
                        color = if (showCalibrateSliders) NeonCyan else TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Canvas Calibration Controls",
                        color = TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Configure offset boundaries to match your target drawing application",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Top Boundary: ${(bounds.top * 100).toInt()}%", color = TextWhite, fontSize = 12.sp)
                    Slider(
                        value = bounds.top,
                        onValueChange = { viewModel.updateCalibrationBounds(bounds.copy(top = it)) },
                        valueRange = 0.05f..0.50f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )

                    Text("Bottom Boundary: ${(bounds.bottom * 100).toInt()}%", color = TextWhite, fontSize = 12.sp)
                    Slider(
                        value = bounds.bottom,
                        onValueChange = { viewModel.updateCalibrationBounds(bounds.copy(bottom = it)) },
                        valueRange = 0.50f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // PROMPT INPUT & AI MODEL SELECTOR
            // ==========================================
            Text(
                text = "AI Drawing Generator",
                color = TextWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Describe an image or subject to generate automated drawing strokes",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Prompt Field
            TextField(
                value = promptText,
                onValueChange = { viewModel.setPrompt(it) },
                placeholder = {
                    Text("Describe what you want to draw (e.g., Cute cat, Mountain sunrise, Castle sketch)...", color = TextMuted, fontSize = 13.sp)
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
                    cursorColor = NeonCyan
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
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate Vector Strokes",
                                tint = NeonCyan
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // PUTER.JS AUTHENTICATION & FREE MODELS CARD
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (puterAuthState.isSignedIn) NeonGreen.copy(alpha = 0.4f) else BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("puter_auth_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (puterAuthState.isSignedIn) NeonGreen.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = if (puterAuthState.isSignedIn) NeonGreen else NeonCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (puterAuthState.isSignedIn) "Signed in with Puter.js" else "Puter.js AI Cloud",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (puterAuthState.isSignedIn) {
                                        "@${puterAuthState.username ?: "User"} • Cloud Quota Active"
                                    } else {
                                        "Free Guest Access • Claude, Gemini, DeepSeek"
                                    },
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Button(
                            onClick = { showPuterAuthSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (puterAuthState.isSignedIn) CardBackground else NeonCyan
                            ),
                            border = if (puterAuthState.isSignedIn) androidx.compose.foundation.BorderStroke(1.dp, BorderGlass) else null,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag(if (puterAuthState.isSignedIn) "puter_account_btn" else "puter_login_btn")
                        ) {
                            Text(
                                text = if (puterAuthState.isSignedIn) "Manage" else "Sign In / Models",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Selection Chips
            Text(
                text = "Selected AI Model",
                color = TextMuted,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CardBackground)
                            .border(1.dp, if (isSelected) NeonCyan else BorderGlass, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectModel(model.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("model_${model.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = model.name,
                                color = if (isSelected) NeonCyan else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (model.isFree) NeonGreen.copy(alpha = 0.15f) else CardBackgroundElevated,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (model.isFree) "FREE" else model.badge,
                                    color = if (model.isFree) NeonGreen else TextMuted,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // QUICK ART PRESET TEMPLATES
            // ==========================================
            Text(
                text = "Quick Art Templates",
                color = TextWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pre-computed vector stroke sets ready to simulate or draw",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))

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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = preset.previewIcon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = preset.title,
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${preset.strokeCountApprox} strokes • ${preset.category}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Drawing Speed Multiplier",
                                color = TextWhite,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Adjust how fast automated gesture strokes execute",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "${String.format("%.1fx", settings.speedMultiplier)}",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Floating Chat Window Overlay
        AnimatedVisibility(
            visible = showFloatingChatWindow,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CompactFloatingChatWindow(
                messages = chatMessages,
                currentPrompt = promptText,
                onPromptChange = { viewModel.setPrompt(it) },
                onSendPrompt = { viewModel.sendChatMessage(it) },
                availableModels = viewModel.availableModels,
                selectedModelId = selectedModel,
                onSelectModel = { viewModel.selectModel(it) },
                executionState = execState,
                onExecuteDrawing = { viewModel.startSandboxSimulation() },
                onClearChat = { viewModel.clearChat() },
                onClose = { showFloatingChatWindow = false }
            )
        }

        // Floating trigger button when chat window is closed
        if (!showFloatingChatWindow) {
            Button(
                onClick = { showFloatingChatWindow = true },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
                    .size(52.dp)
                    .testTag("open_floating_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Open AI Assistant",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Puter.js Auth & AI Model Browser Sheet Overlay
        AnimatedVisibility(
            visible = showPuterAuthSheet,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PuterAuthSheet(
                viewModel = puterAuthViewModel,
                onDismiss = { showPuterAuthSheet = false }
            )
        }
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
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, if (isActive) activeColor.copy(alpha = 0.35f) else BorderGlass, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("status_chip_${title.replace(" ", "_")}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else inactiveColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = status,
                    color = if (isActive) TextWhite else inactiveColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
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
            .border(1.dp, NeonYellow.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .testTag("permission_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permissions Required for Floating Drawing",
                    color = TextWhite,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!overlayGranted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Floating Overlay Permission",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Allows displaying floating controls over drawing apps",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onGrantOverlay,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Grant", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (!accessibilityEnabled) {
                if (!overlayGranted) Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. Accessibility Gesture Service",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Automates precise touch gestures on external canvases",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onGrantAccessibility,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enable", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
