package com.saif.scheduleapp.platform

import com.saif.scheduleapp.domain.platform.PdfReader
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDocument

class IosPdfReader : PdfReader {
    override val isSupported: Boolean = true

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun extractText(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        val nsData: NSData = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }

        val document = PDFDocument(data = nsData)

        // Extract full string if available directly from the document
        val fullString = document.string
        if (!fullString.isNullOrBlank()) {
            return fullString
        }

        // Fallback: extract page by page
        val pageCount = document.pageCount.toInt()
        val stringBuilder = StringBuilder()
        for (i in 0 until pageCount) {
            val page = document.pageAtIndex(i.toULong())
            page?.string?.let { pageText ->
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append("\n")
                }
                stringBuilder.append(pageText)
            }
        }
        return stringBuilder.toString()
    }
}
