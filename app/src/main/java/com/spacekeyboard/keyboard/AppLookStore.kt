package com.spacekeyboard.keyboard

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook

internal object AppLookStore {
    private const val NAME = "space_app"
    private const val KEY = "app_look"
    private val state = mutableStateOf<SpaceLook?>(null)
    private var loaded = false

    fun current(context: Context): State<SpaceLook?> {
        load(context)
        return state
    }

    fun read(context: Context): SpaceLook? = current(context).value

    fun write(context: Context, look: SpaceLook?) {
        load(context)
        val edit = prefs(context).edit()
        if (look == null) edit.remove(KEY) else edit.putString(KEY, look.key)
        edit.apply()
        state.value = look
    }

    private fun load(context: Context) {
        if (loaded) return
        state.value = SpaceLook.fromKey(prefs(context).getString(KEY, null))
        loaded = true
    }

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
