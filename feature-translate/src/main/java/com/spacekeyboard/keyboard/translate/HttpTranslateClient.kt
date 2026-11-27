package com.spacekeyboard.keyboard.translate

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal class HttpTranslateClient(
    private val config: TranslateConfig,
    private val tokens: AppCheckTokens,
    private val installId: () -> String,
) {
    fun translate(request: TranslateRequest): TranslateOutcome {
        if (!config.configured) return TranslateOutcome.Failed(TranslateFailure.SERVER)
        val token = tokens.token() ?: return TranslateOutcome.Failed(TranslateFailure.REJECTED)
        return runCatching { post(request, token) }.getOrElse { TranslateOutcome.Failed(TranslateFailure.NETWORK) }
    }

    private fun post(request: TranslateRequest, token: String): TranslateOutcome {
        val connection = URL(config.url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = CONNECT_MS
            connection.readTimeout = READ_MS
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("X-Firebase-AppCheck", token)
            connection.setRequestProperty("X-Install-Id", installId())
            connection.outputStream.use { it.write(TranslateJson.encode(request).toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in HTTP_OK_RANGE) connection.inputStream else connection.errorStream
            return TranslateReply.from(status, stream?.use(::readLimited).orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private fun readLimited(stream: InputStream): String {
        val buffer = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(CHUNK_BYTES)
        while (buffer.size() < MAX_REPLY_BYTES) {
            val read = stream.read(chunk)
            if (read < 0) break
            buffer.write(chunk, 0, read)
        }
        return buffer.toString(Charsets.UTF_8.name())
    }

    private companion object {
        const val CONNECT_MS = 10_000
        const val READ_MS = 30_000
        const val MAX_REPLY_BYTES = 32_768
        const val CHUNK_BYTES = 4_096
        val HTTP_OK_RANGE = 200..299
    }
}
