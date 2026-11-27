package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.macro.Macro
import com.spacekeyboard.keyboard.core.macro.MacroRepeat
import com.spacekeyboard.keyboard.core.macro.MacroSpeed
import com.spacekeyboard.keyboard.core.macro.MacroStep
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView

internal val MacroSpeed.labelRes: Int
    get() = when (this) {
        MacroSpeed.SLOW -> R.string.macro_speed_slow
        MacroSpeed.NORMAL -> R.string.macro_speed_normal
        MacroSpeed.FAST -> R.string.macro_speed_fast
        MacroSpeed.TURBO -> R.string.macro_speed_turbo
    }

internal val MacroRepeat.labelRes: Int
    get() = when (this) {
        MacroRepeat.ONCE -> R.string.macro_repeat_once
        MacroRepeat.THREE -> R.string.macro_repeat_three
        MacroRepeat.FIVE -> R.string.macro_repeat_five
        MacroRepeat.TEN -> R.string.macro_repeat_ten
        MacroRepeat.FOREVER -> R.string.macro_repeat_forever
    }

private const val PICK_NONE = 0
private const val PICK_SPEED = 1
private const val PICK_REPEAT = 2
private const val SHOWN_STEPS = 8
private const val TEXT_CHIP_CHARS = 10
private const val DARK_INK = 0xFF10131A.toInt()
private const val LIGHT_INK = 0xFFF4F6FF.toInt()

@Composable
internal fun MacroManagerContent(macro: MacroController, look: ButtonLook, onRecord: () -> Unit) {
    val p = look.palette
    var picking by remember { mutableIntStateOf(PICK_NONE) }
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = RECORD_CLEARANCE),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MacroHeader(macro, p, picking) { picking = if (picking == it) PICK_NONE else it }
            PickerRow(macro, p, picking) { picking = PICK_NONE }
            if (macro.macros.isEmpty()) {
                BasicText(stringResource(R.string.macro_empty), style = TextStyle(color = Color(p.label).copy(alpha = 0.7f), fontSize = 12.sp))
            }
            macro.macros.asReversed().forEach { item -> MacroCard(item, macro, p) }
        }
        RecordPill(p, onRecord, Modifier.align(Alignment.BottomCenter).padding(12.dp))
    }
}

private val RECORD_CLEARANCE = 76.dp

@Composable
private fun PickerRow(macro: MacroController, p: ThemePalette, picking: Int, onDone: () -> Unit) {
    if (picking == PICK_NONE) return
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (picking == PICK_SPEED) {
            MacroSpeed.entries.forEach { speed ->
                MacroChip(stringResource(speed.labelRes), macro.speed == speed, p) { macro.chooseSpeed(speed); onDone() }
            }
        } else {
            MacroRepeat.entries.forEach { count ->
                MacroChip(stringResource(count.labelRes), macro.repeat == count, p) { macro.chooseRepeat(count); onDone() }
            }
        }
    }
}

@Composable
private fun MacroHeader(macro: MacroController, p: ThemePalette, picking: Int, onPick: (Int) -> Unit) {
    val ink = Color(p.label)
    val back = if (LocalLayoutDirection.current == LayoutDirection.Rtl) SpaceIcon.RIGHT else SpaceIcon.LEFT
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MacroIconButton(back, stringResource(R.string.macro_back), ink, ink, macro::closeManager)
        BasicText(stringResource(R.string.macro_title), style = TextStyle(color = ink, fontSize = 16.sp))
        Spacer(Modifier.weight(1f))
        RepeatChip(macro.repeat, picking == PICK_REPEAT, p) { onPick(PICK_REPEAT) }
        SpeedChip(macro.speed, picking == PICK_SPEED, p) { onPick(PICK_SPEED) }
    }
}

@Composable
private fun SpeedChip(speed: MacroSpeed, open: Boolean, p: ThemePalette, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val tint = Color(p.glow)
    var box = Modifier.clip(shape).background(Color(ColorMath.mix(p.background, p.keyFill, 1f)))
    if (open) box = box.border(1.dp, tint, shape)
    Row(
        box.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeedBars(speed.ordinal + 1, tint)
        BasicText(stringResource(speed.labelRes), style = TextStyle(color = tint, fontSize = 13.sp), maxLines = 1)
        SpaceIconView(SpaceIcon.DOWN, tint, Modifier, 14.dp)
    }
}

@Composable
private fun RepeatChip(repeat: MacroRepeat, open: Boolean, p: ThemePalette, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val tint = Color(p.glow)
    var box = Modifier.clip(shape).background(Color(ColorMath.mix(p.background, p.keyFill, 1f)))
    if (open) box = box.border(1.dp, tint, shape)
    Box(box.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp)) {
        BasicText(stringResource(repeat.labelRes), style = TextStyle(color = tint, fontSize = 13.sp), maxLines = 1)
    }
}

@Composable
private fun SpeedBars(level: Int, color: Color) {
    Row(Modifier.height(12.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        for (i in 1..MacroSpeed.entries.size) {
            Box(Modifier.width(3.dp).height((4 + 2 * i).dp).background(color.copy(alpha = if (i <= level) 1f else 0.35f), RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
private fun RecordPill(p: ThemePalette, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier.fillMaxWidth().height(48.dp).clip(shape).background(RecordRed).clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(20.dp).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(8.dp).background(Color.White, CircleShape))
        }
        BasicText(stringResource(R.string.macro_record), style = TextStyle(color = Color.White, fontSize = 15.sp))
    }
}

@Composable
private fun MacroCard(item: Macro, macro: MacroController, p: ThemePalette) {
    var asking by remember(item.id) { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    val ink = Color(p.label)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Color(ColorMath.mix(p.background, p.keyFill, 0.75f))).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val title = item.name ?: stringResource(R.string.macro_default_name, item.id.toInt())
                BasicText(title, style = TextStyle(color = ink, fontSize = 14.sp), maxLines = 1)
                BasicText(stringResource(R.string.macro_steps, item.steps.size), style = TextStyle(color = ink.copy(alpha = 0.7f), fontSize = 12.sp), maxLines = 1)
            }
            if (asking) {
                MacroChip(stringResource(R.string.macro_delete_confirm), true, p) { macro.delete(item.id) }
                MacroIconButton(SpaceIcon.CLOSE, stringResource(R.string.macro_back), ink, ink) { asking = false }
            } else {
                CardActions(item, macro, p) { asking = true }
            }
        }
        StepTimeline(item, p)
    }
}

@Composable
private fun CardActions(item: Macro, macro: MacroController, p: ThemePalette, onDelete: () -> Unit) {
    val ink = Color(p.label)
    val starred = macro.favorite?.id == item.id
    val star = stringResource(if (starred) R.string.macro_favorite_remove else R.string.macro_favorite_add)
    MacroIconButton(SpaceIcon.SYM_STARS, star, if (starred) Color(p.glow) else ink.copy(alpha = 0.6f), ink) { macro.toggleFavorite(item.id) }
    MacroIconButton(SpaceIcon.PENCIL, stringResource(R.string.macro_rename), ink, ink) { macro.askRename(item.id) }
    MacroIconButton(SpaceIcon.TRASH, stringResource(R.string.macro_delete), ink, ink, onDelete)
    PlayButton(p) { macro.play(item) }
}

@Composable
private fun PlayButton(p: ThemePalette, onClick: () -> Unit) {
    val glow = Color(p.glow)
    val on = Color(ColorMath.readableOn(p.glow, listOf(DARK_INK, LIGHT_INK)))
    val label = stringResource(R.string.macro_play)
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(glow).clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { SpaceIconView(SpaceIcon.PLAY, on, Modifier, 20.dp) }
}

@Composable
private fun StepTimeline(item: Macro, p: ThemePalette) {
    val shown = item.steps.take(SHOWN_STEPS)
    val rest = item.steps.size - shown.size
    val line = Color(p.label).copy(alpha = 0.25f)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        shown.forEachIndexed { index, step ->
            if (index > 0) Box(Modifier.width(8.dp).height(2.dp).background(line))
            StepChip(step, p)
        }
        if (rest > 0) {
            Box(Modifier.width(8.dp).height(2.dp).background(line))
            StepBubble(Color(ColorMath.mix(p.background, p.keyFill, 1f))) {
                BasicText("+$rest", style = TextStyle(color = Color(p.label).copy(alpha = 0.8f), fontSize = 12.sp), maxLines = 1)
            }
        }
    }
}

@Composable
private fun StepChip(step: MacroStep, p: ThemePalette) {
    val ink = Color(p.label)
    val plain = Color(ColorMath.mix(p.background, p.keyFill, 1f))
    when (step) {
        is MacroStep.Text -> StepBubble(plain) {
            val flat = step.text.replace('\n', ' ')
            val cut = if (flat.length > TEXT_CHIP_CHARS) flat.take(TEXT_CHIP_CHARS) + "…" else flat
            BasicText(cut, style = TextStyle(color = ink, fontSize = 13.sp), maxLines = 1)
        }
        MacroStep.Enter -> StepBubble(plain) { SpaceIconView(SpaceIcon.ENTER, ink, Modifier, 16.dp) }
        MacroStep.Delete -> StepBubble(plain) { SpaceIconView(SpaceIcon.DELETE, ink, Modifier, 16.dp) }
        MacroStep.LiveClip -> StepBubble(Color(p.glow).copy(alpha = 0.18f)) {
            BasicText(stringResource(R.string.macro_live_chip), style = TextStyle(color = Color(p.glow), fontSize = 13.sp), maxLines = 1)
        }
        MacroStep.Send -> {
            val glow = Color(p.glow)
            StepBubble(glow.copy(alpha = 0.18f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    SpaceIconView(SpaceIcon.SEND, glow, Modifier, 16.dp)
                    BasicText(stringResource(R.string.macro_send), style = TextStyle(color = glow, fontSize = 13.sp), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun StepBubble(fill: Color, content: @Composable () -> Unit) {
    Box(
        Modifier.height(28.dp).clip(RoundedCornerShape(50)).background(fill).padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
internal fun MacroChip(text: String, active: Boolean, p: ThemePalette, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    var box = Modifier.clip(shape).background(Color(ColorMath.mix(p.background, p.keyFill, if (active) 1f else 0.75f)))
    if (active) box = box.border(1.dp, Color(p.glow), shape)
    Box(box.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
        BasicText(text, style = TextStyle(color = Color(if (active) p.glow else p.label), fontSize = 13.sp), maxLines = 1)
    }
}
