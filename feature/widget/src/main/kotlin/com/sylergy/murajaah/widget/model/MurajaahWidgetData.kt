package com.sylergy.murajaah.widget.model

import android.content.Context
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.scheduler.DateUtils

data class MurajaahWidgetData(
    val dueCount: Int,
    val activePlanName: String,
    val nextSurahArabic: String,
    val nextSurahEnglish: String,
    val nextAyahRange: String,
    val isAllCaughtUp: Boolean,
    val totalMemorizedCards: Int
)

object MurajaahWidgetDataRepository {

    suspend fun loadData(context: Context): MurajaahWidgetData {
        val db = MurajaahDatabase.getInstance(context)
        val today = DateUtils.today()

        val activePlan = db.revisionPlanDao().getActivePlan()
        val allDue = db.cardDao().getDueCards(today)
        val allCards = db.cardDao().getAllCards()

        val filteredDue = if (activePlan != null) {
            allDue.filter { it.surah in activePlan.surahs }
        } else {
            allDue
        }

        val nextCard = filteredDue.firstOrNull() ?: allCards.firstOrNull()
        val nextSurah = if (nextCard != null) QuranData.surah(nextCard.surah) else null

        return MurajaahWidgetData(
            dueCount = filteredDue.size,
            activePlanName = activePlan?.name ?: "All Memorized · كل المحفوظ",
            nextSurahArabic = nextSurah?.arabicName ?: "",
            nextSurahEnglish = nextSurah?.name ?: "Al-Mulk",
            nextAyahRange = if (nextCard != null) "${nextCard.fromAyah}–${nextCard.toAyah}" else "1–10",
            isAllCaughtUp = filteredDue.isEmpty(),
            totalMemorizedCards = allCards.size
        )
    }
}
