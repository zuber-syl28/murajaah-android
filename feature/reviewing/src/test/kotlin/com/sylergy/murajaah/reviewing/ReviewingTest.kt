package com.sylergy.murajaah.reviewing

import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.reviewing.model.ExaminerCard
import com.sylergy.murajaah.reviewing.model.ExaminerPhase
import com.sylergy.murajaah.reviewing.model.ExaminerSessionState
import com.sylergy.murajaah.reviewing.model.SlipTag
import com.sylergy.murajaah.scheduler.ReviewerRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewingTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun suggestedGradeReflectsSlipsCorrectly() {
        val testCard = ExaminerCard(
            id = "c1",
            surah = 1,
            surahName = "Al-Fatihah",
            surahArabicName = "الفاتحة",
            fromAyah = 1,
            toAyah = 7,
            ayahs = emptyList()
        )

        // 0 slips -> Strong
        val state0 = ExaminerSessionState(
            companionId = "c_bilal",
            companionName = "Bilal",
            dueCards = listOf(testCard),
            slippedAyahs = emptyMap()
        )
        assertEquals(Rating.Strong, state0.suggestedRating)

        // 1 slip -> Review
        val state1 = state0.copy(slippedAyahs = mapOf(2 to SlipTag.TASHKEEL))
        assertEquals(Rating.Review, state1.suggestedRating)

        // Multiple slips (> card.ayahsCount / 5 = 1) -> Weak
        val stateWeak = state0.copy(
            slippedAyahs = mapOf(
                2 to SlipTag.TASHKEEL,
                4 to SlipTag.FORGOT_MIDDLE,
                6 to SlipTag.WAQF
            )
        )
        assertEquals(Rating.Weak, stateWeak.suggestedRating)
    }

    @Test
    fun viewModelTransitionsThroughPhases() {
        val vm = ReviewingViewModel()
        vm.initialize("c_bilal", "Bilal", "بلال", ReviewerRole.Teacher)

        assertEquals(ExaminerPhase.OVERVIEW, vm.state.value.phase)
        assertEquals(3, vm.state.value.dueCards.size)

        vm.startListening()
        assertEquals(ExaminerPhase.LISTENING, vm.state.value.phase)
        assertEquals(0, vm.state.value.currentCardIndex)

        // Mark ayah 3 as slipped
        vm.toggleAyahSlip(3)
        assertTrue(vm.state.value.slippedAyahs.containsKey(3))
        assertEquals(3, vm.state.value.activeTagAyah)

        // Tag slip as Tashkeel
        vm.setSlipTag(3, SlipTag.TASHKEEL)
        assertEquals(SlipTag.TASHKEEL, vm.state.value.slippedAyahs[3])
        assertEquals(null, vm.state.value.activeTagAyah)

        // Submit card 1
        vm.submitGradeAndNext()
        assertEquals(1, vm.state.value.currentCardIndex)
        assertEquals(1, vm.state.value.completedAssessments.size)

        // Submit card 2
        vm.submitGradeAndNext()
        assertEquals(2, vm.state.value.currentCardIndex)

        // Submit card 3 (last card)
        vm.submitGradeAndNext()
        assertEquals(ExaminerPhase.SUMMARY, vm.state.value.phase)
        assertEquals(3, vm.state.value.completedAssessments.size)

        // Validate consensus on completed assessment
        val firstOutcome = vm.state.value.completedAssessments[0]
        assertNotNull(firstOutcome.consensus)
        assertTrue(firstOutcome.consensus!!.graders.any { it.role == ReviewerRole.Teacher })
    }
}
