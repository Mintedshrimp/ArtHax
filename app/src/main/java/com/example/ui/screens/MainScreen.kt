package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PuterAuthState
import com.example.ui.components.PuterAuthSheet
import com.example.ui.components.PuterLoginDialog
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PuterAuthViewModel

/**
 * Minimalist, modern Main Screen.
 * Contains:
 * - Top-right Settings icon (Theme, Puter.js account login/logout, drawing speed, info)
 * - Puter.js AI connectivity status & quick login
 * - Permission toggles (Overlay Permission & Accessibility Service)
 * - Prominent "Start Draw Overlay" action button (appears when granted)
 * - Interactive step-by-step guide on how to crop canvas and draw in paint apps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    puterAuthViewModel: PuterAuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    initialShowLoginDialog: Boolean = false,
    modifier: Modifier = Modifier
) {
    val overlayGranted by viewModel.isOverlayPermissionGranted.collectAsState()
    val accessibilityEnabled by viewModel.isAccessibilityServiceEnabled.collectAsState()
    val overlayServiceRunning by viewModel.isOverlayServiceRunning.collectAsState()
    val sdkReady by viewModel.puterBridge.isSdkReady.collectAsState()
    val puterAuthState by viewModel.puterAuthState.collectAsState()
    val drawingSettings by viewModel.drawingSettings.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showPuterAuthSheet by remember { mutableStateOf(false) }
    var showPuterLoginDialog by remember { mutableStateOf(initialShowLoginDialog) }

    val allPermissionsReady = overlayGranted && accessibilityEnabled

    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 1. TOP APP BAR (Minimalist with Top-Right Settings)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ArtHax AI",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (sdkReady) NeonGreen else NeonYellow)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (sdkReady) {
                                    if (puterAuthState.isSignedIn) "Puter.js @${puterAuthState.username}" else "Puter.js AI Online"
                                } else "Connecting Puter.js...",
                                color = if (sdkReady) NeonGreen else NeonYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // TOP RIGHT SETTINGS BUTTON
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(1.dp, BorderGlass, CircleShape)
                        .testTag("top_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 2. HERO / START DRAW OVERLAY ACTION
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (overlayServiceRunning) NeonGreen.copy(alpha = pulseGlow) else if (allPermissionsReady) NeonCyan else BorderGlass,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("main_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = (if (overlayServiceRunning) NeonGreen else NeonCyan).copy(alpha = 0.15f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (overlayServiceRunning) Icons.Default.PlayArrow else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (overlayServiceRunning) NeonGreen else NeonCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (overlayServiceRunning) "Draw Overlay is Active" else "AI Vector Assistant",
                        color = TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (overlayServiceRunning) {
                            "Tap the floating bubble on screen to open chat or crop canvas in any paint app."
                        } else {
                            "Draws AI prompts into any Android paint app (Ibis Paint, Sketchbook, Infinite Painter)."
                        },
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // PRIMARY DRAW OVERLAY BUTTON
                    Button(
                        onClick = {
                            if (!overlayGranted) {
                                viewModel.openOverlaySettings()
                            } else if (!accessibilityEnabled) {
                                viewModel.openAccessibilitySettings()
                            } else {
                                viewModel.toggleOverlayService()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_draw_overlay_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (overlayServiceRunning) NeonPink else NeonCyan
                        )
                    ) {
                        Icon(
                            imageVector = if (overlayServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (overlayServiceRunning) "Stop Draw Overlay" else if (!allPermissionsReady) "Grant Permissions to Start" else "Start Draw Overlay",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 3. REQUIRED PERMISSIONS (Minimalist Cards)
            // ==========================================
            Text(
                text = "Required Setup",
                color = TextWhite,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // OVERLAY PERMISSION ITEM
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                    .testTag("permission_overlay_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = (if (overlayGranted) NeonGreen else NeonCyan).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (overlayGranted) NeonGreen else NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Overlay Window",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (overlayGranted) "Granted — bubble overlay enabled" else "Display floating bubble over paint apps",
                                color = if (overlayGranted) NeonGreen else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (overlayGranted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ready", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openOverlaySettings() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.height(34.dp).testTag("grant_overlay_btn")
                        ) {
                            Text("Grant", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ACCESSIBILITY SERVICE ITEM
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                    .testTag("permission_accessibility_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = (if (accessibilityEnabled) NeonGreen else NeonPink).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = if (accessibilityEnabled) NeonGreen else NeonPink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Drawing Service",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (accessibilityEnabled) "Enabled — automated drawing ready" else "Required to draw strokes in paint apps",
                                color = if (accessibilityEnabled) NeonGreen else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (accessibilityEnabled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ready", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openAccessibilitySettings() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                            modifier = Modifier.height(34.dp).testTag("enable_accessibility_btn")
                        ) {
                            Text("Enable", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PUTER.JS AUTH CARD (Minimalist)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                    .testTag("main_puter_auth_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (puterAuthState.isSignedIn) "Puter @${puterAuthState.username ?: "User"}" else "Puter.js AI (Free Mode)",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (puterAuthState.isSignedIn) "Full account access unlocked" else "Claude, Gemini, DeepSeek free tier",
                                color = if (puterAuthState.isSignedIn) NeonGreen else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (puterAuthState.isSignedIn) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "LOGGED IN",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showPuterLoginDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp).testTag("main_login_btn")
                        ) {
                            Text("Log In", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. STEP-BY-STEP USER GUIDE
            // ==========================================
            Text(
                text = "How to Draw in Paint Apps",
                color = TextWhite,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GuideStepItem(
                        stepNumber = "1",
                        title = "Start the Draw Overlay",
                        description = "Grant overlay & accessibility permissions above, then tap 'Start Draw Overlay'."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideStepItem(
                        stepNumber = "2",
                        title = "Open Your Paint App",
                        description = "Switch to Ibis Paint X, Autodesk Sketchbook, Infinite Painter, or Notes."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideStepItem(
                        stepNumber = "3",
                        title = "Tap Floating Bubble -> 'Canvas' (Top Right)",
                        description = "Press the floating bubble, then tap 'Canvas' at top right to drag & resize the crop box over your canvas area."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideStepItem(
                        stepNumber = "4",
                        title = "Prompt AI & Draw",
                        description = "Pick a free AI model, type your prompt (e.g. 'Chibi Miku'), and AI will draw vector strokes directly onto your screen!"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ==========================================
        // SETTINGS BOTTOM SHEET
        // ==========================================
        if (showSettingsSheet) {
            SettingsBottomSheet(
                onDismiss = { showSettingsSheet = false },
                puterAuthState = puterAuthState,
                onLoginClick = {
                    showSettingsSheet = false
                    showPuterLoginDialog = true
                },
                onLogoutClick = {
                    viewModel.logoutFromPuter()
                },
                settings = drawingSettings,
                onUpdateSettings = { viewModel.updateSettings(it) }
            )
        }

        // ==========================================
        // MINIMALIST PUTER LOGIN WEBVIEW DIALOG
        // ==========================================
        PuterLoginDialog(
            isOpen = showPuterLoginDialog,
            onDismiss = { showPuterLoginDialog = false },
            onLoginSuccess = { username, email ->
                viewModel.handleLoginSuccess(username, email)
                showPuterLoginDialog = false
            }
        )

        // ==========================================
        // PUTER AUTH SHEET
        // ==========================================
        if (showPuterAuthSheet) {
            PuterAuthSheet(
                viewModel = puterAuthViewModel,
                onDismiss = { showPuterAuthSheet = false }
            )
        }
    }
}

@Composable
private fun GuideStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = NeonCyan.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
