package com.sylergy.murajaah.progress.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.progress.ProgressViewModel
import com.sylergy.murajaah.progress.model.JuzCell
import com.sylergy.murajaah.progress.model.MasterySummary
import com.sylergy.murajaah.progress.model.ProgressDashboardState
import com.sylergy.murajaah.progress.model.SurahProgressItem
import com.sylergy.murajaah.progress.model.TathbeetItem
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateToCard: (String) -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7F2)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF2F4A3D))
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F2))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إتقان",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Text(
                        text = "Retention & Progress Dashboard",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        // Mastery Ring Card
        item {
            MasteryRingCard(mastery = state.mastery, upcomingOverdue = state.upcomingToday)
        }

        // Counts Strip: Ayahs, Tathbeet, Weeks
        item {
            CountsStrip(
                ayahsCount = state.totalAyahs,
                tathbeetCount = state.tathbeetCount,
                weeksRevised = state.weeksRevised
            )
        }

        // Khatmah Revision Cycle
        state.cycle?.let { cycle ->
            item {
                CycleCard(cycle = cycle)
            }
        }

        // Need Tathbeet Section
        if (state.tathbeetList.isNotEmpty()) {
            item {
                TathbeetSection(
                    items = state.tathbeetList,
                    onCardClick = onNavigateToCard
                )
            }
        }

        // Coming Up (Forecast)
        item {
            ComingUpSection(
                today = state.upcomingToday,
                tomorrow = state.upcomingTomorrow,
                week = state.upcomingWeek
            )
        }

        // Weekly Consistency Trend
        item {
            WeeklyTrendCard(trend = state.trend)
        }

        // Permanent Achievements Entry Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAchievements() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = "Achievements · إنجازات",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "Permanent non-lossy milestones",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    Text("View →", fontSize = 13.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                }
            }
        }

        // 30 Juz Grid Map
        item {
            JuzGridSection(juzList = state.juzList)
        }

        // By Surah Breakdown
        item {
            Text(
                text = "By Surah · السور",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.surahs, key = { it.surahNumber }) { surahItem ->
            SurahProgressRow(item = surahItem)
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MasteryRingCard(
    mastery: MasterySummary,
    upcomingOverdue: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Circle Ring Gauge
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                val fraction = (mastery.value ?: 0.0).toFloat().coerceIn(0f, 1f)
                val ringColor = when {
                    fraction >= 0.85f -> Color(0xFF15803D) // Emerald
                    fraction >= 0.70f -> Color(0xFF2F4A3D) // Deep Slate Emerald
                    fraction >= 0.40f -> Color(0xFFD97706) // Amber
                    else -> Color(0xFFDC2626) // Brick Red
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    // Track
                    drawCircle(
                        color = Color(0xFFF0EFEA),
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress Arc
                    if (mastery.value != null) {
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = fraction * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (mastery.value != null) "${(mastery.value * 100).roundToInt()}%" else "—",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Text(
                        text = "ITQAN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                val levelLabel = mastery.level?.let { "${it.englishLabel} · ${it.arabicLabel}" } ?: "Not tested yet"
                Text(
                    text = levelLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )

                Spacer(modifier = Modifier.height(4.dp))
                val note = when {
                    mastery.value == null -> "Recite cards to build your retention profile"
                    upcomingOverdue == 0 -> "All memorized cards are on schedule"
                    else -> "$upcomingOverdue cards due for revision"
                }
                Text(
                    text = note,
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${mastery.heardAyahs} heard of ${mastery.totalAyahs} ayahs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF777777)
                )

                if (mastery.strength != null) {
                    Text(
                        text = "Tested strength: ${(mastery.strength * 100).roundToInt()}%",
                        fontSize = 11.sp,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CountsStrip(
    ayahsCount: Int,
    tathbeetCount: Int,
    weeksRevised: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CountCard(
            modifier = Modifier.weight(1f),
            label = "Ayahs",
            arabicLabel = "آية محفوظة",
            value = ayahsCount.toString(),
            color = Color(0xFF2F4A3D)
        )
        CountCard(
            modifier = Modifier.weight(1f),
            label = "Tathbeet",
            arabicLabel = "تثبيت مطلوب",
            value = tathbeetCount.toString(),
            color = if (tathbeetCount > 0) Color(0xFFDC2626) else Color(0xFF15803D)
        )
        CountCard(
            modifier = Modifier.weight(1f),
            label = "Weeks",
            arabicLabel = "أسابيع نشطة",
            value = weeksRevised.toString(),
            color = Color(0xFF0369A1)
        )
    }
}

@Composable
fun CountCard(
    label: String,
    arabicLabel: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF555555)
            )
            Text(
                text = arabicLabel,
                fontSize = 9.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
fun CycleCard(cycle: com.sylergy.murajaah.progress.model.CycleStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Revision Cycle · دورة المراجعة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "${(cycle.progressFraction * 100).roundToInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }

            LinearProgressIndicator(
                progress = { cycle.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF15803D),
                trackColor = Color(0xFFE5E7EB)
            )

            Text(
                text = "${cycle.heardCards} of ${cycle.totalCards} cards revised in this ${cycle.cycleDays}-day cycle",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun TathbeetSection(
    items: List<TathbeetItem>,
    onCardClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayList = if (expanded) items else items.take(4)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Need Tathbeet · بحاجة لتثبيت",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = items.size.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            if (items.size > 4) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "Show less" else "See all (${items.size})",
                        fontSize = 12.sp,
                        color = Color(0xFF2F4A3D)
                    )
                }
            }
        }

        displayList.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardClick(item.cardId) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = item.arabicName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "(${item.surahName})",
                                fontSize = 12.sp,
                                color = Color(0xFF777777)
                            )
                        }
                        Text(
                            text = "Ayahs ${item.fromAyah}–${item.toAyah}",
                            fontSize = 12.sp,
                            color = Color(0xFF555555)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (item.status == "weak") Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (item.status == "weak") "Weak · ضعيف" else "Review · مراجعة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.status == "weak") Color(0xFFDC2626) else Color(0xFFB45309)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComingUpSection(
    today: Int,
    tomorrow: Int,
    week: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Coming Up · المراجعات القادمة",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F4A3D)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForecastCard(modifier = Modifier.weight(1f), title = "Today", count = today)
            ForecastCard(modifier = Modifier.weight(1f), title = "Tomorrow", count = tomorrow)
            ForecastCard(modifier = Modifier.weight(1f), title = "This Week", count = week)
        }
    }
}

@Composable
fun ForecastCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun WeeklyTrendCard(trend: List<com.sylergy.murajaah.progress.model.TrendWeek>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Weekly Consistency · الاتساق الأسبوعي",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEachIndexed { index, week ->
                    val isCurrent = index == trend.size - 1
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val barHeight = (week.fraction * 56).dp.coerceAtLeast(6.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (week.cardsReviewed == 0) Color(0xFFE5E7EB)
                                    else if (isCurrent) Color(0xFF15803D)
                                    else Color(0xFF2F4A3D)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = week.label,
                            fontSize = 9.sp,
                            color = if (isCurrent) Color(0xFF15803D) else Color(0xFF888888),
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JuzGridSection(juzList: List<JuzCell>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "By Juz · خريطة الأجزاء الثلاثين",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )

            // 6 columns x 5 rows = 30 Juz
            val rows = juzList.chunked(6)
            rows.forEach { rowJuzs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowJuzs.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (cell.status) {
                                        "strong" -> Color(0xFFDCFCE7)
                                        "review" -> Color(0xFFFEF3C7)
                                        "weak" -> Color(0xFFFEE2E2)
                                        "untested" -> Color(0xFFF3F4F6)
                                        else -> Color(0xFFFAF9F5)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when (cell.status) {
                                        "strong" -> Color(0xFF86EFAC)
                                        "review" -> Color(0xFFFDE68A)
                                        "weak" -> Color(0xFFFCA5A5)
                                        "untested" -> Color(0xFFD1D5DB)
                                        else -> Color(0xFFE5E7EB)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell.juz.toString(),
                                fontSize = 12.sp,
                                fontWeight = if (cell.status != null) FontWeight.Bold else FontWeight.Normal,
                                color = when (cell.status) {
                                    "strong" -> Color(0xFF15803D)
                                    "review" -> Color(0xFFB45309)
                                    "weak" -> Color(0xFFDC2626)
                                    "untested" -> Color(0xFF4B5563)
                                    else -> Color(0xFFBBBBBB)
                                }
                            )
                        }
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF15803D), label = "Strong")
                LegendItem(color = Color(0xFFD97706), label = "Review")
                LegendItem(color = Color(0xFFDC2626), label = "Weak")
                LegendItem(color = Color(0xFF9CA3AF), label = "Untested")
                LegendItem(color = Color(0xFFD1D5DB), label = "Empty")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 10.sp, color = Color(0xFF666666))
    }
}

@Composable
fun SurahProgressRow(item: SurahProgressItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.arabicName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Text(
                        text = item.surahName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                }

                Text(
                    text = if (item.mastery != null) "${(item.mastery * 100).roundToInt()}%" else "Untested",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.mastery != null) Color(0xFF15803D) else Color(0xFF888888)
                )
            }

            // Visual Surah segments
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    item.segments.forEach { segment ->
                        val segmentColor = when (segment.status) {
                            "strong" -> Color(0xFF15803D)
                            "review" -> Color(0xFFF59E0B)
                            "weak" -> Color(0xFFEF4444)
                            else -> Color(0xFFD1D5DB)
                        }
                        Box(
                            modifier = Modifier
                                .weight(segment.widthFraction.coerceAtLeast(0.01f))
                                .height(10.dp)
                                .background(segmentColor)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.memorizedAyahs} of ${item.totalAyahsInSurah} Ayahs memorized",
                    fontSize = 11.sp,
                    color = Color(0xFF777777)
                )
                Text(
                    text = "Surah ${item.surahNumber}",
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}
