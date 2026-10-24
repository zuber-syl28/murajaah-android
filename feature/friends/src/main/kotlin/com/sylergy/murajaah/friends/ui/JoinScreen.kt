package com.sylergy.murajaah.friends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.friends.repository.FriendsRepository
import com.sylergy.murajaah.network.model.InvitePreviewDto
import kotlinx.coroutines.launch

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)
private val CrimsonRed = Color(0xFF8B261D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(
    token: String,
    repository: FriendsRepository,
    onSuccess: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var preview by remember { mutableStateOf<InvitePreviewDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        isLoading = true
        val result = repository.getInvitePreview(token)
        result.onSuccess {
            preview = it
        }.onFailure {
            errorMessage = "Invalid or expired invite link"
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "انضمام لمراجعة / Join Companion",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp, color = DeepBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ParchmentBg)
            )
        },
        containerColor = ParchmentBg,
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ForestGreen)
            } else if (errorMessage != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: "Error",
                        color = CrimsonRed,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text("Back to Home", color = Color.White)
                    }
                }
            } else if (isAccepted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "تم الاتصال بنجاح!",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You and ${preview?.studentName} are now connected for mutual tasmīʿ.",
                        fontSize = 14.sp,
                        color = DeepBrown,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onSuccess(preview?.studentName ?: "") },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("عرض قائمة الرفقاء / View Friends", color = Color.White)
                    }
                }
            } else if (preview != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightParchment)
                        .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "دعوة مراجعة متبادلة",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${preview?.studentName} invites you to connect for Quran recitation and tasmīʿ.",
                            fontSize = 14.sp,
                            color = DeepBrown,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Both of you will be able to recite to each other, assess retention, and track progress together.",
                            fontSize = 12.sp,
                            color = MutedInk,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    val acceptRes = repository.acceptInvite(token)
                                    acceptRes.onSuccess {
                                        isAccepted = true
                                    }.onFailure {
                                        errorMessage = "Failed to accept invite"
                                    }
                                    isLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("قبول والانضمام / Accept & Connect", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
