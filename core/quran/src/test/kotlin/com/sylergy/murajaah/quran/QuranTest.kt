package com.sylergy.murajaah.quran

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuranTest {

    @Test
    fun verifySurahCountAndTotalAyahs() {
        assertEquals(114, QuranData.SURAHS.size)
        val sum = QuranData.SURAHS.sumOf { it.ayahCount }
        assertEquals(QuranData.TOTAL_AYAHS, sum)
        assertEquals(6236, sum)
    }

    @Test
    fun verifySurahNamesAndLookups() {
        val fatihah = QuranData.surah(1)
        assertNotNull(fatihah)
        assertEquals("الفاتحة", fatihah.arabicName)
        assertEquals("Al-Fātiḥah", fatihah.name)
        assertEquals(7, fatihah.ayahCount)

        val baqarah = QuranData.surah(2)
        assertNotNull(baqarah)
        assertEquals(286, baqarah.ayahCount)

        val nas = QuranData.surah(114)
        assertNotNull(nas)
        assertEquals(6, nas.ayahCount)
    }

    @Test
    fun verifyHizbSpansCoverage() {
        val allAhzab = QuranData.hizbSpans(1, 60)
        assertTrue(allAhzab.isNotEmpty())
        assertEquals(1, allAhzab.first().surah)
        assertEquals(1, allAhzab.first().fromAyah)
        assertEquals(114, allAhzab.last().surah)
        assertEquals(6, allAhzab.last().toAyah)

        val totalAyahsInAhzab = allAhzab.sumOf { it.count }
        assertEquals(6236, totalAyahsInAhzab)
    }

    @Test
    fun verifyNarrationsCounts() {
        assertEquals(6236, NarrationData.totalAyahsIn(Narration.HAFS))
        assertEquals(6214, NarrationData.totalAyahsIn(Narration.WARSH))
        assertEquals(6214, NarrationData.totalAyahsIn(Narration.QALOON))
        assertEquals(6217, NarrationData.totalAyahsIn(Narration.DOORI))
        assertEquals(6217, NarrationData.totalAyahsIn(Narration.SOOSI))
        assertEquals(6220, NarrationData.totalAyahsIn(Narration.BAZZI))
        assertEquals(6220, NarrationData.totalAyahsIn(Narration.QUMBUL))

        // Al-Baqarah has 285 ayahs in Warsh, not 286
        assertEquals(285, NarrationData.ayahCountIn(Narration.WARSH, 2))
        assertEquals(286, NarrationData.ayahCountIn(Narration.HAFS, 2))
    }

    @Test
    fun verifyUncoveredSpans() {
        val fullBaqarah = AyahSpan(2, 1, 286)
        val existing = listOf(AyahSpan(2, 1, 100), AyahSpan(2, 201, 286))
        val uncovered = QuranData.uncoveredSpans(fullBaqarah, existing)
        assertEquals(1, uncovered.size)
        assertEquals(2, uncovered[0].surah)
        assertEquals(101, uncovered[0].fromAyah)
        assertEquals(200, uncovered[0].toAyah)
    }
}
