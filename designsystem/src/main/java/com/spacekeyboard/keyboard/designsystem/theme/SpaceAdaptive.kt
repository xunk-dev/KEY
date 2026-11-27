package com.spacekeyboard.keyboard.designsystem.theme

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

val SpaceRailCompactWidth: Dp = 72.dp
val SpaceRailExpandedWidth: Dp = 220.dp
val SpaceContentMaxWidth: Dp = 840.dp

private val RAIL_GUTTER = 24.dp
private val REFERENCE_WIDTH = 400.dp
private const val MAX_COLUMNS = 16

enum class SpaceRail { None, Compact, Expanded }

@Immutable
class SpaceAdaptive(val width: Dp, val height: Dp, val isTv: Boolean) {
    val size: SpaceWindowSize get() = spaceWindowSize(width)

    val landscape: Boolean get() = width > height

    val sideEditor: Boolean get() = landscape && (height < 500.dp || width >= 840.dp)

    val rail: SpaceRail
        get() = when (size) {
            SpaceWindowSize.Compact -> SpaceRail.None
            SpaceWindowSize.Medium -> SpaceRail.Compact
            SpaceWindowSize.Expanded -> SpaceRail.Expanded
        }

    val railInset: Dp
        get() = when (rail) {
            SpaceRail.None -> 0.dp
            SpaceRail.Compact -> SpaceRailCompactWidth + RAIL_GUTTER
            SpaceRail.Expanded -> SpaceRailExpandedWidth + RAIL_GUTTER
        }

    val usableWidth: Dp get() = (width - railInset).coerceAtLeast(0.dp)

    fun columns(base: Int): Int {
        val factor = usableWidth.value / REFERENCE_WIDTH.value
        return (base * factor).roundToInt().coerceIn(base, MAX_COLUMNS.coerceAtLeast(base))
    }

    fun listColumns(): Int = when {
        usableWidth >= 1100.dp -> 3
        usableWidth >= 700.dp -> 2
        else -> 1
    }
}

@Composable
fun rememberSpaceAdaptive(): SpaceAdaptive {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val tv = remember(context) {
        (context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager)?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
    return remember(configuration.screenWidthDp, configuration.screenHeightDp, tv) {
        SpaceAdaptive(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp, tv)
    }
}

@Composable
fun Modifier.spaceRailPadding(): Modifier = padding(end = rememberSpaceAdaptive().railInset)

fun Modifier.spaceMaxWidth(max: Dp = SpaceContentMaxWidth): Modifier =
    wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = max).fillMaxWidth()
