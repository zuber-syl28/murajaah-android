package com.sylergy.murajaah.reviewing.model

import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.mushaf.Ayah
import com.sylergy.murajaah.scheduler.Consensus
import com.sylergy.murajaah.scheduler.GraderGrade
import com.sylergy.murajaah.scheduler.ReviewerRole
import com.sylergy.murajaah.scheduler.WeightedGrade

enum class SlipTag(val label: String, val arabicLabel: String) {
    FORGOT_START("Forgot start", "نسي البداية"),
    FORGOT_MIDDLE("Forgot middle", "نسي الوسط"),
    TASHKEEL("Tashkeel / Harakat", "تشكيل وحركات"),
    WAQF("Waqf / Pause", "وقف وابتداء"),
    TAJWEED("Tajweed", "أحكام التجويد")
}

data class ExaminerCard(
    val id: String,
    val surah: Int,
    val surahName: String,
    val surahArabicName: String,
    val fromAyah: Int,
    val toAyah: Int,
    val ayahs: List<Ayah>,
    val currentRating: Rating = Rating.Review,
    val stability: Double = 1.0,
    val pastAssessments: List<GraderGrade> = emptyList()
) {
    val ayahsCount: Int get() = toAyah - fromAyah + 1
}

enum class ExaminerPhase {
    OVERVIEW,
    LISTENING,
    SUMMARY
}

data class AttestedGradeOutcome(
    val card: ExaminerCard,
    val rating: Rating,
    val slipsCount: Int,
    val slipDetails: Map<Int, SlipTag?>,
    val inPerson: Boolean,
    val consensus: Consensus?,
    val weightedGrade: WeightedGrade,
    val recordedAt: String
)

data class ExaminerSessionState(
    val companionId: String,
    val companionName: String,
    val companionArabicName: String? = null,
    val companionRole: ReviewerRole = ReviewerRole.Peer,
    val dueCards: List<ExaminerCard> = emptyList(),
    val currentCardIndex: Int = 0,
    val phase: ExaminerPhase = ExaminerPhase.OVERVIEW,
    val slippedAyahs: Map<Int, SlipTag?> = emptyMap(),
    val activeTagAyah: Int? = null,
    val inPerson: Boolean = true,
    val selectedRating: Rating? = null,
    val elapsedSeconds: Int = 0,
    val isListeningActive: Boolean = true,
    val completedAssessments: List<AttestedGradeOutcome> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val currentCard: ExaminerCard?
        get() = dueCards.getOrNull(currentCardIndex)

    val isTeacher: Boolean
        get() = companionRole == ReviewerRole.Teacher

    val suggestedRating: Rating
        get() {
            val slips = slippedAyahs.size
            val card = currentCard ?: return Rating.Strong
            val allowedSlips = Math.max(1, card.ayahsCount / 5)
            return when {
                slips > allowedSlips -> Rating.Weak
                slips > 0 -> Rating.Review
                else -> Rating.Strong
            }
        }
}
