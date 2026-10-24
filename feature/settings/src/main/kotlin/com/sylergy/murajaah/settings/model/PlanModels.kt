package com.sylergy.murajaah.settings.model

import com.sylergy.murajaah.quran.AyahSpan
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.quran.NarrationData
import com.sylergy.murajaah.quran.QuranData
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object PlanRules {
    const val TOTAL_JUZ = 30

    /**
     * Surahs a juz touches.
     * Starts at hizb 2n - 1, ends before hizb 2n + 1 starts.
     */
    fun juzSurahs(juz: Int): List<Int> {
        if (juz !in 1..TOTAL_JUZ) return emptyList()
        val start = QuranData.hizbStart(juz * 2 - 1) ?: return emptyList()
        val next = if (juz * 2 + 1 <= QuranData.HIZB_COUNT) QuranData.hizbStart(juz * 2 + 1) else Pair(115, 1)
        val last = if (next != null && next.second == 1) next.first - 1 else next?.first ?: 114
        return (start.first..min(114, last)).toList()
    }

    /**
     * Whether a juz starts and ends on clean surah boundaries (e.g. Juz 14, 17, 28, 29, 30).
     */
    fun juzIsWholeSurahs(juz: Int): Boolean {
        if (juz !in 1..TOTAL_JUZ) return false
        val start = QuranData.hizbStart(juz * 2 - 1) ?: return false
        val next = if (juz * 2 + 1 <= QuranData.HIZB_COUNT) QuranData.hizbStart(juz * 2 + 1) else null
        val startsClean = start.second == 1
        val endsClean = next == null || next.second == 1
        return startsClean && endsClean
    }

    /**
     * Whether a card with the given surah belongs to the plan.
     * null surahs indicates "All memorized" (no active filter).
     */
    fun inPlan(surah: Int, planSurahs: List<Int>?): Boolean {
        return planSurahs == null || surah in planSurahs
    }
}

data class NarrationMetadata(
    val narration: Narration,
    val arabicName: String,
    val englishName: String,
    val school: String,
    val totalAyahs: Int
)

object NarrationRegistry {
    val ALL: List<NarrationMetadata> = listOf(
        NarrationMetadata(
            narration = Narration.HAFS,
            arabicName = "حفص عن عاصم",
            englishName = "Ḥafṣ ʿan ʿĀṣim",
            school = "Kūfah (الكوفة)",
            totalAyahs = 6236
        ),
        NarrationMetadata(
            narration = Narration.QALOON,
            arabicName = "قالون عن نافع",
            englishName = "Qālūn ʿan Nāfiʿ",
            school = "Madīnah (المدينة)",
            totalAyahs = 6214
        ),
        NarrationMetadata(
            narration = Narration.WARSH,
            arabicName = "ورش عن نافع",
            englishName = "Warsh ʿan Nāfiʿ",
            school = "Madīnah (المدينة)",
            totalAyahs = 6214
        ),
        NarrationMetadata(
            narration = Narration.SHOUBA,
            arabicName = "شعبة عن عاصم",
            englishName = "Shuʿbah ʿan ʿĀṣim",
            school = "Kūfah (الكوفة)",
            totalAyahs = 6236
        ),
        NarrationMetadata(
            narration = Narration.DOORI,
            arabicName = "الدوري عن أبي عمرو",
            englishName = "Ad-Dūrī ʿan Abī ʿAmr",
            school = "Baṣrah (البصرة)",
            totalAyahs = 6217
        ),
        NarrationMetadata(
            narration = Narration.SOOSI,
            arabicName = "السوسي عن أبي عمرو",
            englishName = "As-Sūsī ʿan Abī ʿAmr",
            school = "Baṣrah (البصرة)",
            totalAyahs = 6217
        ),
        NarrationMetadata(
            narration = Narration.BAZZI,
            arabicName = "البزي عن ابن كثير",
            englishName = "Al-Bazzī ʿan Ibn Kathīr",
            school = "Makkah (مكة)",
            totalAyahs = 6220
        ),
        NarrationMetadata(
            narration = Narration.QUMBUL,
            arabicName = "قنبل عن ابن كثير",
            englishName = "Qunbul ʿan Ibn Kathīr",
            school = "Makkah (مكة)",
            totalAyahs = 6220
        )
    )

    fun get(narration: Narration): NarrationMetadata =
        ALL.firstOrNull { it.narration == narration } ?: ALL[0]
}

sealed interface NarrationRangeCarry {
    val surah: Int
    val fromAyah: Int
    val toAyah: Int

    data class Direct(
        override val surah: Int,
        override val fromAyah: Int,
        override val toAyah: Int
    ) : NarrationRangeCarry

    data class WholeSurah(
        override val surah: Int,
        override val fromAyah: Int,
        override val toAyah: Int
    ) : NarrationRangeCarry

    data class Confirm(
        override val surah: Int,
        override val fromAyah: Int,
        override val toAyah: Int,
        val proposedToAyah: Int
    ) : NarrationRangeCarry
}

fun calculateNarrationCarry(
    fromNarration: Narration,
    toNarration: Narration,
    spans: List<AyahSpan>
): List<NarrationRangeCarry> {
    if (fromNarration == toNarration || NarrationData.sameNumbering(fromNarration, toNarration)) {
        return spans.map { NarrationRangeCarry.Direct(it.surah, it.fromAyah, it.toAyah) }
    }

    return spans.map { span ->
        val fromTotal = NarrationData.ayahCountIn(fromNarration, span.surah) ?: span.toAyah
        val toTotal = NarrationData.ayahCountIn(toNarration, span.surah) ?: fromTotal

        if (span.fromAyah == 1 && span.toAyah >= fromTotal) {
            NarrationRangeCarry.WholeSurah(span.surah, 1, toTotal)
        } else if (fromTotal == toTotal) {
            NarrationRangeCarry.Direct(span.surah, span.fromAyah, min(span.toAyah, toTotal))
        } else {
            val adjustedTo = min(toTotal, max(span.fromAyah, round(span.toAyah.toDouble() * toTotal / fromTotal).toInt()))
            NarrationRangeCarry.Confirm(
                surah = span.surah,
                fromAyah = span.fromAyah,
                toAyah = span.toAyah,
                proposedToAyah = adjustedTo
            )
        }
    }
}
