package com.sylergy.murajaah.onboarding

import com.sylergy.murajaah.onboarding.model.OnboardingPresets
import com.sylergy.murajaah.onboarding.model.OnboardingStep
import com.sylergy.murajaah.quran.QuranData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingTest {

    @Test
    fun testOnboardingPresetsValidity() {
        assertEquals(4, OnboardingPresets.PRESETS.size)

        val juz30 = OnboardingPresets.PRESETS.first { it.juzNumber == 30 }
        assertEquals(78, juz30.fromSurah)
        assertEquals(114, juz30.toSurah)

        val juz29 = OnboardingPresets.PRESETS.first { it.juzNumber == 29 }
        assertEquals(67, juz29.fromSurah)
        assertEquals(77, juz29.toSurah)
    }

    @Test
    fun testChunkingCardCalculation() {
        // Test Al-Mulk (Surah 67, 30 ayahs): 3 cards of 10 ayahs
        val mulk = QuranData.SURAHS.first { it.number == 67 }
        assertEquals(30, mulk.ayahCount)

        val chunkSize = 10
        val cards = mutableListOf<Pair<Int, Int>>()
        var startAyah = 1
        while (startAyah <= mulk.ayahCount) {
            var endAyah = minOf(startAyah + chunkSize - 1, mulk.ayahCount)
            val remainingAfter = mulk.ayahCount - endAyah
            if (remainingAfter in 1..3) {
                endAyah = mulk.ayahCount
            }
            cards.add(startAyah to endAyah)
            startAyah = endAyah + 1
        }

        assertEquals(3, cards.size)
        assertEquals(1 to 10, cards[0])
        assertEquals(11 to 20, cards[1])
        assertEquals(21 to 30, cards[2])
    }

    @Test
    fun testShortSurahSmallRemainderMerge() {
        // Al-Infitar (Surah 82, 19 ayahs):
        // 1..10 -> 9 remaining (>3) -> 11..19 -> total 2 cards
        val infitar = QuranData.SURAHS.first { it.number == 82 }
        assertEquals(19, infitar.ayahCount)

        val chunkSize = 10
        val cards = mutableListOf<Pair<Int, Int>>()
        var startAyah = 1
        while (startAyah <= infitar.ayahCount) {
            var endAyah = minOf(startAyah + chunkSize - 1, infitar.ayahCount)
            val remainingAfter = infitar.ayahCount - endAyah
            if (remainingAfter in 1..3) {
                endAyah = infitar.ayahCount
            }
            cards.add(startAyah to endAyah)
            startAyah = endAyah + 1
        }

        assertEquals(2, cards.size)
        assertEquals(1 to 10, cards[0])
        assertEquals(11 to 19, cards[1])
    }
}
