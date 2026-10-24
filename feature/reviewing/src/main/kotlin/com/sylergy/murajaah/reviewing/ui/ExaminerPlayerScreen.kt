package com.sylergy.murajaah.reviewing.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.mushaf.AyahFormatter
import com.sylergy.murajaah.reviewing.model.ExaminerSessionState
import com.sylergy.murajaah.reviewing.model.SlipTag

private val DeepBrown = Color(0xFF2C221E)
private val Parchment = Color(0xFFFAF6F0)
private val BorderParchment = Color(0xFFE5DDD0)
private val ForestGreen = Color(0xFF2F4A3D)
private val AmberGold = Color(0xFFB45309)
private val CoralRed = Color(0xFFC2410C)
private val MutedInk = Color(0xFF6B5E55)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExaminerPlayerScreen(
    state: ExaminerSessionState,
    onToggleSlip: (Int) -> Unit,
    onOpenTagPicker: (Int) -> Unit,
    onSelectRating: (Rating) -> Unit,
    onSubmitAndNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = state.currentCard ?: return
    val selectedRating = state.selectedRating ?: state.suggestedRating

    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Parchment)
    ) {
        // Examiner Banner
        ExaminerBanner(
            companionName = state.companionName,
            companionArabicName = state.companionArabicName,
            role = state.companionRole,
            inPerson = state.inPerson,
            onClose = onClose
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Progression & Timer Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(6.dp))
                        .border(1.dp, BorderParchment, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Card ${state.currentCardIndex + 1} of ${state.dueCards.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepBrown
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(6.dp))
                        .border(1.dp, BorderParchment, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⏱ $timeFormatted",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }
            }

            // Mushaf Canvas Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderParchment),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Surah title banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = card.surahArabicName,
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBrown
                            )
                            Text(
                                text = "${card.surahName} · Ayahs ${card.fromAyah}–${card.toAyah}",
                                fontSize = 12.sp,
                                color = MutedInk
                            )
                        }

                        Text(
                            text = "Tap ayah to mark slip",
                            fontSize = 11.sp,
                            color = MutedInk,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Basmala if beginning
                    if (AyahFormatter.hasBasmalaHeader(card.surah, card.fromAyah)) {
                        Text(
                            text = AyahFormatter.BASMALA_TEXT,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Verses Flow
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            card.ayahs.forEach { ayah ->
                                val isSlipped = state.slippedAyahs.containsKey(ayah.number)
                                val tag = state.slippedAyahs[ayah.number]

                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .background(
                                            color = if (isSlipped) Color(0xFFFFECE0) else Color(0xFFFBF9F6),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = if (isSlipped) 1.5.dp else 1.dp,
                                            color = if (isSlipped) CoralRed else Color(0xFFEDE6DA),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onToggleSlip(ayah.number) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = ayah.text,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 20.sp,
                                                color = if (isSlipped) CoralRed else DeepBrown,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDirection = TextDirection.Rtl,
                                                    lineHeight = 34.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = " ﴿${AyahFormatter.formatAyahMarker(ayah.number)}﴾",
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSlipped) CoralRed else ForestGreen
                                            )
                                        }

                                        if (isSlipped) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                                                    .clickable { onOpenTagPicker(ayah.number) }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tag?.arabicLabel ?: "⚠️ تعثر (تحديد السبب)",
                                                    fontSize = 11.sp,
                                                    color = CoralRed,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Slip Summary Banner
            val slips = state.slippedAyahs.size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (slips == 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (slips == 0) "قراءة متقنة دون تعثرات" else "تم تسجيل $slips تعثرات",
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (slips == 0) ForestGreen else CoralRed
                        )
                        Text(
                            text = if (slips == 0) "No slips marked on canvas" else "Ayahs slipped: ${state.slippedAyahs.keys.sorted().joinToString()}",
                            fontSize = 12.sp,
                            color = MutedInk
                        )
                    }

                    // Suggested Rating Badge
                    Box(
                        modifier = Modifier
                            .background(Color.White, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Suggested: ${state.suggestedRating.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )
                    }
                }
            }

            // Examiner Rating Selection
            Text(
                text = "Examiner Assessment / تقييم المستمع",
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingOptionCard(
                    title = "Solid",
                    arabicTitle = "متقن",
                    rating = Rating.Strong,
                    isSelected = selectedRating == Rating.Strong,
                    color = ForestGreen,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectRating(Rating.Strong) }
                )
                RatingOptionCard(
                    title = "Review",
                    arabicTitle = "مراجعة",
                    rating = Rating.Review,
                    isSelected = selectedRating == Rating.Review,
                    color = AmberGold,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectRating(Rating.Review) }
                )
                RatingOptionCard(
                    title = "Weak",
                    arabicTitle = "ضعيف",
                    rating = Rating.Weak,
                    isSelected = selectedRating == Rating.Weak,
                    color = CoralRed,
                    modifier = Modifier.weight(1f),
                    onSelect = { onSelectRating(Rating.Weak) }
                )
            }
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            val isLastCard = state.currentCardIndex + 1 >= state.dueCards.size
            Button(
                onClick = onSubmitAndNext,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isLastCard) "Finish & Attest Tasmīʿ · إنهاء واعتماد" else "Attest & Next Passage · اعتماد والتالي",
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
private fun RatingOptionCard(
    title: String,
    arabicTitle: String,
    rating: Rating,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) color.copy(alpha = 0.12f) else Color.White,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else BorderParchment,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = arabicTitle,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else DeepBrown
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isSelected) color else MutedInk
            )
        }
    }
}
