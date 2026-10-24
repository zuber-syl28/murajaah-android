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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.session.model.SessionPhase

@Composable
fun SessionSummaryScreen(
    summary: SessionPhase.Summary,
    onExtendQueue: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = summary.totalSeconds / 60
    val seconds = summary.totalSeconds % 60
    val durationText = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F2))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "تقبّل الله",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4A3D)
        )
        Text(
            text = "Nice Work!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4A3D).copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Stopping here is a complete outcome, not an abandoned one.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF555555),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = "Cards Revised", value = "${summary.completedCount}")
                    StatBox(label = "Ayahs Recited", value = "${summary.totalAyahs}")
                    StatBox(label = "Time Spent", value = durationText)
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PillBadge(label = "Strong", count = summary.strongCount, color = Color(0xFF2E7D32), bg = Color(0xFFE8F5E9))
                    PillBadge(label = "Review", count = summary.reviewCount, color = Color(0xFFE65100), bg = Color(0xFFFFF3E0))
                    PillBadge(label = "Weak", count = summary.weakCount, color = Color(0xFFC62828), bg = Color(0xFFFFEBEE))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Want 2 more? (Extend button)
        OutlinedButton(
            onClick = onExtendQueue,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Want 2 more? (+2 Cards)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Primary Finish Button
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "Finish Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4A3D)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF777777)
        )
    }
}

@Composable
private fun PillBadge(label: String, count: Int, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$count $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
