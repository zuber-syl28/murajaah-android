package com.sylergy.murajaah.onboarding.model

import com.sylergy.murajaah.quran.Narration

enum class OnboardingStep {
    WELCOME,
    NARRATION_AND_SIZE,
    MEMORIZED_RANGES,
    GOAL_AND_STYLE
}

data class JuzPreset(
    val juzNumber: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val surahsSummary: String,
    val fromSurah: Int,
    val toSurah: Int,
    val totalAyahs: Int
)

object OnboardingPresets {
    val PRESETS = listOf(
        JuzPreset(
            juzNumber = 30,
            nameArabic = "جزء عمّ",
            nameEnglish = "Juz 30 (Juz 'Amma)",
            surahsSummary = "An-Naba' (78) – An-Nas (114) · 37 Surahs",
            fromSurah = 78,
            toSurah = 114,
            totalAyahs = 564
        ),
        JuzPreset(
            juzNumber = 29,
            nameArabic = "جزء تبارك",
            nameEnglish = "Juz 29 (Juz Tabārak)",
            surahsSummary = "Al-Mulk (67) – Al-Mursalat (77) · 11 Surahs",
            fromSurah = 67,
            toSurah = 77,
            totalAyahs = 431
        ),
        JuzPreset(
            juzNumber = 28,
            nameArabic = "جزء قد سمع",
            nameEnglish = "Juz 28 (Qad Sami'a)",
            surahsSummary = "Al-Mujadila (58) – At-Tahrim (66) · 9 Surahs",
            fromSurah = 58,
            toSurah = 66,
            totalAyahs = 137
        ),
        JuzPreset(
            juzNumber = 1,
            nameArabic = "الجزء الأول",
            nameEnglish = "Juz 1 (Al-Fatihah & Al-Baqarah 1-141)",
            surahsSummary = "Al-Fatihah (1) – Al-Baqarah (141)",
            fromSurah = 1,
            toSurah = 2,
            totalAyahs = 148
        )
    )

    val NARRATIONS = listOf(
        Narration.HAFS to "Ḥafṣ 'an 'Āṣim (Most widely recited)",
        Narration.WARSH to "Warsh 'an Nāfi' (North & West Africa)",
        Narration.QALOON to "Qālūn 'an Nāfi' (Libya & Tunisia)",
        Narration.DOORI to "Ad-Dūrī 'an Abī 'Amr (Sudan & Levant)",
        Narration.SHOUBA to "Shu'bah 'an 'Āṣim",
        Narration.SOOSI to "As-Sūsī 'an Abī 'Amr",
        Narration.BAZZI to "Al-Bazzī 'an Ibn Kathīr",
        Narration.QUMBUL to "Qunbul 'an Ibn Kathīr"
    )
}
