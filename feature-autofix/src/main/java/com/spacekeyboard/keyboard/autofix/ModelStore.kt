package com.spacekeyboard.keyboard.autofix

import android.content.Context
import android.content.SharedPreferences

class ModelStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun load(into: ErrorModel = ErrorModel()): ErrorModel = ModelCodec.decode(prefs.getString(MODEL, null), into)

    fun save(model: ErrorModel) {
        prefs.edit().putString(MODEL, ModelCodec.encode(model)).apply()
    }

    fun clear() {
        prefs.edit().remove(MODEL).apply()
    }

    private companion object {
        const val NAME = "space_keyboard_autofix_model"
        const val MODEL = "model"
    }
}
