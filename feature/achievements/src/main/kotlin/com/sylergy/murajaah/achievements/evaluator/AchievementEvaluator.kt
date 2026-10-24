package com.sylergy.murajaah.achievements.evaluator

import com.sylergy.murajaah.achievements.model.AchievementDefinition
import com.sylergy.murajaah.achievements.model.AchievementFacts
import com.sylergy.murajaah.achievements.model.AchievementItem
import com.sylergy.murajaah.achievements.model.AchievementRole
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.GradeOutboxEntity
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.scheduler.DateUtils
import kotlin.math.max
import kotlin.math.min

object AchievementEvaluator {

    val DEFINITIONS = listOf(
        // SELF
        AchievementDefinition(
            key = "first_revision",
            role = AchievementRole.SELF,
            target = 1,
            titleEn = "First passage revised",
            titleAr = "أول مقطع روجع",
            bodyEn = "You recited a passage and it was graded — alone or with a companion.",
            bodyAr = "قرأت مقطعًا وقُيّم، وحدك أو مع صاحب."
        ),
        AchievementDefinition(
            key = "revised_50",
            role = AchievementRole.SELF,
            target = 50,
            titleEn = "50 recitations",
            titleAr = "٥٠ تلاوة",
            bodyEn = "Fifty recitations of your passages, graded.",
            bodyAr = "خمسون تلاوة من مقاطعك قُيّمت."
        ),
        AchievementDefinition(
            key = "surah_revised",
            role = AchievementRole.SELF,
            target = 1,
            titleEn = "A whole surah revised",
            titleAr = "سورة كاملة روجعت",
            bodyEn = "Every ayah of one surah, recited and graded.",
            bodyAr = "كل آيات سورة واحدة قرأتها وقُيّمت."
        ),
        AchievementDefinition(
            key = "revised_4_weeks",
            role = AchievementRole.SELF,
            target = 4,
            titleEn = "Revised in four weeks",
            titleAr = "مراجعة في أربعة أسابيع",
            bodyEn = "You revised in four different weeks — any four.",
            bodyAr = "راجعت في أربعة أسابيع مختلفة، أيًّا كانت."
        ),

        // STUDENT (From Companions)
        AchievementDefinition(
            key = "first_heard",
            role = AchievementRole.STUDENT,
            target = 1,
            titleEn = "First recitation heard",
            titleAr = "أول تلاوة مسموعة",
            bodyEn = "A companion listened to you recite and graded it.",
            bodyAr = "استمع صاحب لتلاوتك وقيّمها."
        ),
        AchievementDefinition(
            key = "heard_25",
            role = AchievementRole.STUDENT,
            target = 25,
            titleEn = "25 recitations heard",
            titleAr = "٢٥ تلاوة مسموعة",
            bodyEn = "Companions have heard you recite twenty-five times.",
            bodyAr = "سمعك أصحابك تقرأ خمسًا وعشرين مرة."
        ),
        AchievementDefinition(
            key = "heard_100",
            role = AchievementRole.STUDENT,
            target = 100,
            titleEn = "100 recitations heard",
            titleAr = "١٠٠ تلاوة مسموعة",
            bodyEn = "A hundred recitations, heard by companions.",
            bodyAr = "مئة تلاوة سمعها أصحابك."
        ),
        AchievementDefinition(
            key = "first_strong",
            role = AchievementRole.STUDENT,
            target = 1,
            titleEn = "First Strong from a companion",
            titleAr = "أول «متقن» من صاحب",
            bodyEn = "A companion heard a passage and marked it Strong.",
            bodyAr = "سمع صاحب مقطعًا منك وقيّمه متقنًا."
        ),
        AchievementDefinition(
            key = "rebuilt",
            role = AchievementRole.STUDENT,
            target = 1,
            titleEn = "A weak passage rebuilt",
            titleAr = "مقطع ضعيف أُعيد بناؤه",
            bodyEn = "A companion graded Strong a passage that was last Weak.",
            bodyAr = "قيّم صاحب متقنًا مقطعًا كان ضعيفًا في آخر تقييم."
        ),
        AchievementDefinition(
            key = "surah_heard",
            role = AchievementRole.STUDENT,
            target = 1,
            titleEn = "A whole surah heard",
            titleAr = "سورة كاملة مسموعة",
            bodyEn = "Companions have heard every ayah of one surah.",
            bodyAr = "سمع أصحابك كل آيات سورة واحدة."
        ),
        AchievementDefinition(
            key = "two_listeners",
            role = AchievementRole.STUDENT,
            target = 2,
            titleEn = "Two companions listening",
            titleAr = "صاحبان يستمعان",
            bodyEn = "Two different companions have heard you recite.",
            bodyAr = "استمع لتلاوتك صاحبان مختلفان."
        ),
        AchievementDefinition(
            key = "heard_4_weeks",
            role = AchievementRole.STUDENT,
            target = 4,
            titleEn = "Heard in four weeks",
            titleAr = "مسموع في أربعة أسابيع",
            bodyEn = "Companions listened to you in four different weeks — any four.",
            bodyAr = "استمع لك أصحابك في أربعة أسابيع مختلفة، أيًّا كانت."
        ),

        // REVIEWER (For Companions)
        AchievementDefinition(
            key = "first_review",
            role = AchievementRole.REVIEWER,
            target = 1,
            titleEn = "First review given",
            titleAr = "أول مراجعة قدّمتها",
            bodyEn = "You listened to a companion recite and graded it.",
            bodyAr = "استمعت لتلاوة صاحب وقيّمتها."
        ),
        AchievementDefinition(
            key = "reviews_25",
            role = AchievementRole.REVIEWER,
            target = 25,
            titleEn = "25 reviews given",
            titleAr = "٢٥ مراجعة",
            bodyEn = "You have heard twenty-five passages for your companions.",
            bodyAr = "سمعت خمسة وعشرين مقطعًا لأصحابك."
        ),
        AchievementDefinition(
            key = "reviews_100",
            role = AchievementRole.REVIEWER,
            target = 100,
            titleEn = "100 reviews given",
            titleAr = "١٠٠ مراجعة",
            bodyEn = "A hundred passages heard for your companions.",
            bodyAr = "مئة مقطع سمعتها لأصحابك."
        ),
        AchievementDefinition(
            key = "helped_two",
            role = AchievementRole.REVIEWER,
            target = 2,
            titleEn = "Helping two companions",
            titleAr = "تعين صاحبين",
            bodyEn = "You have listened to two different companions.",
            bodyAr = "استمعت لصاحبين مختلفين."
        ),
        AchievementDefinition(
            key = "helped_rebuild",
            role = AchievementRole.REVIEWER,
            target = 1,
            titleEn = "Helped rebuild a passage",
            titleAr = "أعنت على إعادة بناء مقطع",
            bodyEn = "You graded Strong a companion’s passage that was last Weak.",
            bodyAr = "قيّمت متقنًا مقطعًا لصاحب كان ضعيفًا في آخر تقييم."
        ),
        AchievementDefinition(
            key = "reviewed_4_weeks",
            role = AchievementRole.REVIEWER,
            target = 4,
            titleEn = "Listening in four weeks",
            titleAr = "استماع في أربعة أسابيع",
            bodyEn = "You listened for companions in four different weeks — any four.",
            bodyAr = "استمعت لأصحابك في أربعة أسابيع مختلفة، أيًّا كانت."
        )
    )

    fun evaluate(
        facts: AchievementFacts,
        storedEarned: Map<String, String>, // key -> earnedAt
        today: String
    ): List<AchievementItem> {
        return DEFINITIONS.map { def ->
            val previouslyEarnedAt = storedEarned[def.key]
            if (previouslyEarnedAt != null) {
                // Permanently earned! Never lossy.
                AchievementItem(
                    key = def.key,
                    role = def.role,
                    target = def.target,
                    current = def.target,
                    earned = true,
                    earnedAt = previouslyEarnedAt,
                    titleEn = def.titleEn,
                    titleAr = def.titleAr,
                    bodyEn = def.bodyEn,
                    bodyAr = def.bodyAr
                )
            } else {
                val value = measureFact(def.key, facts)
                val earnedNow = value >= def.target
                AchievementItem(
                    key = def.key,
                    role = def.role,
                    target = def.target,
                    current = min(value, def.target),
                    earned = earnedNow,
                    earnedAt = if (earnedNow) today else null,
                    titleEn = def.titleEn,
                    titleAr = def.titleAr,
                    bodyEn = def.bodyEn,
                    bodyAr = def.bodyAr
                )
            }
        }
    }

    private fun measureFact(key: String, f: AchievementFacts): Int {
        return when (key) {
            "first_revision", "revised_50" -> f.revisedReviews
            "surah_revised" -> f.revisedSurahs
            "revised_4_weeks" -> f.revisedWeeks
            "first_heard", "heard_25", "heard_100" -> f.received.reviews
            "first_strong" -> f.received.strong
            "rebuilt" -> f.received.rebuilt
            "surah_heard" -> f.receivedSurahsHeard
            "two_listeners" -> f.received.friends
            "heard_4_weeks" -> f.received.weeks
            "first_review", "reviews_25", "reviews_100" -> f.given.reviews
            "helped_two" -> f.given.friends
            "helped_rebuild" -> f.given.rebuilt
            "reviewed_4_weeks" -> f.given.weeks
            else -> 0
        }
    }

    fun distinctWeeks(dates: List<String>): Int {
        if (dates.isEmpty()) return 0
        // Group by calendar week (epoch day / 7)
        val weekSet = dates.mapNotNull { d ->
            val days = DateUtils.daysBetween("2000-01-01", d)
            days / 7
        }.toSet()
        return weekSet.size
    }

    fun surahsCovered(testedCards: List<CardEntity>): Int {
        val bySurah = testedCards.groupBy { it.surah }
        var coveredCount = 0

        for ((surahNum, cards) in bySurah) {
            val meta = QuranData.surah(surahNum) ?: continue
            val totalAyahs = meta.ayahCount
            val sorted = cards.sortedBy { it.fromAyah }

            var reached = 0
            for (card in sorted) {
                if (card.fromAyah > reached + 1) break
                reached = max(reached, card.toAyah)
            }

            if (reached >= totalAyahs) {
                coveredCount++
            }
        }
        return coveredCount
    }
}
