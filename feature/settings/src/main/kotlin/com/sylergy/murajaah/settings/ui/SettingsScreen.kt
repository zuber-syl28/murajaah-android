package com.sylergy.murajaah.settings.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.settings.SettingsViewModel
import com.sylergy.murajaah.settings.model.NarrationRangeCarry
import com.sylergy.murajaah.settings.model.NarrationRegistry

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenPlanEditor: (planId: String?) -> Unit,
    onOpenReminders: () -> Unit,
    onOpenSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by viewModel.userSession.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val narrationSwitchPreview by viewModel.narrationSwitchPreview.collectAsState()
    val pendingNarration by viewModel.pendingNarration.collectAsState()

    var showNarrationMenu by remember { mutableStateOf(false) }
    var confirmingDeleteAccount by remember { mutableStateOf(false) }

    BackHandler {
        if (confirmingDeleteAccount) {
            confirmingDeleteAccount = false
        } else {
            onBack()
        }
    }

    val currentNarrationId = session?.narration ?: "hafs"
    val currentNarration = Narration.fromId(currentNarrationId)
    val currentNarrationMeta = NarrationRegistry.get(currentNarration)
    val currentStyle = session?.revisionStyle ?: "smart"

    Column(
        modifier = modifier
            .background(Color(0xFFFBF8F1)) // Warm Cream Parchment
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
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
                    text = "الإعدادات / Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "Personalize your revision rhythm, riwāyah, and plans",
                    fontSize = 12.sp,
                    color = Color(0xFF7A6B56)
                )
            }
        }

        // Section 1: Daily Review Style / Goal
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Revision Goal / أسلوب الوِرد اليومي",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "Non-shaming pacing tailored to long-term Qur'an retention",
                    fontSize = 11.sp,
                    color = Color(0xFF7A6B56)
                )
                Spacer(modifier = Modifier.height(10.dp))

                val styles = listOf(
                    Triple("smart", "Smart (FSRS-6) · ذكي", "Dynamic intervals based on memory stability & recitation difficulty"),
                    Triple("due_only", "Due Only · المستحق فقط", "Only recite cards that have reached their calculated due date"),
                    Triple("light", "Gentle / Light · خفيف", "A modest, calm daily batch with zero pressure"),
                    Triple("target", "Daily Target · مستهدف", "Fixed page target (5 pages daily) regardless of interval")
                )

                styles.forEach { (styleKey, title, desc) ->
                    val isSelected = currentStyle == styleKey
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setRevisionStyle(styleKey) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setRevisionStyle(styleKey) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E6F40))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = Color(0xFF7A6B56)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Qira'at Riwayah Switcher
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Qira'at Riwāyah / الرواية القرآنية",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "Every card, grade, and ayah count follows your chosen riwāyah.",
                    fontSize = 11.sp,
                    color = Color(0xFF7A6B56)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFAF6EE))
                        .border(1.dp, Color(0xFFE2D9CC), RoundedCornerShape(10.dp))
                        .clickable { showNarrationMenu = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${currentNarrationMeta.arabicName} · \u2066${currentNarrationMeta.englishName}\u2069",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "School: ${currentNarrationMeta.school} · ${currentNarrationMeta.totalAyahs} Ayahs",
                                fontSize = 11.sp,
                                color = Color(0xFF7A6B56)
                            )
                        }
                        Text("▾", fontSize = 18.sp, color = Color(0xFF2E6F40), fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showNarrationMenu,
                        onDismissRequest = { showNarrationMenu = false }
                    ) {
                        NarrationRegistry.ALL.forEach { meta ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${meta.arabicName} (\u2066${meta.englishName}\u2069)",
                                            fontWeight = if (meta.narration == currentNarration) FontWeight.Bold else FontWeight.Normal,
                                            color = if (meta.narration == currentNarration) Color(0xFF2E6F40) else Color(0xFF2F4A3D)
                                        )
                                        Text(
                                            text = "${meta.school} · ${meta.totalAyahs} ayahs",
                                            fontSize = 10.sp,
                                            color = Color(0xFF7A6B56)
                                        )
                                    }
                                },
                                onClick = {
                                    showNarrationMenu = false
                                    viewModel.prepareNarrationSwitch(meta.narration)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Revision Plans
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Revision Plans / خطط المراجعة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    TextButton(onClick = { onOpenPlanEditor(null) }) {
                        Text("+ New Plan", fontSize = 12.sp, color = Color(0xFF2E6F40), fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "A plan focuses your Wird on specific surahs or juz'. Unselected surahs rest untouched.",
                    fontSize = 11.sp,
                    color = Color(0xFF7A6B56)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Default "All memorized" row
                val isAllActive = activePlan == null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAllActive) Color(0xFFE8F0EC) else Color(0xFFFAF6EE))
                        .border(1.dp, if (isAllActive) Color(0xFF2E6F40) else Color(0xFFE2D9CC), RoundedCornerShape(8.dp))
                        .clickable { viewModel.activatePlan(null) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All memorized · كل المحفوظ",
                        fontSize = 13.sp,
                        fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF2F4A3D)
                    )
                    if (isAllActive) {
                        Text("Active ✓", fontSize = 12.sp, color = Color(0xFF2E6F40), fontWeight = FontWeight.Bold)
                    }
                }

                // Custom plans list
                plans.forEach { plan ->
                    Spacer(modifier = Modifier.height(6.dp))
                    val isActive = activePlan?.id == plan.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) Color(0xFFE8F0EC) else Color(0xFFFAF6EE))
                            .border(1.dp, if (isActive) Color(0xFF2E6F40) else Color(0xFFE2D9CC), RoundedCornerShape(8.dp))
                            .clickable { viewModel.activatePlan(plan.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = plan.name,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "${plan.surahs.size} Surahs included",
                                fontSize = 10.sp,
                                color = Color(0xFF7A6B56)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Text("Active ✓", fontSize = 12.sp, color = Color(0xFF2E6F40), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            TextButton(onClick = { onOpenPlanEditor(plan.id) }) {
                                Text("Edit", fontSize = 11.sp, color = Color(0xFF7A6B56))
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Reminders Shortcut
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenReminders() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔔", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Daily Revision Reminders · التذكيرات",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                        Text(
                            text = "Intelligent notifications with zero streak pressure",
                            fontSize = 11.sp,
                            color = Color(0xFF7A6B56)
                        )
                    }
                }
                Text("→", fontSize = 18.sp, color = Color(0xFF2E6F40))
            }
        }

        // Section 4.5: Glance AppWidget
        val context = androidx.compose.ui.platform.LocalContext.current
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📱", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Home Screen Glance Widget · ويدجت الشاشة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                        Text(
                            text = "Track your Wird, active plan, and jump directly into revision from your home screen.",
                            fontSize = 11.sp,
                            color = Color(0xFF7A6B56)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val appWidgetManager = context.getSystemService(android.appwidget.AppWidgetManager::class.java)
                        val provider = android.content.ComponentName(context.packageName, "com.sylergy.murajaah.widget.MurajaahGlanceReceiver")
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            appWidgetManager.requestPinAppWidget(provider, null, null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAF6EE)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E6F40), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "📌 Pin Widget to Home Screen · إضافة للشاشة الرئيسية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E6F40)
                    )
                }
            }
        }

        // Section 5: Memorized Ranges Management
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSetup() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📖", fontSize = 20.sp)
                    Column {
                        Text(
                            text = "Manage Memorized Ranges · إدارة المحفوظ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                        Text(
                            text = "Add newly memorized surahs, juz', or hizb cuts",
                            fontSize = 11.sp,
                            color = Color(0xFF7A6B56)
                        )
                    }
                }
                Text("→", fontSize = 18.sp, color = Color(0xFF2E6F40))
            }
        }

        // Section 6: Account & Security
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Account · الحساب",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = session?.name ?: "Guest Hafidh",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                        Text(
                            text = if (session?.isGuest == true) "Local guest data · Unsynced" else session?.email ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFF7A6B56)
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.signOut() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sign Out · خروج", fontSize = 11.sp, color = Color(0xFF7A6B56))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                if (confirmingDeleteAccount) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Deleting your account permanently erases your flashcards, history, and streaks. This cannot be undone.",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.signOut()
                                        confirmingDeleteAccount = false
                                        onBack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Confirm Delete", fontSize = 11.sp, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { confirmingDeleteAccount = false },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Cancel", fontSize = 11.sp, color = Color(0xFF4B5563))
                                }
                            }
                        }
                    }
                } else {
                    TextButton(onClick = { confirmingDeleteAccount = true }) {
                        Text("Delete Account · حذف الحساب", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }
            }
        }

        // Section 7: Ethos & Attribution
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "مراجعة · Murajaah v1.0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A6B56)
            )
            Text(
                text = "Built with non-shaming spaced repetition for Qur'an Huffadh worldwide.",
                fontSize = 11.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quran text & ayah counts sourced from King Fahd Glorious Quran Printing Complex.",
                fontSize = 10.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center
            )
        }
    }

    // Narration Switch Dialog
    if (narrationSwitchPreview != null && pendingNarration != null) {
        val target = pendingNarration!!
        val targetMeta = NarrationRegistry.get(target)
        val carries = narrationSwitchPreview!!

        AlertDialog(
            onDismissRequest = { viewModel.dismissNarrationSwitch() },
            title = {
                Text(
                    text = "Switch to ${targetMeta.englishName}?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are switching your recitation riwāyah to ${targetMeta.arabicName} (${targetMeta.school}).",
                        fontSize = 13.sp,
                        color = Color(0xFF2F4A3D)
                    )
                    Text(
                        text = "Your memorized ranges will automatically adapt to ${targetMeta.englishName}'s numbering:",
                        fontSize = 12.sp,
                        color = Color(0xFF7A6B56)
                    )

                    val wholeCount = carries.count { it is NarrationRangeCarry.WholeSurah }
                    val directCount = carries.count { it is NarrationRangeCarry.Direct }
                    val confirmCount = carries.count { it is NarrationRangeCarry.Confirm }

                    if (wholeCount > 0) {
                        Text("• $wholeCount whole surahs carried automatically", fontSize = 12.sp, color = Color(0xFF2E6F40))
                    }
                    if (directCount > 0) {
                        Text("• $directCount ranges with matching verse counts preserved", fontSize = 12.sp, color = Color(0xFF2E6F40))
                    }
                    if (confirmCount > 0) {
                        Text("• $confirmCount mid-surah ranges scaled proportionally to ayah boundaries", fontSize = 12.sp, color = Color(0xFFD97706))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmNarrationSwitch() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F40))
                ) {
                    Text("Confirm Switch · تأكيد التبديل", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissNarrationSwitch() }) {
                    Text("Cancel · إلغاء", color = Color(0xFF4B5563))
                }
            }
        )
    }
}
