package com.spacekeyboard.keyboard.core.theme

object MotionPhoto {
    private const val HEADER_SCAN = 256 * 1024
    private val ITEM = Regex("<Container:Item\\s[^>]*>")
    private val LENGTH = Regex("Item:Length=\"(\\d+)\"")
    private val MICRO = Regex("MicroVideoOffset=\"(\\d+)\"")

    fun videoRange(bytes: ByteArray): IntRange? {
        if (bytes.size < MIN_SIZE || bytes[0] != 0xFF.toByte() || bytes[1] != 0xD8.toByte()) return null
        val header = String(bytes, 0, minOf(bytes.size, HEADER_SCAN), Charsets.ISO_8859_1)
        val length = containerLength(header) ?: microLength(header) ?: return null
        val start = bytes.size - length
        if (length <= 0 || start <= 0 || !hasFtyp(bytes, start)) return null
        return start until bytes.size
    }

    private fun containerLength(header: String): Int? = ITEM.findAll(header)
        .map { it.value }
        .firstOrNull { it.contains("Item:Semantic=\"MotionPhoto\"") }
        ?.let { LENGTH.find(it)?.groupValues?.get(1)?.toIntOrNull() }
        ?.takeIf { it > 0 }

    private fun microLength(header: String): Int? =
        MICRO.find(header)?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it > 0 }

    private fun hasFtyp(bytes: ByteArray, start: Int): Boolean {
        if (start + FTYP_END > bytes.size) return false
        return bytes[start + 4] == 'f'.code.toByte() &&
            bytes[start + 5] == 't'.code.toByte() &&
            bytes[start + 6] == 'y'.code.toByte() &&
            bytes[start + 7] == 'p'.code.toByte()
    }

    private const val MIN_SIZE = 1024
    private const val FTYP_END = 8
}
