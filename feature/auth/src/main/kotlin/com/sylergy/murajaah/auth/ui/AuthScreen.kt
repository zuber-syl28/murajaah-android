package com.sylergy.murajaah.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.auth.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf("guest") }
    var guestName by remember { mutableStateOf("Hafidh") }
    var email by remember { mutableStateOf("") }
    var cloudName by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F2))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Spiritual Header
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Structured Qur'an revision with the people who help you retain it.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mode Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDECE6), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (mode == "guest") Color.White else Color.Transparent)
                    .clickable {
                        mode = "guest"
                        viewModel.clearError()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Guest Mode",
                    fontSize = 14.sp,
                    fontWeight = if (mode == "guest") FontWeight.Bold else FontWeight.Medium,
                    color = if (mode == "guest") Color(0xFF2F4A3D) else Color(0xFF777777)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (mode == "cloud") Color.White else Color.Transparent)
                    .clickable {
                        mode = "cloud"
                        viewModel.clearError()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cloud Account",
                    fontSize = 14.sp,
                    fontWeight = if (mode == "cloud") FontWeight.Bold else FontWeight.Medium,
                    color = if (mode == "cloud") Color(0xFF2F4A3D) else Color(0xFF777777)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error message banner
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(text = msg, color = Color(0xFFC62828), fontSize = 13.sp)
                }
            }
        }

        // Form Container Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mode == "guest") {
                    Text(
                        text = "Instant Offline Access",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No sign-in or network required. All your memorized ranges, FSRS schedules, and recitation logs stay privately on this device.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2F4A3D),
                            focusedLabelColor = Color(0xFF2F4A3D)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.signInAsGuest(guestName)
                            onAuthSuccess()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Continue as Guest", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Text(
                        text = "Cloud Sync & Friends",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sign in to back up your progress to the cloud, join social revision circles, and receive tasmīʿ evaluations.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = cloudName,
                        onValueChange = { cloudName = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2F4A3D),
                            focusedLabelColor = Color(0xFF2F4A3D)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2F4A3D),
                            focusedLabelColor = Color(0xFF2F4A3D)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.signInWithCloud(email, cloudName)
                            onAuthSuccess()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In with Cloud", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
