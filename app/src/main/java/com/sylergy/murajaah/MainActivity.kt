package com.sylergy.murajaah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.fsrs.MurajaahFsrs
import com.sylergy.murajaah.fsrs.Rating
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.quran.NarrationData
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.scheduler.MurajaahScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MurajaahTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF8F7F2) // Warm Cream Mushaf Parchment
                ) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "مراجعة",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
            Text(
                text = "Murajaah",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F4A3D).copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Structured Qur'an revision with the people who help you retain it.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF555555),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Primary Wird Launch CTA
        item {
            Button(
                onClick = { /* Launch session */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F4A3D)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Start Revision (Wird)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8F7F2)
                    )
                    Text(
                        text = "Focus Mode · 3 Cards due today",
                        fontSize = 11.sp,
                        color = Color(0xFFF8F7F2).copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Qur'an Metadata Status Card (Issue #2)
        item {
            StatusCard(
                title = "Qur'an Domain Engine",
                badge = "114 Surahs · 6,236 Ayahs"
            ) {
                InfoRow("Ḥafṣ (Kufan)", "${QuranData.TOTAL_AYAHS} ayahs")
                InfoRow("Warsh & Qālūn (Madani)", "${NarrationData.totalAyahsIn(Narration.WARSH)} ayahs")
                InfoRow("Ad-Dūrī & As-Sūsī (Basran)", "${NarrationData.totalAyahsIn(Narration.DOORI)} ayahs")
                InfoRow("Al-Bazzī & Qunbul (Meccan)", "${NarrationData.totalAyahsIn(Narration.BAZZI)} ayahs")
                InfoRow("Partitions", "${QuranData.HIZB_COUNT} Ahzab · 30 Ajzāʾ")
            }
        }

        // FSRS-6 & Khatmah Scheduler Card (Issue #4 & Issue #5)
        item {
            StatusCard(
                title = "FSRS-6 & Khatmah Scheduler",
                badge = "21 Weights · Verified"
            ) {
                InfoRow("Request Retention", "90% target retrievability")
                InfoRow("Maximum Interval", "${MurajaahScheduler.MAX_STABILITY_DAYS} days")
                InfoRow("Khatmah Cycle (1-5 Juz)", "7 days")
                InfoRow("Khatmah Cycle (30 Juz)", "30 days")
                InfoRow("Teacher-Hold Safety Brake", "30 days active")
                InfoRow("Peer Growth Weight", "0.625 discount")
            }
        }

        // Surah Sample Preview
        item {
            StatusCard(
                title = "Active Range Overview",
                badge = "Surah Preview"
            ) {
                val fatihah = QuranData.surah(1)!!
                val baqarah = QuranData.surah(2)!!
                val nas = QuranData.surah(114)!!
                InfoRow("1. ${fatihah.name}", "${fatihah.arabicName} · ${fatihah.ayahCount} ayahs")
                InfoRow("2. ${baqarah.name}", "${baqarah.arabicName} · ${baqarah.ayahCount} ayahs")
                InfoRow("114. ${nas.name}", "${nas.arabicName} · ${nas.ayahCount} ayahs")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    badge: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF2F4A3D).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2F4A3D)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF222222)
        )
    }
}

@Composable
fun MurajaahTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
