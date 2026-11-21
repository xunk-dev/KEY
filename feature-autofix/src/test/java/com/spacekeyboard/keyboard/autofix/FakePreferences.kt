package com.spacekeyboard.keyboard.autofix

import android.content.SharedPreferences
import java.lang.reflect.Proxy

private inline fun <reified T : Any> fake(noinline body: (Any, String, Array<out Any?>) -> Any?): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
        body(proxy, method.name, args ?: emptyArray<Any?>())
    } as T

class FakePreferences {
    private val values = LinkedHashMap<String, Any>()
    private val listeners = ArrayList<SharedPreferences.OnSharedPreferenceChangeListener>()

    val prefs: SharedPreferences = fake { proxy, name, args -> read(proxy, name, args) }

    fun keys(): Set<String> = values.keys.toSet()

    private fun read(proxy: Any, name: String, args: Array<out Any?>): Any? = when (name) {
        "getInt", "getLong", "getFloat", "getBoolean", "getString" -> values[args[0] as String] ?: args[1]
        "contains" -> values.containsKey(args[0] as String)
        "getAll" -> HashMap(values)
        "edit" -> editor()
        "registerOnSharedPreferenceChangeListener" -> listeners.add(args[0] as SharedPreferences.OnSharedPreferenceChangeListener)
        "unregisterOnSharedPreferenceChangeListener" -> listeners.remove(args[0])
        "hashCode" -> System.identityHashCode(proxy)
        "equals" -> proxy === args[0]
        "toString" -> "FakePreferences"
        else -> error("unsupported call $name")
    }

    private fun editor(): SharedPreferences.Editor {
        val pending = LinkedHashMap<String, Any?>()
        return fake { proxy, name, args ->
            when (name) {
                "putString", "putInt", "putLong", "putFloat", "putBoolean", "putStringSet" -> {
                    pending[args[0] as String] = args[1]
                    proxy
                }
                "remove" -> {
                    pending[args[0] as String] = null
                    proxy
                }
                "apply" -> flush(pending)
                "commit" -> flush(pending).let { true }
                else -> error("unsupported editor call $name")
            }
        }
    }

    private fun flush(pending: Map<String, Any?>) {
        val changed = ArrayList<String>()
        pending.forEach { (key, value) ->
            val before = values[key]
            if (value == null) values.remove(key) else values[key] = value
            if (before != value) changed += key
        }
        changed.forEach { key -> listeners.toList().forEach { it.onSharedPreferenceChanged(prefs, key) } }
    }
}
