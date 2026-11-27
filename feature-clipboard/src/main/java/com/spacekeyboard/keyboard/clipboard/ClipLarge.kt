package com.spacekeyboard.keyboard.clipboard

import android.content.ClipData
import android.content.Context
import android.os.Handler
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class ClipOffer(val data: ClipData, val key: String)

internal class ClipLarge(
    private val app: Context,
    private val worker: ExecutorService,
    private val main: Handler,
    private val onBusy: (ClipBusy?) -> Unit,
) {
    var blobs: ClipBlobs? = null

    @Volatile
    private var saving: Meter? = null

    @Volatile
    private var pasting: Meter? = null
    private val pump: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val tick = Runnable { report() }

    fun ingest(item: ClipData.Item, done: (Intake) -> Unit) {
        val store = blobs ?: return
        val text = item.text
        if (text != null && text.length <= ClipPolicy.MAX_TEXT) {
            done(ClipIngest.run(StringReader(text.toString()), store, 0L))
            return
        }
        val meter = Meter(BusyKind.SAVING, text?.length?.toLong() ?: 0L)
        saving = meter
        report()
        if (runCatching { worker.execute { finish(item, store, meter, done) } }.isFailure) settle(meter)
    }

    fun discardIfUnused(blob: String, book: ClipBook) {
        if (book.items.none { it.blob == blob }) blobs?.delete(blob)
    }

    fun offer(payload: ClipPayload): ClipOffer? {
        val blob = payload.blob ?: return textOffer(payload.text)
        if (payload.length <= ClipPolicy.DIRECT_COPY) return blobs?.readText(blob, ClipPolicy.DIRECT_COPY)?.let(::textOffer)
        val uri = ClipBlobProvider.uri(app, blob)
        return ClipOffer(ClipData.newUri(app.contentResolver, null, uri), uri.toString())
    }

    fun share(payload: ClipPayload): Boolean {
        val blob = payload.blob ?: return ClipTransfer.share(app, payload.text)
        if (payload.length > ClipPolicy.DIRECT_COPY) return ClipTransfer.shareUri(app, ClipBlobProvider.uri(app, blob))
        val text = blobs?.readText(blob, ClipPolicy.DIRECT_COPY) ?: return false
        return ClipTransfer.share(app, text)
    }

    fun stream(blob: String, total: Long, sink: (String) -> Unit, finished: (Boolean) -> Unit) {
        pasting?.stopped = true
        val meter = Meter(BusyKind.PASTING, total)
        pasting = meter
        report()
        val queued = runCatching { pump.execute { pour(blob, meter, sink, finished) } }
        if (queued.isFailure) {
            settle(meter)
            finished(false)
        }
    }

    fun halt(kind: BusyKind) {
        val meter = if (kind == BusyKind.PASTING) pasting else saving
        meter?.stopped = true
        report()
    }

    fun cancel() = halt(BusyKind.PASTING)

    fun detach() {
        pasting?.stopped = true
        saving?.stopped = true
        blobs = null
        report()
    }

    fun shutdown() {
        pasting?.stopped = true
        saving?.stopped = true
        main.removeCallbacks(tick)
        pump.shutdownNow()
    }

    private fun textOffer(text: String): ClipOffer = ClipOffer(ClipData.newPlainText(null, text), text)

    private fun report() {
        main.removeCallbacks(tick)
        val current = pasting?.takeIf { !it.stopped } ?: saving?.takeIf { !it.stopped }
        onBusy(current?.busy())
        if (current != null) main.postDelayed(tick, TICK_MS)
    }

    private fun settle(meter: Meter) {
        if (saving === meter) saving = null
        if (pasting === meter) pasting = null
        report()
    }

    private fun finish(item: ClipData.Item, store: ClipBlobs, meter: Meter, done: (Intake) -> Unit) {
        val source = open(item, meter)
        val result = if (source == null) Intake.Failed else ClipIngest.run(source, store, budget(), meter)
        main.post {
            settle(meter)
            done(result)
        }
    }

    private fun open(item: ClipData.Item, meter: Meter): Reader? {
        item.text?.let { return CountingReader(StringReader(it.toString()), meter) }
        val uri = item.uri ?: return null
        val resolver = app.contentResolver
        val typed = runCatching { resolver.openTypedAssetFileDescriptor(uri, "text/*", null) }.getOrNull()
        if (typed != null) meter.expected = typed.length.coerceAtLeast(0L)
        val stream = runCatching { typed?.createInputStream() ?: resolver.openInputStream(uri) }.getOrNull()
        return stream?.let { InputStreamReader(CountingStream(it, meter), Charsets.UTF_8) }
    }

    private fun budget(): Long =
        (app.filesDir.usableSpace - ClipPolicy.RESERVE_BYTES).coerceIn(0L, ClipPolicy.HARD_BYTES)

    private fun pour(blob: String, meter: Meter, sink: (String) -> Unit, finished: (Boolean) -> Unit) {
        val complete = runCatching { feed(blob, meter, sink) }.getOrDefault(false)
        main.post {
            settle(meter)
            finished(complete && !meter.stopped)
        }
    }

    private fun feed(blob: String, meter: Meter, sink: (String) -> Unit): Boolean {
        val source = blobs?.reader(blob) ?: return false
        source.use {
            val chunks = ClipChunks(it, ClipPolicy.CHUNK_CHARS)
            while (!meter.stopped) {
                val text = chunks.next() ?: return true
                val gate = CountDownLatch(1)
                main.post {
                    try {
                        if (!meter.stopped) {
                            sink(text)
                            meter.done += text.length
                        }
                    } finally {
                        gate.countDown()
                    }
                }
                gate.await()
            }
        }
        return false
    }

    private companion object {
        const val TICK_MS = 250L
    }
}
