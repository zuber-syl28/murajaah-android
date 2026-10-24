package com.sylergy.murajaah.friends.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.friends.FriendsViewModel
import com.sylergy.murajaah.friends.model.FriendRules

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    onBack: () -> Unit,
    onOpenFriendDetail: (String) -> Unit,
    onListenToThem: (FriendEntity) -> Unit = {},
    onReciteToThem: (FriendEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.joinSuccess) {
        uiState.joinSuccess?.let { name ->
            snackbarHostState.showSnackbar("Connected with $name for mutual revision!")
            viewModel.clearJoinSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "الرفقاء / Friends & Circle",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Companions for mutual tasmīʿ and review",
                            fontSize = 12.sp,
                            color = MutedInk
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp, color = DeepBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ParchmentBg)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParchmentBg)
                    .border(1.dp, BorderParchment)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.openInviteSheet() },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("دعوة رفيق / Invite", color = Color.White, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openScanDialog() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مسح رمز / Scan QR", color = DeepBrown, fontSize = 13.sp)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ParchmentBg,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Calm Header Lede
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightParchment)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Everyone you do tasmīʿ with, once each. Recite to companions or listen to what they have scheduled today.",
                        fontSize = 13.sp,
                        color = DeepBrown,
                        lineHeight = 18.sp
                    )
                }
            }

            // Requests Pending Approval
            if (uiState.requests.isNotEmpty()) {
                item {
                    Text(
                        text = "طلبات بانتظار الموافقة / Needs Approval",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedInk,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(uiState.requests, key = { it.userId }) { friend ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, BorderParchment, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = friend.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBrown
                                )
                                Text(
                                    text = "Asked to listen to your recitation",
                                    fontSize = 12.sp,
                                    color = MutedInk
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.approveRequest(friend) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Approve", color = Color.White, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.declineRequest(friend) },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Decline", color = DeepBrown, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Companions Section Header
            item {
                Text(
                    text = "رفقاء المراجعة / Active Companions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Empty state
            if (uiState.friends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "لا يوجد رفقاء بعد",
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                color = MutedInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invite a companion or scan their QR code to recite together.",
                                fontSize = 13.sp,
                                color = MutedInk,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Active Companions List
            items(uiState.friends, key = { it.userId }) { friend ->
                CompanionRowCard(
                    friend = friend,
                    onOpen = { onOpenFriendDetail(friend.userId) },
                    onListen = { onListenToThem(friend) },
                    onRecite = { onReciteToThem(friend) },
                    onAskMutual = { viewModel.openInviteSheet() }
                )
            }
        }
    }

    if (uiState.showInviteSheet) {
        InviteBottomSheet(
            invite = uiState.invite,
            onDismiss = { viewModel.closeInviteSheet() }
        )
    }

    if (uiState.showScanDialog) {
        ScanQrDialog(
            isLoading = uiState.isLoading,
            preview = uiState.joinPreview,
            errorMessage = uiState.joinError,
            onScan = { viewModel.onScanCode(it) },
            onAccept = { viewModel.acceptInvite(it) },
            onDismiss = { viewModel.closeScanDialog() }
        )
    }
}

@Composable
private fun CompanionRowCard(
    friend: FriendEntity,
    onOpen: () -> Unit,
    onListen: () -> Unit,
    onRecite: () -> Unit,
    onAskMutual: () -> Unit,
    modifier: Modifier = Modifier
) {
    val due = FriendRules.dueFor(friend)

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderParchment),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = FriendRules.rowLine(friend),
                        fontSize = 12.sp,
                        color = if (due > 0) ForestGreen else MutedInk,
                        fontWeight = if (due > 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                // If due > 0, show badge
                if (due > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightParchment)
                            .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$due due",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (FriendRules.canListen(friend)) {
                    Button(
                        onClick = onListen,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("اسمع له / Listen", color = Color.White, fontSize = 12.sp)
                    }
                }
                if (FriendRules.canRecite(friend)) {
                    OutlinedButton(
                        onClick = onRecite,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("سمّع له / Recite", color = DeepBrown, fontSize = 12.sp)
                    }
                }
                if (!FriendRules.isMutual(friend)) {
                    OutlinedButton(
                        onClick = onAskMutual,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مراجعة متبادلة", color = DeepBrown, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
