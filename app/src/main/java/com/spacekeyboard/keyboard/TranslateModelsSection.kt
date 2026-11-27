package com.spacekeyboard.keyboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.ModelFailure
import com.spacekeyboard.keyboard.translate.ModelStatus
import com.spacekeyboard.keyboard.translate.ModelsSnapshot
import com.spacekeyboard.keyboard.translate.OnDeviceModels
import com.spacekeyboard.keyboard.translate.TranslateLanguages
import com.spacekeyboard.keyboard.translate.TranslatePrefsStore
import kotlinx.coroutines.delay

private const val CONFIRM_MS = 4_000L
private const val TICK_MS = 2_000L

@Composable
private fun rememberModels(): ModelsSnapshot {
    var snapshot by remember { mutableStateOf(OnDeviceModels.snapshot) }
    DisposableEffect(Unit) {
        val subscription = OnDeviceModels.subscribe { snapshot = it }
        onDispose { subscription.close() }
    }
    return snapshot
}

@Composable
private fun rememberNotificationAsker(store: TranslatePrefsStore): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    return {
        val needed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needed && !store.notifyAsked()) {
            store.setNotifyAsked()
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun ModelsEffects(busy: Boolean, confirming: String?, onTick: () -> Unit, onExpire: () -> Unit) {
    DisposableEffect(Unit) {
        OnDeviceModels.screenVisible = true
        onDispose { OnDeviceModels.screenVisible = false }
    }
    LaunchedEffect(Unit) { OnDeviceModels.refresh() }
    LaunchedEffect(busy) {
        while (busy) {
            onTick()
            delay(TICK_MS)
        }
    }
    LaunchedEffect(confirming) {
        if (confirming != null) {
            delay(CONFIRM_MS)
            onExpire()
        }
    }
}

@Composable
internal fun TranslateModelsSection() {
    val context = LocalContext.current
    val store = remember(context) { TranslatePrefsStore(context) }
    var wifiOnly by remember { mutableStateOf(store.modelsWifiOnly()) }
    var confirming by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    OnDeviceModels.attach(context)
    val snapshot = rememberModels()
    val busy = snapshot.status.values.any { it is ModelStatus.Downloading }
    val askNotifications = rememberNotificationAsker(store)
    ModelsEffects(busy, confirming, { now = System.currentTimeMillis() }) { confirming = null }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceText(stringResource(R.string.settings_translate_models_title), SpaceTheme.typography.title)
        TranslateNote(R.string.settings_translate_models_note)
        SwitchRow(stringResource(R.string.settings_translate_models_wifi), wifiOnly) {
            wifiOnly = it
            store.setModelsWifiOnly(it)
        }
        val locale = LocalConfiguration.current.locales.get(0)
        ModelsBody(snapshot, wifiOnly, now, confirming, locale) { action ->
            confirming = handleModelAction(action, confirming, wifiOnly, askNotifications)
        }
    }
}

private fun handleModelAction(action: ModelAction, confirming: String?, wifiOnly: Boolean, askNotifications: () -> Unit): String? =
    when (action) {
        is ModelAction.Get -> {
            askNotifications()
            OnDeviceModels.download(action.code, wifiOnly)
            null
        }
        is ModelAction.Remove -> if (confirming == action.code) {
            OnDeviceModels.remove(action.code)
            null
        } else {
            action.code
        }
        is ModelAction.Retry -> {
            askNotifications()
            OnDeviceModels.dismissFailure(action.code)
            OnDeviceModels.download(action.code, wifiOnly)
            confirming
        }
        ModelAction.Reload -> {
            OnDeviceModels.refresh()
            confirming
        }
    }

private sealed interface ModelAction {
    data class Get(val code: String) : ModelAction
    data class Remove(val code: String) : ModelAction
    data class Retry(val code: String) : ModelAction
    data object Reload : ModelAction
}

@Composable
private fun ModelsBody(
    snapshot: ModelsSnapshot,
    wifiOnly: Boolean,
    now: Long,
    confirming: String?,
    locale: java.util.Locale,
    act: (ModelAction) -> Unit,
) {
    val installed = snapshot.installed
    when {
        installed == null && snapshot.loadFailed -> {
            TranslateNote(R.string.settings_translate_models_load_failed)
            SpaceButton(stringResource(R.string.settings_translate_models_reload), { act(ModelAction.Reload) })
        }
        installed == null -> TranslateNote(R.string.settings_translate_models_loading)
        else -> OnDeviceModels.languages().forEach { code ->
            ModelRow(code, TranslateLanguages.displayName(code, locale), snapshot, wifiOnly, now, confirming == code, act)
        }
    }
}

@Composable
private fun ModelRow(
    code: String,
    name: String,
    snapshot: ModelsSnapshot,
    wifiOnly: Boolean,
    now: Long,
    confirming: Boolean,
    act: (ModelAction) -> Unit,
) {
    val status = snapshot.status[code]
    val installed = snapshot.isInstalled(code)
    val removing = code in snapshot.removing
    val colors = SpaceTheme.colors
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SpaceText(name, SpaceTheme.typography.body)
                val (text, tone) = when {
                    removing -> stringResource(R.string.settings_translate_models_removing) to colors.onSurfaceVariant
                    status is ModelStatus.Downloading -> downloadingText(now - status.since, wifiOnly) to colors.onSurfaceVariant
                    status is ModelStatus.Failed -> failureText(status.reason) to colors.error
                    installed -> stringResource(R.string.settings_translate_models_installed) to colors.success
                    else -> stringResource(R.string.settings_translate_models_size) to colors.onSurfaceVariant
                }
                SpaceText(text, SpaceTheme.typography.bodySmall, color = tone)
            }
            val buttonModifier = Modifier.width(112.dp)
            when {
                removing || status is ModelStatus.Downloading -> Unit
                status is ModelStatus.Failed -> SpaceButton(stringResource(R.string.settings_translate_models_retry), { act(ModelAction.Retry(code)) }, buttonModifier)
                installed && confirming -> SpaceButton(stringResource(R.string.settings_translate_models_confirm), { act(ModelAction.Remove(code)) }, buttonModifier)
                installed -> SpaceButton(stringResource(R.string.settings_translate_models_remove), { act(ModelAction.Remove(code)) }, buttonModifier)
                else -> SpaceButton(stringResource(R.string.settings_translate_models_get), { act(ModelAction.Get(code)) }, buttonModifier)
            }
        }
        if (removing || status is ModelStatus.Downloading) IndeterminateBar()
    }
}

@Composable
private fun downloadingText(elapsedMs: Long, wifiOnly: Boolean): String = when {
    elapsedMs < OnDeviceModels.SLOW_AFTER_MS -> stringResource(R.string.settings_translate_models_downloading)
    wifiOnly -> stringResource(R.string.settings_translate_models_waiting_wifi)
    else -> stringResource(R.string.settings_translate_models_slow)
}

@Composable
private fun failureText(reason: ModelFailure): String = stringResource(
    when (reason) {
        ModelFailure.NETWORK -> R.string.settings_translate_models_fail_network
        ModelFailure.STORAGE -> R.string.settings_translate_models_fail_storage
        ModelFailure.OTHER -> R.string.settings_translate_models_fail_other
    },
)

@Composable
private fun IndeterminateBar() {
    val track = SpaceTheme.colors.containerHigh
    val fill = SpaceTheme.colors.primary
    val shift by rememberInfiniteTransition(label = "models").animateFloat(
        initialValue = -0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing), RepeatMode.Restart),
        label = "shift",
    )
    Column(
        Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(track).drawBehind {
            val width = size.width * 0.35f
            drawRect(fill, Offset(size.width * shift, 0f), Size(width, size.height))
        },
    ) {}
}
