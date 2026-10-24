package com.sylergy.murajaah.friends.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.friends.model.FriendRules

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)
private val CrimsonRed = Color(0xFF8B261D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friend: FriendEntity,
    onBack: () -> Unit,
    onListenToThem: (FriendEntity) -> Unit,
    onReciteToThem: (FriendEntity) -> Unit,
    onAskMutual: () -> Unit,
    onApprove: (FriendEntity) -> Unit,
    onDecline: (FriendEntity) -> Unit,
    onUpdateRole: (FriendEntity, String) -> Unit,
    onUpdateRemind: (FriendEntity, Boolean) -> Unit,
    onStopReviewing: (FriendEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStopDialog by remember { mutableStateOf(false) }
    val due = FriendRules.dueFor(friend)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = friend.name,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown,
                        fontSize = 20.sp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Calm relationship sentence
            Text(
                text = FriendRules.relation(friend),
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                color = MutedInk,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Pending approval banner if needed
            if (friend.reviewsYouStatus == "pending") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightParchment)
                        .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "${friend.name} requested to listen to your recitation.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepBrown
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove(friend) },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Approve / موافقة", color = Color.White, fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = { onDecline(friend) },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Decline / رفض", color = DeepBrown, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Primary actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (FriendRules.canListen(friend)) {
                    Button(
                        onClick = { onListenToThem(friend) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (due > 0) "اسمع له ($due)" else "اسمع له / Listen",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                if (FriendRules.canRecite(friend)) {
                    OutlinedButton(
                        onClick = { onReciteToThem(friend) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "سمّع له / Recite",
                            color = DeepBrown,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (!FriendRules.isMutual(friend)) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAskMutual,
                    colors = ButtonDefaults.buttonColors(containerColor = LightParchment),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "طلب مراجعة متبادلة / Ask for Mutual Tasmīʿ",
                        color = DeepBrown,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Together Stats Strip
            Text(
                text = "إحصائيات المراجعة معاً / Together in 30 Days",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MutedInk,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Heard You",
                    value = "${friend.reviewsYouCount} times",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "You Heard",
                    value = "${friend.youReviewCount} times",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Due Today",
                    value = "$due cards",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Companion Settings
            Text(
                text = "إعدادات الرفيق / Companion Settings",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MutedInk,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightParchment)
                    .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Weight of their assessments:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepBrown
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isTeacher = friend.reviewsYouRole == "teacher"
                        FilterChip(
                            selected = isTeacher,
                            onClick = { onUpdateRole(friend, "teacher") },
                            label = { Text("مُعلّم / Teacher (Authoritative)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = !isTeacher,
                            onClick = { onUpdateRole(friend, "peer") },
                            label = { Text("رفيق / Peer (Balanced)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Revision Reminder",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepBrown
                            )
                            Text(
                                text = "Remind me when ${friend.name} has cards waiting",
                                fontSize = 11.sp,
                                color = MutedInk
                            )
                        }
                        Switch(
                            checked = friend.youReviewRemind,
                            onCheckedChange = { onUpdateRemind(friend, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ForestGreen
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent recitations feed
            Text(
                text = "الجلسات الأخيرة / Recent Together",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MutedInk,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Feed samples
            RecentFeedCard(
                direction = "You heard",
                title = "Al-Kahf (الكهف) 1-10",
                timeAgo = "2 days ago · Peer review",
                rating = "Mutqan (Good)"
            )
            Spacer(modifier = Modifier.height(8.dp))
            RecentFeedCard(
                direction = "They heard you",
                title = "Al-Mulk (الملك) 1-30",
                timeAgo = "5 days ago · Examiner grade",
                rating = "Itqan (Easy)"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // End / Stop Reviewing Button
            OutlinedButton(
                onClick = { showStopDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إنهاء المراجعة معاً / Stop Reciting Together", color = CrimsonRed, fontSize = 13.sp)
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = {
                Text(
                    text = "Stop reciting together?",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
            },
            text = {
                Text(
                    text = "You and ${friend.name} will no longer be connected for tasmīʿ. You can always reconnect later with a new invite link.",
                    color = DeepBrown,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        onStopReviewing(friend)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Stop Connection", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Cancel", color = MutedInk)
                }
            },
            containerColor = ParchmentBg
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightParchment)
            .border(1.dp, BorderParchment, RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 11.sp, color = MutedInk)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
        }
    }
}

@Composable
private fun RecentFeedCard(
    direction: String,
    title: String,
    timeAgo: String,
    rating: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, BorderParchment, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "$direction: $title", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DeepBrown)
                Text(text = timeAgo, fontSize = 12.sp, color = MutedInk)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LightParchment)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = rating, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
            }
        }
    }
}
