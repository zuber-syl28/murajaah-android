package com.sylergy.murajaah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "مراجعة",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4A3D)
        )
        Text(
            text = "Murajaah",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2F4A3D).copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Structured Qur'an revision with the people who help you retain it.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FSRS-6 Engine Active",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Spacer(modifier = Modifier.height(6.dp))
                val initStrong = MurajaahFsrs.initialDifficulty(Rating.Strong)
                Text(
                    text = "Initial Strong Difficulty: $initStrong\nRequest Retention: 90%",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF666666)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { /* Will launch session */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F4A3D)
            )
        ) {
            Text(
                text = "Start Revision (Wird)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8F7F2)
            )
        }
    }
}

@Composable
fun MurajaahTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
