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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.AiModelOption
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
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
            .shadow(24.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), spotColor = NeonCyan)
            .border(1.5.dp, BorderGlass, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("puter_auth_sheet"),
        color = CyberBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (authState.isSignedIn) NeonGreen else if (isSdkReady) NeonCyan else NeonYellow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PUTER.JS AUTH & AI HUB",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
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

            Spacer(modifier = Modifier.height(12.dp))

            // ACCOUNT AUTHENTICATION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (authState.isSignedIn) NeonGreen.copy(alpha = 0.6f) else NeonPink.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    ),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (authState.isSignedIn) NeonGreen else NeonPink,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (authState.isSignedIn) "@${authState.username ?: "User"}" else "Guest Mode",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = authState.statusMessage,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        if (!authState.isSignedIn) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sign in to Puter.js to unlock unlimited cloud LLM tokens & GPT-4o.",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    } else if (authState.isSignedIn) {
                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
                            modifier = Modifier.testTag("puter_sign_out_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIGN OUT", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.signIn() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.testTag("puter_sign_in_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = CyberBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIGN IN", color = CyberBlack, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ $authError",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
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
                    tint = NeonYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AVAILABLE AI MODELS (${filteredModels.size})",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Search models (Claude, GPT-4o, Gemini...)", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedIndicatorColor = NeonCyan,
                    unfocusedIndicatorColor = BorderGlass,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
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
                    "Puter.js" to "LOCAL/NATIVE"
                ).forEach { (prov, label) ->
                    val isSelected = providerFilter == prov
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else CardBackground)
                            .border(1.dp, if (isSelected) NeonCyan else BorderGlass, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setProviderFilter(prov) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("provider_filter_${label.lowercase()}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) NeonCyan else TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                    .height(280.dp)
                    .testTag("puter_models_list")
            ) {
                items(filteredModels, key = { it.id }) { model ->
                    val isSelected = model.id == selectedModel.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonCyan else BorderGlass,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.selectModel(model.id) }
                            .testTag("model_item_${model.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CardBackgroundElevated else CardBackground
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
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (model.badge) {
                                                    "RECOMMENDED" -> NeonCyan.copy(alpha = 0.2f)
                                                    "FASTEST" -> NeonGreen.copy(alpha = 0.2f)
                                                    "AUTH UNLOCKED" -> NeonPink.copy(alpha = 0.2f)
                                                    else -> NeonPurple.copy(alpha = 0.2f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = model.badge,
                                            color = when (model.badge) {
                                                "RECOMMENDED" -> NeonCyan
                                                "FASTEST" -> NeonGreen
                                                "AUTH UNLOCKED" -> NeonPink
                                                else -> NeonPurple
                                            },
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Provider: ${model.provider} • ${model.description}",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = CyberBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.selectModel(model.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass)
                                ) {
                                    Text("SELECT", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Log Status
            Text(
                text = "LOG: $lastLog",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
