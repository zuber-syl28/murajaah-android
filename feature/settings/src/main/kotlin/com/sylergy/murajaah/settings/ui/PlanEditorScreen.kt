package com.sylergy.murajaah.settings.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.settings.SettingsViewModel
import com.sylergy.murajaah.settings.model.PlanRules

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanEditorScreen(
    viewModel: SettingsViewModel,
    planId: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plans by viewModel.plans.collectAsState()
    val memorizedRanges by viewModel.memorizedRanges.collectAsState()

    val existingPlan = remember(planId, plans) {
        plans.firstOrNull { it.id == planId }
    }

    // Extract unique memorized surahs, fallback to Juz 29 & 30 for new/guest accounts
    val memorizedSurahs = remember(memorizedRanges) {
        if (memorizedRanges.isNotEmpty()) {
            memorizedRanges.map { it.surah }.distinct().sorted()
        } else {
            (67..114).toList()
        }
    }

    var chosenSurahs by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var planName by remember { mutableStateOf("") }
    var userNamed by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler {
        if (confirmingDelete) {
            confirmingDelete = false
        } else {
            onBack()
        }
    }

    LaunchedEffect(existingPlan) {
        if (existingPlan != null) {
            chosenSurahs = existingPlan.surahs.toSet()
            planName = existingPlan.name
            userNamed = true
        }
    }

    // Auto-naming when user hasn't explicitly edited the title
    LaunchedEffect(chosenSurahs, userNamed) {
        if (!userNamed) {
            val picked = chosenSurahs.sorted()
            if (picked.isEmpty()) {
                planName = ""
            } else {
                // Check if an exact single Juz is selected
                val matchedJuz = (1..30).firstOrNull { juz ->
                    val surahsInJuz = PlanRules.juzSurahs(juz).filter { it in memorizedSurahs }
                    surahsInJuz.isNotEmpty() && surahsInJuz.all { it in chosenSurahs } && picked.size == surahsInJuz.size
                }
                if (matchedJuz != null) {
                    planName = "Juz' $matchedJuz (الجزء $matchedJuz)"
                } else if (picked.size == 1) {
                    val surahMeta = QuranData.surah(picked[0])
                    planName = if (surahMeta != null) "${surahMeta.name} (${surahMeta.arabicName})" else "Surah ${picked[0]}"
                } else {
                    planName = "Revision Plan (${picked.size} Surahs)"
                }
            }
        }
    }

    fun toggleJuz(juz: Int) {
        val surahsInJuz = PlanRules.juzSurahs(juz).filter { it in memorizedSurahs }
        if (surahsInJuz.isEmpty()) return
        val allSelected = surahsInJuz.all { it in chosenSurahs }
        chosenSurahs = if (allSelected) {
            chosenSurahs - surahsInJuz.toSet()
        } else {
            chosenSurahs + surahsInJuz.toSet()
        }
    }

    fun toggleSurah(surah: Int) {
        chosenSurahs = if (surah in chosenSurahs) {
            chosenSurahs - surah
        } else {
            chosenSurahs + surah
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFFFBF8F1)) // Warm Cream Parchment
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 22.sp, color = Color(0xFF2F4A3D), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (existingPlan != null) "Edit Plan · تعديل الخطة" else "New Revision Plan · خطة جديدة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "Focus your daily Wird on specific juz' or surahs",
                    fontSize = 12.sp,
                    color = Color(0xFF7A6B56)
                )
            }
        }

        // Plan Name Field
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Plan Name / اسم الخطة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7A6B56)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = planName,
                    onValueChange = {
                        planName = it
                        userNamed = true
                    },
                    placeholder = { Text("e.g. Juz' 30, Surat Al-Baqarah", color = Color(0xFF9E9E9E), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAF6EE),
                        unfocusedContainerColor = Color(0xFFFAF6EE),
                        focusedIndicatorColor = Color(0xFF2E6F40),
                        unfocusedIndicatorColor = Color(0xFFE2D9CC)
                    )
                )
            }
        }

        // Juz 30-Grid
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "30 Juz' Selector / اختيار بالأجزاء",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "Tap a juz' tile to add or remove all its memorized surahs.",
                    fontSize = 11.sp,
                    color = Color(0xFF7A6B56)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 6 rows of 5 juz
                for (row in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 1..5) {
                            val juz = row * 5 + col
                            val surahsInJuz = PlanRules.juzSurahs(juz).filter { it in memorizedSurahs }
                            val totalInJuz = surahsInJuz.size
                            val selectedCount = surahsInJuz.count { it in chosenSurahs }

                            val isEmpty = totalInJuz == 0
                            val isAll = !isEmpty && selectedCount == totalInJuz
                            val isSome = !isEmpty && selectedCount in 1 until totalInJuz

                            val bgColor = when {
                                isEmpty -> Color(0xFFF3EFE6).copy(alpha = 0.5f)
                                isAll -> Color(0xFF2E6F40) // Forest green
                                isSome -> Color(0xFFD97706) // Warm amber
                                else -> Color(0xFFFAF6EE)
                            }

                            val textColor = when {
                                isEmpty -> Color(0xFFB5A998)
                                isAll -> Color.White
                                isSome -> Color.White
                                else -> Color(0xFF2F4A3D)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (isAll || isSome) Color.Transparent else Color(0xFFE2D9CC),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = !isEmpty) { toggleJuz(juz) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$juz",
                                    fontSize = 13.sp,
                                    fontWeight = if (isAll || isSome) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                    if (row < 5) Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        // Memorized Surahs Chips
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Memorized Surahs / السور المحفوظة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Text(
                        text = "${chosenSurahs.size} / ${memorizedSurahs.size} selected",
                        fontSize = 11.sp,
                        color = Color(0xFF2E6F40),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (memorizedSurahs.isEmpty()) {
                    Text(
                        text = "No memorized ranges found. Complete onboarding or add ranges in Setup.",
                        fontSize = 12.sp,
                        color = Color(0xFF7A6B56)
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        memorizedSurahs.forEach { surah ->
                            val meta = QuranData.surah(surah)
                            val isSelected = surah in chosenSurahs
                            val chipBg = if (isSelected) Color(0xFF2E6F40) else Color(0xFFFAF6EE)
                            val chipText = if (isSelected) Color.White else Color(0xFF2F4A3D)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF2E6F40) else Color(0xFFE2D9CC),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { toggleSurah(surah) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${meta?.name ?: "Surah $surah"} (${meta?.arabicName ?: ""})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = chipText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error Notice
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                if (chosenSurahs.isEmpty()) {
                    errorMessage = "Please pick at least one surah for the revision plan."
                    return@Button
                }
                errorMessage = null
                viewModel.savePlan(
                    planId = existingPlan?.id,
                    name = planName.ifBlank { "Revision Plan" },
                    surahs = chosenSurahs.sorted(),
                    makeActive = true
                )
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F40)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Save Plan & Set Active · حفظ وتفعيل", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Delete Plan Button if editing
        if (existingPlan != null) {
            if (confirmingDelete) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Are you sure you want to delete '${existingPlan.name}'? Your memorized cards and grades will stay untouched.",
                            fontSize = 12.sp,
                            color = Color(0xFFB91C1C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.deletePlan(existingPlan.id)
                                    onBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Delete · حذف", fontSize = 12.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { confirmingDelete = false },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel · إلغاء", fontSize = 12.sp, color = Color(0xFF4B5563))
                            }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { confirmingDelete = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Plan · حذف الخطة", fontSize = 13.sp, color = Color(0xFFDC2626))
                }
            }
        }
    }
}
