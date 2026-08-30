package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ExecutionState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CardBackgroundElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextWhite

/**
 * 56dp floating action button with formal subtle status ring indicator.
 */
@Composable
fun FloatingButtonWidget(
    executionState: ExecutionState,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDrawing = executionState is ExecutionState.Drawing
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isDrawing) 1.25f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val activeGlowColor = when (executionState) {
        is ExecutionState.Drawing -> NeonPink
        is ExecutionState.Generating -> NeonYellow
        is ExecutionState.Completed -> NeonGreen
        is ExecutionState.Error -> NeonPink
        else -> NeonCyan
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .testTag("floating_bubble_container"),
        contentAlignment = Alignment.Center
    ) {
        // Status pulse indicator ring
        if (isDrawing || executionState is ExecutionState.Generating) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .border(2.dp, activeGlowColor.copy(alpha = pulseAlpha), CircleShape)
            )
        }

        // Main 56dp floating bubble button
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(if (isExpanded) NeonCyan else CardBackgroundElevated)
                .border(
                    width = 1.5.dp,
                    color = if (isExpanded) NeonCyan else BorderGlass,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = NeonCyan),
                    onClick = onClick
                )
                .testTag("floating_bubble_btn"),
            contentAlignment = Alignment.Center
        ) {
            if (executionState is ExecutionState.Drawing) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Drawing",
                    tint = NeonPink,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = "Art Assistant Floating Action",
                    tint = if (isExpanded) TextWhite else NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
