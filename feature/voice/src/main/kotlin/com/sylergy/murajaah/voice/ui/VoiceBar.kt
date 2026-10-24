package com.sylergy.murajaah.voice.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.voice.model.EndReason
import com.sylergy.murajaah.voice.model.VoiceStatus

/**
 * Top bar displaying current peer-to-peer voice recitation session status across all screens.
 * Matches web app VoiceBar.tsx styling with warm parchment tones and interactive controls.
 */
@Composable
fun VoiceBar(
    status: VoiceStatus,
    onJoin: (String) -> Unit,
    onToggleMute: () -> Unit,
    onEnd: () -> Unit,
    onDismiss: () -> Unit,
    onAcceptWaiting: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = status !is VoiceStatus.Idle,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val (bgBrush, borderColor) = when (status) {
            is VoiceStatus.Connected -> Color(0xFFF0FDF4) to Color(0xFF86EFAC) // Green tint
            is VoiceStatus.Waiting, is VoiceStatus.Connecting, is VoiceStatus.Starting -> Color(0xFFFFFBEB) to Color(0xFFFDE68A) // Amber
            is VoiceStatus.Incoming -> Color(0xFFEEF2FF) to Color(0xFFA5B4FC) // Indigo
            is VoiceStatus.Ended -> Color(0xFFFEF2F2) to Color(0xFFFECACA) // Red/rose
            VoiceStatus.Idle -> Color.Transparent to Color.Transparent
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(enabled = status is VoiceStatus.Connected) {
                    showDialog = true
                },
            shape = RoundedCornerShape(12.dp),
            color = bgBrush,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Leading Icon and Main Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    when (status) {
                        is VoiceStatus.Connected -> {
                            // Pulsing green audio status
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        is VoiceStatus.Incoming -> {
                            Text("🎧", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        is VoiceStatus.Waiting, is VoiceStatus.Connecting, is VoiceStatus.Starting -> {
                            Text("⏳", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        is VoiceStatus.Ended -> {
                            Text("📞", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        VoiceStatus.Idle -> {}
                    }

                    Column {
                        val titleText = when (status) {
                            is VoiceStatus.Incoming -> {
                                if (status.toHear) "${status.name} wants to hear you · يريد الاستماع لك"
                                else "${status.name} is ready to recite · جاهز للتسميع"
                            }
                            is VoiceStatus.Starting -> "Starting voice · جاري بدء التسميع..."
                            is VoiceStatus.Waiting -> "Waiting for ${status.name} (${status.secondsLeft}s) · بانتظاره"
                            is VoiceStatus.Connecting -> "Connecting to ${status.name}… · جاري الاتصال"
                            is VoiceStatus.Connected -> {
                                val minutes = status.durationSeconds / 60
                                val seconds = status.durationSeconds % 60
                                val timeFormatted = "%02d:%02d".format(minutes, seconds)
                                if (status.currentPassage != null) {
                                    "Reciting ${status.currentPassage.surahName} (${status.currentPassage.fromAyah}–${status.currentPassage.toAyah}) with ${status.name} · $timeFormatted"
                                } else {
                                    "With ${status.name} · $timeFormatted"
                                }
                            }
                            is VoiceStatus.Ended -> {
                                val reasonText = when (status.reason) {
                                    EndReason.HUNG_UP -> "Call ended · انتهت الجلسة"
                                    EndReason.NO_ANSWER -> "No answer · لم يستجب"
                                    EndReason.MICROPHONE -> "Microphone permission needed"
                                    EndReason.FAILED -> "Call failed · تعذر الاتصال"
                                }
                                "${status.name} — $reasonText"
                            }
                            VoiceStatus.Idle -> ""
                        }

                        Text(
                            text = titleText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (status is VoiceStatus.Connected) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔒 E2E Encrypted (AES-256)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Real-time audio waveform level dots
                                MiniAudioWaveform(level = status.audioLevel, isMuted = status.isMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Trailing Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (status) {
                        is VoiceStatus.Incoming -> {
                            Button(
                                onClick = { onJoin(status.callId) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E3A8A),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Join · انضمام", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        is VoiceStatus.Starting, is VoiceStatus.Waiting, is VoiceStatus.Connecting -> {
                            if (status is VoiceStatus.Waiting) {
                                Button(
                                    onClick = onAcceptWaiting,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF16A34A),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Accept · قبول", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            OutlinedButton(
                                onClick = onEnd,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("End · إنهاء", fontSize = 12.sp)
                            }
                        }
                        is VoiceStatus.Connected -> {
                            // Mic Mute Toggle
                            FilledTonalButton(
                                onClick = onToggleMute,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (status.isMuted) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                    contentColor = if (status.isMuted) Color(0xFFDC2626) else Color(0xFF16A34A)
                                )
                            ) {
                                Text(
                                    text = if (status.isMuted) "🔇 Unmute" else "🎙️ Mute",
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // End Call Button
                            Button(
                                onClick = onEnd,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("End · إنهاء", fontSize = 12.sp)
                            }
                        }
                        is VoiceStatus.Ended -> {
                            FilledTonalButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close · إغلاق", fontSize = 12.sp)
                            }
                        }
                        VoiceStatus.Idle -> {}
                    }
                }
            }
        }
    }

    if (showDialog && status is VoiceStatus.Connected) {
        VoiceCallDialog(
            status = status,
            onToggleMute = onToggleMute,
            onEnd = {
                showDialog = false
                onEnd()
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun MiniAudioWaveform(
    level: Float,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = 4
        for (i in 0 until bars) {
            val threshold = (i + 1) * 0.2f
            val active = !isMuted && level >= threshold
            val h = if (active) (8 + i * 2).dp else 4.dp
            val c = if (isMuted) Color(0xFFEF4444) else if (active) Color(0xFF16A34A) else Color(0xFFD1D5DB)

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h)
                    .clip(RoundedCornerShape(1.dp))
                    .background(c)
            )
        }
    }
}
