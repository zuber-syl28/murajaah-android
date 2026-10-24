package com.sylergy.murajaah.reviewing.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.reviewing.model.ExaminerSessionState
import com.sylergy.murajaah.scheduler.ReviewerRole
import com.sylergy.murajaah.scheduler.WeakSpot

private val DeepBrown = Color(0xFF2C221E)
private val Parchment = Color(0xFFFAF6F0)
private val BorderParchment = Color(0xFFE5DDD0)
private val ForestGreen = Color(0xFF2F4A3D)
private val MutedInk = Color(0xFF6B5E55)

@Composable
fun ExaminerSummaryScreen(
    state: ExaminerSessionState,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAyahs = state.completedAssessments.sumOf { it.card.ayahsCount }
    val totalSlips = state.completedAssessments.sumOf { it.slipsCount }
    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60
    val durationFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Parchment)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderParchment),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تم التسميع والاعتماد",
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tasmīʿ Attested & Saved",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepBrown
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "You listened to ${state.companionName}'s recitation. The attested grades have been weighted and updated in their revision history.",
                            fontSize = 12.sp,
                            color = MutedInk,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Examiner Stamp
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F0EC), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFC2D9CD), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            val roleText = if (state.companionRole == ReviewerRole.Teacher) "Teacher · معلّم" else "Peer · قرين"
                            Text(
                                text = "✓ Attested by You ($roleText) ${if (state.inPerson) "· In-Person" else ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryStatCard(
                        value = "${state.completedAssessments.size}",
                        label = "Cards",
                        arabicLabel = "مقاطع",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        value = "$totalAyahs",
                        label = "Ayahs",
                        arabicLabel = "آيات",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        value = "$totalSlips",
                        label = "Slips",
                        arabicLabel = "تعثرات",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        value = durationFormatted,
                        label = "Time",
                        arabicLabel = "مدة",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Passages Breakdown Section Header
            item {
                Text(
                    text = "Passage Outcomes & Consensus / نتائج المقاطع والتوافق",
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
            }

            // Each Card Outcome
            items(state.completedAssessments) { outcome ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderParchment),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = outcome.card.surahArabicName,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepBrown
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${outcome.card.surahName})",
                                        fontSize = 12.sp,
                                        color = MutedInk
                                    )
                                }
                                Text(
                                    text = "Ayahs ${outcome.card.fromAyah}–${outcome.card.toAyah} · Slips: ${outcome.slipsCount}",
                                    fontSize = 12.sp,
                                    color = MutedInk
                                )
                            }

                            RatingStatusTag(rating = outcome.rating)
                        }

                        // Convert tagged slips into WeakSpots
                        val spots = outcome.slipDetails.entries
                            .mapNotNull { it.value }
                            .groupBy { it.label }
                            .map { (label, tags) ->
                                WeakSpot(
                                    tag = label,
                                    count = tags.size,
                                    by = listOf("You")
                                )
                            }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Consensus breakdown
                        GradeBreakdownView(
                            consensus = outcome.consensus,
                            weakSpots = spots
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Finish Button Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Return to Companions · العودة للرفقاء",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    value: String,
    label: String,
    arabicLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderParchment),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$arabicLabel / $label",
                fontSize = 10.sp,
                color = MutedInk,
                textAlign = TextAlign.Center
            )
        }
    }
}
