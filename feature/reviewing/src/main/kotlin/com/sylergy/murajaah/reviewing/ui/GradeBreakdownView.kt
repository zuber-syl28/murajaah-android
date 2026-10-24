package com.sylergy.murajaah.reviewing.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.scheduler.Consensus
import com.sylergy.murajaah.scheduler.ReviewerRole
import com.sylergy.murajaah.scheduler.WeakSpot

private val DeepBrown = Color(0xFF2C221E)
private val MutedInk = Color(0xFF6B5E55)
private val ForestGreen = Color(0xFF2F4A3D)
private val AmberGold = Color(0xFFB45309)
private val CoralRed = Color(0xFFC2410C)
private val ParchmentCard = Color(0xFFF9F5EF)

@Composable
fun GradeBreakdownView(
    consensus: Consensus?,
    weakSpots: List<WeakSpot> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (consensus == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ParchmentCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE5DDD0), shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "من قيّم ماذا؟ · Who said what",
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )
            RatingStatusTag(rating = consensus.band)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Combined Grade Summary Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Combined Grade / الدرجة التراكمية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepBrown
                )
                Text(
                    text = "Weighted by examiner role & 30d time decay",
                    fontSize = 11.sp,
                    color = MutedInk
                )
            }
            Text(
                text = "${(consensus.score * 100).toInt()}%",
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (consensus.band == Rating.Strong) ForestGreen else if (consensus.band == Rating.Review) AmberGold else CoralRed
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Graders list
        Text(
            text = "Examiner & Grader Weights / أوزان المقيمين",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MutedInk
        )
        Spacer(modifier = Modifier.height(4.dp))

        consensus.graders.forEach { grader ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = grader.graderName ?: grader.grader,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepBrown
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val roleText = when (grader.role) {
                            ReviewerRole.Teacher -> "Teacher · معلّم"
                            ReviewerRole.Peer -> "Peer · قرين"
                            ReviewerRole.Self -> "Self · فحص ذاتي"
                        }
                        Text(
                            text = "· $roleText",
                            fontSize = 11.sp,
                            color = MutedInk
                        )
                        if (grader.inPerson) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "· In-person",
                                fontSize = 11.sp,
                                color = Color(0xFF4338CA)
                            )
                        }
                    }
                    Text(
                        text = "Counts for ${grader.weightPercent.toInt()}% of grade (weight: ${String.format("%.2f", grader.weight)})",
                        fontSize = 11.sp,
                        color = MutedInk
                    )
                }

                RatingStatusTag(rating = grader.rating)
            }
        }

        // Weak Spots if any
        if (weakSpots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "مواضع التعثر الملحوظة / Flagged Weak Spots",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MutedInk
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weakSpots.forEach { spot ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF3C7), shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${spot.tag} ×${spot.count} — ${spot.by.joinToString()}",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingStatusTag(rating: Rating, modifier: Modifier = Modifier) {
    val (label, arabic, bg, text) = when (rating) {
        Rating.Strong -> Quad("Solid", "متقن", Color(0xFFE8F5E9), ForestGreen)
        Rating.Review -> Quad("Review", "مراجعة", Color(0xFFFEF3C7), AmberGold)
        Rating.Weak -> Quad("Weak", "ضعيف", Color(0xFFFFEBEE), CoralRed)
    }

    Box(
        modifier = modifier
            .background(bg, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "$label · $arabic",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = text
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
