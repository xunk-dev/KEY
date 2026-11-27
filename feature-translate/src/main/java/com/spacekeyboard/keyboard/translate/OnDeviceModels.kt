package com.spacekeyboard.keyboard.translate

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage as MlLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import java.util.concurrent.TimeUnit

object OnDeviceModels {
    const val SLOW_AFTER_MS = 20_000L
    private const val LIST_SECONDS = 10L
    private const val ENGLISH = "en"
    private const val STORE = "space_keyboard_models"
    private const val CONFIRMED = "confirmed"

    private val manager: RemoteModelManager get() = RemoteModelManager.getInstance()
    private val main = Handler(Looper.getMainLooper())
    private val listeners = LinkedHashSet<(ModelsSnapshot) -> Unit>()

    private var appContext: Context? = null
    private var store: SharedPreferences? = null

    @Volatile
    var screenVisible: Boolean = false

    var snapshot: ModelsSnapshot = ModelsSnapshot()
        private set

    fun attach(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        store = appContext?.getSharedPreferences(STORE, Context.MODE_PRIVATE)
    }

    fun languages(): List<String> =
        TranslateLanguages.ordered.filter { MlLanguage.fromLanguageTag(it) != null }

    fun subscribe(listener: (ModelsSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun refresh() = onMain {
        if (snapshot.installed == null && snapshot.loadFailed) set { it.copy(loadFailed = false) }
        manager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models -> set { it.withLoaded(models.map { m -> m.language }.toSet() + confirmed()) } }
            .addOnFailureListener { set { it.copy(loadFailed = true) } }
    }

    fun download(code: String, wifiOnly: Boolean) = onMain {
        if (snapshot.downloading(code) || snapshot.isInstalled(code)) return@onMain
        set { it.withStarted(code, System.currentTimeMillis()) }
        val conditions = DownloadConditions.Builder().apply { if (wifiOnly) requireWifi() }.build()
        manager.download(model(code), conditions)
            .addOnSuccessListener {
                remember(code, true)
                set { snap -> snap.withSuccess(code) }
                tell { ModelNotifier.done(it, code) }
            }
            .addOnFailureListener { error ->
                set { snap -> snap.withFailure(code, ModelFailure.of(error)) }
                tell { ModelNotifier.failed(it, code) }
            }
    }

    fun remove(code: String) = onMain {
        if (code in snapshot.removing) return@onMain
        set { it.withRemoving(code, true).withoutStatus(code) }
        manager.deleteDownloadedModel(model(code))
            .addOnSuccessListener {
                remember(code, false)
                set { snap -> snap.withRemoving(code, false).copy(installed = snap.installed?.minus(code)) }
            }
            .addOnFailureListener { set { it.withRemoving(code, false).withFailure(code, ModelFailure.OTHER) } }
    }

    fun dismissFailure(code: String) = onMain { set { it.withoutStatus(code) } }

    fun missing(needed: Collection<String>): List<String>? {
        val listed = runCatching {
            Tasks.await(manager.getDownloadedModels(TranslateRemoteModel::class.java), LIST_SECONDS, TimeUnit.SECONDS)
                .map { it.language }.toSet()
        }.getOrNull() ?: return null
        val present = listed + confirmed()
        return (needed + ENGLISH).filter { it !in present }.distinct()
    }

    private fun confirmed(): Set<String> = store?.getStringSet(CONFIRMED, emptySet()).orEmpty().toSet()

    private fun remember(code: String, on: Boolean) {
        val prefs = store ?: return
        val next = if (on) confirmed() + code else confirmed() - code
        prefs.edit().putStringSet(CONFIRMED, next).apply()
    }

    private fun tell(send: (Context) -> Unit) {
        val context = appContext ?: return
        if (!screenVisible) send(context)
    }

    private fun model(code: String): TranslateRemoteModel =
        TranslateRemoteModel.Builder(MlLanguage.fromLanguageTag(code) ?: code).build()

    private fun set(change: (ModelsSnapshot) -> ModelsSnapshot) {
        val next = change(snapshot)
        if (next == snapshot) return
        snapshot = next
        listeners.toList().forEach { it(next) }
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }
}
