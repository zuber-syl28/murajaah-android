package com.sylergy.murajaah.settings

import com.sylergy.murajaah.quran.AyahSpan
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.settings.model.NarrationRangeCarry
import com.sylergy.murajaah.settings.model.NarrationRegistry
import com.sylergy.murajaah.settings.model.PlanRules
import com.sylergy.murajaah.settings.model.calculateNarrationCarry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanRulesTest {

    @Test
    fun testJuzSurahs() {
        val juz1 = PlanRules.juzSurahs(1)
        assertTrue(juz1.contains(1))
        assertTrue(juz1.contains(2))

        val juz30 = PlanRules.juzSurahs(30)
        assertEquals(78, juz30.first())
        assertEquals(114, juz30.last())
        assertEquals(37, juz30.size)

        // Invalid juz
        assertTrue(PlanRules.juzSurahs(0).isEmpty())
        assertTrue(PlanRules.juzSurahs(31).isEmpty())
    }

    @Test
    fun testJuzIsWholeSurahs() {
        assertTrue(PlanRules.juzIsWholeSurahs(30))
        assertTrue(PlanRules.juzIsWholeSurahs(29))
        assertTrue(PlanRules.juzIsWholeSurahs(28))
        assertFalse(PlanRules.juzIsWholeSurahs(1))
        assertFalse(PlanRules.juzIsWholeSurahs(2))
    }

    @Test
    fun testInPlan() {
        assertTrue(PlanRules.inPlan(2, null)) // All memorized
        assertTrue(PlanRules.inPlan(2, listOf(1, 2, 3)))
        assertFalse(PlanRules.inPlan(4, listOf(1, 2, 3)))
    }

    @Test
    fun testNarrationRegistry() {
        assertEquals(8, NarrationRegistry.ALL.size)
        val hafs = NarrationRegistry.get(Narration.HAFS)
        assertEquals("حفص عن عاصم", hafs.arabicName)
        assertEquals(6236, hafs.totalAyahs)

        val qaloon = NarrationRegistry.get(Narration.QALOON)
        assertEquals("قالون عن نافع", qaloon.arabicName)
        assertEquals(6214, qaloon.totalAyahs)
    }

    @Test
    fun testCalculateNarrationCarryWholeSurah() {
        // Hafs to Qaloon for whole Surah Al-Baqarah (286 ayahs in Hafs -> 285 in Qaloon)
        val span = AyahSpan(surah = 2, fromAyah = 1, toAyah = 286)
        val carry = calculateNarrationCarry(Narration.HAFS, Narration.QALOON, listOf(span))
        assertEquals(1, carry.size)
        assertTrue(carry[0] is NarrationRangeCarry.WholeSurah)
        val whole = carry[0] as NarrationRangeCarry.WholeSurah
        assertEquals(1, whole.fromAyah)
        assertEquals(285, whole.toAyah)
    }

    @Test
    fun testCalculateNarrationCarrySameSchool() {
        // Qaloon and Warsh have identical Madani numbering
        val span = AyahSpan(surah = 2, fromAyah = 1, toAyah = 100)
        val carry = calculateNarrationCarry(Narration.QALOON, Narration.WARSH, listOf(span))
        assertEquals(1, carry.size)
        assertTrue(carry[0] is NarrationRangeCarry.Direct)
    }
}
