package com.sylergy.murajaah.voice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sylergy.murajaah.voice.model.VoiceStatus

/**
 * Detailed bottom sheet / dialog displaying live call metrics, animated equalizer,
 * end-to-end cryptographic fingerprint, and passage synchronization.
 */
@Composable
fun VoiceCallDialog(
    status: VoiceStatus.Connected,
    onToggleMute: () -> Unit,
    onEnd: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFDFBF7), // Parchment
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = status.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2416)
                        )
                        Text(
                            text = if (status.isReciter) "Reciting to them · تسميع" else "Listening to their recitation · استماع",
                            fontSize = 12.sp,
                            color = Color(0xFF786C58)
                        )
                    }

                    val minutes = status.durationSeconds / 60
                    val seconds = status.durationSeconds % 60
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Equalizer / Audio Waveform Visualizer
                AudioWaveformVisualizer(
                    level = status.audioLevel,
                    isMuted = status.isMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Passage Card (if synchronized)
                if (status.currentPassage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = status.currentPassage.surahArabicName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C2416)
                                )
                                Text(
                                    text = "${status.currentPassage.surahName} · Ayahs ${status.currentPassage.fromAyah}–${status.currentPassage.toAyah}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF786C58)
                                )
                            }
                            Text(
                                text = "📖 Reciting",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Security & Cryptographic Fingerprint Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "End-to-End Encrypted (AES-256-GCM)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fingerprint: ${status.fingerprint}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Direct peer-to-peer audio stream. Never recorded or stored.",
                            fontSize = 10.sp,
                            color = Color(0xFF166534).copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mute Button
                    FilledTonalButton(
                        onClick = onToggleMute,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (status.isMuted) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            contentColor = if (status.isMuted) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                    ) {
                        Text(
                            text = if (status.isMuted) "🔇 Unmute Mic" else "🎙️ Mute Mic",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // End Call Button
                    Button(
                        onClick = onEnd,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        )
                    ) {
                        Text("End Call · إنهاء", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Minimize to Bar · تصغير للشريط", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 16-bar responsive audio spectrum / waveform visualizer.
 */
@Composable
fun AudioWaveformVisualizer(
    level: Float,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEDE8D9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barCount = 16
            for (i in 0 until barCount) {
                val weightFactor = 1.0f - (Math.abs(i - barCount / 2f) / (barCount / 2f)) * 0.4f
                val barLevel = if (isMuted) 0.05f else (level * weightFactor).coerceIn(0.05f, 1.0f)
                val barHeight = (barLevel * 40.dp.value).coerceAtLeast(4f).dp

                val color = if (isMuted) {
                    Color(0xFFDC2626).copy(alpha = 0.5f)
                } else if (barLevel > 0.4f) {
                    Color(0xFF16A34A)
                } else {
                    Color(0xFF15803D).copy(alpha = 0.6f)
                }

                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}
