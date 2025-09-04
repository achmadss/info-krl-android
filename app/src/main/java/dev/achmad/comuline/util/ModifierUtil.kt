package dev.achmad.comuline.util

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.shimmerEffect(
    shimmerSize: Dp = 152.dp,
    shimmerColor: Color = Color.White.copy(alpha = 0.25f),
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(
            durationMillis = 1000,
            delayMillis = 500,
            easing = EaseInOutQuad,
        ),
    ),
) = this.composed {
    val density = LocalDensity.current
    val shimmerSizePx = with(density) { shimmerSize.toPx() }
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "ShimmerProgress",
    )

    Modifier.drawWithContent {
        drawContent()
        val adjustedWidth = size.width + shimmerSizePx * 2
        val x = adjustedWidth * progress - shimmerSizePx
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor,
                    Color.Transparent,
                ),
                startX = x,
                endX = x + shimmerSizePx,
            ),
            topLeft = Offset(x = x, y = 0f),
            size = Size(
                width = shimmerSizePx,
                height = size.height,
            ),
        )
    }
}

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(0.78f)

/**
 * For TextField, the provided [action] will be invoked when
 * physical enter key is pressed.
 *
 * Naturally, the TextField should be set to single line only.
 */
fun Modifier.runOnEnterKeyPressed(action: () -> Unit): Modifier = this.onPreviewKeyEvent {
    when (it.key) {
        Key.Enter, Key.NumPadEnter -> {
            // Physical keyboards generate two event types:
            // - KeyDown when the key is pressed
            // - KeyUp when the key is released
            if (it.type == KeyEventType.KeyDown) {
                action()
                true
            } else {
                false
            }
        }

        else -> false
    }
}

/**
 * For TextField on AppBar, this modifier will request focus
 * to the element the first time it's composed.
 */
@Composable
fun Modifier.showSoftKeyboard(show: Boolean): Modifier {
    if (!show) return this
    val focusRequester = remember { FocusRequester() }
    var openKeyboard by rememberSaveable { mutableStateOf(show) }
    LaunchedEffect(focusRequester) {
        if (openKeyboard) {
            focusRequester.requestFocus()
            openKeyboard = false
        }
    }
    return this.focusRequester(focusRequester)
}

/**
 * For TextField, this modifier will clear focus when soft
 * keyboard is hidden.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Modifier.clearFocusOnSoftKeyboardHide(
    onFocusCleared: (() -> Unit)? = null,
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    var keyboardShowedSinceFocused by remember { mutableStateOf(false) }
    if (isFocused) {
        val imeVisible = WindowInsets.isImeVisible
        val focusManager = LocalFocusManager.current
        LaunchedEffect(imeVisible) {
            if (imeVisible) {
                keyboardShowedSinceFocused = true
            } else if (keyboardShowedSinceFocused) {
                focusManager.clearFocus()
                onFocusCleared?.invoke()
            }
        }
    }

    return this.onFocusChanged {
        if (isFocused != it.isFocused) {
            if (isFocused) {
                keyboardShowedSinceFocused = false
            }
            isFocused = it.isFocused
        }
    }
}

fun Modifier.onClickInput(
    pass: PointerEventPass = PointerEventPass.Initial,
    ripple: Boolean = true,
    onDown: () -> Unit = {},
    onUp: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val rippleIndication = if (ripple) ripple() else null

    this
        .indication(interactionSource, rippleIndication)
        .pointerInput(pass) {
            awaitEachGesture {
                val down = awaitFirstDown(pass = pass)
                val press = PressInteraction.Press(down.position)
                if (ripple) {
                    interactionSource.tryEmit(press) // Start ripple
                }
                down.consume()
                onDown()

                val up = waitForUpOrCancellation(pass)
                if (up != null) {
                    if (ripple) {
                        interactionSource.tryEmit(PressInteraction.Release(press)) // End ripple
                    }
                    onUp()
                } else {
                    if (ripple) {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                    }
                }
            }
        }
}