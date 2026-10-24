package com.sylergy.murajaah.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.GradeOutboxEntity
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import com.sylergy.murajaah.progress.calculator.MasteryCalculator
import com.sylergy.murajaah.progress.model.CycleStatus
import com.sylergy.murajaah.progress.model.ProgressDashboardState
import com.sylergy.murajaah.progress.model.SurahProgressItem
import com.sylergy.murajaah.progress.model.TathbeetItem
import com.sylergy.murajaah.progress.model.TrendWeek
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.scheduler.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.max

class ProgressViewModel(
    private val database: MurajaahDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressDashboardState())
    val uiState: StateFlow<ProgressDashboardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                database.cardDao().observeAllCards(),
                database.gradeOutboxDao().observeAllGrades(),
                database.revisionPlanDao().observeActivePlan()
            ) { cards: List<CardEntity>, grades: List<GradeOutboxEntity>, plan: RevisionPlanEntity? ->
                computeDashboard(cards, grades, plan)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val cards = database.cardDao().getAllCards()
            val grades = database.gradeOutboxDao().getAllGrades()
            val plan = database.revisionPlanDao().getActivePlan()
            _uiState.value = computeDashboard(cards, grades, plan)
        }
    }

    private fun computeDashboard(
        cards: List<CardEntity>,
        grades: List<GradeOutboxEntity>,
        plan: RevisionPlanEntity?
    ): ProgressDashboardState {
        val today = DateUtils.today()
        val mastery = MasteryCalculator.computeMasterySummary(cards, today)
        val totalAyahs = cards.sumOf { MasteryCalculator.ayahCount(it) }

        // Tathbeet: passages graded weak or review (weakest first)
        val tathbeetList = cards
            .filter { MasteryCalculator.isTested(it) && it.status.lowercase() != "strong" }
            .sortedBy { card ->
                val score = MasteryCalculator.cardMastery(card, today)
                if (card.status.lowercase() == "weak") score else score + 10.0
            }
            .map { card ->
                val surahMeta = QuranData.surah(card.surah)
                TathbeetItem(
                    cardId = card.id,
                    surah = card.surah,
                    surahName = surahMeta?.name ?: "Surah ${card.surah}",
                    arabicName = surahMeta?.arabicName ?: "",
                    fromAyah = card.fromAyah,
                    toAyah = card.toAyah,
                    status = card.status.lowercase(),
                    dueInDays = DateUtils.daysBetween(today, card.dueAt),
                    score = MasteryCalculator.cardMastery(card, today)
                )
            }

        // Surah breakdown
        val surahCardsMap = cards.groupBy { it.surah }
        val surahItems = surahCardsMap.map { (surahNum, sCards) ->
            val meta = QuranData.surah(surahNum)
            val totalAyahsInSurah = meta?.ayahCount ?: sCards.maxOfOrNull { it.toAyah } ?: 1
            val memorizedAyahs = sCards.flatMap { (it.fromAyah..it.toAyah).toList() }.toSet().size
            val surahMastery = MasteryCalculator.overallMastery(sCards, today)
            val segments = MasteryCalculator.computeSurahSegments(sCards, totalAyahsInSurah)

            SurahProgressItem(
                surahNumber = surahNum,
                surahName = meta?.name ?: "Surah $surahNum",
                arabicName = meta?.arabicName ?: "",
                totalAyahsInSurah = totalAyahsInSurah,
                memorizedAyahs = memorizedAyahs,
                mastery = if (sCards.any { MasteryCalculator.isTested(it) }) surahMastery else null,
                segments = segments
            )
        }.sortedBy { it.surahNumber }

        // Juz grid
        val juzGrid = MasteryCalculator.computeJuzGrid(cards)

        // Upcoming forecast
        val tomorrow = DateUtils.addDays(today, 1)
        val weekEnd = DateUtils.addDays(today, 7)
        val upcomingToday = cards.count { it.dueAt <= today }
        val upcomingTomorrow = cards.count { it.dueAt == tomorrow }
        val upcomingWeek = cards.count { it.dueAt <= weekEnd }

        // Khatmah Revision Cycle
        val cycleDays = 14
        val cycleStartDate = DateUtils.addDays(today, -cycleDays)
        val heardInCycle = cards.count { card ->
            val lastReviewed = card.lastReviewedAt
            lastReviewed != null && lastReviewed >= cycleStartDate
        }
        val cycleStatus = if (cards.isNotEmpty()) {
            val fraction = (heardInCycle.toFloat() / cards.size.toFloat()).coerceIn(0f, 1f)
            CycleStatus(
                cycleDays = cycleDays,
                heardCards = heardInCycle,
                totalCards = cards.size,
                daysRemaining = cycleDays,
                progressFraction = fraction
            )
        } else null

        // Weekly consistency and trend (last 8 weeks)
        val trendWeeks = mutableListOf<TrendWeek>()
        for (w in 7 downTo 0) {
            val weekStartDays = -(w * 7)
            val weekEndDays = -(w * 7 - 6)
            val wStart = DateUtils.addDays(today, weekStartDays)
            val wEnd = DateUtils.addDays(today, weekEndDays)

            val count = grades.count {
                val d = it.recordedAt.take(10)
                d >= wStart && d <= wEnd
            }
            val label = if (w == 0) "Now" else "-$w"
            trendWeeks.add(TrendWeek(weekOffset = -w, label = label, cardsReviewed = count, fraction = 0f))
        }

        val maxReviews = max(1, trendWeeks.maxOf { it.cardsReviewed })
        val normalizedTrend = trendWeeks.map {
            it.copy(fraction = (it.cardsReviewed.toFloat() / maxReviews.toFloat()).coerceIn(0.08f, 1f))
        }

        val activeWeeks = normalizedTrend.count { it.cardsReviewed > 0 }

        return ProgressDashboardState(
            isLoading = false,
            mastery = mastery,
            cycle = cycleStatus,
            totalAyahs = totalAyahs,
            tathbeetCount = tathbeetList.size,
            weeksRevised = activeWeeks,
            tathbeetList = tathbeetList,
            surahs = surahItems,
            juzList = juzGrid,
            upcomingToday = upcomingToday,
            upcomingTomorrow = upcomingTomorrow,
            upcomingWeek = upcomingWeek,
            trend = normalizedTrend
        )
    }
}
