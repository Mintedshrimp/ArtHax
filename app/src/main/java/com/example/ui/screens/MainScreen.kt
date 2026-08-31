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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
    var showRestrictedSettingsDialog by remember { mutableStateOf(false) }

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
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = ObsidianBlack
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
            // 1. TOP APP BAR (Neo-Precision Monolithic Header)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CarbonElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineCobalt),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = TungstenAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ARTHAX",
                                color = TextWhite,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TungstenAmber.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, HairlineAmber)
                            ) {
                                Text(
                                    text = "PRECISION",
                                    color = TungstenAmber,
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (sdkReady) SignalEmerald else TungstenAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (sdkReady) {
                                    if (puterAuthState.isSignedIn) "PUTER.JS // @${puterAuthState.username?.uppercase()}" else "PUTER.JS // ONLINE"
                                } else "PUTER.JS // CONNECTING...",
                                color = if (sdkReady) SignalEmerald else TungstenAmber,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                }

                // TOP RIGHT SETTINGS BUTTON
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonElevated)
                        .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                        .testTag("top_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
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
                        width = 1.dp,
                        color = if (overlayServiceRunning) SignalEmerald.copy(alpha = pulseGlow) else if (allPermissionsReady) HairlineAmber else HairlineBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("main_hero_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CarbonElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (overlayServiceRunning) SignalEmerald.copy(alpha = 0.4f) else HairlineCobalt),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (overlayServiceRunning) Icons.Default.PlayArrow else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (overlayServiceRunning) SignalEmerald else TungstenAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (overlayServiceRunning) "OVERLAY ENGINE ACTIVE" else "VECTOR SYNTHESIS ENGINE",
                        color = TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (overlayServiceRunning) {
                            "Precision overlay active. Tap the floating instrument on screen to open chat or calibrate canvas."
                        } else {
                            "Directly translates AI prompts into calibrated trajectory vectors across all Android paint applications."
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
                            .height(48.dp)
                            .testTag("start_draw_overlay_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (overlayServiceRunning) LaserCrimson else TungstenAmber
                        )
                    ) {
                        Icon(
                            imageVector = if (overlayServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (overlayServiceRunning) TextWhite else ObsidianBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (overlayServiceRunning) "TERMINATE OVERLAY" else if (!allPermissionsReady) "INITIALIZE PERMISSIONS" else "START DRAW OVERLAY",
                            color = if (overlayServiceRunning) TextWhite else ObsidianBlack,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 13.sp,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 3. REQUIRED PERMISSIONS (Neo-Precision Telemetry Cards)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEM PERMISSIONS",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = if (allPermissionsReady) "[STATUS: ALL GREEN]" else "[STATUS: PENDING]",
                    color = if (allPermissionsReady) SignalEmerald else TungstenAmber,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // OVERLAY PERMISSION ITEM
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
                    .testTag("permission_overlay_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonElevated)
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
                            shape = RoundedCornerShape(6.dp),
                            color = (if (overlayGranted) SignalEmerald else CobaltBeam).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (overlayGranted) SignalEmerald.copy(alpha = 0.3f) else HairlineCobalt),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (overlayGranted) SignalEmerald else CobaltBeam,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Overlay Window",
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (overlayGranted) "[ACTIVE]" else "[REQUIRED]",
                                    color = if (overlayGranted) SignalEmerald else TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (overlayGranted) "Floating instrument rendering enabled" else "Required to display floating hub over canvases",
                                color = if (overlayGranted) SignalEmerald else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (overlayGranted) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SignalEmerald.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, SignalEmerald.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "READY",
                                color = SignalEmerald,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openOverlaySettings() },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltBeam),
                            modifier = Modifier.height(32.dp).testTag("grant_overlay_btn")
                        ) {
                            Text("GRANT", color = TextWhite, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ACCESSIBILITY SERVICE ITEM
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
                    .testTag("permission_accessibility_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonElevated)
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
                            shape = RoundedCornerShape(6.dp),
                            color = (if (accessibilityEnabled) SignalEmerald else TungstenAmber).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (accessibilityEnabled) SignalEmerald.copy(alpha = 0.3f) else HairlineAmber),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = if (accessibilityEnabled) SignalEmerald else TungstenAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Touch Injection Service",
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (accessibilityEnabled) "[ACTIVE]" else "[REQUIRED]",
                                    color = if (accessibilityEnabled) SignalEmerald else TungstenAmber,
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (accessibilityEnabled) "Gesture trajectory engine ready" else "Required to automate stylus & touch strokes",
                                color = if (accessibilityEnabled) SignalEmerald else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (accessibilityEnabled) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SignalEmerald.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, SignalEmerald.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "READY",
                                color = SignalEmerald,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.openAccessibilitySettings() },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TungstenAmber),
                            modifier = Modifier.height(32.dp).testTag("enable_accessibility_btn")
                        ) {
                            Text("ENABLE", color = ObsidianBlack, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ==========================================
            // RESTRICTED SETTINGS ONBOARDING ASSISTANT (Android 13+)
            // ==========================================
            if (!accessibilityEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TungstenAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .testTag("restricted_settings_helper_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonElevated)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TungstenAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RESTRICTED SETTINGS ASSISTANT",
                                    color = TungstenAmber,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            IconButton(
                                onClick = { showRestrictedSettingsDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Help Guide",
                                    tint = TungstenAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "On Android 13+, Accessibility services for sideloaded APKs are locked by default ('Restricted setting'). Use these 2 steps to unlock:",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openAppInfoSettings() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("open_app_info_btn"),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CobaltBeam)
                            ) {
                                Text(
                                    text = "1. OPEN APP INFO",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            Button(
                                onClick = { viewModel.openAccessibilitySettings() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("open_accessibility_btn"),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TungstenAmber)
                            ) {
                                Text(
                                    text = "2. TURN SERVICE ON",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "💡 Step 1: In App Info, tap ⋮ (top-right) -> 'Allow restricted settings'\n💡 Step 2: In Accessibility, find 'ArtHax Drawing Service' -> Turn ON",
                            color = TextWhite.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PUTER.JS AUTH CARD (Minimalist Neo-Precision)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
                    .testTag("main_puter_auth_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonElevated)
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
                            shape = RoundedCornerShape(6.dp),
                            color = CobaltBeam.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HairlineCobalt),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = CobaltBeam,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (puterAuthState.isSignedIn) "@${puterAuthState.username ?: "User"}" else "Puter.js AI Engine",
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (puterAuthState.isSignedIn) "[AUTHENTICATED]" else "[FREE TIER]",
                                    color = if (puterAuthState.isSignedIn) SignalEmerald else TungstenAmber,
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (puterAuthState.isSignedIn) "Full model quota unlocked" else "Claude 3.5, Gemini 1.5, DeepSeek free access",
                                color = if (puterAuthState.isSignedIn) SignalEmerald else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (puterAuthState.isSignedIn) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SignalEmerald.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, SignalEmerald.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "LOGGED IN",
                                color = SignalEmerald,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showPuterLoginDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CobaltBeam),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HairlineCobalt),
                            modifier = Modifier.height(32.dp).testTag("main_login_btn")
                        ) {
                            Text("LOG IN", fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. STEP-BY-STEP USER GUIDE (Industrial Workflow Matrix)
            // ==========================================
            Text(
                text = "EXECUTION WORKFLOW",
                color = TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GuideStepItem(
                        stepNumber = "01",
                        title = "INITIALIZE OVERLAY",
                        description = "Enable system permissions above and launch the overlay instrument."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideStepItem(
                        stepNumber = "02",
                        title = "OPEN TARGET APPLICATION",
                        description = "Launch Ibis Paint X, Autodesk Sketchbook, Infinite Painter, or Notes."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideStepItem(
                        stepNumber = "03",
                        title = "CALIBRATE BOUNDARIES",
                        description = "Tap the floating instrument -> 'Crop Canvas' to align the precision viewfinder."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GuideStepItem(
                        stepNumber = "04",
                        title = "SYNTHESIZE & DISPATCH",
                        description = "Prompt the model (e.g. 'Cyber Katana') to synthesize and execute native screen strokes."
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

        // ==========================================
        // RESTRICTED SETTINGS GUIDE DIALOG (Android 13+)
        // ==========================================
        if (showRestrictedSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showRestrictedSettingsDialog = false },
                containerColor = MatteCarbon,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TungstenAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unblock Restricted Settings",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Android 13, 14, and 15 automatically restrict Accessibility services for apps installed outside Google Play. Here is how to unlock ArtHax in 15 seconds:",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CarbonElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HairlineCobalt),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "1️⃣ Tap 'OPEN APP INFO' below",
                                    color = CobaltBeam,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                                Text(
                                    text = "2️⃣ In the top right corner, tap the 3 vertical dots (⋮)",
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "3️⃣ Select 'Allow restricted settings' and verify with fingerprint/PIN",
                                    color = SignalEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "4️⃣ Return to ArtHax, tap 'TURN SERVICE ON' and toggle ArtHax Drawing Service ON",
                                    color = TungstenAmber,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRestrictedSettingsDialog = false
                            viewModel.openAppInfoSettings()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBeam),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("OPEN APP INFO", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRestrictedSettingsDialog = false }
                    ) {
                        Text("CLOSE", color = TextMuted, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
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
            shape = RoundedCornerShape(4.dp),
            color = CarbonElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, HairlineAmber),
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    color = TungstenAmber,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
