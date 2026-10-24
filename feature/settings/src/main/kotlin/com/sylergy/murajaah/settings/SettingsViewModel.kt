package com.sylergy.murajaah.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylergy.murajaah.auth.model.AuthState
import com.sylergy.murajaah.auth.model.UserSession
import com.sylergy.murajaah.auth.storage.SessionStorage
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import com.sylergy.murajaah.quran.AyahSpan
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.quran.NarrationData
import com.sylergy.murajaah.settings.model.NarrationRangeCarry
import com.sylergy.murajaah.settings.model.calculateNarrationCarry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(
    private val database: MurajaahDatabase,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    val userSession: StateFlow<UserSession?> = sessionStorage.authState.map { state ->
        when (state) {
            is AuthState.Guest -> state.session
            is AuthState.Authenticated -> state.session
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val plans: StateFlow<List<RevisionPlanEntity>> = database.revisionPlanDao()
        .observeAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlan: StateFlow<RevisionPlanEntity?> = database.revisionPlanDao()
        .observeActivePlan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val memorizedRanges: StateFlow<List<MemorizedRangeEntity>> = database.memorizedRangeDao()
        .observeAllRanges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _narrationSwitchPreview = MutableStateFlow<List<NarrationRangeCarry>?>(null)
    val narrationSwitchPreview: StateFlow<List<NarrationRangeCarry>?> = _narrationSwitchPreview.asStateFlow()

    private val _pendingNarration = MutableStateFlow<Narration?>(null)
    val pendingNarration: StateFlow<Narration?> = _pendingNarration.asStateFlow()

    fun setRevisionStyle(style: String) {
        sessionStorage.updateSession { it.copy(revisionStyle = style) }
    }

    fun setDailyTarget(pages: Int) {
        sessionStorage.updateSession { it.copy(dailyPages = pages) }
    }

    fun activatePlan(planId: String?) {
        viewModelScope.launch {
            if (planId == null) {
                database.revisionPlanDao().clearActivePlan()
            } else {
                database.revisionPlanDao().switchActivePlan(planId)
            }
        }
    }

    fun savePlan(planId: String?, name: String, surahs: List<Int>, makeActive: Boolean = false) {
        viewModelScope.launch {
            val id = planId ?: UUID.randomUUID().toString()
            val entity = RevisionPlanEntity(
                id = id,
                name = name.trim().ifEmpty { "Plan ${id.take(4)}" },
                surahs = surahs,
                isActive = makeActive
            )
            database.revisionPlanDao().insertPlan(entity)
            if (makeActive) {
                database.revisionPlanDao().switchActivePlan(id)
            }
        }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            val wasActive = activePlan.value?.id == planId
            database.revisionPlanDao().deletePlan(planId)
            if (wasActive) {
                database.revisionPlanDao().clearActivePlan()
            }
        }
    }

    fun prepareNarrationSwitch(targetNarration: Narration) {
        val currentId = userSession.value?.narration ?: "hafs"
        val currentNarration = Narration.fromId(currentId)
        if (currentNarration == targetNarration) {
            _pendingNarration.value = null
            _narrationSwitchPreview.value = null
            return
        }

        val ranges = memorizedRanges.value
        if (ranges.isEmpty()) {
            // No memorized ranges to transfer, execute switch directly
            confirmNarrationSwitch(targetNarration)
            return
        }

        val spans = ranges.map { AyahSpan(it.surah, it.fromAyah, it.toAyah) }
        val carries = calculateNarrationCarry(currentNarration, targetNarration, spans)
        _pendingNarration.value = targetNarration
        _narrationSwitchPreview.value = carries
    }

    fun dismissNarrationSwitch() {
        _pendingNarration.value = null
        _narrationSwitchPreview.value = null
    }

    fun confirmNarrationSwitch(targetNarration: Narration? = null) {
        val narrationToApply = targetNarration ?: _pendingNarration.value ?: return
        viewModelScope.launch {
            // Update session narration
            sessionStorage.updateSession { it.copy(narration = narrationToApply.id) }

            // Adjust ranges in database if counts differ
            val currentRanges = database.memorizedRangeDao().getAllRanges()
            for (range in currentRanges) {
                val maxAyahs = NarrationData.ayahCountIn(narrationToApply, range.surah) ?: range.toAyah
                if (range.toAyah > maxAyahs) {
                    database.memorizedRangeDao().insertAll(
                        listOf(range.copy(toAyah = maxAyahs))
                    )
                }
            }

            _pendingNarration.value = null
            _narrationSwitchPreview.value = null
        }
    }

    fun signOut() {
        sessionStorage.clearSession()
    }
}
