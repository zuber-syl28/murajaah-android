package com.sylergy.murajaah.onboarding

import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import com.sylergy.murajaah.onboarding.model.OnboardingPresets
import com.sylergy.murajaah.quran.QuranData
import java.util.UUID

object OnboardingGenerator {

    suspend fun generateAndSeed(
        database: MurajaahDatabase,
        selectedJuzNumbers: Set<Int>,
        cardSize: String = "ten",
        today: String
    ): Int {
        val chunkSize = when (cardSize) {
            "five" -> 5
            "ten" -> 10
            else -> 10
        }

        val cardsToInsert = mutableListOf<CardEntity>()
        val rangesToInsert = mutableListOf<MemorizedRangeEntity>()
        val allSurahNumbers = mutableSetOf<Int>()

        val presets = OnboardingPresets.PRESETS.filter { it.juzNumber in selectedJuzNumbers }

        for (preset in presets) {
            for (surahNum in preset.fromSurah..preset.toSurah) {
                allSurahNumbers.add(surahNum)
                val surah = QuranData.SURAHS.find { it.number == surahNum } ?: continue
                val totalAyahs = surah.ayahCount

                var startAyah = 1
                var cardIndex = 0

                while (startAyah <= totalAyahs) {
                    var endAyah = minOf(startAyah + chunkSize - 1, totalAyahs)
                    // If remaining ayahs after this card would be <= 3, extend this card to include them
                    val remainingAfter = totalAyahs - endAyah
                    if (remainingAfter in 1..3) {
                        endAyah = totalAyahs
                    }

                    val cardId = "card_${surahNum}_${startAyah}_${endAyah}"
                    cardsToInsert.add(
                        CardEntity(
                            id = cardId,
                            surah = surahNum,
                            fromAyah = startAyah,
                            toAyah = endAyah,
                            juz = preset.juzNumber,
                            status = "learning",
                            stability = 2.0,
                            difficulty = 5.0,
                            scheduledDays = 0,
                            dueAt = today,
                            reviewCount = 0
                        )
                    )

                    cardIndex++
                    startAyah = endAyah + 1
                }

                rangesToInsert.add(
                    MemorizedRangeEntity(
                        id = "range_${surahNum}_1_${totalAyahs}",
                        surah = surahNum,
                        fromAyah = 1,
                        toAyah = totalAyahs,
                        surahName = surah.name,
                        surahArabicName = surah.arabicName,
                        ayahsCount = totalAyahs,
                        cardsCount = cardIndex
                    )
                )
            }
        }

        // Batch insert to database
        if (rangesToInsert.isNotEmpty()) {
            database.memorizedRangeDao().insertAll(rangesToInsert)
        }
        if (cardsToInsert.isNotEmpty()) {
            database.cardDao().insertAll(cardsToInsert)
        }

        // Insert or update active revision plan
        val plan = RevisionPlanEntity(
            id = "plan_primary",
            name = if (selectedJuzNumbers.size == 1) "Juz ${selectedJuzNumbers.first()} Plan" else "Murajaah Wird Plan",
            surahs = allSurahNumbers.sorted(),
            isActive = true
        )
        database.revisionPlanDao().insertAll(listOf(plan))

        return cardsToInsert.size
    }
}
