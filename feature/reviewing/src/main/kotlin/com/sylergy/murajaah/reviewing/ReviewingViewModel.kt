package com.sylergy.murajaah.reviewing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.reviewing.model.AttestedGradeOutcome
import com.sylergy.murajaah.reviewing.model.ExaminerCard
import com.sylergy.murajaah.reviewing.model.ExaminerPhase
import com.sylergy.murajaah.reviewing.model.ExaminerSessionState
import com.sylergy.murajaah.reviewing.model.SlipTag
import com.sylergy.murajaah.scheduler.Card
import com.sylergy.murajaah.scheduler.ConsensusRules
import com.sylergy.murajaah.scheduler.DateUtils
import com.sylergy.murajaah.scheduler.GradeRecord
import com.sylergy.murajaah.scheduler.GraderGrade
import com.sylergy.murajaah.scheduler.ReviewerRole
import com.sylergy.murajaah.scheduler.Reviewers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewingViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        ExaminerSessionState(
            companionId = "c_bilal",
            companionName = "Bilal",
            companionArabicName = "بلال",
            companionRole = ReviewerRole.Teacher,
            dueCards = generateDefaultCards()
        )
    )
    val state: StateFlow<ExaminerSessionState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun initialize(
        companionId: String,
        companionName: String,
        companionArabicName: String? = null,
        role: ReviewerRole = ReviewerRole.Teacher
    ) {
        timerJob?.cancel()
        _state.value = ExaminerSessionState(
            companionId = companionId,
            companionName = companionName,
            companionArabicName = companionArabicName,
            companionRole = role,
            dueCards = generateCompanionCards(companionId)
        )
    }

    fun startListening() {
        _state.update {
            it.copy(
                phase = ExaminerPhase.LISTENING,
                elapsedSeconds = 0,
                isListeningActive = true,
                slippedAyahs = emptyMap(),
                selectedRating = null
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { current ->
                    if (current.phase == ExaminerPhase.LISTENING && current.isListeningActive) {
                        current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                    } else current
                }
            }
        }
    }

    fun toggleAyahSlip(ayahNumber: Int) {
        _state.update { current ->
            val existing = current.slippedAyahs
            if (existing.containsKey(ayahNumber)) {
                current.copy(slippedAyahs = existing - ayahNumber, activeTagAyah = null)
            } else {
                current.copy(
                    slippedAyahs = existing + (ayahNumber to null),
                    activeTagAyah = ayahNumber
                )
            }
        }
    }

    fun setSlipTag(ayahNumber: Int, tag: SlipTag?) {
        _state.update { current ->
            val updated = current.slippedAyahs.toMutableMap()
            updated[ayahNumber] = tag
            current.copy(slippedAyahs = updated, activeTagAyah = null)
        }
    }

    fun closeTagDialog() {
        _state.update { it.copy(activeTagAyah = null) }
    }

    fun selectRating(rating: Rating) {
        _state.update { it.copy(selectedRating = rating) }
    }

    fun toggleInPerson(inPerson: Boolean) {
        _state.update { it.copy(inPerson = inPerson) }
    }

    fun toggleTimerPause() {
        _state.update { it.copy(isListeningActive = !it.isListeningActive) }
    }

    fun submitGradeAndNext() {
        val current = _state.value
        val card = current.currentCard ?: return
        val rating = current.selectedRating ?: current.suggestedRating
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // 1. Convert examiner card to scheduler Card
        val schedulerCard = Card(
            id = card.id,
            surah = card.surah,
            fromAyah = card.fromAyah,
            toAyah = card.toAyah,
            status = card.currentRating,
            stability = card.stability,
            difficulty = null,
            scheduledDays = card.stability.toInt().coerceAtLeast(1),
            dueAt = today,
            lastReviewedAt = null,
            reviewCount = 1,
            gradedBy = current.companionRole
        )

        // 2. Prepare past grade records
        val historyRecords = card.pastAssessments.map {
            GradeRecord(
                rating = it.rating,
                role = it.role,
                recordedAt = it.recordedAt
            )
        }

        // 3. Apply examiner weighting
        val weightedGrade = Reviewers.applyWeightedRating(
            card = schedulerCard,
            rating = rating,
            role = current.companionRole,
            history = historyRecords,
            on = today
        )

        // 4. Calculate updated consensus
        val newGraderGrade = GraderGrade(
            grader = "examiner_me",
            graderName = "You (Examiner)",
            rating = rating,
            role = current.companionRole,
            recordedAt = today,
            inPerson = current.inPerson
        )
        val allGrades = listOf(newGraderGrade) + card.pastAssessments
        val consensus = ConsensusRules.calculate(allGrades, today)

        val outcome = AttestedGradeOutcome(
            card = card,
            rating = rating,
            slipsCount = current.slippedAyahs.size,
            slipDetails = current.slippedAyahs,
            inPerson = current.inPerson,
            consensus = consensus,
            weightedGrade = weightedGrade,
            recordedAt = today
        )

        val nextIndex = current.currentCardIndex + 1
        val isFinished = nextIndex >= current.dueCards.size

        if (isFinished) {
            timerJob?.cancel()
            _state.update {
                it.copy(
                    phase = ExaminerPhase.SUMMARY,
                    completedAssessments = it.completedAssessments + outcome,
                    slippedAyahs = emptyMap(),
                    selectedRating = null
                )
            }
        } else {
            _state.update {
                it.copy(
                    currentCardIndex = nextIndex,
                    completedAssessments = it.completedAssessments + outcome,
                    slippedAyahs = emptyMap(),
                    selectedRating = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun generateDefaultCards(): List<ExaminerCard> = listOf(
            ExaminerCard(
                id = "card_fatihah",
                surah = 1,
                surahName = "Al-Fatihah",
                surahArabicName = "الفاتحة",
                fromAyah = 1,
                toAyah = 7,
                ayahs = MushafRepository.getPassageText(1, 1, 7),
                currentRating = Rating.Review,
                stability = 2.0,
                pastAssessments = listOf(
                    GraderGrade("student", "Student", Rating.Review, ReviewerRole.Self, "2026-10-15")
                )
            ),
            ExaminerCard(
                id = "card_ikhlas",
                surah = 112,
                surahName = "Al-Ikhlas",
                surahArabicName = "الإخلاص",
                fromAyah = 1,
                toAyah = 4,
                ayahs = MushafRepository.getPassageText(112, 1, 4),
                currentRating = Rating.Strong,
                stability = 10.0,
                pastAssessments = listOf(
                    GraderGrade("student", "Student", Rating.Strong, ReviewerRole.Self, "2026-10-10"),
                    GraderGrade("c_ali", "Sheikh Ali", Rating.Strong, ReviewerRole.Teacher, "2026-10-01")
                )
            ),
            ExaminerCard(
                id = "card_falaq",
                surah = 113,
                surahName = "Al-Falaq",
                surahArabicName = "الفلق",
                fromAyah = 1,
                toAyah = 5,
                ayahs = MushafRepository.getPassageText(113, 1, 5),
                currentRating = Rating.Review,
                stability = 3.0,
                pastAssessments = emptyList()
            )
        )

        fun generateCompanionCards(companionId: String): List<ExaminerCard> {
            return when (companionId) {
                "c_tariq" -> listOf(
                    ExaminerCard(
                        id = "tariq_c1",
                        surah = 114,
                        surahName = "An-Nas",
                        surahArabicName = "الناس",
                        fromAyah = 1,
                        toAyah = 6,
                        ayahs = MushafRepository.getPassageText(114, 1, 6),
                        currentRating = Rating.Review,
                        stability = 3.0
                    ),
                    ExaminerCard(
                        id = "tariq_c2",
                        surah = 1,
                        surahName = "Al-Fatihah",
                        surahArabicName = "الفاتحة",
                        fromAyah = 1,
                        toAyah = 7,
                        ayahs = MushafRepository.getPassageText(1, 1, 7),
                        currentRating = Rating.Strong,
                        stability = 8.0
                    )
                )
                "c_ali" -> listOf(
                    ExaminerCard(
                        id = "ali_c1",
                        surah = 67,
                        surahName = "Al-Mulk",
                        surahArabicName = "الملك",
                        fromAyah = 1,
                        toAyah = 5,
                        ayahs = MushafRepository.getPassageText(67, 1, 5),
                        currentRating = Rating.Strong,
                        stability = 24.0,
                        pastAssessments = listOf(
                            GraderGrade("c_ali", "Sheikh Ali", Rating.Strong, ReviewerRole.Teacher, "2026-09-20")
                        )
                    )
                )
                else -> generateDefaultCards()
            }
        }
    }
}
