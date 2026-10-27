package com.sylergy.murajaah.reviewing.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sylergy.murajaah.reviewing.ReviewingViewModel
import com.sylergy.murajaah.reviewing.model.ExaminerPhase

@Composable
fun ReviewingScreen(
    viewModel: ReviewingViewModel = viewModel(),
    onClose: () -> Unit,
    onStartListening: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    when (state.phase) {
        ExaminerPhase.OVERVIEW -> {
            ExaminerOverviewScreen(
                state = state,
                onStartListening = {
                    viewModel.startListening()
                    onStartListening?.invoke()
                },
                onToggleInPerson = { viewModel.toggleInPerson(it) },
                onBack = onClose,
                modifier = modifier.fillMaxSize()
            )
        }
        ExaminerPhase.LISTENING -> {
            ExaminerPlayerScreen(
                state = state,
                onToggleSlip = { viewModel.toggleAyahSlip(it) },
                onOpenTagPicker = { viewModel.toggleAyahSlip(it) },
                onSelectRating = { viewModel.selectRating(it) },
                onSubmitAndNext = { viewModel.submitGradeAndNext() },
                onClose = onClose,
                modifier = modifier.fillMaxSize()
            )
        }
        ExaminerPhase.SUMMARY -> {
            ExaminerSummaryScreen(
                state = state,
                onFinish = onClose,
                modifier = modifier.fillMaxSize()
            )
        }
    }

    // Slip tag picker dialog
    val activeAyah = state.activeTagAyah
    if (activeAyah != null) {
        SlipTagDialog(
            ayahNumber = activeAyah,
            currentTag = state.slippedAyahs[activeAyah],
            onSelectTag = { tag -> viewModel.setSlipTag(activeAyah, tag) },
            onDismiss = { viewModel.closeTagDialog() }
        )
    }
}
