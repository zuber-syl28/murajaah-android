package com.sylergy.murajaah.session.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.mushaf.MushafCanvas
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.mushaf.audio.AudioClip
import com.sylergy.murajaah.mushaf.audio.QuranAudioPlayer
import com.sylergy.murajaah.mushaf.audio.QuranAudioRepository
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.session.model.SessionPhase

/**
 * Tathbeet on the spot (Spot Repair) with Audio Reciter Read-Along.
 * A recitation graded Shaky or Weak is immediately read looking at the Mushaf
 * several times (3, 5, or 7) with streaming reciter playback in the account's narration.
 * Never a wall: Skip moves on.
 */
@Composable
fun SpotRepairScreen(
    repair: SessionPhase.SpotRepair,
    onAdvanceRead: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioRepo = remember { QuranAudioRepository(context) }
    val audioPlayer = remember { QuranAudioPlayer(coroutineScope) }

    val surah = repair.card.card.surah
    val from = repair.repairAyahs.minOrNull() ?: repair.card.card.fromAyah
    val to = repair.repairAyahs.maxOrNull() ?: repair.card.card.toAyah
    val ayahs = MushafRepository.getPassageText(surah, from, to)
    val progressFraction = repair.currentRead.toFloat() / repair.totalReadsNeeded.toFloat()

    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val isLoadingAudio by audioPlayer.isLoading.collectAsState()
    val activeRecitingAyah by audioPlayer.activeAyah.collectAsState()
    val playbackSpeed by audioPlayer.playbackSpeed.collectAsState()

    var audioClip by remember { mutableStateOf<AudioClip?>(null) }

    LaunchedEffect(surah, from, to) {
        audioClip = audioRepo.loadClip(Narration.HAFS, surah, from, to)
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    // Determine highlighted ayahs: if playing audio, show currently reciting ayah; otherwise slipped ayahs
    val highlightedAyahs = remember(activeRecitingAyah, repair.attempt.slippedAyahs) {
        if (activeRecitingAyah != null) {
            setOf(activeRecitingAyah!!)
        } else {
            repair.attempt.slippedAyahs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F2))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تثبيت على الفور · Spot Repair",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
            TextButton(onClick = {
                audioPlayer.stop()
                onSkip()
            }) {
                Text("Skip · تخطّي", color = Color(0xFF888888), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Instruction card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Read looking at the Mushaf",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Repetition ${repair.currentRead} of ${repair.totalReadsNeeded}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "As teachers of the Qur'an advise: read what slipped a few times looking, before reciting it again.",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFF2F4A3D),
                    trackColor = Color(0xFFE0E0E0),
                )
            }
        }

        // Reciter Audio Player Bar
        if (audioClip != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5F2)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎧", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Listen to Reciter · تلاوة الشيخ الحصري",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = if (isPlaying) "Playing Ayah ${activeRecitingAyah ?: from}..." else "Audio read-along available",
                                fontSize = 10.sp,
                                color = Color(0xFF555555)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Speed Chip
                        OutlinedButton(
                            onClick = { audioPlayer.cycleSpeed() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("${playbackSpeed}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F4A3D))
                        }

                        // Play/Pause Button
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    audioPlayer.togglePlayPause()
                                } else {
                                    audioClip?.let { audioPlayer.play(it) }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isLoadingAudio) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text(if (isPlaying) "Pause ⏸" else "Play ▶", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mushaf Canvas with highlighted slipped/repair ayahs
        MushafCanvas(
            surahNumber = surah,
            fromAyah = from,
            toAyah = to,
            ayahs = ayahs,
            slippedAyahs = highlightedAyahs,
            onAyahToggled = { /* read only during repair */ }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Button: I read this (X of Y)
        Button(
            onClick = {
                audioPlayer.stop()
                onAdvanceRead()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = if (repair.currentRead < repair.totalReadsNeeded) {
                    "I read this (${repair.currentRead}/${repair.totalReadsNeeded}) · قرأتُها"
                } else {
                    "Finished Repair · أتممتُ التثبيت"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
