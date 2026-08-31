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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelOption
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.ExecutionState
import com.example.ui.theme.CarbonElevated
import com.example.ui.theme.CarbonInteractive
import com.example.ui.theme.CobaltBeam
import com.example.ui.theme.HairlineAmber
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HairlineCobalt
import com.example.ui.theme.MatteCarbon
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TungstenAmber

/**
 * Compact Neo-Precision floating chat window component for interacting with the AI assistant.
 * Offers floating overlay capabilities, AI prompt input, model quick-select, and direct stroke triggering.
 */
@Composable
fun CompactFloatingChatWindow(
    messages: List<ChatMessage>,
    currentPrompt: String,
    onPromptChange: (String) -> Unit,
    onSendPrompt: (String) -> Unit,
    availableModels: List<AiModelOption>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    executionState: ExecutionState = ExecutionState.Idle,
    onExecuteDrawing: () -> Unit = {},
    onClearChat: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .width(360.dp)
            .shadow(16.dp, RoundedCornerShape(12.dp))
            .border(1.dp, HairlineBorder, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .testTag("compact_floating_chat_window"),
        color = CarbonElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // ----------------------------------------------------
            // 1. WINDOW HEADER
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MatteCarbon)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TungstenAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineAmber),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Assistant",
                                tint = TungstenAmber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYNTHESIS CONSOLE",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Clear messages
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.size(28.dp).testTag("chat_clear_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear Chat",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Minimize/Expand Toggle
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp).testTag("chat_toggle_expand_btn")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Minimize Window" else "Expand Window",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close Window
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("chat_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Floating Chat",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isExpanded) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // ----------------------------------------------------
                    // 2. MODEL SELECTOR CHIPS
                    // ----------------------------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableModels.forEach { model ->
                            val isSelected = model.id == selectedModelId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) TungstenAmber.copy(alpha = 0.15f) else MatteCarbon)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) HairlineAmber else HairlineBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSelectModel(model.id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("chat_model_chip_${model.id}")
                            ) {
                                Text(
                                    text = model.name.uppercase(),
                                    color = if (isSelected) TungstenAmber else TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // 3. MESSAGES HISTORY STREAM
                    // ----------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MatteCarbon)
                            .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        if (messages.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TungstenAmber.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Synthesize vector strokes with neural models. Prompt any scene, diagram, or blueprint.",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("chat_message_list")
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    ChatMessageBubble(
                                        message = msg,
                                        onExecuteDrawing = onExecuteDrawing
                                    )
                                }
                            }
                        }
                    }

                    // Execution Progress indicator if active
                    if (executionState is ExecutionState.Generating) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = TungstenAmber,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = executionState.message,
                                color = TungstenAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ----------------------------------------------------
                    // 4. QUICK SUGGESTION CHIPS
                    // ----------------------------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Minimal Cat",
                            "Mountain Skyline",
                            "Coffee Chemex",
                            "Origami Bird",
                            "Architectural Cube"
                        ).forEach { quickPrompt ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MatteCarbon,
                                border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
                                modifier = Modifier
                                    .clickable {
                                        onPromptChange(quickPrompt)
                                        onSendPrompt(quickPrompt)
                                    }
                                    .testTag("chat_quick_preset_${quickPrompt.take(5)}")
                            ) {
                                Text(
                                    text = quickPrompt,
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ----------------------------------------------------
                    // 5. INPUT TEXTFIELD & SEND BUTTON
                    // ----------------------------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = currentPrompt,
                            onValueChange = onPromptChange,
                            placeholder = {
                                Text(
                                    text = "Enter synthesis prompt...",
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
                                focusedContainerColor = MatteCarbon,
                                unfocusedContainerColor = MatteCarbon,
                                focusedIndicatorColor = TungstenAmber,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                                .testTag("chat_prompt_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (currentPrompt.isNotBlank()) {
                                    onSendPrompt(currentPrompt)
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = currentPrompt.isNotBlank() && executionState !is ExecutionState.Generating,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TungstenAmber,
                                contentColor = ObsidianBlack,
                                disabledContainerColor = MatteCarbon
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("send_prompt_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Prompt",
                                tint = if (currentPrompt.isNotBlank() && executionState !is ExecutionState.Generating) ObsidianBlack else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onExecuteDrawing: () -> Unit
) {
    val isUser = message.sender == ChatSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) TungstenAmber.copy(alpha = 0.12f) else CarbonInteractive
    val borderColor = if (isUser) HairlineAmber else HairlineBorder
    val textColor = TextWhite

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (isUser) 8.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 8.dp
            ),
            color = bgColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.modelName != null) {
                    Text(
                        text = message.modelName.uppercase(),
                        color = TungstenAmber,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // If instruction set was generated, show instant draw button
                if (message.isInstructionGenerated && message.instructionSet != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onExecuteDrawing,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TungstenAmber,
                            contentColor = ObsidianBlack
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("chat_msg_draw_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ObsidianBlack,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EXECUTE ${message.instructionSet.strokes.size} STROKES",
                            color = ObsidianBlack,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


