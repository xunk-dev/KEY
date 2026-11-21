package com.spacekeyboard.keyboard.core.layout

import org.json.JSONArray
import org.json.JSONObject

object LayoutParser {
    fun parse(json: String): LayoutSpec {
        val root = JSONObject(json)
        val layoutId = root.getString("id")
        val rows = root.getJSONArray("rows").arrays { row -> row.items { key(layoutId, it) } }
        val ids = rows.flatten().map { it.id }
        require(ids.toSet().size == ids.size) { "duplicate key id in $layoutId" }
        require(rows.isNotEmpty() && rows.all { it.isNotEmpty() }) { "empty row in $layoutId" }
        return LayoutSpec(layoutId, rows)
    }

    private fun key(layoutId: String, node: Any): KeySpec {
        if (node is String) return commit(layoutId, node)
        val spec = node as JSONObject
        val action = KeyAction.valueOf(spec.optString("action", KeyAction.COMMIT.name))
        val label = spec.optString("label", "")
        val id = spec.optString("id", "").ifEmpty { derivedId(layoutId, label) }
        val weight = spec.optDouble("weight", 1.0).toFloat()
        require(weight > 0f) { "weight must be positive for $id" }
        val fixed = action == KeyAction.SPACE || action == KeyAction.SPACER
        val hold = spec.optJSONArray("hold")
        val alts = if (hold == null) emptyList() else List(hold.length()) { hold.getString(it) }
        return KeySpec(id, label, action, weight, spec.optBoolean("movable", !fixed), alts)
    }

    private fun commit(layoutId: String, label: String) =
        KeySpec(derivedId(layoutId, label), label, KeyAction.COMMIT, 1f, true)

    private fun derivedId(layoutId: String, label: String): String {
        require(label.isNotEmpty()) { "key without id or label in $layoutId" }
        return layoutId + "_" + label.map { it.code.toString(16) }.joinToString("")
    }

    private fun <T> JSONArray.arrays(map: (JSONArray) -> T): List<T> =
        List(length()) { map(getJSONArray(it)) }

    private fun <T> JSONArray.items(map: (Any) -> T): List<T> =
        List(length()) { map(get(it)) }
}
