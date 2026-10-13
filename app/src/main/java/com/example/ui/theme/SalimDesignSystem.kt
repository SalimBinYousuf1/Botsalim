package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// One single corner radius — 22dp — used identically everywhere across Salim
val SalimCornerRadius: Dp = 22.dp
val SalimShape = RoundedCornerShape(SalimCornerRadius)

/**
 * Visual background gradient:
 * Light mode: sage-olive green at top easing into pale warm cream at bottom.
 * Dark mode: deep charcoal to warm near-black.
 */
@Composable
fun SalimGradientBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(DarkCharcoalTop, DarkCharcoalMid, WarmNearBlackBottom)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(SageOliveTop, SageOliveMid, PaleWarmCreamBottom)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradientBrush),
        content = content
    )
}

/**
 * Modifier for flat subtle darken & slight scale-down on contact with ZERO platform ripple or flash.
 */
fun Modifier.salimPressScale(
    enabled: Boolean = true,
    scaleDownFactor: Float = 0.97f,
    onClick: () -> Unit
): Modifier = this.pointerInput(enabled) {
    if (!enabled) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val change = waitForUpOrCancellation()
        if (change != null && !change.isConsumed) {
            onClick()
        }
    }
}

/**
 * Fully opaque card surface: zero border, zero shadow, identical 22dp radius.
 */
@Composable
fun SalimCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val surfaceColor = backgroundColor ?: if (isDark) CardSurfaceDark else CardSurfaceLight

    var baseModifier = modifier
        .scale(scale)
        .clip(SalimShape)
        .background(surfaceColor)

    if (onClick != null) {
        baseModifier = baseModifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val up = waitForUpOrCancellation()
                isPressed = false
                if (up != null) {
                    onClick()
                }
            }
        }
    }

    Box(
        modifier = baseModifier
    ) {
        content()
    }
}

/**
 * Strict Tier-1 Button: Solid near-black fully-rounded pill with white text, minimum 48dp height.
 */
@Composable
fun SalimPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "primaryBtnScale"
    )

    val bgColor = if (isDark) PrimaryButtonBgDark else PrimaryButtonBgLight
    val textColor = if (isDark) PrimaryButtonTextDark else PrimaryButtonTextLight
    val activeBg = if (isPressed) bgColor.copy(alpha = 0.88f) else bgColor

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .clip(SalimShape)
            .background(if (enabled) activeBg else activeBg.copy(alpha = 0.4f))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        onClick()
                    }
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) {}
        ) {
            if (leadingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides textColor) {
                    leadingIcon()
                }
                Box(modifier = Modifier.padding(end = 8.dp))
            }
            Text(
                text = text,
                style = SalimTextStyles.Secondary,
                color = textColor
            )
        }
    }
}

/**
 * Strict Tier-2 Button: White pill with thin light-gray border and black text, min 48dp height, 22dp radius.
 */
@Composable
fun SalimSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "secondaryBtnScale"
    )

    val bgColor = if (isDark) SecondaryButtonBgDark else SecondaryButtonBgLight
    val textColor = if (isDark) SecondaryButtonTextDark else SecondaryButtonTextLight
    val borderColor = if (isDark) SecondaryButtonBorderDark else SecondaryButtonBorderLight
    val activeBg = if (isPressed) bgColor.copy(alpha = 0.88f) else bgColor

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .clip(SalimShape)
            .border(width = 1.dp, color = borderColor, shape = SalimShape)
            .background(if (enabled) activeBg else activeBg.copy(alpha = 0.4f))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        onClick()
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) {}
        ) {
            if (leadingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides textColor) {
                    leadingIcon()
                }
                Box(modifier = Modifier.padding(end = 8.dp))
            }
            Text(
                text = text,
                style = SalimTextStyles.Secondary,
                color = textColor
            )
        }
    }
}
