package com.saif.scheduleapp.platform

import com.saif.scheduleapp.domain.platform.PdfReader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class AndroidPdfReader : PdfReader {
    override val isSupported: Boolean = true

    override suspend fun extractText(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        var document: PDDocument? = null
        try {
            document = PDDocument.load(ByteArrayInputStream(bytes))
            val stripper = PDFTextStripper()
            stripper.getText(document) ?: ""
        } finally {
            document?.close()
        }
    }
}

