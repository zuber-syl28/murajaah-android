package com.sylergy.murajaah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import com.sylergy.murajaah.network.sync.SyncRepository
import com.sylergy.murajaah.network.sync.SyncResult
import com.sylergy.murajaah.scheduler.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val cardCount: Int = 0,
    val dueCount: Int = 0,
    val activePlan: RevisionPlanEntity? = null,
    val rangeCount: Int = 0,
    val pendingOutboxCount: Int = 0,
    val dueCompanions: List<FriendEntity> = emptyList()
)

class HomeViewModel(
    private val database: MurajaahDatabase,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val today: String = DateUtils.today()

    val uiState: StateFlow<HomeUiState> = combine(
        database.cardDao().observeCardCount(),
        database.cardDao().observeDueCards(today),
        database.revisionPlanDao().observeActivePlan(),
        database.memorizedRangeDao().observeRangeCount(),
        database.gradeOutboxDao().observePendingCount(),
        database.friendDao().observeAllFriends()
    ) { args: Array<Any?> ->
        val cardCount = args[0] as Int
        @Suppress("UNCHECKED_CAST")
        val dueCards = args[1] as List<CardEntity>
        val activePlan = args[2] as? RevisionPlanEntity
        val rangeCount = args[3] as Int
        val pendingOutbox = args[4] as Int
        @Suppress("UNCHECKED_CAST")
        val companions = args[5] as List<FriendEntity>

        val dueCount = if (activePlan != null) {
            dueCards.count { it.surah in activePlan.surahs }
        } else {
            dueCards.size
        }
        val dueComps = companions.filter { it.youReviewStatus == "active" && it.youReviewDueToday > 0 }
        HomeUiState(
            cardCount = cardCount,
            dueCount = dueCount,
            activePlan = activePlan,
            rangeCount = rangeCount,
            pendingOutboxCount = pendingOutbox,
            dueCompanions = dueComps
        )
    }.flowOn(Dispatchers.Default).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )

    fun seedHifz(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val mulkRange = MemorizedRangeEntity(
                id = "range-67",
                surah = 67,
                fromAyah = 1,
                toAyah = 30,
                surahName = "Al-Mulk",
                surahArabicName = "الملك",
                ayahsCount = 30,
                cardsCount = 3
            )
            val cards = listOf(
                CardEntity(
                    id = "card-mulk-1",
                    surah = 67,
                    fromAyah = 1,
                    toAyah = 10,
                    juz = 29,
                    page = 562,
                    status = "weak",
                    stability = 1.0,
                    difficulty = 6.4,
                    scheduledDays = 1,
                    dueAt = today,
                    reviewCount = 1,
                    firstAyahText = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ"
                ),
                CardEntity(
                    id = "card-mulk-2",
                    surah = 67,
                    fromAyah = 11,
                    toAyah = 20,
                    juz = 29,
                    page = 563,
                    status = "review",
                    stability = 4.0,
                    difficulty = 5.2,
                    scheduledDays = 4,
                    dueAt = today,
                    reviewCount = 2,
                    firstAyahText = "فَاعْتَرَفُوا بِذَنبِهِمْ"
                ),
                CardEntity(
                    id = "card-mulk-3",
                    surah = 67,
                    fromAyah = 21,
                    toAyah = 30,
                    juz = 29,
                    page = 564,
                    status = "strong",
                    stability = 14.0,
                    difficulty = 3.5,
                    scheduledDays = 14,
                    dueAt = DateUtils.addDays(today, 6),
                    reviewCount = 5,
                    firstAyahText = "أَمَّنْ هَذَا الَّذِي يَرْزُقُكُمْ"
                )
            )
            database.memorizedRangeDao().insertAll(listOf(mulkRange))
            database.cardDao().insertAll(cards)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun gradeOffline(onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val clientId = syncRepository.recordOfflineGrade(
                cardId = "card-mulk-1",
                rating = "strong",
                recordedAt = today,
                recallSeconds = 25,
                hintsCount = 0,
                surah = 67,
                fromAyah = 1,
                toAyah = 10
            )
            withContext(Dispatchers.Main) {
                onComplete(clientId)
            }
        }
    }

    fun triggerDeltaSync(onResult: (SyncResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = syncRepository.syncPendingGrades()
            withContext(Dispatchers.Main) {
                onResult(res)
            }
        }
    }
}
