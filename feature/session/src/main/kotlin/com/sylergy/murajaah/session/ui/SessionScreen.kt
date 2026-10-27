package com.sylergy.murajaah.session.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sylergy.murajaah.session.SessionViewModel
import com.sylergy.murajaah.session.model.SessionPhase

@Composable
fun SessionScreen(
    viewModel: SessionViewModel,
    onFinishSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phase by viewModel.phase.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    when (val currentPhase = phase) {
        null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F7F2)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2F4A3D))
            }
        }
        is SessionPhase.Prompt -> {
            SessionPromptScreen(
                card = currentPhase.card,
                elapsedSeconds = elapsedSeconds,
                hintsUsed = currentPhase.hintsUsed,
                onNextHint = { viewModel.onNextHint() },
                onCheck = { viewModel.onCheck() },
                onSkip = { viewModel.onSkipCard() },
                onExit = onFinishSession,
                modifier = modifier
            )
        }
        is SessionPhase.InspectMushaf -> {
            SessionMushafScreen(
                card = currentPhase.card,
                attempt = currentPhase.attempt,
                onToggleSlipped = { ayah -> viewModel.onToggleSlippedAyah(ayah) },
                onSelectGrade = { rating -> viewModel.onSelectGrade(rating) },
                onExit = onFinishSession,
                modifier = modifier
            )
        }
        is SessionPhase.SpotRepair -> {
            SpotRepairScreen(
                repair = currentPhase,
                onAdvanceRead = { viewModel.onAdvanceRepairRead() },
                onSkip = { viewModel.onSkipRepair() },
                modifier = modifier
            )
        }
        is SessionPhase.Retest -> {
            SessionRetestScreen(
                retest = currentPhase,
                onToggleText = { viewModel.onToggleRetestText() },
                onResult = { result -> viewModel.onRecordRetestResult(result) },
                modifier = modifier
            )
        }
        is SessionPhase.Summary -> {
            SessionSummaryScreen(
                summary = currentPhase,
                onExtendQueue = { viewModel.onExtendQueue(2) },
                onFinish = onFinishSession,
                modifier = modifier
            )
        }
    }
}
