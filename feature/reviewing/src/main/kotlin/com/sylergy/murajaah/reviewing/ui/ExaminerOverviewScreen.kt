package com.sylergy.murajaah.reviewing.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.reviewing.model.ExaminerSessionState
import com.sylergy.murajaah.scheduler.ReviewerRole

private val DeepBrown = Color(0xFF2C221E)
private val Parchment = Color(0xFFFAF6F0)
private val BorderParchment = Color(0xFFE5DDD0)
private val ForestGreen = Color(0xFF2F4A3D)
private val MutedInk = Color(0xFF6B5E55)

@Composable
fun ExaminerOverviewScreen(
    state: ExaminerSessionState,
    onStartListening: () -> Unit,
    onToggleInPerson: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Parchment)
    ) {
        // Top app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Tasmīʿ · تسميع مباشر",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
                Text(
                    text = "Examiner listening mode for companion",
                    fontSize = 12.sp,
                    color = MutedInk
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Companion Profile Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderParchment),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = state.companionName,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBrown
                                )
                                if (!state.companionArabicName.isNullOrBlank()) {
                                    Text(
                                        text = state.companionArabicName,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 14.sp,
                                        color = MutedInk
                                    )
                                }
                            }

                            // Role Chip
                            val roleText = if (state.companionRole == ReviewerRole.Teacher) "Teacher · معلّم" else "Peer · قرين"
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F0EC), shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = roleText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You are listening to their due recitation today. Your attested grades will be weighted according to your examiner role.",
                            fontSize = 12.sp,
                            color = MutedInk,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // In-Person switch row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFBF8F4), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Listening beside them in person",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepBrown
                                )
                                Text(
                                    text = "يستمع بجانبه حضورياً",
                                    fontSize = 11.sp,
                                    color = MutedInk
                                )
                            }
                            Switch(
                                checked = state.inPerson,
                                onCheckedChange = onToggleInPerson,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ForestGreen
                                )
                            )
                        }
                    }
                }
            }

            // Cards Queue Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Passages to Hear Today · مقاطع للتسميع",
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown
                    )
                    Text(
                        text = "${state.dueCards.size} cards",
                        fontSize = 12.sp,
                        color = MutedInk
                    )
                }
            }

            // Due Cards
            items(state.dueCards) { card ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderParchment),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = card.surahArabicName,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBrown
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${card.surahName})",
                                    fontSize = 13.sp,
                                    color = MutedInk
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ayahs ${card.fromAyah}–${card.toAyah} · ${card.ayahsCount} ayahs",
                                fontSize = 12.sp,
                                color = MutedInk
                            )
                        }

                        RatingStatusTag(rating = card.currentRating)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = onStartListening,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "🎧 Listen to ${state.companionName} · أستمع له الآن",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
