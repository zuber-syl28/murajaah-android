package com.sylergy.murajaah.reminders.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.reminders.RemindersViewModel
import com.sylergy.murajaah.reminders.model.ReminderRules

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)

/**
 * Settings screen for scheduling Qur'an revision reminders and companion listening nudges.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderSettingsScreen(
    viewModel: RemindersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val plan by viewModel.plan.collectAsState()
    var newTimeInput by remember { mutableStateOf("13:30") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var testSentMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "التذكيرات / Reminders",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DeepBrown
                        )
                        Text(
                            text = "Intelligent daily revision nudges",
                            fontSize = 12.sp,
                            color = MutedInk
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = DeepBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ParchmentBg)
            )
        },
        containerColor = ParchmentBg,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Switch Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderParchment),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (plan.enabled) "Reminders are on · التذكيرات مفعلة" else "Reminders are off · التذكيرات متوقفة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nudges only when passages are due today. No guilt, no streak pressure.",
                                fontSize = 12.sp,
                                color = MutedInk
                            )
                        }

                        Switch(
                            checked = plan.enabled,
                            onCheckedChange = { viewModel.toggleEnabled() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ForestGreen
                            )
                        )
                    }
                }
            }

            // Scheduled Times Section
            if (plan.enabled) {
                item {
                    Text(
                        text = "أوقات التذكير / Scheduled Times",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DeepBrown,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(plan.times.size) { index ->
                    val time = plan.times[index]
                    val slotName = when (time) {
                        "08:00" -> "صباحاً / Morning (Fajr)"
                        "13:30" -> "ظهراً / Afternoon (Dhuhr)"
                        "17:00" -> "عصراً / Late Afternoon (Asr)"
                        "19:00", "19:30" -> "مساءً / Evening (Maghrib)"
                        "21:30" -> "ليلاً / Night (Isha)"
                        else -> "وقت مخصص / Custom slot"
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderParchment),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏰", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = time,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = DeepBrown
                                    )
                                    Text(
                                        text = slotName,
                                        fontSize = 12.sp,
                                        color = MutedInk
                                    )
                                }
                            }

                            if (plan.times.size > 1) {
                                OutlinedButton(
                                    onClick = { viewModel.removeTime(time) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Remove · حذف", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }

                // Add Time Section
                if (plan.times.size < plan.maxTimes) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = LightParchment),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "إضافة وقت جديد / Add Time Slot",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = DeepBrown
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newTimeInput,
                                        onValueChange = {
                                            newTimeInput = it
                                            inputError = null
                                        },
                                        label = { Text("Time (HH:mm)") },
                                        singleLine = true,
                                        isError = inputError != null,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Button(
                                        onClick = {
                                            if (ReminderRules.isValidTime(newTimeInput)) {
                                                val added = viewModel.addTime(newTimeInput)
                                                if (!added) {
                                                    inputError = "Time already added or limit reached"
                                                }
                                            } else {
                                                inputError = "Invalid 24h format (e.g. 14:30)"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Add · إضافة")
                                    }
                                }

                                if (inputError != null) {
                                    Text(
                                        text = inputError!!,
                                        color = Color(0xFFDC2626),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Quick Presets:",
                                    fontSize = 11.sp,
                                    color = MutedInk
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("08:00", "13:30", "17:00", "19:00", "21:30").forEach { preset ->
                                        if (!plan.times.contains(preset)) {
                                            FilledTonalButton(
                                                onClick = { viewModel.addTime(preset) },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(preset, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Test Notification Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.triggerTestNotification()
                            testSentMessage = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Text(
                            text = "🔔 Send Test Nudge · تجربة إشعار فوري",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (testSentMessage) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✓ Notification sent to system tray with quick action buttons!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
