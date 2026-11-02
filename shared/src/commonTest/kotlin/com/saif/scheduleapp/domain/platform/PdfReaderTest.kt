package com.saif.scheduleapp.domain.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PdfReaderTest {

    private class FakePdfReader(
        override val isSupported: Boolean,
        private val stubText: String
    ) : PdfReader {
        override suspend fun extractText(bytes: ByteArray): String = stubText
    }

    @Test
    fun testPdfReaderIsSupportedAndDelegation() = runTest {
        val reader = FakePdfReader(isSupported = true, stubText = "Sample Extracted Text")
        assertTrue(reader.isSupported)
        assertEquals("Sample Extracted Text", reader.extractText(byteArrayOf(1, 2, 3)))
        assertEquals("Sample Extracted Text", reader.readText(byteArrayOf(1, 2, 3)))
    }
}

