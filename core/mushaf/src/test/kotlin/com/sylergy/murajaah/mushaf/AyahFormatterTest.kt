package com.sylergy.murajaah.mushaf

import com.sylergy.murajaah.quran.Narration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AyahFormatterTest {

    @Test
    fun testArabicDigitsConversion() {
        assertEquals("٠", AyahFormatter.toArabicDigits(0))
        assertEquals("١", AyahFormatter.toArabicDigits(1))
        assertEquals("١٠", AyahFormatter.toArabicDigits(10))
        assertEquals("٦٧", AyahFormatter.toArabicDigits(67))
        assertEquals("١١٤", AyahFormatter.toArabicDigits(114))
    }

    @Test
    fun testAyahMarkerFormatting() {
        assertEquals("\uFD3E١\uFD3F", AyahFormatter.formatAyahMarker(1))
        assertEquals("\uFD3E٣٠\uFD3F", AyahFormatter.formatAyahMarker(30))
    }

    @Test
    fun testBasmalaHeaderRules() {
        // Al-Fatihah (1) includes Basmala as verse 1, so separate header is false
        assertFalse(AyahFormatter.hasBasmalaHeader(1, 1))

        // At-Tawbah (9) has no Basmala
        assertFalse(AyahFormatter.hasBasmalaHeader(9, 1))

        // Al-Mulk (67) verse 1 starts with Basmala
        assertTrue(AyahFormatter.hasBasmalaHeader(67, 1))

        // Al-Mulk verse 11 does not have Basmala header
        assertFalse(AyahFormatter.hasBasmalaHeader(67, 11))
    }

    @Test
    fun testBuiltinMushafRepository() {
        val fatihah = MushafRepository.getPassageText(1, 1, 7, Narration.HAFS)
        assertEquals(7, fatihah.size)
        assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", fatihah[0].text)

        val mulkFirst10 = MushafRepository.getPassageText(67, 1, 10, Narration.HAFS)
        assertEquals(10, mulkFirst10.size)
        assertEquals(1, mulkFirst10.first().number)
        assertEquals(10, mulkFirst10.last().number)
    }
}
