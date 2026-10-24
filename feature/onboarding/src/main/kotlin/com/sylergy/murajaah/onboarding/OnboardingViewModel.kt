package com.sylergy.murajaah.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.auth.storage.SessionStorage
import com.sylergy.murajaah.scheduler.DateUtils
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.onboarding.model.OnboardingPresets
import com.sylergy.murajaah.onboarding.model.OnboardingStep
import com.sylergy.murajaah.quran.Narration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val database: MurajaahDatabase,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _currentStep = MutableStateFlow(OnboardingStep.WELCOME)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private val _selectedNarration = MutableStateFlow(Narration.HAFS)
    val selectedNarration: StateFlow<Narration> = _selectedNarration.asStateFlow()

    private val _selectedCardSize = MutableStateFlow("ten")
    val selectedCardSize: StateFlow<String> = _selectedCardSize.asStateFlow()

    private val _selectedJuzSet = MutableStateFlow(setOf(30))
    val selectedJuzSet: StateFlow<Set<Int>> = _selectedJuzSet.asStateFlow()

    private val _dailyPages = MutableStateFlow(5)
    val dailyPages: StateFlow<Int> = _dailyPages.asStateFlow()

    private val _revisionStyle = MutableStateFlow("ordered")
    val revisionStyle: StateFlow<String> = _revisionStyle.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun nextStep() {
        val next = when (_currentStep.value) {
            OnboardingStep.WELCOME -> OnboardingStep.NARRATION_AND_SIZE
            OnboardingStep.NARRATION_AND_SIZE -> OnboardingStep.MEMORIZED_RANGES
            OnboardingStep.MEMORIZED_RANGES -> OnboardingStep.GOAL_AND_STYLE
            OnboardingStep.GOAL_AND_STYLE -> OnboardingStep.GOAL_AND_STYLE
        }
        _currentStep.value = next
    }

    fun prevStep() {
        val prev = when (_currentStep.value) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.NARRATION_AND_SIZE -> OnboardingStep.WELCOME
            OnboardingStep.MEMORIZED_RANGES -> OnboardingStep.NARRATION_AND_SIZE
            OnboardingStep.GOAL_AND_STYLE -> OnboardingStep.MEMORIZED_RANGES
        }
        _currentStep.value = prev
    }

    fun selectNarration(narration: Narration) {
        _selectedNarration.value = narration
    }

    fun selectCardSize(cardSize: String) {
        _selectedCardSize.value = cardSize
    }

    fun toggleJuz(juzNumber: Int) {
        val updated = _selectedJuzSet.value.toMutableSet()
        if (updated.contains(juzNumber)) {
            // Keep at least one Juz selected
            if (updated.size > 1) {
                updated.remove(juzNumber)
            }
        } else {
            updated.add(juzNumber)
        }
        _selectedJuzSet.value = updated
    }

    fun selectAllJuz() {
        _selectedJuzSet.value = OnboardingPresets.PRESETS.map { it.juzNumber }.toSet()
    }

    fun setDailyPages(pages: Int) {
        _dailyPages.value = pages.coerceIn(1, 40)
    }

    fun setRevisionStyle(style: String) {
        _revisionStyle.value = style
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val today = DateUtils.today()
                // Seed database with chosen memorized ranges & cards
                OnboardingGenerator.generateAndSeed(
                    database = database,
                    selectedJuzNumbers = _selectedJuzSet.value,
                    cardSize = _selectedCardSize.value,
                    today = today
                )

                // Update session state
                sessionStorage.updateSession { session ->
                    session.copy(
                        narration = _selectedNarration.value.name.lowercase(),
                        cardSize = _selectedCardSize.value,
                        dailyPages = _dailyPages.value,
                        revisionStyle = _revisionStyle.value,
                        onboarded = true
                    )
                }

                onSuccess()
            } finally {
                _isGenerating.value = false
            }
        }
    }
}
