package com.sylergy.murajaah.achievements.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.achievements.AchievementsViewModel
import com.sylergy.murajaah.achievements.model.AchievementItem
import com.sylergy.murajaah.achievements.model.AchievementsState

@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel,
    onBack: () -> Unit,
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
                TextButton(onClick = onBack) {
                    Text("← Back", fontSize = 14.sp, color = Color(0xFF2F4A3D), fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "إنجازات",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Text(
                    text = "Permanent Milestones & Companion Recitations",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEBE0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "What revising has earned: your own revision first, then both sides of the two-person loop. Nothing here is ranked against anyone, nothing counts days in a row, and nothing once earned is taken away.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // Section 1: Your Revision
        item {
            AchievementSection(
                title = "Your Revision · مراجعتك",
                kicker = "STUDENT",
                earnedCount = state.earnedSelfCount,
                totalCount = state.totalSelfCount,
                items = state.selfList
            )
        }

        // Section 2: From Companions
        item {
            AchievementSection(
                title = "From Companions · من أصحابك",
                kicker = "RECITED FOR COMPANIONS",
                earnedCount = state.studentList.count { it.earned },
                totalCount = state.studentList.size,
                items = state.studentList
            )
        }

        // Section 3: For Companions
        item {
            AchievementSection(
                title = "For Companions · لأصحابك",
                kicker = "LISTENING FOR COMPANIONS",
                earnedCount = state.reviewerList.count { it.earned },
                totalCount = state.reviewerList.size,
                items = state.reviewerList
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AchievementSection(
    title: String,
    kicker: String,
    earnedCount: Int,
    totalCount: Int,
    items: List<AchievementItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = kicker,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF888888)
                )
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFFE8F0EC), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$earnedCount of $totalCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
            }
        }

        items.forEach { item ->
            AchievementCard(item = item)
        }
    }
}

@Composable
fun AchievementCard(item: AchievementItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.earned) Color.White else Color(0xFFFCFBF9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.earned) 1.dp else 0.dp),
        border = if (item.earned) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.earned) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF15803D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column {
                        Text(
                            text = item.titleEn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.earned) Color(0xFF2F4A3D) else Color(0xFF444444)
                        )
                        Text(
                            text = item.titleAr,
                            fontSize = 12.sp,
                            color = Color(0xFF777777)
                        )
                    }
                }

                if (item.earned && item.earnedAt != null) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Earned · ${item.earnedAt}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                } else if (!item.earned && item.target > 1) {
                    Text(
                        text = "${item.current} / ${item.target}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF888888)
                    )
                }
            }

            Text(
                text = "${item.bodyEn} · ${item.bodyAr}",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF666666)
            )

            if (!item.earned && item.target > 1) {
                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF2F4A3D),
                    trackColor = Color(0xFFE5E7EB)
                )
            }
        }
    }
}
