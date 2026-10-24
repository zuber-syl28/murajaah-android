package com.sylergy.murajaah.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.toDomain
import com.sylergy.murajaah.database.toEntity
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.network.sync.SyncRepository
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.scheduler.Card
import com.sylergy.murajaah.scheduler.DateUtils
import com.sylergy.murajaah.scheduler.MurajaahScheduler
import com.sylergy.murajaah.scheduler.ReviewerRole
import com.sylergy.murajaah.session.model.RecitationAttempt
import com.sylergy.murajaah.session.model.SessionCard
import com.sylergy.murajaah.session.model.SessionPhase
import com.sylergy.murajaah.session.model.SessionRules
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SessionViewModel(
    private val database: MurajaahDatabase,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val queue = mutableListOf<SessionCard>()
    private val completedHistory = mutableListOf<Pair<SessionCard, RecitationAttempt>>()

    private val _phase = kotlinx.coroutines.flow.MutableStateFlow<SessionPhase?>(null)
    val phase: kotlinx.coroutines.flow.StateFlow<SessionPhase?> = _phase.asStateFlow()

    private val _elapsedSeconds = kotlinx.coroutines.flow.MutableStateFlow(0)
    val elapsedSeconds: kotlinx.coroutines.flow.StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private var activeAttempt: RecitationAttempt? = null
    private var timerJob: Job? = null

    fun startSession(initialCards: List<Card>? = null) {
        viewModelScope.launch {
            queue.clear()
            completedHistory.clear()

            val cardsToUse = if (!initialCards.isNullOrEmpty()) {
                initialCards
            } else {
                val activePlan = database.revisionPlanDao().getActivePlan()
                val dueEntities = database.cardDao().getDueCards(DateUtils.today())
                val filteredDue = if (activePlan != null) {
                    dueEntities.filter { it.surah in activePlan.surahs }
                } else {
                    dueEntities
                }

                if (filteredDue.isNotEmpty()) {
                    filteredDue.map { it.toDomain() }
                } else {
                    val allCards = database.cardDao().getAllCards()
                    val filteredAll = if (activePlan != null) {
                        allCards.filter { it.surah in activePlan.surahs }
                    } else {
                        allCards
                    }
                    filteredAll.take(3).map { it.toDomain() }
                }
            }

            for (c in cardsToUse) {
                val meta = QuranData.surah(c.surah)
                queue.add(
                    SessionCard(
                        card = c,
                        surahName = meta?.name ?: "Surah ${c.surah}",
                        surahArabicName = meta?.arabicName ?: "",
                        juz = 29,
                        page = 562
                    )
                )
            }

            advanceToNextCard()
        }
    }

    private fun advanceToNextCard() {
        if (queue.isEmpty()) {
            showSummary()
            return
        }

        val nextCard = queue.removeAt(0)
        val attempt = RecitationAttempt(cardId = nextCard.card.id)
        activeAttempt = attempt

        _elapsedSeconds.value = 0
        startTimer()
        _phase.value = SessionPhase.Prompt(nextCard, hintsUsed = 0)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _elapsedSeconds.value += 1
                activeAttempt = activeAttempt?.copy(elapsedSeconds = _elapsedSeconds.value)
            }
        }
    }

    fun onNextHint() {
        val currentPhase = _phase.value as? SessionPhase.Prompt ?: return
        if (currentPhase.hintsUsed < SessionRules.MAX_HINTS) {
            val nextHints = currentPhase.hintsUsed + 1
            activeAttempt = activeAttempt?.copy(hintsUsed = nextHints)
            _phase.value = SessionPhase.Prompt(currentPhase.card, hintsUsed = nextHints)
        }
    }

    fun onCheck() {
        timerJob?.cancel()
        val currentPhase = _phase.value as? SessionPhase.Prompt ?: return
        val currentElapsed = _elapsedSeconds.value
        val attempt = (activeAttempt ?: RecitationAttempt(cardId = currentPhase.card.card.id))
            .copy(elapsedSeconds = currentElapsed, hintsUsed = currentPhase.hintsUsed)
        activeAttempt = attempt
        _phase.value = SessionPhase.InspectMushaf(currentPhase.card, attempt)
    }

    fun onToggleSlippedAyah(ayahNumber: Int) {
        val currentPhase = _phase.value as? SessionPhase.InspectMushaf ?: return
        val attempt = currentPhase.attempt
        val updated = attempt.slippedAyahs.toMutableSet()
        if (updated.contains(ayahNumber)) {
            updated.remove(ayahNumber)
        } else {
            updated.add(ayahNumber)
        }
        val newAttempt = attempt.copy(slippedAyahs = updated)
        activeAttempt = newAttempt
        _phase.value = SessionPhase.InspectMushaf(currentPhase.card, newAttempt)
    }

    fun onSelectGrade(rating: Rating) {
        val currentPhase = _phase.value as? SessionPhase.InspectMushaf ?: return
        val sessionCard = currentPhase.card
        val attempt = currentPhase.attempt.copy(selectedGrade = rating)
        activeAttempt = attempt

        viewModelScope.launch {
            val today = DateUtils.today()
            val allCards = database.cardDao().getAllCards().map { it.toDomain() }
            val cycleDays = MurajaahScheduler.revisionCycleDays(allCards)

            // Compute next schedule via FSRS-6 Reviewers engine
            val weighted = com.sylergy.murajaah.scheduler.Reviewers.applyWeightedRating(
                card = sessionCard.card,
                rating = rating,
                role = ReviewerRole.Self,
                history = emptyList(),
                on = today,
                cycleDays = cycleDays
            )

            // Update Card in Room
            val updatedCard = weighted.card
            database.cardDao().update(updatedCard.toEntity(juz = sessionCard.juz, page = sessionCard.page))

            // Enqueue grade to offline outbox
            syncRepository.recordOfflineGrade(
                cardId = updatedCard.id,
                rating = when (rating) {
                    Rating.Weak -> "weak"
                    Rating.Review -> "review"
                    Rating.Strong -> "strong"
                },
                recordedAt = today,
                recallSeconds = attempt.elapsedSeconds,
                hintsCount = attempt.hintsUsed,
                slippedAyahs = attempt.slippedAyahs.toList(),
                surah = updatedCard.surah,
                fromAyah = updatedCard.fromAyah,
                toAyah = updatedCard.toAyah
            )

            completedHistory.add(sessionCard to attempt)
            advanceToNextCard()
        }
    }

    fun onSkipCard() {
        timerJob?.cancel()
        val currentPhase = _phase.value as? SessionPhase.Prompt ?: return
        // Move card to back of queue
        queue.add(currentPhase.card)
        advanceToNextCard()
    }

    fun onExtendQueue(count: Int = 2) {
        viewModelScope.launch {
            val allCards = database.cardDao().getAllCards()
            val notInHistory = allCards.filter { entity ->
                completedHistory.none { it.first.card.id == entity.id }
            }.take(count).map { it.toDomain() }

            if (notInHistory.isNotEmpty()) {
                for (c in notInHistory) {
                    val meta = QuranData.surah(c.surah)
                    queue.add(
                        SessionCard(
                            card = c,
                            surahName = meta?.name ?: "Surah ${c.surah}",
                            surahArabicName = meta?.arabicName ?: ""
                        )
                    )
                }
                advanceToNextCard()
            }
        }
    }

    private fun showSummary() {
        timerJob?.cancel()
        val strongCount = completedHistory.count { it.second.selectedGrade == Rating.Strong }
        val reviewCount = completedHistory.count { it.second.selectedGrade == Rating.Review }
        val weakCount = completedHistory.count { it.second.selectedGrade == Rating.Weak }
        val totalAyahs = completedHistory.sumOf { it.first.ayahsCount }
        val totalSeconds = completedHistory.sumOf { it.second.elapsedSeconds }

        _phase.value = SessionPhase.Summary(
            completedCount = completedHistory.size,
            totalAyahs = totalAyahs,
            totalSeconds = totalSeconds,
            strongCount = strongCount,
            reviewCount = reviewCount,
            weakCount = weakCount,
            history = completedHistory.toList()
        )
    }
}
