package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawingSettings
import com.example.model.PuterAuthState
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
 * Settings bottom sheet accessed via the top-right Settings icon.
 * Manages Puter.js login/logout, drawing speed/smoothing preferences, theme info, and about guide.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    puterAuthState: PuterAuthState,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    settings: DrawingSettings,
    onUpdateSettings: (DrawingSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CarbonElevated,
        dragHandle = null,
        modifier = modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(TungstenAmber.copy(alpha = 0.15f))
                            .border(1.dp, HairlineAmber, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = TungstenAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SYSTEM CONFIGURATION",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Engine parameters & synthesis controls",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("settings_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. PUTER.JS AUTH CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .testTag("settings_puter_auth_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (puterAuthState.isSignedIn) SignalEmerald else TungstenAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PUTER.JS NEURAL ENGINE",
                                color = TextWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = (if (puterAuthState.isSignedIn) SignalEmerald else TungstenAmber).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (puterAuthState.isSignedIn) SignalEmerald.copy(alpha = 0.3f) else HairlineAmber)
                        ) {
                            Text(
                                text = if (puterAuthState.isSignedIn) "AUTHENTICATED" else "ANONYMOUS / FREE",
                                color = if (puterAuthState.isSignedIn) SignalEmerald else TungstenAmber,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (puterAuthState.isSignedIn) {
                            "Authenticated as @${puterAuthState.username ?: "User"}. Full neural model access tier active."
                        } else {
                            "Free tier active. Sign in to your Puter account for higher rate limits and session backups."
                        },
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (puterAuthState.isSignedIn) {
                        OutlinedButton(
                            onClick = onLogoutClick,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CarbonElevated,
                                contentColor = LaserCrimson
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LaserCrimson.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("settings_logout_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DISCONNECT ACCOUNT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = onLoginClick,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TungstenAmber,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("settings_login_btn")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONNECT PUTER.JS", color = ObsidianBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DRAWING & EXECUTION CONTROLS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .testTag("settings_drawing_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Execution Profile (Cyber Turbo vs Organic Human)
                    Text(
                        text = "EXECUTION TRAJECTORY PROFILE",
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.model.ExecutionProfile.values().forEach { profile ->
                            val isSel = settings.executionProfile == profile
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) TungstenAmber.copy(alpha = 0.15f) else CarbonElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) HairlineAmber else HairlineBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateSettings(settings.copy(executionProfile = profile)) }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = profile.displayName.uppercase(),
                                        color = if (isSel) TungstenAmber else TextWhite,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = if (profile == com.example.model.ExecutionProfile.CYBER_TURBO) "Machine 5ms Turbo" else "Speedpaint Natural Easing",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HairlineBorder))
                    Spacer(modifier = Modifier.height(14.dp))

                    // PEN TYPE BUBBLES
                    Text(
                        text = "INSTRUMENT EMULATION",
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.model.PenType.values().forEach { pen ->
                            val isSel = settings.penType == pen
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) TungstenAmber.copy(alpha = 0.2f) else CarbonElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) HairlineAmber else HairlineBorder),
                                modifier = Modifier.clickable { onUpdateSettings(settings.copy(penType = pen)) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) TungstenAmber else TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pen.displayName.uppercase(),
                                        color = if (isSel) TungstenAmber else TextMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STROKE THICKNESS MODE & SLIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STROKE CALIBRATION",
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            com.example.model.ThicknessMode.values().forEach { mode ->
                                val isSel = settings.thicknessMode == mode
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSel) TungstenAmber.copy(alpha = 0.2f) else CarbonElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) HairlineAmber else HairlineBorder),
                                    modifier = Modifier.clickable { onUpdateSettings(settings.copy(thicknessMode = mode)) }
                                ) {
                                    Text(
                                        text = mode.displayName.uppercase(),
                                        color = if (isSel) TungstenAmber else TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (settings.thicknessMode == com.example.model.ThicknessMode.MANUAL) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = settings.manualStrokeWidth,
                            onValueChange = { onUpdateSettings(settings.copy(manualStrokeWidth = it)) },
                            valueRange = 1.0f..25.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = TungstenAmber,
                                activeTrackColor = TungstenAmber,
                                inactiveTrackColor = CarbonElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HairlineBorder))
                    Spacer(modifier = Modifier.height(14.dp))

                    // GHOST TRACING MODE (HOLOGRAPHIC AR GUIDE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ghost Tracing Mode",
                                    color = if (settings.ghostTracingMode) TungstenAmber else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TungstenAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineAmber)
                                ) {
                                    Text(
                                        text = "LIGHT TABLE",
                                        color = TungstenAmber,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Projects blueprint trajectories over canvas so you can trace manually with your stylus.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = settings.ghostTracingMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(ghostTracingMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = TungstenAmber
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HairlineBorder))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TungstenAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRAJECTORY SPEED",
                                color = TextWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${String.format("%.1fx", settings.speedMultiplier)}",
                            color = TungstenAmber,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Controls gesture dispatch speed in the target paint app.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Slider(
                        value = settings.speedMultiplier,
                        onValueChange = { onUpdateSettings(settings.copy(speedMultiplier = it)) },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = TungstenAmber,
                            activeTrackColor = TungstenAmber,
                            inactiveTrackColor = CarbonElevated
                        ),
                        modifier = Modifier.testTag("settings_speed_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto-minimize switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Auto-minimize during draw",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Hides floating chat window while drawing strokes.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = settings.autoMinimizeOnExecute,
                            onCheckedChange = { onUpdateSettings(settings.copy(autoMinimizeOnExecute = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = TungstenAmber
                            ),
                            modifier = Modifier.testTag("settings_auto_minimize_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HairlineBorder))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Edge Hugging toggle for draggable floating bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Floating Instrument Edge Hugging",
                                    color = if (settings.edgeHugging) TungstenAmber else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TungstenAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineAmber)
                                ) {
                                    Text(
                                        text = "SMART SNAP",
                                        color = TungstenAmber,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "When released, the floating orb smoothly snaps to the closest screen edge and stays docked.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.edgeHugging,
                            onCheckedChange = { onUpdateSettings(settings.copy(edgeHugging = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = TungstenAmber
                            ),
                            modifier = Modifier.testTag("settings_edge_hugging_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. AI PROMPT MODES & GAME DEV SETTINGS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .testTag("settings_prompt_modes_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TungstenAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEURAL PROMPT POLICIES",
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 1: Unrestricted Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Unrestricted Mode",
                                    color = if (settings.unrestrictedMode) TungstenAmber else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TungstenAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineAmber)
                                ) {
                                    Text(
                                        text = "GAME DEV / HORROR",
                                        color = TungstenAmber,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Optimized for developers creating 2D horror games, dark monsters, gore, and gritty game assets without aggressive prompt refusal.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.unrestrictedMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(unrestrictedMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = TungstenAmber
                            ),
                            modifier = Modifier.testTag("settings_unrestricted_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HairlineBorder))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle 2: Copyright Bypass & Cleaner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Copyright Bypass Normalizer",
                                    color = if (settings.copyrightBypassMode) CobaltBeam else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CobaltBeam.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineCobalt)
                                ) {
                                    Text(
                                        text = "PROMPT SANITIZER",
                                        color = CobaltBeam,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sanitizes prompts away from trademark keywords while directing the neural engine to synthesize matching aesthetics.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.copyrightBypassMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(copyrightBypassMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = CobaltBeam
                            ),
                            modifier = Modifier.testTag("settings_copyright_bypass_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. ABOUT & GUIDE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .testTag("settings_about_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TungstenAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ABOUT ARTHAX",
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ArtHax transforms text prompts into vector drawing trajectories and dispatches native touch strokes directly onto your canvas via Android Accessibility.\n\nEngineered for high compatibility with Ibis Paint X, Infinite Painter, Autodesk Sketchbook, and HiPaint.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("VERSION 1.1.0 (PUTER.JS)", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("NEO-PRECISION", color = TungstenAmber, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

