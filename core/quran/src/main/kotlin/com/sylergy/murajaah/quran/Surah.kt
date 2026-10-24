package com.sylergy.murajaah.quran

data class Surah(
    val number: Int,
    val arabicName: String,
    val name: String,
    val ayahCount: Int
)

data class AyahSpan(
    val surah: Int,
    val fromAyah: Int,
    val toAyah: Int
) {
    val count: Int get() = toAyah - fromAyah + 1
}
