package com.sylergy.murajaah.achievements.model

enum class AchievementRole(val code: String, val englishLabel: String, val arabicLabel: String) {
    SELF("self", "Your Revision", "مراجعتك"),
    STUDENT("student", "From Companions", "من أصحابك"),
    REVIEWER("reviewer", "For Companions", "لأصحابك")
}

data class AchievementDefinition(
    val key: String,
    val role: AchievementRole,
    val target: Int,
    val titleEn: String,
    val titleAr: String,
    val bodyEn: String,
    val bodyAr: String
)

data class AchievementItem(
    val key: String,
    val role: AchievementRole,
    val target: Int,
    val current: Int,
    val earned: Boolean,
    val earnedAt: String? = null,
    val titleEn: String,
    val titleAr: String,
    val bodyEn: String,
    val bodyAr: String
) {
    val progressFraction: Float
        get() = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
}

data class ReviewFacts(
    val reviews: Int = 0,
    val strong: Int = 0,
    val friends: Int = 0,
    val rebuilt: Int = 0,
    val weeks: Int = 0
)

data class AchievementFacts(
    val revisedReviews: Int = 0,
    val revisedWeeks: Int = 0,
    val revisedSurahs: Int = 0,
    val received: ReviewFacts = ReviewFacts(),
    val receivedSurahsHeard: Int = 0,
    val given: ReviewFacts = ReviewFacts()
)

data class AchievementsState(
    val isLoading: Boolean = true,
    val selfList: List<AchievementItem> = emptyList(),
    val studentList: List<AchievementItem> = emptyList(),
    val reviewerList: List<AchievementItem> = emptyList(),
    val earnedSelfCount: Int = 0,
    val totalSelfCount: Int = 0,
    val hasCompanionActivity: Boolean = false
)
