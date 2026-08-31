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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.PuterAuthViewModel

@Composable
fun PuterAuthSheet(
    viewModel: PuterAuthViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val isSdkReady by viewModel.isSdkReady.collectAsState()
    val lastLog by viewModel.lastLog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val providerFilter by viewModel.selectedProviderFilter.collectAsState()
    val filteredModels by viewModel.filteredModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, HairlineBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("puter_auth_sheet"),
        color = CarbonElevated,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // SHEET HANDLE & HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = (if (authState.isSignedIn) SignalEmerald else if (isSdkReady) TungstenAmber else CobaltBeam).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (authState.isSignedIn) SignalEmerald.copy(alpha = 0.4f) else HairlineAmber),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (authState.isSignedIn) SignalEmerald else if (isSdkReady) TungstenAmber else CobaltBeam,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NEURAL PROVIDERS & AUTH",
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (authState.isSignedIn) "Authenticated via Puter.js" else "Standard mode active",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_puter_auth_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sheet",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ACCOUNT AUTHENTICATION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (authState.isSignedIn) SignalEmerald.copy(alpha = 0.4f) else HairlineBorder,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MatteCarbon)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (authState.isSignedIn) SignalEmerald else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (authState.isSignedIn) "@${authState.username ?: "User"}" else "Standard / Guest",
                                color = TextWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = authState.statusMessage,
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (!authState.isSignedIn) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sign in to unlock live multi-model quotas and session persistence.",
                                color = TungstenAmber,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TungstenAmber,
                            strokeWidth = 2.dp
                        )
                    } else if (authState.isSignedIn) {
                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LaserCrimson),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LaserCrimson.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("puter_sign_out_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIGN OUT", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.signIn() },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TungstenAmber,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier.testTag("puter_sign_in_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = ObsidianBlack,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIGN IN", color = ObsidianBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notice: $authError",
                    color = LaserCrimson,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI MODELS SEARCH & PROVIDER FILTERS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = TungstenAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEURAL MODELS (${filteredModels.size})",
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Search models (Claude, GPT-4o, Gemini...)", color = TextMuted, fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MatteCarbon,
                    unfocusedContainerColor = MatteCarbon,
                    focusedIndicatorColor = TungstenAmber,
                    unfocusedIndicatorColor = HairlineBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("puter_model_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Provider Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    null to "ALL",
                    "Anthropic" to "ANTHROPIC",
                    "Google" to "GOOGLE",
                    "OpenAI" to "OPENAI",
                    "DeepSeek" to "DEEPSEEK",
                    "Puter.js" to "NATIVE"
                ).forEach { (prov, label) ->
                    val isSelected = providerFilter == prov
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) TungstenAmber.copy(alpha = 0.15f) else MatteCarbon,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) HairlineAmber else HairlineBorder),
                        modifier = Modifier
                            .clickable { viewModel.setProviderFilter(prov) }
                            .testTag("provider_filter_${label.lowercase()}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TungstenAmber else TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MODEL LISTING LAZY COLUMN
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("puter_models_list")
            ) {
                items(filteredModels, key = { it.id }) { model ->
                    val isSelected = model.id == selectedModel.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isSelected) HairlineAmber else HairlineBorder,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { viewModel.selectModel(model.id) }
                            .testTag("model_item_${model.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CarbonInteractive else MatteCarbon
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.name,
                                        color = TextWhite,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = TungstenAmber.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, HairlineAmber)
                                    ) {
                                        Text(
                                            text = model.badge.uppercase(),
                                            color = TungstenAmber,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${model.provider} • ${model.description}",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TungstenAmber,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = ObsidianBlack,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.selectModel(model.id) },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TungstenAmber),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("SELECT", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Log Status
            Text(
                text = "STATUS: $lastLog",
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    }
}

