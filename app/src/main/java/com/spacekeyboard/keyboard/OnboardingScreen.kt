package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.components.SpaceStatusPill
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.KeyboardStatus
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private val titles = listOf(
    R.string.onboarding_welcome_title,
    R.string.onboarding_enable_title,
    R.string.onboarding_features_title,
    R.string.onboarding_privacy_title,
)

private val features = listOf(
    R.string.onboarding_feat_delete_title to R.string.onboarding_feat_delete_body,
    R.string.onboarding_feat_cursor_title to R.string.onboarding_feat_cursor_body,
    R.string.onboarding_feat_hold_title to R.string.onboarding_feat_hold_body,
    R.string.onboarding_feat_lang_title to R.string.onboarding_feat_lang_body,
    R.string.onboarding_feat_tools_title to R.string.onboarding_feat_tools_body,
    R.string.onboarding_feat_theme_title to R.string.onboarding_feat_theme_body,
)

@Composable
internal fun OnboardingScreen(status: KeyboardStatus, onEnable: () -> Unit, onPick: () -> Unit, onFinish: () -> Unit) {
    var index by rememberSaveable { mutableStateOf(0) }
    BackHandler(enabled = index > 0) { index -= 1 }
    ScreenFrame(titles[index], null) {
        Progress(index, titles.size)
        when (index) {
            WELCOME_PAGE -> BodyText(R.string.onboarding_welcome_body)
            ENABLE_PAGE -> EnablePage(status, onEnable, onPick)
            FEATURES_PAGE -> FeaturesPage()
            else -> PrivacyPage(status)
        }
        NavButtons(index, { index -= 1 }, { index += 1 }, onFinish)
    }
}

@Composable
private fun Progress(index: Int, count: Int) {
    Row(
        modifier = Modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(count) { page ->
            val active = page == index
            Box(
                modifier = Modifier
                    .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (active) SpaceTheme.colors.primary else SpaceTheme.colors.outline),
            )
        }
    }
    SpaceText(
        text = stringResource(R.string.onboarding_step_progress, index + 1, count),
        style = SpaceTheme.typography.bodySmall,
        color = SpaceTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun EnablePage(status: KeyboardStatus, onEnable: () -> Unit, onPick: () -> Unit) {
    BodyText(R.string.onboarding_enable_why)
    StepCard(1, R.string.onboarding_enable_step1, status != KeyboardStatus.DISABLED) {
        if (status == KeyboardStatus.DISABLED) SpacePrimaryButton(stringResource(R.string.action_enable), onEnable)
    }
    StepCard(2, R.string.onboarding_enable_step2, status == KeyboardStatus.SELECTED) {
        if (status == KeyboardStatus.ENABLED) SpacePrimaryButton(stringResource(R.string.action_pick), onPick)
    }
    if (status == KeyboardStatus.SELECTED) {
        SpaceStatusPill(stringResource(R.string.onboarding_status_ready))
    }
}

@Composable
private fun StepCard(number: Int, title: Int, done: Boolean, action: @Composable ColumnScope.() -> Unit) {
    val state = stringResource(if (done) R.string.onboarding_step_done else R.string.onboarding_step_todo)
    val tint = if (done) SpaceTheme.colors.success else SpaceTheme.colors.onSurfaceVariant
    LookCard(Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                SpaceText("$number. ${stringResource(title)}", SpaceTheme.typography.titleSmall, Modifier.weight(1f))
                if (done) SpaceIconView(SpaceIcon.CHECK, tint, size = 20.dp)
            }
            SpaceText(state, SpaceTheme.typography.bodySmall, color = tint)
            action()
        }
    }
}

@Composable
private fun FeaturesPage() {
    LookCard(Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            features.forEach { (title, body) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SpaceText(stringResource(title), SpaceTheme.typography.titleSmall)
                    BodyText(body)
                }
            }
        }
    }
}

@Composable
private fun PrivacyPage(status: KeyboardStatus) {
    var sample by remember { mutableStateOf("") }
    BodyText(R.string.onboarding_privacy_body)
    SpaceSectionTitle(stringResource(R.string.onboarding_try_title))
    if (status != KeyboardStatus.SELECTED) BodyText(R.string.onboarding_try_needs)
    SampleField(value = sample, onChange = { sample = it })
}

@Composable
private fun NavButtons(index: Int, onBack: () -> Unit, onNext: () -> Unit, onFinish: () -> Unit) {
    val last = index == titles.lastIndex
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (index > 0) SpaceButton(stringResource(R.string.onboarding_back), onBack, Modifier.weight(1f))
        if (last) {
            SpacePrimaryButton(stringResource(R.string.onboarding_done), onFinish, Modifier.weight(1f), SpaceIcon.DONE)
        } else {
            SpacePrimaryButton(stringResource(R.string.onboarding_next), onNext, Modifier.weight(1f), SpaceIcon.NEXT)
        }
    }
    if (!last) SpaceButton(stringResource(R.string.onboarding_skip), onFinish)
}

private const val WELCOME_PAGE = 0
private const val ENABLE_PAGE = 1
private const val FEATURES_PAGE = 2
