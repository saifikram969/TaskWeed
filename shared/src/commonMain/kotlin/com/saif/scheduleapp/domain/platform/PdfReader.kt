package com.saif.scheduleapp.domain.platform

interface PdfReader {
    val isSupported: Boolean
    suspend fun extractText(bytes: ByteArray): String
    suspend fun readText(bytes: ByteArray): String = extractText(bytes)
}
