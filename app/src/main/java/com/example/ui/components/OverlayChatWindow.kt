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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelOption
import com.example.model.ArtHaxInstructionSet
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.DrawingSettings
import com.example.model.ExecutionState
import com.example.model.PuterAuthState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Floating Overlay Chat Window.
 * Features:
 * - Top header with AI status, minimize/expand, and top-right **Canvas (Crop)** button
 * - Scrollable Chat area with prompt history and AI stroke synthesis responses
 * - Bottom Model Select dropdown showing available free models
 * - Prompt input text box with Send button at the right
 */
@Composable
fun OverlayChatWindow(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClose: () -> Unit,
    messages: List<ChatMessage>,
    currentPrompt: String,
    onPromptChange: (String) -> Unit,
    onSendPrompt: (String) -> Unit,
    availableModels: List<AiModelOption>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    instructionSet: ArtHaxInstructionSet?,
    executionState: ExecutionState,
    onExecuteDraw: () -> Unit,
    onAbortDraw: () -> Unit,
    onToggleCanvasCrop: () -> Unit,
    isCanvasCropActive: Boolean,
    isPuterSdkReady: Boolean = true,
    puterAuthState: PuterAuthState? = null,
    drawingSettings: DrawingSettings? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    var isModelDropdownOpen by remember { mutableStateOf(false) }

    val currentModel = availableModels.find { it.id == selectedModelId } ?: availableModels.firstOrNull()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .animateContentSize()
            .testTag("overlay_chat_window_surface"),
        color = CyberBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // ----------------------------------------------------
            // 1. HEADER ROW with Top-Right Canvas Crop Button
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = (if (executionState is ExecutionState.Drawing) NeonPink else NeonCyan).copy(alpha = 0.15f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (executionState is ExecutionState.Drawing) NeonPink else NeonCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ArtHax AI Overlay",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isPuterSdkReady) NeonGreen else NeonYellow)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPuterSdkReady) {
                                    if (puterAuthState?.isSignedIn == true) "@${puterAuthState.username} (Auth)" else "Puter AI Free"
                                } else "Connecting...",
                                color = if (isPuterSdkReady) NeonGreen else NeonYellow,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // TOP RIGHT ACTIONS: [Canvas Button] + [Minimize] + [Close]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // TOP RIGHT CANVAS BUTTON (Toggles free crop box on target app)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCanvasCropActive) NeonCyan.copy(alpha = 0.2f) else CardBackgroundElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCanvasCropActive) NeonCyan else BorderGlass
                        ),
                        modifier = Modifier
                            .clickable { onToggleCanvasCrop() }
                            .testTag("overlay_top_canvas_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = "Canvas Crop",
                                tint = if (isCanvasCropActive) NeonCyan else TextCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCanvasCropActive) "Done Crop" else "Canvas",
                                color = if (isCanvasCropActive) NeonCyan else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Minimize / Expand
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(28.dp).testTag("overlay_expand_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Minimize" else "Expand",
                            tint = TextMuted
                        )
                    }

                    // Close / Hide
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("overlay_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide Chat",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Status indicator when generating or drawing
            if (executionState is ExecutionState.Generating) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI Generating Vector Trajectories...", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(executionState.message, color = TextMuted, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { executionState.progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = NeonCyan,
                        trackColor = CardBackground
                    )
                }
            } else if (executionState is ExecutionState.Drawing) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Drawing in Paint App: Stroke ${executionState.currentStrokeIndex} of ${executionState.totalStrokes}", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${(executionState.progress * 100).toInt()}%", color = NeonGreen, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { executionState.progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = NeonGreen,
                        trackColor = CardBackground
                    )
                }
            }

            // ----------------------------------------------------
            // 2. CHAT AREA & MESSAGES (Collapsible)
            // ----------------------------------------------------
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        if (messages.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ask AI to draw anything (e.g. 'Chibi Cat', 'Cyber Katana'). Configure Canvas crop top-right to match your canvas size.",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.testTag("overlay_chat_message_list")
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    OverlayMessageBubble(
                                        message = msg,
                                        onExecuteDrawing = onExecuteDraw
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Draw / Stop Action bar if strokes are ready
                    if (executionState is ExecutionState.Drawing) {
                        Button(
                            onClick = onAbortDraw,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("overlay_abort_drawing_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop Drawing", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (instructionSet != null && instructionSet.strokes.isNotEmpty()) {
                        Button(
                            onClick = onExecuteDraw,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("overlay_draw_strokes_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Draw ${instructionSet.strokes.size} Strokes in Paint App",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ----------------------------------------------------
            // 3. MODEL SELECT DROPDOWN + PROMPT INPUT + SEND BUTTON
            // ----------------------------------------------------
            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                // MODEL SELECT DROPDOWN ROW (Shows free models)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                            modifier = Modifier
                                .clickable { isModelDropdownOpen = true }
                                .testTag("model_select_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Model: ${currentModel?.name ?: selectedModelId}",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                if (currentModel?.isFree == true) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NeonGreen.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = "FREE",
                                            color = NeonGreen,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Model",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // DROPDOWN MENU OF AVAILABLE FREE MODELS
                        DropdownMenu(
                            expanded = isModelDropdownOpen,
                            onDismissRequest = { isModelDropdownOpen = false },
                            modifier = Modifier
                                .background(CardBackgroundElevated)
                                .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                        ) {
                            availableModels.forEach { model ->
                                val isSelected = model.id == selectedModelId
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text(
                                                    text = model.name,
                                                    color = if (isSelected) NeonCyan else TextWhite,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = model.description,
                                                    color = TextMuted,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (model.isFree) NeonGreen.copy(alpha = 0.15f) else NeonPink.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (model.isFree) "FREE" else model.badge,
                                                    color = if (model.isFree) NeonGreen else NeonPink,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSelectModel(model.id)
                                        isModelDropdownOpen = false
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }

                    // Active Filter Badges & Quick Suggestion Chips (horizontal scroll)
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (drawingSettings?.unrestrictedMode == true) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonPink.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink)
                            ) {
                                Text(
                                    text = "UNRESTRICTED",
                                    color = NeonPink,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (drawingSettings?.copyrightBypassMode == true) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonYellow.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow)
                            ) {
                                Text(
                                    text = "COPYRIGHT BYPASS",
                                    color = NeonYellow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val suggestions = if (drawingSettings?.unrestrictedMode == true) {
                            listOf("Horror Eye", "Zombie Hand", "Spooky Ghost", "Cyber Katana")
                        } else {
                            listOf("Chibi Miku", "Cyber Katana", "Cute Kitten", "Dragon")
                        }

                        suggestions.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                                modifier = Modifier
                                    .clickable {
                                        onPromptChange(prompt)
                                        onSendPrompt(prompt)
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    color = TextWhite,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // CHAT PROMPT INPUT BOX + SEND BUTTON AT RIGHT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = currentPrompt,
                        onValueChange = onPromptChange,
                        placeholder = {
                            Text(
                                text = "Ask AI to draw (e.g. 'Chibi Anime Boy')...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (currentPrompt.isNotBlank()) {
                                onSendPrompt(currentPrompt)
                                focusManager.clearFocus()
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedIndicatorColor = NeonCyan,
                            unfocusedIndicatorColor = BorderGlass,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("overlay_chat_prompt_input")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // SEND BUTTON AT RIGHT OF CHAT BOX
                    Button(
                        onClick = {
                            if (currentPrompt.isNotBlank()) {
                                onSendPrompt(currentPrompt)
                                focusManager.clearFocus()
                            }
                        },
                        enabled = currentPrompt.isNotBlank() && executionState !is ExecutionState.Generating,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            disabledContainerColor = CardBackground
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("overlay_send_prompt_btn")
                    ) {
                        if (executionState is ExecutionState.Generating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Prompt",
                                tint = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayMessageBubble(
    message: ChatMessage,
    onExecuteDrawing: () -> Unit
) {
    val isUser = message.sender == ChatSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) NeonCyan.copy(alpha = 0.15f) else CardBackgroundElevated
    val borderColor = if (isUser) NeonCyan.copy(alpha = 0.4f) else BorderGlass

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = if (isUser) 10.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 10.dp
            ),
            color = bgColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                if (message.modelName != null) {
                    Text(
                        text = message.modelName,
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = message.text,
                    color = TextWhite,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                if (message.isInstructionGenerated && message.instructionSet != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonCyan.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onExecuteDrawing() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Draw Strokes", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
