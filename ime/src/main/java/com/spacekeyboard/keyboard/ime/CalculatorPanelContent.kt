package com.spacekeyboard.keyboard.ime

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.calc.CalcFormat
import com.spacekeyboard.keyboard.core.calc.CalcKey
import com.spacekeyboard.keyboard.core.calc.CalcView
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceFontFamily
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

private class PadSpec(val key: CalcKey?, val kind: CalcKind)

private class CalcPad(
    val look: CalcLook,
    val native: Boolean,
    val grouping: Boolean,
    val canInsert: Boolean,
    val onKey: (CalcKey) -> Unit,
    val onInsert: () -> Unit,
)

private fun digits(vararg keys: CalcKey): List<PadSpec> = keys.map { PadSpec(it, CalcKind.DIGIT) }

private val padRows = listOf(
    listOf(PadSpec(CalcKey.CLEAR, CalcKind.FUNCTION), PadSpec(null, CalcKind.OPERATOR), PadSpec(CalcKey.PERCENT, CalcKind.FUNCTION)),
    digits(CalcKey.D7, CalcKey.D8, CalcKey.D9),
    digits(CalcKey.D4, CalcKey.D5, CalcKey.D6),
    digits(CalcKey.D1, CalcKey.D2, CalcKey.D3),
    digits(CalcKey.D0, CalcKey.DOT) + PadSpec(CalcKey.GROUP, CalcKind.FUNCTION),
)

private val sideKeys = listOf(CalcKey.DIV, CalcKey.MUL, CalcKey.SUB, CalcKey.ADD).map { PadSpec(it, CalcKind.OPERATOR) } +
    PadSpec(CalcKey.EQUALS, CalcKind.EQUALS)

private val DISPLAY_HEIGHT = 66.dp
private val DELETE_SIZE = 46.dp
private val GAP = 6.dp
private const val EXPRESSION_MAX = 30
private const val BACKDROP_ALPHA = 0.28f
private const val SMALL_ALPHA = 0.62f

internal fun calculatorHost(
    context: Context,
    theme: ThemeController,
    layouts: LayoutController,
    calculator: CalculatorController,
    onInsert: (String) -> Unit,
): ComposeView = ComposeView(context).apply {
    setContent { CalculatorPanelContent(theme, layouts, calculator, onInsert) }
}

@Composable
fun CalculatorPanelContent(
    theme: ThemeController,
    layouts: LayoutController,
    calculator: CalculatorController,
    onInsert: (String) -> Unit,
) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val themeSnapshot = rememberThemeSnapshot(theme)
        val layoutSnapshot = rememberLayoutSnapshot(layouts)
        val look = CalcLook(themeSnapshot.palette, KeyStyle.of(themeSnapshot.choice.theme), layoutSnapshot.prefs.feedback.vibrate)
        val native = layoutSnapshot.prefs.typing.nativeDigits
        val view = CalcFormat.view(calculator.state, native)
        val pad = CalcPad(look, native, calculator.state.grouping, view.insertText != null, calculator::press) {
            view.insertText?.let { text ->
                onInsert(text)
                calculator.close()
            }
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier.fillMaxSize().calcBackdrop(look).padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(GAP),
            ) {
                CalcDisplay(view, pad)
                CalcKeys(pad, Modifier.weight(1f))
            }
        }
    }
}

private fun Modifier.calcBackdrop(look: CalcLook): Modifier {
    if (look.style != KeyStyle.GALAXY) return this
    val glow = Color(ColorMath.withAlpha(look.palette.glow, BACKDROP_ALPHA))
    return drawBehind {
        val center = Offset(size.width * 0.2f, size.height * 0.1f)
        drawRect(Brush.radialGradient(listOf(glow, Color.Transparent), center = center, radius = size.width * 0.75f))
    }
}

private fun resultSize(length: Int): Int = when {
    length <= 10 -> 40
    length <= 14 -> 32
    length <= 18 -> 26
    else -> 21
}

private fun tail(text: String): String =
    if (text.length <= EXPRESSION_MAX) text else "…" + text.takeLast(EXPRESSION_MAX - 1)

@Composable
private fun CalcDisplay(view: CalcView, pad: CalcPad) {
    Row(
        modifier = Modifier.fillMaxWidth().height(DISPLAY_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CalcReadout(view, pad.look, Modifier.weight(1f))
        CalcKeyView(
            CalcKind.DELETE, pad.look, Modifier.size(DELETE_SIZE), stringResource(R.string.calc_delete),
            shape = CircleShape, repeat = true, onClick = { pad.onKey(CalcKey.DELETE) },
        ) { ink -> SpaceIconView(SpaceIcon.DELETE, ink, Modifier, 24.dp) }
    }
}

@Composable
private fun CalcReadout(view: CalcView, look: CalcLook, modifier: Modifier) {
    val p = look.palette
    val result = if (view.error) stringResource(R.string.calc_error) else view.result
    val spoken = stringResource(R.string.calc_result_value, result)
    val size = resultSize(result.length)
    val glow = if (look.style == KeyStyle.GALAXY) Shadow(Color(ColorMath.withAlpha(p.glow, 0.6f)), Offset.Zero, 24f) else null
    Column(
        modifier = modifier.padding(start = 6.dp).semantics {
            contentDescription = spoken
            liveRegion = LiveRegionMode.Polite
        },
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(
            text = tail(view.expression),
            style = TextStyle(color = Color(ColorMath.withAlpha(p.label, SMALL_ALPHA)), fontSize = 13.sp, fontFamily = SpaceFontFamily),
            maxLines = 1,
        )
        BasicText(
            text = result,
            style = TextStyle(
                color = if (view.error) roseInk(look) else Color(p.label),
                fontSize = size.sp,
                lineHeight = (size * 1.1f).sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SpaceFontFamily,
                shadow = glow,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun CalcKeys(pad: CalcPad, modifier: Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.weight(3f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(GAP)) {
            padRows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(GAP)) {
                    row.forEach { spec -> PadKey(spec, pad, Modifier.weight(1f).fillMaxHeight()) }
                }
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(GAP)) {
            sideKeys.forEach { spec -> PadKey(spec, pad, Modifier.fillMaxWidth().weight(1f)) }
        }
    }
}

@Composable
private fun PadKey(spec: PadSpec, pad: CalcPad, modifier: Modifier) {
    val key = spec.key
    if (key == null) {
        CalcKeyView(
            spec.kind, pad.look, modifier, stringResource(R.string.calc_insert),
            enabled = pad.canInsert, onClick = pad.onInsert,
        ) { ink -> SpaceIconView(SpaceIcon.ENTER, ink, Modifier, 24.dp) }
        return
    }
    val label = CalcFormat.label(key, pad.native)
    val selected = key == CalcKey.GROUP && pad.grouping
    CalcKeyView(spec.kind, pad.look, modifier, keyDescription(key), selected = selected, onClick = { pad.onKey(key) }) { ink ->
        CalcKeyText(label, spec.kind, ink)
    }
}

@Composable
private fun keyDescription(key: CalcKey): String? = when (key) {
    CalcKey.CLEAR -> stringResource(R.string.calc_clear)
    CalcKey.PERCENT -> stringResource(R.string.calc_percent)
    CalcKey.GROUP -> stringResource(R.string.calc_group)
    CalcKey.DOT -> stringResource(R.string.calc_decimal)
    CalcKey.ADD -> stringResource(R.string.calc_add)
    CalcKey.SUB -> stringResource(R.string.calc_subtract)
    CalcKey.MUL -> stringResource(R.string.calc_multiply)
    CalcKey.DIV -> stringResource(R.string.calc_divide)
    CalcKey.EQUALS -> stringResource(R.string.calc_equals)
    CalcKey.DELETE -> stringResource(R.string.calc_delete)
    else -> null
}
