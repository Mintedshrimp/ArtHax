package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawingSettings
import com.example.model.PuterAuthState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

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
        containerColor = CyberBackground,
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
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settings & Account",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure Puter.js AI & drawing preferences",
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
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("settings_puter_auth_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                                tint = if (puterAuthState.isSignedIn) NeonGreen else NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Puter.js AI Account",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = (if (puterAuthState.isSignedIn) NeonGreen else NeonYellow).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (puterAuthState.isSignedIn) "LOGGED IN" else "ANONYMOUS / FREE",
                                color = if (puterAuthState.isSignedIn) NeonGreen else NeonYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (puterAuthState.isSignedIn) {
                            "Signed in as @${puterAuthState.username ?: "User"}. Full model tier access unlocked."
                        } else {
                            "Puter.js free tier is active. Log in to your Puter account for higher rate limits and saved session histories."
                        },
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (puterAuthState.isSignedIn) {
                        OutlinedButton(
                            onClick = onLogoutClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CardBackgroundElevated,
                                contentColor = NeonPink
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("settings_logout_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Out of Puter.js", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onLoginClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.fillMaxWidth().testTag("settings_login_btn")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log In with Puter.js", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DRAWING SPEED & CALIBRATION PREFERENCES
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("settings_drawing_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Drawing Speed",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "${String.format("%.1fx", settings.speedMultiplier)}",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
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
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CardBackgroundElevated
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
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("settings_auto_minimize_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderGlass))
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
                                    text = "Floating Bubble Edge Hugging",
                                    color = if (settings.edgeHugging) NeonCyan else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NeonCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Smart Snap",
                                        color = NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "When released, the floating bubble smoothly snaps to the closest screen edge and stays there until moved.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.edgeHugging,
                            onCheckedChange = { onUpdateSettings(settings.copy(edgeHugging = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
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
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("settings_prompt_modes_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Prompt Modes & Filters",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
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
                                    color = if (settings.unrestrictedMode) NeonPink else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NeonPink.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "2D Horror / Devs",
                                        color = NeonPink,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Prompts won't be blocked. Designed for developers creating 2D horror games, dark monsters, gore, or gritty game assets without censorship.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.unrestrictedMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(unrestrictedMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonPink,
                                checkedTrackColor = NeonPink.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("settings_unrestricted_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderGlass))
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
                                    text = "Copyright Bypass",
                                    color = if (settings.copyrightBypassMode) NeonYellow else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NeonYellow.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Prompt Cleaner",
                                        color = NeonYellow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Bypasses copyright restrictions by veering prompt off to not contain copyright words, while AI cleans prompt to make similar artwork without copyright wording.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Switch(
                            checked = settings.copyrightBypassMode,
                            onCheckedChange = { onUpdateSettings(settings.copy(copyrightBypassMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonYellow,
                                checkedTrackColor = NeonYellow.copy(alpha = 0.3f)
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
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("settings_about_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About ArtHax AI",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ArtHax transforms text prompts into vector drawing trajectories and dispatches native touch strokes directly onto your screen via Android Accessibility.\n\nWorks across all painting tools including Ibis Paint X, Infinite Painter, Autodesk Sketchbook, and Notes.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version 1.2.0 (Puter.js Engine)", color = TextMuted, fontSize = 11.sp)
                        Text("Cyber Theme", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
