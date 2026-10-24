package com.sylergy.murajaah.session.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.mushaf.AyahFormatter
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.session.model.SessionCard
import com.sylergy.murajaah.session.model.SessionRules

@Composable
fun SessionPromptScreen(
    card: SessionCard,
    elapsedSeconds: Int,
    hintsUsed: Int,
    onNextHint: () -> Unit,
    onCheck: () -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val floorSeconds = SessionRules.recallFloorSeconds(card.ayahsCount)
    val floorMet = elapsedSeconds >= floorSeconds
    val firstAyah = MushafRepository.getPassageText(card.card.surah, card.card.fromAyah, card.card.fromAyah).firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F2))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onExit) {
                Text("✕ Close", color = Color(0xFF666666), fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF2F4A3D).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${card.ayahsCount} Ayahs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F4A3D)
                )
            }
            TextButton(onClick = onSkip) {
                Text("Skip ↷", color = Color(0xFF666666), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recitation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.surahArabicName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "${card.surahName} · Ayahs ${card.card.fromAyah}–${card.card.toAyah}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // Opening ayah cue
                if (firstAyah != null) {
                    Text(
                        text = "Opening Cue:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF888888)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = firstAyah.text.take(35) + "…",
                        fontSize = 18.sp,
                        color = Color(0xFF2F4A3D),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Stopwatch Display
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                val timerText = String.format("%02d:%02d", minutes, seconds)

                Box(
                    modifier = Modifier
                        .background(
                            color = if (floorMet) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = timerText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (floorMet) Color(0xFF2E7D32) else Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (floorMet) "Recall floor met · Ready to check" else "Reciting aloud… (${floorSeconds - elapsedSeconds}s floor)",
                    fontSize = 12.sp,
                    color = if (floorMet) Color(0xFF2E7D32) else Color(0xFF888888)
                )

                // Hint Ladder Display
                AnimatedVisibility(visible = hintsUsed > 0 && firstAyah != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(Color(0xFFFFF8E1), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (hintsUsed == 1) "Hint 1: First words" else "Hint 2: Half ayah",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hintsUsed == 1) {
                                firstAyah?.text?.split(" ")?.take(3)?.joinToString(" ") ?: ""
                            } else {
                                firstAyah?.text?.take(firstAyah.text.length / 2) ?: ""
                            },
                            fontSize = 16.sp,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hint Button
        if (hintsUsed < SessionRules.MAX_HINTS) {
            OutlinedButton(
                onClick = onNextHint,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (hintsUsed == 0) "Need a Hint? (First words)" else "More Help (Half of ayah)",
                    fontSize = 13.sp,
                    color = Color(0xFF2F4A3D)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Primary Check Button
        Button(
            onClick = onCheck,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "I've Recited · Check Mushaf",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
