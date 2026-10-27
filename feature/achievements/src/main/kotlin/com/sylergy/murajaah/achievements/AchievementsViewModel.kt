package com.sylergy.murajaah.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.achievements.evaluator.AchievementEvaluator
import com.sylergy.murajaah.achievements.model.AchievementFacts
import com.sylergy.murajaah.achievements.model.AchievementItem
import com.sylergy.murajaah.achievements.model.AchievementRole
import com.sylergy.murajaah.achievements.model.AchievementsState
import com.sylergy.murajaah.achievements.model.ReviewFacts
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.AchievementEntity
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.GradeOutboxEntity
import com.sylergy.murajaah.scheduler.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val database: MurajaahDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsState())
    val uiState: StateFlow<AchievementsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                database.cardDao().observeAllCards(),
                database.gradeOutboxDao().observeAllGrades(),
                database.achievementDao().observeAllEarned()
            ) { cards: List<CardEntity>, grades: List<GradeOutboxEntity>, storedAchievements: List<AchievementEntity> ->
                computeAndPersistAchievements(cards, grades, storedAchievements)
            }.flowOn(Dispatchers.Default).collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun computeAndPersistAchievements(
        cards: List<CardEntity>,
        grades: List<GradeOutboxEntity>,
        storedList: List<AchievementEntity>
    ): AchievementsState {
        val today = DateUtils.today()
        val storedMap = storedList.associate { it.key to it.earnedAt }

        val testedCards = cards.filter { it.reviewCount > 0 }
        val distinctGradeDates = grades.map { it.recordedAt.take(10) }
        val distinctWeeksCount = AchievementEvaluator.distinctWeeks(distinctGradeDates)
        val surahsCoveredCount = AchievementEvaluator.surahsCovered(testedCards)
        val totalReviewsCount = maxOf(grades.size, cards.sumOf { it.reviewCount })

        // Check companion-based reviews (grades where examiner is not self)
        val companionHeardGrades = grades.filter { it.note?.contains("companion") == true }
        val companionGivenGrades = emptyList<GradeOutboxEntity>() // Populated when reviewing friends

        val facts = AchievementFacts(
            revisedReviews = totalReviewsCount,
            revisedWeeks = maxOf(if (totalReviewsCount > 0) 1 else 0, distinctWeeksCount),
            revisedSurahs = surahsCoveredCount,
            received = ReviewFacts(
                reviews = companionHeardGrades.size,
                strong = companionHeardGrades.count { it.rating.lowercase() == "strong" },
                friends = 0,
                rebuilt = 0,
                weeks = AchievementEvaluator.distinctWeeks(companionHeardGrades.map { it.recordedAt.take(10) })
            ),
            receivedSurahsHeard = 0,
            given = ReviewFacts(
                reviews = companionGivenGrades.size,
                strong = companionGivenGrades.count { it.rating.lowercase() == "strong" },
                friends = 0,
                rebuilt = 0,
                weeks = 0
            )
        )

        val evaluated = AchievementEvaluator.evaluate(facts, storedMap, today)

        // Persist newly earned achievements permanently
        val newlyEarned = evaluated.filter { it.earned && !storedMap.containsKey(it.key) }
        if (newlyEarned.isNotEmpty()) {
            val entities = newlyEarned.map {
                AchievementEntity(
                    key = it.key,
                    role = it.role.code,
                    earnedAt = today
                )
            }
            database.achievementDao().insertAll(entities)
        }

        // Group into roles: earned items first, then in-progress
        val selfItems = evaluated
            .filter { it.role == AchievementRole.SELF }
            .sortedByDescending { it.earned }
        val studentItems = evaluated
            .filter { it.role == AchievementRole.STUDENT }
            .sortedByDescending { it.earned }
        val reviewerItems = evaluated
            .filter { it.role == AchievementRole.REVIEWER }
            .sortedByDescending { it.earned }

        val earnedSelf = selfItems.count { it.earned }
        val hasCompanionActivity = facts.received.reviews > 0 || facts.given.reviews > 0

        return AchievementsState(
            isLoading = false,
            selfList = selfItems,
            studentList = studentItems,
            reviewerList = reviewerItems,
            earnedSelfCount = earnedSelf,
            totalSelfCount = selfItems.size,
            hasCompanionActivity = hasCompanionActivity
        )
    }
}
