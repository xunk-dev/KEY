package com.spacekeyboard.keyboard.core.theme

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.nio.ByteBuffer

data class VideoInfo(val durationMs: Long, val width: Int, val height: Int, val rotation: Int) {
    val side: Int get() = maxOf(width, height)
}

object VideoClipper {
    private const val MICROS = 1000L
    private const val DEFAULT_BUFFER = 1024 * 1024

    fun info(source: FileDescriptor): VideoInfo? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(source)
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != "yes") return null
            VideoInfo(
                durationMs = number(retriever, MediaMetadataRetriever.METADATA_KEY_DURATION),
                width = number(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt(),
                height = number(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt(),
                rotation = number(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).toInt(),
            )
        } catch (_: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }

    fun clip(source: FileDescriptor, range: TrimRange, rotation: Int, target: File): Boolean {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        return try {
            extractor.setDataSource(source)
            val track = videoTrack(extractor)
            if (track < 0) return false
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            muxer = MediaMuxer(target.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer.setOrientationHint(rotation)
            val out = muxer.addTrack(format)
            muxer.start()
            val written = copy(extractor, muxer, out, range, bufferSize(format))
            muxer.stop()
            written
        } catch (_: RuntimeException) {
            false
        } catch (_: IOException) {
            false
        } finally {
            extractor.release()
            runCatching { muxer?.release() }
        }
    }

    private fun copy(extractor: MediaExtractor, muxer: MediaMuxer, track: Int, range: TrimRange, size: Int): Boolean {
        val buffer = ByteBuffer.allocate(size)
        val info = MediaCodec.BufferInfo()
        val endUs = range.endMs * MICROS
        extractor.seekTo(range.startMs * MICROS, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        var base = -1L
        var count = 0
        while (true) {
            buffer.clear()
            val read = extractor.readSampleData(buffer, 0)
            val time = extractor.sampleTime
            if (read < 0 || time < 0 || time > endUs) break
            if (base < 0) base = time
            val key = extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0
            info.set(0, read, maxOf(0L, time - base), if (key) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
            muxer.writeSampleData(track, buffer, info)
            count++
            extractor.advance()
        }
        return count > 0
    }

    private fun videoTrack(extractor: MediaExtractor): Int = (0 until extractor.trackCount).firstOrNull {
        extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME).orEmpty().startsWith("video/")
    } ?: -1

    private fun bufferSize(format: MediaFormat): Int {
        val declared = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) else 0
        return maxOf(declared, DEFAULT_BUFFER)
    }

    private fun number(retriever: MediaMetadataRetriever, key: Int): Long =
        retriever.extractMetadata(key)?.toLongOrNull() ?: 0L
}
