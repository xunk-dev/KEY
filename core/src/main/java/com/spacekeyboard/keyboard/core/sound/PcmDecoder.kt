package com.spacekeyboard.keyboard.core.sound

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal interface PcmSink {
    fun onFormat(sampleRate: Int)

    fun onPcm(timeUs: Long, mono: ShortArray, length: Int)
}

internal class PcmDecoder(private val path: String) {

    fun durationMs(): Long? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(path)
            val track = audioTrack(extractor) ?: return null
            val format = extractor.getTrackFormat(track)
            val fromFormat = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) / 1000L else 0L
            if (fromFormat > 0L) fromFormat else retrieverDuration()
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
            null
        } finally {
            extractor.release()
        }
    }

    fun decode(startMs: Long, endMs: Long, sink: PcmSink): Boolean {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        return try {
            extractor.setDataSource(path)
            val track = audioTrack(extractor) ?: return false
            val format = extractor.getTrackFormat(track)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return false
            extractor.selectTrack(track)
            extractor.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            val decoder = MediaCodec.createDecoderByType(mime)
            codec = decoder
            decoder.configure(format, null, null, 0)
            decoder.start()
            DecodePump(extractor, decoder, format, startMs * 1000L, endMs * 1000L, sink).run()
        } catch (_: IOException) {
            false
        } catch (_: RuntimeException) {
            false
        } finally {
            runCatching { codec?.stop() }
            codec?.release()
            extractor.release()
        }
    }

    private fun audioTrack(extractor: MediaExtractor): Int? =
        (0 until extractor.trackCount).firstOrNull {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME).orEmpty().startsWith("audio/")
        }

    private fun retrieverDuration(): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        } catch (_: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }
}

private class DecodePump(
    private val extractor: MediaExtractor,
    private val codec: MediaCodec,
    format: MediaFormat,
    private val startUs: Long,
    private val endUs: Long,
    private val sink: PcmSink,
) {
    private val info = MediaCodec.BufferInfo()
    private val deadline = System.nanoTime() + TIMEOUT_NS
    private var inputDone = false
    private var finished = false
    private var rate = 0
    private var channels = 1
    private var width = PcmSample.width(PcmSample.DEFAULT)
    private var encoding = PcmSample.DEFAULT

    init {
        readFormat(format)
    }

    fun run(): Boolean {
        while (System.nanoTime() < deadline) {
            if (!inputDone) feed()
            if (drain()) return rate > 0 && width > 0
        }
        return false
    }

    private fun feed() {
        val index = codec.dequeueInputBuffer(WAIT_US)
        if (index < 0) return
        val buffer = codec.getInputBuffer(index) ?: return
        val size = extractor.readSampleData(buffer, 0)
        val time = extractor.sampleTime
        if (size < 0 || time > endUs + MARGIN_US) {
            codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            inputDone = true
        } else {
            codec.queueInputBuffer(index, 0, size, time, 0)
            extractor.advance()
        }
    }

    private fun drain(): Boolean {
        val index = codec.dequeueOutputBuffer(info, WAIT_US)
        if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            readFormat(codec.outputFormat)
            return false
        }
        if (index < 0) return false
        val buffer = codec.getOutputBuffer(index)
        if (buffer != null && info.size > 0 && !finished) emit(buffer)
        codec.releaseOutputBuffer(index, false)
        return finished || (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
    }

    private fun readFormat(format: MediaFormat) {
        if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceAtLeast(1)
        encoding = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) format.getInteger(MediaFormat.KEY_PCM_ENCODING) else PcmSample.DEFAULT
        width = PcmSample.width(encoding)
        if (rate > 0) sink.onFormat(rate)
    }

    private fun emit(buffer: ByteBuffer) {
        if (rate <= 0 || width <= 0) return
        buffer.order(ByteOrder.nativeOrder())
        buffer.position(info.offset)
        buffer.limit(info.offset + info.size)
        val frames = info.size / (width * channels)
        val first = ((startUs - info.presentationTimeUs) * rate / MICROS).coerceIn(0L, frames.toLong()).toInt()
        val last = ((endUs - info.presentationTimeUs) * rate / MICROS + 1).coerceIn(0L, frames.toLong()).toInt()
        if (last < frames) finished = true
        if (last <= first) return
        val mono = ShortArray(last - first)
        for (frame in first until last) mono[frame - first] = frameAt(buffer, frame)
        sink.onPcm(info.presentationTimeUs + first * MICROS / rate, mono, mono.size)
    }

    private fun frameAt(buffer: ByteBuffer, frame: Int): Short {
        var sum = 0f
        for (channel in 0 until channels) {
            sum += PcmSample.read(buffer, info.offset + (frame * channels + channel) * width, encoding)
        }
        return (sum / channels).toInt().coerceIn(-32768, 32767).toShort()
    }

    private companion object {
        const val WAIT_US = 10_000L
        const val MARGIN_US = 200_000L
        const val MICROS = 1_000_000L
        const val TIMEOUT_NS = 180_000_000_000L
    }
}
