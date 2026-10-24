package com.sylergy.murajaah.reviewing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.scheduler.ReviewerRole

private val DeepBrown = Color(0xFF2C221E)
private val ParchmentDark = Color(0xFFF3ECE2)
private val ForestGreen = Color(0xFF2F4A3D)
private val AmberGold = Color(0xFFB45309)
private val MutedInk = Color(0xFF6B5E55)

@Composable
fun ExaminerBanner(
    companionName: String,
    companionArabicName: String? = null,
    role: ReviewerRole,
    inPerson: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ParchmentDark, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎧 استماع لـ $companionName",
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown
                    )
                    if (!companionArabicName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "($companionArabicName)",
                            fontSize = 13.sp,
                            color = MutedInk
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Role badge
                    val roleLabel = if (role == ReviewerRole.Teacher) "معلّم / Teacher" else "قرين / Peer"
                    val roleColor = if (role == ReviewerRole.Teacher) ForestGreen else AmberGold
                    Box(
                        modifier = Modifier
                            .background(roleColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = roleLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = roleColor
                        )
                    }

                    // In-person badge
                    if (inPerson) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE0E7FF), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "حضورياً / In Person",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF3730A3)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onClose) {
                Text(
                    text = "✕",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk
                )
            }
        }
    }
}
