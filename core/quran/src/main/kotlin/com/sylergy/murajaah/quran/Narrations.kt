package com.sylergy.murajaah.quran

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

enum class Narration(val id: String, val displayName: String) {
    HAFS("hafs", "Ḥafṣ ʿan ʿĀṣim"),
    QALOON("qaloon", "Qālūn ʿan Nāfiʿ"),
    WARSH("warsh", "Warsh ʿan Nāfiʿ"),
    SHOUBA("shouba", "Shuʿbah ʿan ʿĀṣim"),
    DOORI("doori", "Ad-Dūrī ʿan Abī ʿAmr"),
    SOOSI("soosi", "As-Sūsī ʿan Abī ʿAmr"),
    BAZZI("bazzi", "Al-Bazzī ʿan Ibn Kathīr"),
    QUMBUL("qumbul", "Qunbul ʿan Ibn Kathīr");

    companion object {
        fun fromId(id: String): Narration =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HAFS
    }
}

object NarrationData {
    val COUNTS_QALOON: IntArray = intArrayOf(
        7, 285, 200, 175, 122, 167, 206, 76, 130, 109, 121, 111, 44, 54, 99, 128, 110, 105, 99, 134,
        111, 76, 119, 62, 77, 226, 95, 88, 69, 59, 33, 30, 73, 54, 46, 82, 182, 86, 72, 84,
        53, 50, 89, 56, 36, 34, 39, 29, 18, 45, 60, 47, 61, 55, 77, 99, 28, 21, 24, 13,
        14, 11, 11, 18, 12, 12, 31, 52, 52, 44, 30, 28, 18, 55, 39, 31, 50, 40, 45, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 32, 20, 15, 21, 11, 8, 8, 20, 5, 8, 9, 11,
        10, 8, 3, 9, 5, 5, 6, 3, 6, 3, 5, 4, 5, 6
    )

    val COUNTS_WARSH: IntArray = COUNTS_QALOON

    val COUNTS_SHOUBA: IntArray = intArrayOf(
        7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
        112, 78, 118, 64, 77, 227, 93, 88, 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
        54, 53, 89, 59, 37, 35, 38, 29, 18, 45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
        14, 11, 11, 18, 12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
        11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
    )

    val COUNTS_DOORI: IntArray = intArrayOf(
        7, 285, 200, 175, 122, 167, 206, 76, 130, 109, 122, 111, 44, 54, 99, 128, 110, 105, 98, 134,
        111, 76, 119, 62, 77, 227, 95, 88, 69, 60, 33, 30, 73, 54, 45, 82, 182, 86, 72, 84,
        53, 50, 89, 56, 36, 34, 39, 29, 18, 45, 60, 47, 61, 55, 77, 99, 28, 22, 24, 13,
        14, 11, 11, 18, 12, 12, 30, 52, 52, 44, 30, 28, 20, 56, 39, 31, 50, 40, 45, 42,
        29, 19, 36, 25, 22, 16, 19, 26, 32, 20, 16, 21, 11, 8, 8, 20, 5, 8, 8, 11,
        10, 8, 3, 9, 5, 5, 6, 3, 6, 3, 5, 4, 5, 6
    )

    val COUNTS_SOOSI: IntArray = COUNTS_DOORI

    val COUNTS_BAZZI: IntArray = intArrayOf(
        7, 285, 200, 175, 122, 167, 206, 76, 130, 109, 121, 111, 44, 54, 99, 128, 110, 105, 99, 134,
        111, 77, 119, 62, 77, 226, 95, 88, 69, 59, 33, 30, 73, 54, 45, 82, 182, 86, 72, 84,
        53, 50, 89, 56, 36, 34, 39, 29, 18, 45, 60, 47, 61, 55, 77, 99, 28, 21, 24, 13,
        14, 11, 11, 18, 12, 12, 31, 52, 52, 44, 30, 28, 20, 55, 39, 31, 50, 41, 45, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 32, 20, 15, 21, 11, 8, 8, 20, 6, 8, 9, 11,
        10, 8, 3, 9, 5, 5, 6, 3, 6, 3, 5, 5, 5, 7
    )

    val COUNTS_QUMBUL: IntArray = COUNTS_BAZZI

    fun ayahCountIn(narration: Narration, surahNumber: Int): Int? {
        if (surahNumber !in 1..114) return null
        return when (narration) {
            Narration.HAFS -> QuranData.surah(surahNumber)?.ayahCount
            Narration.QALOON -> COUNTS_QALOON[surahNumber - 1]
            Narration.WARSH -> COUNTS_WARSH[surahNumber - 1]
            Narration.SHOUBA -> COUNTS_SHOUBA[surahNumber - 1]
            Narration.DOORI -> COUNTS_DOORI[surahNumber - 1]
            Narration.SOOSI -> COUNTS_SOOSI[surahNumber - 1]
            Narration.BAZZI -> COUNTS_BAZZI[surahNumber - 1]
            Narration.QUMBUL -> COUNTS_QUMBUL[surahNumber - 1]
        }
    }

    fun totalAyahsIn(narration: Narration): Int {
        return when (narration) {
            Narration.HAFS -> QuranData.TOTAL_AYAHS
            Narration.QALOON, Narration.WARSH -> 6214
            Narration.SHOUBA -> 6236
            Narration.DOORI, Narration.SOOSI -> 6217
            Narration.BAZZI, Narration.QUMBUL -> 6220
        }
    }

    fun sameNumbering(a: Narration, b: Narration): Boolean {
        if (a == b) return true
        for (n in 1..114) {
            if (ayahCountIn(a, n) != ayahCountIn(b, n)) return false
        }
        return true
    }

    fun narrationSurahSpans(narration: Narration, from: Int, to: Int): List<AyahSpan> {
        val spans = mutableListOf<AyahSpan>()
        val limit = min(to, 114)
        for (n in from..limit) {
            val count = ayahCountIn(narration, n) ?: continue
            spans.add(AyahSpan(n, 1, count))
        }
        return spans
    }

    fun narrationHizbSpans(narration: Narration, from: Int, to: Int): List<AyahSpan> {
        val spans = QuranData.hizbSpans(from, to)
        if (sameNumbering(Narration.HAFS, narration)) return spans

        return spans.map { span ->
            val hafsCount = ayahCountIn(Narration.HAFS, span.surah) ?: span.toAyah
            val ownCount = ayahCountIn(narration, span.surah) ?: hafsCount
            val endOf: (Int) -> Int = { ayah ->
                if (ayah >= hafsCount) ownCount
                else min(ownCount, max(1, round((ayah.toDouble() * ownCount) / hafsCount).toInt()))
            }
            val fromA = if (span.fromAyah == 1) 1 else min(ownCount, endOf(span.fromAyah - 1) + 1)
            val toA = max(fromA, endOf(span.toAyah))
            AyahSpan(span.surah, fromA, toA)
        }
    }
}
