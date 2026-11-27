package com.spacekeyboard.keyboard.designsystem.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.designsystem.a11y.LocalSystemA11y

object SpaceMotion {
    val springSoft = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val springSnap = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val tweenFast = tween<Float>(durationMillis = 180)
    val tweenMedium = tween<Float>(durationMillis = 280)
}

@Composable
fun <T> spaceSpring(): FiniteAnimationSpec<T> =
    if (LocalSystemA11y.current.reduceMotion) snap() else spring()

@Composable
fun spaceEnter(): EnterTransition =
    if (LocalSystemA11y.current.reduceMotion) EnterTransition.None else fadeIn() + expandIn()

@Composable
fun spaceExit(): ExitTransition =
    if (LocalSystemA11y.current.reduceMotion) ExitTransition.None else fadeOut() + shrinkOut()

@Composable
fun spaceFadeIn(): EnterTransition =
    if (LocalSystemA11y.current.reduceMotion) EnterTransition.None else fadeIn()

@Composable
fun spaceFadeOut(): ExitTransition =
    if (LocalSystemA11y.current.reduceMotion) ExitTransition.None else fadeOut()

@Composable
fun spaceBarEnter(): EnterTransition =
    if (LocalSystemA11y.current.reduceMotion) EnterTransition.None else fadeIn() + expandVertically()

@Composable
fun spaceBarExit(): ExitTransition =
    if (LocalSystemA11y.current.reduceMotion) ExitTransition.None else fadeOut() + shrinkVertically()
