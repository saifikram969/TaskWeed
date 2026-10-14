package com.saif.scheduleapp.domain.platform

interface PdfReader {
    suspend fun extractText(bytes: ByteArray): String
}
