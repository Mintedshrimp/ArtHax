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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ExecutionState
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow

/**
 * 56dp floating circular action button with animated pulsing neon ring.
 */
@Composable
fun FloatingButtonWidget(
    executionState: ExecutionState,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
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
        // Outer pulsing neon aura ring
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .border(2.dp, activeGlowColor.copy(alpha = pulseAlpha), CircleShape)
        )

        // Main 56dp floating bubble button
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = activeGlowColor)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CardBackground,
                            CyberBlack
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(activeGlowColor, NeonCyan)
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = activeGlowColor),
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
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = "ArtHax Floating Assistant",
                    tint = if (isExpanded) NeonPink else NeonCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
