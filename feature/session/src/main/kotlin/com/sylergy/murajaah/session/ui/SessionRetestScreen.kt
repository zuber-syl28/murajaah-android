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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.sylergy.murajaah.mushaf.MushafCanvas
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.scheduler.MendResult
import com.sylergy.murajaah.session.model.SessionPhase

/**
 * Retest Screen before closing sitting.
 * The reciter recites from memory what slipped during today's session once more.
 * One answer: It Held or Still Slipped.
 * Never a wall: Skip moves on.
 */
@Composable
fun SessionRetestScreen(
    retest: SessionPhase.Retest,
    onToggleText: () -> Unit,
    onResult: (MendResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val card = retest.card.card
    val ayahs = MushafRepository.getPassageText(card.surah, card.fromAyah, card.toAyah)

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
                text = "إعادة التسميع · Final Retest",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
            TextButton(onClick = { onResult(MendResult.Skipped) }) {
                Text("Skip · تخطّي", color = Color(0xFF888888), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Prompt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = retest.card.surahArabicName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "${retest.card.surahName} · Ayahs ${card.fromAyah}–${card.toAyah}",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Recite this passage once more from memory to verify that the repair held.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF444444)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Peek / Hide Button
                OutlinedButton(
                    onClick = onToggleText,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (retest.isTextRevealed) "Hide Text · إخفاء النص" else "Peek at Mushaf · معاينة النص",
                        fontSize = 12.sp,
                        color = Color(0xFF2F4A3D)
                    )
                }
            }
        }

        if (retest.isTextRevealed) {
            Spacer(modifier = Modifier.height(16.dp))
            MushafCanvas(
                surahNumber = card.surah,
                fromAyah = card.fromAyah,
                toAyah = card.toAyah,
                ayahs = ayahs,
                slippedAyahs = retest.attempt.slippedAyahs,
                onAyahToggled = { /* read only */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dual Outcome Buttons: It Held vs Still Slipped
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Slipped Button
            Button(
                onClick = { onResult(MendResult.Slipped) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Still Slipped",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "تَعَثَّرَتْ",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Held Button
            Button(
                onClick = { onResult(MendResult.Held) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "It Held Firm",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ثَبَتَتْ بِحَمْدِ الله",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
