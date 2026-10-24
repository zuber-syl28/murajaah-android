package com.sylergy.murajaah.mushaf

import com.sylergy.murajaah.quran.Narration

data class Ayah(
    val number: Int,
    val text: String,
    val juz: Int? = null,
    val page: Int? = null
)

data class SurahText(
    val surah: Int,
    val narration: Narration,
    val ayahs: List<Ayah>
)
