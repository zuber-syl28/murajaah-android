package com.sylergy.murajaah.widget

import com.sylergy.murajaah.widget.model.MurajaahWidgetData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDataTest {

    @Test
    fun testWidgetDataDueCards() {
        val data = MurajaahWidgetData(
            dueCount = 5,
            activePlanName = "Juz' 30 (الجزء 30)",
            nextSurahArabic = "النبأ",
            nextSurahEnglish = "An-Nabaʾ",
            nextAyahRange = "1–10",
            isAllCaughtUp = false,
            totalMemorizedCards = 48
        )

        assertEquals(5, data.dueCount)
        assertEquals("Juz' 30 (الجزء 30)", data.activePlanName)
        assertEquals("النبأ", data.nextSurahArabic)
        assertEquals("An-Nabaʾ", data.nextSurahEnglish)
        assertEquals("1–10", data.nextAyahRange)
        assertFalse(data.isAllCaughtUp)
        assertEquals(48, data.totalMemorizedCards)
    }

    @Test
    fun testWidgetDataAllCaughtUp() {
        val data = MurajaahWidgetData(
            dueCount = 0,
            activePlanName = "All Memorized · كل المحفوظ",
            nextSurahArabic = "الملك",
            nextSurahEnglish = "Al-Mulk",
            nextAyahRange = "1–10",
            isAllCaughtUp = true,
            totalMemorizedCards = 30
        )

        assertEquals(0, data.dueCount)
        assertTrue(data.isAllCaughtUp)
        assertEquals("All Memorized · كل المحفوظ", data.activePlanName)
    }
}
