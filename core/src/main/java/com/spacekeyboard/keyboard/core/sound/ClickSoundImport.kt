package com.spacekeyboard.keyboard.core.sound

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.abs

enum class ClickImportError { TOO_LARGE, TOO_LONG, UNSUPPORTED, SILENT, SILENT_PART }

class StagedSound(val file: File, val name: String, val durationMs: Long, val peaks: FloatArray)

sealed interface StageResult {
    class Ready(val sound: StagedSound) : StageResult

    class Failed(val error: ClickImportError) : StageResult
}

class ClickSoundImport(
    private val resolver: ContentResolver,
    private val store: ClickSoundStore,
    private val cacheDir: File,
) {

    fun stage(uri: Uri): StageResult {
        val file = File(cacheDir, STAGE_FILE)
        StagedPeaks.delete(file)
        return try {
            if (!copyLimited(uri, file)) return fail(file, ClickImportError.TOO_LARGE)
            analyse(file, displayName(uri))
        } catch (_: IOException) {
            fail(file, ClickImportError.UNSUPPORTED)
        } catch (_: SecurityException) {
            fail(file, ClickImportError.UNSUPPORTED)
        }
    }

    fun save(sound: StagedSound, range: ClipRange): ClickImportError? {
        val collector = Collector()
        val ok = PcmDecoder(sound.file.path).decode(range.startMs, range.endMs, collector)
        val raw = collector.samples()
        if (!ok || raw.isEmpty() || collector.rate <= 0) return ClickImportError.UNSUPPORTED
        if (PcmClip.level(raw) < PeakBuilder.SILENCE) return ClickImportError.SILENT_PART
        return if (writeClip(PcmClip.finish(raw, collector.rate), collector.rate)) {
            store.commit(sound.name, range.lengthMs)
            null
        } else {
            ClickImportError.UNSUPPORTED
        }
    }

    fun discard(sound: StagedSound) {
        sound.file.delete()
        StagedPeaks.delete(sound.file)
    }

    fun restore(name: String, durationMs: Long): StagedSound? {
        val file = File(cacheDir, STAGE_FILE)
        val peaks = StagedPeaks.read(file) ?: return null
        if (!file.isFile || durationMs <= 0L) return null
        return StagedSound(file, name, durationMs, peaks)
    }

    private fun analyse(file: File, name: String): StageResult {
        val decoder = PcmDecoder(file.path)
        val duration = decoder.durationMs()?.takeIf { it > 0L } ?: return fail(file, ClickImportError.UNSUPPORTED)
        if (duration > MAX_SOURCE_MS) return fail(file, ClickImportError.TOO_LONG)
        val meter = Meter(PeakBuilder(duration))
        if (!decoder.decode(0L, duration, meter)) return fail(file, ClickImportError.UNSUPPORTED)
        if (meter.builder.silent) return fail(file, ClickImportError.SILENT)
        val peaks = meter.builder.build()
        StagedPeaks.write(file, peaks)
        return StageResult.Ready(StagedSound(file, name, duration, peaks))
    }

    private fun writeClip(samples: ShortArray, rate: Int): Boolean {
        val dir = store.workDir()
        val temp = File(dir, TEMP_FILE)
        return try {
            temp.outputStream().use { WavFile.write(it, samples, rate) }
            store.clipFile.delete()
            temp.renameTo(store.clipFile)
        } catch (_: IOException) {
            false
        } finally {
            temp.delete()
        }
    }

    private fun fail(file: File, error: ClickImportError): StageResult {
        file.delete()
        StagedPeaks.delete(file)
        return StageResult.Failed(error)
    }

    private fun displayName(uri: Uri): String {
        val fromProvider = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        }.getOrNull()
        return (fromProvider ?: uri.lastPathSegment).orEmpty().take(MAX_NAME)
    }

    private fun copyLimited(uri: Uri, target: File): Boolean {
        var total = 0L
        val input = resolver.openInputStream(uri) ?: throw IOException("Cannot open $uri")
        input.use { source ->
            target.outputStream().use { output ->
                val buffer = ByteArray(BUFFER)
                while (true) {
                    val read = source.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_SOURCE_BYTES) return false
                    output.write(buffer, 0, read)
                }
            }
        }
        if (total == 0L) throw IOException("Empty file")
        return true
    }

    private class Meter(val builder: PeakBuilder) : PcmSink {
        private var rate = DEFAULT_RATE

        override fun onFormat(sampleRate: Int) {
            rate = sampleRate.coerceAtLeast(1)
        }

        override fun onPcm(timeUs: Long, mono: ShortArray, length: Int) {
            val perBucket = (rate * PeakBuilder.STEP_MS / 1000L).toInt().coerceAtLeast(1)
            var offset = 0
            while (offset < length) {
                val end = minOf(length, offset + perBucket)
                var top = 0
                for (i in offset until end) top = maxOf(top, abs(mono[i].toInt()))
                builder.add(timeUs + offset * MICROS / rate, top / FULL_SCALE)
                offset = end
            }
        }
    }

    private class Collector : PcmSink {
        private val bytes = ByteArrayOutputStream()
        var rate = 0

        override fun onFormat(sampleRate: Int) {
            rate = sampleRate
        }

        override fun onPcm(timeUs: Long, mono: ShortArray, length: Int) {
            for (i in 0 until length) {
                bytes.write(mono[i].toInt() and 0xFF)
                bytes.write(mono[i].toInt() shr 8 and 0xFF)
            }
        }

        fun samples(): ShortArray {
            val data = bytes.toByteArray()
            return ShortArray(data.size / 2) { ((data[it * 2].toInt() and 0xFF) or (data[it * 2 + 1].toInt() shl 8)).toShort() }
        }
    }

    private companion object {
        const val STAGE_FILE = "click_stage.bin"
        const val TEMP_FILE = "click.wav.tmp"
        const val BUFFER = 64 * 1024
        const val MAX_SOURCE_BYTES = 64L * 1024 * 1024
        const val MAX_SOURCE_MS = 10L * 60L * 1000L
        const val MAX_NAME = 60
        const val MICROS = 1_000_000L
        const val FULL_SCALE = 32767f
        const val DEFAULT_RATE = 44_100
    }
}
