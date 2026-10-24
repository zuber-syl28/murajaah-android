package com.sylergy.murajaah.progress.model

enum class MasteryLevel(val englishLabel: String, val arabicLabel: String) {
    MASTERED("Mastered", "مُتقَن"),
    SOLID("Solid", "راسخ"),
    HOLDING("Holding", "في طور التثبيت"),
    REBUILD("Needs Consolidation", "بحاجة لتكرار")
}

data class MasterySummary(
    val value: Double?,
    val level: MasteryLevel?,
    val testedCount: Int,
    val untestedCount: Int,
    val totalAyahs: Int,
    val heardAyahs: Int,
    val heardFraction: Double,
    val strength: Double?
)

data class CycleStatus(
    val cycleDays: Int,
    val heardCards: Int,
    val totalCards: Int,
    val daysRemaining: Int,
    val progressFraction: Float
)

data class TathbeetItem(
    val cardId: String,
    val surah: Int,
    val surahName: String,
    val arabicName: String,
    val fromAyah: Int,
    val toAyah: Int,
    val status: String,
    val dueInDays: Int,
    val score: Double
)

data class SurahSegment(
    val fromAyah: Int,
    val toAyah: Int,
    val status: String, // "strong", "review", "weak", "untested"
    val startFraction: Float,
    val widthFraction: Float
)

data class SurahProgressItem(
    val surahNumber: Int,
    val surahName: String,
    val arabicName: String,
    val totalAyahsInSurah: Int,
    val memorizedAyahs: Int,
    val mastery: Double?,
    val segments: List<SurahSegment>
)

data class JuzCell(
    val juz: Int,
    val status: String?, // null = not memorized, "strong", "review", "weak", "untested"
    val cardCount: Int,
    val ayahCount: Int
)

data class TrendWeek(
    val weekOffset: Int,
    val label: String,
    val cardsReviewed: Int,
    val fraction: Float
)

data class ProgressDashboardState(
    val isLoading: Boolean = true,
    val mastery: MasterySummary = MasterySummary(
        value = null,
        level = null,
        testedCount = 0,
        untestedCount = 0,
        totalAyahs = 0,
        heardAyahs = 0,
        heardFraction = 0.0,
        strength = null
    ),
    val cycle: CycleStatus? = null,
    val totalAyahs: Int = 0,
    val tathbeetCount: Int = 0,
    val weeksRevised: Int = 0,
    val tathbeetList: List<TathbeetItem> = emptyList(),
    val surahs: List<SurahProgressItem> = emptyList(),
    val juzList: List<JuzCell> = emptyList(),
    val upcomingToday: Int = 0,
    val upcomingTomorrow: Int = 0,
    val upcomingWeek: Int = 0,
    val trend: List<TrendWeek> = emptyList()
)
