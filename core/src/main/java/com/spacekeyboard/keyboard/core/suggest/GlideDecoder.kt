package com.spacekeyboard.keyboard.core.suggest

import kotlin.math.hypot
import kotlin.math.ln

class GlideKey(val ch: Char, val x: Float, val y: Float, val width: Float, val height: Float)

class GlidePoint(val x: Float, val y: Float)

class GlideCandidate(val word: String, val weight: Float)

class GlideDecoder {
    fun decode(path: List<GlidePoint>, keys: List<GlideKey>, candidates: List<GlideCandidate>, limit: Int = LIMIT): List<String> {
        val map = keys.filter { it.width > 0f && it.height > 0f }.associateBy { it.ch.lowercaseChar() }
        if (map.isEmpty() || path.size < 2) return emptyList()
        val unit = map.values.map { it.width }.sorted().let { it[it.size / 2] }
        if (length(path) < unit * MIN_LENGTH) return emptyList()
        val shape = resample(path)
        val first = path.first()
        val last = path.last()
        val scored = ArrayList<Pair<String, Float>>()
        for (candidate in candidates) {
            val centers = centersOf(candidate.word, map) ?: continue
            if (centers.size < 2) continue
            if (far(centers.first(), first, unit) || far(centers.last(), last, unit)) continue
            val cost = cost(centers, shape, path, unit)
            if (cost <= MAX_COST) scored += candidate.word to cost - WEIGHT * candidate.weight
        }
        return scored.sortedBy { it.second }.map { it.first }.distinct().take(limit)
    }

    private fun centersOf(word: String, map: Map<Char, GlideKey>): List<GlideKey>? {
        val out = ArrayList<GlideKey>(word.length)
        for (raw in word) {
            val key = map[raw.lowercaseChar()] ?: map[ArabicFold.fold(raw.toString()).firstOrNull() ?: return null] ?: return null
            if (out.isEmpty() || out.last() !== key) out += key
        }
        return out
    }

    private fun far(key: GlideKey, point: GlidePoint, unit: Float): Boolean =
        hypot(key.x - point.x, key.y - point.y) > unit * START_REACH

    private fun cost(centers: List<GlideKey>, shape: List<GlidePoint>, path: List<GlidePoint>, unit: Float): Float {
        val ideal = resample(centers.map { GlidePoint(it.x, it.y) })
        var total = 0f
        for (i in shape.indices) total += hypot(shape[i].x - ideal[i].x, shape[i].y - ideal[i].y)
        var missed = 0f
        for (key in centers) missed += nearest(path, key)
        return total / shape.size / unit + MISS_WEIGHT * missed / centers.size / unit
    }

    private fun nearest(path: List<GlidePoint>, key: GlideKey): Float {
        var best = Float.MAX_VALUE
        for (i in 0 until path.size - 1) best = minOf(best, segment(path[i], path[i + 1], key.x, key.y))
        return best
    }

    private fun segment(a: GlidePoint, b: GlidePoint, px: Float, py: Float): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val span = dx * dx + dy * dy
        val t = if (span == 0f) 0f else (((px - a.x) * dx + (py - a.y) * dy) / span).coerceIn(0f, 1f)
        return hypot(px - (a.x + t * dx), py - (a.y + t * dy))
    }

    private fun length(points: List<GlidePoint>): Float {
        var sum = 0f
        for (i in 1 until points.size) sum += hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y)
        return sum
    }

    private fun resample(points: List<GlidePoint>): List<GlidePoint> {
        val total = length(points)
        if (total == 0f) return List(SAMPLES) { points.first() }
        val out = ArrayList<GlidePoint>(SAMPLES)
        var index = 1
        var walked = 0f
        for (i in 0 until SAMPLES) {
            val target = total * i / (SAMPLES - 1)
            while (index < points.size - 1 && walked + step(points, index) < target) {
                walked += step(points, index)
                index++
            }
            val run = step(points, index)
            val t = if (run == 0f) 0f else ((target - walked) / run).coerceIn(0f, 1f)
            val a = points[index - 1]
            val b = points[index]
            out += GlidePoint(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
        }
        return out
    }

    private fun step(points: List<GlidePoint>, index: Int): Float =
        hypot(points[index].x - points[index - 1].x, points[index].y - points[index - 1].y)

    companion object {
        const val LIMIT = 3
        const val SAMPLES = 32
        private const val MIN_LENGTH = 1.2f
        private const val START_REACH = 1.6f
        private const val MAX_COST = 1.6f
        private const val MISS_WEIGHT = 0.6f
        private const val WEIGHT = 0.3f

        fun weightOf(count: Int): Float = (BASE_WEIGHT + ln(1f + count) * COUNT_WEIGHT).coerceAtMost(1f)

        const val BASE_WEIGHT = 0.4f
        private const val COUNT_WEIGHT = 0.15f
    }
}
