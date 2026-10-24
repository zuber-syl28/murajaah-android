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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.mushaf.MushafCanvas
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.session.model.RecitationAttempt
import com.sylergy.murajaah.session.model.SessionCard
import com.sylergy.murajaah.session.model.SessionRules

@Composable
fun SessionMushafScreen(
    card: SessionCard,
    attempt: RecitationAttempt,
    onToggleSlipped: (Int) -> Unit,
    onSelectGrade: (Rating) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ayahs = MushafRepository.getPassageText(card.card.surah, card.card.fromAyah, card.card.toAyah)
    val suggested = SessionRules.suggestedGrade(
        ayahs = card.ayahsCount,
        slippedCount = attempt.slippedAyahs.size,
        hintsUsed = attempt.hintsUsed
    )

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
            TextButton(onClick = onExit) {
                Text("✕ Close", color = Color(0xFF666666), fontSize = 14.sp)
            }
            Text(
                text = "Recited in ${attempt.elapsedSeconds}s",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F4A3D)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mushaf Canvas
        MushafCanvas(
            surahNumber = card.card.surah,
            fromAyah = card.card.fromAyah,
            toAyah = card.card.toAyah,
            ayahs = ayahs,
            slippedAyahs = attempt.slippedAyahs,
            onAyahToggled = onToggleSlipped
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Suggested Grade Guidance Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Suggested Rating",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = when (suggested) {
                            Rating.Strong -> "Clean recall · Solid"
                            Rating.Review -> "${attempt.slippedAyahs.size} slip(s) · Review soon"
                            Rating.Weak -> "Needs reinforcement · Weak"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (suggested) {
                            Rating.Strong -> Color(0xFF2E7D32)
                            Rating.Review -> Color(0xFFE65100)
                            Rating.Weak -> Color(0xFFC62828)
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = when (suggested) {
                                Rating.Strong -> Color(0xFFE8F5E9)
                                Rating.Review -> Color(0xFFFFF3E0)
                                Rating.Weak -> Color(0xFFFFEBEE)
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (suggested) {
                            Rating.Strong -> "★ Solid"
                            Rating.Review -> "★ Review"
                            Rating.Weak -> "★ Weak"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (suggested) {
                            Rating.Strong -> Color(0xFF2E7D32)
                            Rating.Review -> Color(0xFFE65100)
                            Rating.Weak -> Color(0xFFC62828)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Three Rating Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Weak Button
            Button(
                onClick = { onSelectGrade(Rating.Weak) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (suggested == Rating.Weak) Color(0xFFC62828) else Color(0xFFFFCDD2)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Weak",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (suggested == Rating.Weak) Color.White else Color(0xFFB71C1C)
                    )
                    Text(
                        text = "1 day",
                        fontSize = 10.sp,
                        color = if (suggested == Rating.Weak) Color.White.copy(alpha = 0.8f) else Color(0xFFB71C1C).copy(alpha = 0.8f)
                    )
                }
            }

            // Review Button
            Button(
                onClick = { onSelectGrade(Rating.Review) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (suggested == Rating.Review) Color(0xFFE65100) else Color(0xFFFFE0B2)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Review",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (suggested == Rating.Review) Color.White else Color(0xFFE65100)
                    )
                    Text(
                        text = "~4 days",
                        fontSize = 10.sp,
                        color = if (suggested == Rating.Review) Color.White.copy(alpha = 0.8f) else Color(0xFFE65100).copy(alpha = 0.8f)
                    )
                }
            }

            // Strong Button
            Button(
                onClick = { onSelectGrade(Rating.Strong) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (suggested == Rating.Strong) Color(0xFF2F4A3D) else Color(0xFFC8E6C9)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Strong",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (suggested == Rating.Strong) Color.White else Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Solid · 14d+",
                        fontSize = 10.sp,
                        color = if (suggested == Rating.Strong) Color.White.copy(alpha = 0.8f) else Color(0xFF1B5E20).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
