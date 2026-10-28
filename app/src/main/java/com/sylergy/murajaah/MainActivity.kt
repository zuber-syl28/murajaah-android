package com.sylergy.murajaah

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.auth.AuthViewModel
import com.sylergy.murajaah.auth.model.AuthState
import com.sylergy.murajaah.auth.model.UserSession
import com.sylergy.murajaah.auth.storage.AuthPreferences
import com.sylergy.murajaah.auth.ui.AuthScreen
import com.sylergy.murajaah.onboarding.OnboardingViewModel
import com.sylergy.murajaah.onboarding.ui.OnboardingScreen
import com.sylergy.murajaah.progress.ProgressViewModel
import com.sylergy.murajaah.progress.ui.ProgressScreen
import com.sylergy.murajaah.achievements.AchievementsViewModel
import com.sylergy.murajaah.achievements.ui.AchievementsScreen
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import com.sylergy.murajaah.friends.FriendsViewModel
import com.sylergy.murajaah.friends.repository.FriendsRepository
import com.sylergy.murajaah.friends.scanner.JoinUrlParser
import com.sylergy.murajaah.friends.ui.FriendDetailScreen
import com.sylergy.murajaah.friends.ui.FriendsScreen
import com.sylergy.murajaah.friends.ui.JoinScreen
import com.sylergy.murajaah.mushaf.MushafCanvas
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.network.api.NetworkClient
import com.sylergy.murajaah.network.sync.SyncRepository
import com.sylergy.murajaah.network.sync.SyncWorker
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.quran.NarrationData
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.reviewing.ReviewingViewModel
import com.sylergy.murajaah.reviewing.model.ExaminerPhase
import com.sylergy.murajaah.reviewing.ui.ReviewingScreen
import com.sylergy.murajaah.voice.VoiceSessionViewModel
import com.sylergy.murajaah.voice.model.VoiceStatus
import com.sylergy.murajaah.voice.ui.VoiceBar
import com.sylergy.murajaah.reminders.RemindersViewModel
import com.sylergy.murajaah.reminders.ui.ReminderSettingsScreen
import com.sylergy.murajaah.settings.SettingsViewModel
import com.sylergy.murajaah.settings.ui.PlanChips
import com.sylergy.murajaah.settings.ui.PlanEditorScreen
import com.sylergy.murajaah.settings.ui.SettingsScreen
import com.sylergy.murajaah.scheduler.DateUtils
import com.sylergy.murajaah.scheduler.MurajaahScheduler
import com.sylergy.murajaah.scheduler.ReviewerRole
import com.sylergy.murajaah.session.SessionViewModel
import com.sylergy.murajaah.session.ui.FreeTestScreen
import com.sylergy.murajaah.session.ui.SessionScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

enum class MainTab {
    REVISION,
    PROGRESS,
    FRIENDS
}

class MainActivity : ComponentActivity() {
    private val pendingDeepLink = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            MurajaahTheme {
                val context = LocalContext.current
                val db = remember { MurajaahDatabase.getInstance(context) }
                val authPreferences = remember { AuthPreferences(context) }
                val authViewModel = remember { AuthViewModel(authPreferences) }
                val onboardingViewModel = remember { OnboardingViewModel(db, authPreferences) }
                val apiClient = remember { NetworkClient.create() }
                val syncRepo = remember { SyncRepository(apiClient, db) }
                val homeViewModel = remember { HomeViewModel(db, syncRepo) }
                val sessionViewModel = remember { SessionViewModel(db, syncRepo) }
                val progressViewModel = remember { ProgressViewModel(db) }
                val achievementsViewModel = remember { AchievementsViewModel(db) }
                val friendsRepo = remember { FriendsRepository(db.friendDao(), apiClient) }
                val friendsViewModel = remember { FriendsViewModel(friendsRepo) }
                val reviewingViewModel = remember { ReviewingViewModel() }
                val voiceViewModel = remember { VoiceSessionViewModel(apiClient) }
                val remindersViewModel = remember { RemindersViewModel(application) }
                val settingsViewModel = remember { SettingsViewModel(db, authPreferences, apiClient) }

                val authState by authViewModel.authState.collectAsState()
                val voiceStatus by voiceViewModel.status.collectAsState()
                val deepLinkToken by pendingDeepLink.collectAsState()
                var isSessionActive by remember { mutableStateOf(false) }
                var isViewingFreeTest by remember { mutableStateOf(false) }
                var isViewingAchievements by remember { mutableStateOf(false) }
                var isViewingReminders by remember { mutableStateOf(false) }
                var isViewingSettings by remember { mutableStateOf(false) }
                var isEditingPlan by remember { mutableStateOf(false) }
                var editingPlanId by remember { mutableStateOf<String?>(null) }
                var selectedFriendId by remember { mutableStateOf<String?>(null) }
                var reviewingCompanion by remember { mutableStateOf<FriendEntity?>(null) }
                var currentTab by remember { mutableStateOf(MainTab.REVISION) }
                val reviewingState by reviewingViewModel.state.collectAsState()

                LaunchedEffect(reviewingCompanion, reviewingState.currentCard, reviewingState.phase) {
                    val card = reviewingState.currentCard
                    if (reviewingCompanion != null && card != null && reviewingState.phase == ExaminerPhase.LISTENING) {
                        voiceViewModel.sharePassage(card.surahName, card.surahArabicName, card.fromAyah, card.toAyah)
                    }
                }

                when (val state = authState) {
                    AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF8F7F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2F4A3D))
                        }
                    }
                    AuthState.Unauthenticated -> {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = { /* reactive state handles transition */ },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is AuthState.Guest, is AuthState.Authenticated -> {
                        val session = if (state is AuthState.Guest) state.session else (state as AuthState.Authenticated).session
                        if (!session.onboarded) {
                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                onFinishOnboarding = {
                                    authViewModel.markOnboarded()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                VoiceBar(
                                    status = voiceStatus,
                                    onJoin = { callId -> voiceViewModel.joinCall(callId) },
                                    onToggleMute = { voiceViewModel.toggleMute() },
                                    onEnd = { voiceViewModel.endCall() },
                                    onDismiss = { voiceViewModel.dismiss() },
                                    onAcceptWaiting = { voiceViewModel.simulateFriendAnswers() }
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (deepLinkToken != null) {
                                        JoinScreen(
                                            token = deepLinkToken!!,
                                            repository = friendsRepo,
                                            onSuccess = {
                                                pendingDeepLink.value = null
                                                currentTab = MainTab.FRIENDS
                                            },
                                            onBack = {
                                                pendingDeepLink.value = null
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (reviewingCompanion != null) {
                                        ReviewingScreen(
                                            viewModel = reviewingViewModel,
                                            onClose = {
                                                reviewingCompanion = null
                                                voiceViewModel.endCall()
                                            },
                                            onStartListening = {
                                                voiceViewModel.simulateFriendAnswers()
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (selectedFriendId != null) {
                                        val friendFlow = remember(selectedFriendId) { friendsRepo.observeFriend(selectedFriendId!!) }
                                        val friend by friendFlow.collectAsState(initial = null)
                                        if (friend != null) {
                                            FriendDetailScreen(
                                                friend = friend!!,
                                                onBack = { selectedFriendId = null },
                                                onListenToThem = { companion ->
                                                    reviewingViewModel.initialize(
                                                        companionId = companion.userId,
                                                        companionName = companion.name,
                                                        companionArabicName = null,
                                                        role = if (companion.youReviewRole == "teacher") ReviewerRole.Teacher else ReviewerRole.Peer
                                                    )
                                                    voiceViewModel.hearFrom(companion.userId, companion.name)
                                                    selectedFriendId = null
                                                    reviewingCompanion = companion
                                                },
                                                onReciteToThem = {
                                                    selectedFriendId = null
                                                    voiceViewModel.reciteTo(friend!!.userId, friend!!.name)
                                                },
                                                onAskMutual = { friendsViewModel.openInviteSheet() },
                                                onApprove = { friendsViewModel.approveRequest(it) },
                                                onDecline = { friendsViewModel.declineRequest(it) },
                                                onUpdateRole = { f, r -> friendsViewModel.updateRole(f, r) },
                                                onUpdateRemind = { f, rem -> friendsViewModel.updateRemind(f, rem) },
                                                onStopReviewing = { friendsViewModel.stopReviewing(it) },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFFBF8F1)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = Color(0xFF2E6F40))
                                            }
                                        }
                                    } else if (isViewingFreeTest) {
                                        FreeTestScreen(
                                            database = db,
                                            onStartFreeSession = { card ->
                                                isViewingFreeTest = false
                                                sessionViewModel.startFreeSession(card)
                                                isSessionActive = true
                                            },
                                            onBack = { isViewingFreeTest = false },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (isSessionActive) {
                                        SessionScreen(
                                            viewModel = sessionViewModel,
                                            onFinishSession = { isSessionActive = false },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (isViewingAchievements) {
                                        AchievementsScreen(
                                            viewModel = achievementsViewModel,
                                            onBack = { isViewingAchievements = false },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (isViewingReminders) {
                                        ReminderSettingsScreen(
                                            viewModel = remindersViewModel,
                                            onBack = { isViewingReminders = false },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (isEditingPlan) {
                                        PlanEditorScreen(
                                            viewModel = settingsViewModel,
                                            planId = editingPlanId,
                                            onBack = { isEditingPlan = false },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (isViewingSettings) {
                                        SettingsScreen(
                                            viewModel = settingsViewModel,
                                            onBack = { isViewingSettings = false },
                                            onOpenPlanEditor = { planId ->
                                                editingPlanId = planId
                                                isEditingPlan = true
                                            },
                                            onOpenReminders = { isViewingReminders = true },
                                            onOpenSetup = { /* manage memorized */ },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Scaffold(
                                            modifier = Modifier.fillMaxSize(),
                                            containerColor = Color(0xFFF8F7F2), // Warm Cream Mushaf Parchment
                                            bottomBar = {
                                                NavigationBar(
                                                    containerColor = Color.White,
                                                    tonalElevation = 3.dp
                                                ) {
                                                    NavigationBarItem(
                                                        selected = currentTab == MainTab.REVISION,
                                                        onClick = { currentTab = MainTab.REVISION },
                                                        icon = { Text("📖", fontSize = 18.sp) },
                                                        label = {
                                                            Text(
                                                                "Revision · وِرد",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (currentTab == MainTab.REVISION) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        },
                                                        colors = NavigationBarItemDefaults.colors(
                                                            selectedIconColor = Color(0xFF2F4A3D),
                                                            selectedTextColor = Color(0xFF2F4A3D),
                                                            indicatorColor = Color(0xFFE8F0EC)
                                                        )
                                                    )
                                                    NavigationBarItem(
                                                        selected = currentTab == MainTab.PROGRESS,
                                                        onClick = { currentTab = MainTab.PROGRESS },
                                                        icon = { Text("📊", fontSize = 18.sp) },
                                                        label = {
                                                            Text(
                                                                "Progress · إتقان",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (currentTab == MainTab.PROGRESS) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        },
                                                        colors = NavigationBarItemDefaults.colors(
                                                            selectedIconColor = Color(0xFF2F4A3D),
                                                            selectedTextColor = Color(0xFF2F4A3D),
                                                            indicatorColor = Color(0xFFE8F0EC)
                                                        )
                                                    )
                                                    NavigationBarItem(
                                                        selected = currentTab == MainTab.FRIENDS,
                                                        onClick = { currentTab = MainTab.FRIENDS },
                                                        icon = { Text("👥", fontSize = 18.sp) },
                                                        label = {
                                                            Text(
                                                                "Friends · الرفقاء",
                                                                fontSize = 11.sp,
                                                                fontWeight = if (currentTab == MainTab.FRIENDS) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        },
                                                        colors = NavigationBarItemDefaults.colors(
                                                            selectedIconColor = Color(0xFF2F4A3D),
                                                            selectedTextColor = Color(0xFF2F4A3D),
                                                            indicatorColor = Color(0xFFE8F0EC)
                                                        )
                                                    )
                                                }
                                            }
                                        ) { innerPadding ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(innerPadding)
                                            ) {
                                                UserProfileHeader(
                                                    session = session,
                                                    onSignOut = { authViewModel.signOut() },
                                                    onOpenReminders = { isViewingReminders = true },
                                                    onOpenSettings = { isViewingSettings = true }
                                                )

                                                when (currentTab) {
                                                    MainTab.REVISION -> {
                                                        HomeScreen(
                                                            homeViewModel = homeViewModel,
                                                            settingsViewModel = settingsViewModel,
                                                            onOpenPlanEditor = { planId ->
                                                                editingPlanId = planId
                                                                isEditingPlan = true
                                                            },
                                                            onStartSession = {
                                                                sessionViewModel.startSession(revisionStyle = session.revisionStyle)
                                                                isSessionActive = true
                                                            },
                                                            onOpenFreeTest = {
                                                                isViewingFreeTest = true
                                                            },
                                                            onNavigateToFriends = {
                                                                currentTab = MainTab.FRIENDS
                                                            },
                                                            onListenToCompanion = { companion ->
                                                                reviewingViewModel.initialize(
                                                                    companionId = companion.userId,
                                                                    companionName = companion.name,
                                                                    companionArabicName = null,
                                                                    role = if (companion.youReviewRole == "teacher") ReviewerRole.Teacher else ReviewerRole.Peer
                                                                )
                                                                voiceViewModel.hearFrom(companion.userId, companion.name)
                                                                reviewingCompanion = companion
                                                            },
                                                            onReciteToCompanion = { companion ->
                                                                voiceViewModel.reciteTo(companion.userId, companion.name)
                                                            },
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    MainTab.PROGRESS -> {
                                                        ProgressScreen(
                                                            viewModel = progressViewModel,
                                                            onNavigateToCard = {
                                                                sessionViewModel.startSession()
                                                                isSessionActive = true
                                                            },
                                                            onNavigateToAchievements = {
                                                                isViewingAchievements = true
                                                            },
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    MainTab.FRIENDS -> {
                                                        FriendsScreen(
                                                            viewModel = friendsViewModel,
                                                            onBack = { currentTab = MainTab.REVISION },
                                                            onOpenFriendDetail = { friendId ->
                                                                selectedFriendId = friendId
                                                            },
                                                            onListenToThem = { companion ->
                                                                reviewingViewModel.initialize(
                                                                    companionId = companion.userId,
                                                                    companionName = companion.name,
                                                                    companionArabicName = null,
                                                                    role = if (companion.youReviewRole == "teacher") ReviewerRole.Teacher else ReviewerRole.Peer
                                                                )
                                                                voiceViewModel.hearFrom(companion.userId, companion.name)
                                                                reviewingCompanion = companion
                                                            },
                                                            onReciteToThem = { companion ->
                                                                voiceViewModel.reciteTo(companion.userId, companion.name)
                                                            },
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val dataString = intent?.dataString
        if (!dataString.isNullOrBlank()) {
            val token = JoinUrlParser.parseToken(dataString)
            if (!token.isNullOrBlank()) {
                pendingDeepLink.value = token
            }
        }
    }
}

@Composable
fun UserProfileHeader(
    session: UserSession,
    onSignOut: () -> Unit,
    onOpenReminders: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.sylergy.murajaah.ui.MurajaahLogo(size = 28.dp)
            Text(
                text = "Salam, ${session.name}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
            Box(
                modifier = Modifier
                    .background(if (session.isGuest) Color(0xFFE0F2FE) else Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (session.isGuest) "Guest Mode" else "Cloud Sync",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (session.isGuest) Color(0xFF0369A1) else Color(0xFF15803D)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenReminders,
                modifier = Modifier.size(36.dp)
            ) {
                Text("🔔", fontSize = 16.sp)
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(36.dp)
            ) {
                Text("⚙️", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onSignOut) {
                Text("Sign Out", fontSize = 12.sp, color = Color(0xFF888888))
            }
        }
    }
}

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    onOpenPlanEditor: (String?) -> Unit = {},
    onStartSession: () -> Unit,
    onOpenFreeTest: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {},
    onListenToCompanion: (FriendEntity) -> Unit = {},
    onReciteToCompanion: (FriendEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeState by homeViewModel.uiState.collectAsState()
    val passageAyahs = remember { MushafRepository.getPassageText(67, 1, 10) }
    var slippedAyahs: Set<Int> by remember { mutableStateOf(emptySet()) }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(8.dp))
            com.sylergy.murajaah.ui.MurajaahLogo(size = 64.dp)
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Structured Qur'an revision with the people who help you retain it.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF555555),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Revision Plan Bar (PlanChips)
        item {
            PlanChips(
                viewModel = settingsViewModel,
                onOpenPlanEditor = onOpenPlanEditor,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Primary Wird Launch CTA
        item {
            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F4A3D)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Start Revision (Wird)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8F7F2)
                    )
                    Text(
                        text = if (homeState.activePlan != null) "${homeState.activePlan!!.name} · ${homeState.dueCount} Cards due" else "Focus Mode · ${homeState.dueCount} Cards due today",
                        fontSize = 11.sp,
                        color = Color(0xFFF8F7F2).copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Secondary Free Tasmīʿ CTA
        item {
            OutlinedButton(
                onClick = onOpenFreeTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✨", fontSize = 16.sp)
                    Text(
                        text = "Free Tasmīʿ (Pick any Ayah) · تسميع حر",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                }
            }
        }

        // Companions quick banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (homeState.dueCompanions.isNotEmpty()) {
                        val firstComp = homeState.dueCompanions.first()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToFriends() }
                        ) {
                            Text(
                                text = "🎧 استماع لـ ${firstComp.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "${firstComp.name} has ${firstComp.youReviewDueToday} cards due to hear",
                                fontSize = 12.sp,
                                color = Color(0xFF7A6B56)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onReciteToCompanion(firstComp) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🎙️ Recite · أسمِع", fontSize = 12.sp, color = Color(0xFF2F4A3D))
                            }
                            Button(
                                onClick = { onListenToCompanion(firstComp) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Listen · أستمع", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "الرفقاء / Revision Companions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF2F4A3D)
                            )
                            Text(
                                text = "Recite to friends or listen to what they have scheduled",
                                fontSize = 12.sp,
                                color = Color(0xFF7A6B56)
                            )
                        }
                        Text("👥 →", fontSize = 16.sp, color = Color(0xFF2F4A3D))
                    }
                }
            }
        }

        // Live Interactive Mushaf Canvas (Issue #3)
        item {
            MushafCanvas(
                surahNumber = 67,
                fromAyah = 1,
                toAyah = 10,
                ayahs = passageAyahs,
                narration = Narration.HAFS,
                slippedAyahs = slippedAyahs,
                onAyahToggled = { ayah ->
                    val next = slippedAyahs.toMutableSet()
                    if (next.contains(ayah)) {
                        next.remove(ayah)
                    } else {
                        next.add(ayah)
                    }
                    slippedAyahs = next
                }
            )
        }

        // Offline Room Database Status Card (Issue #6)
        item {
            StatusCard(
                title = "Offline Room Database",
                badge = if (homeState.cardCount > 0) "Local Cache Active" else "Empty Cache"
            ) {
                InfoRow("Stored Cards", "${homeState.cardCount} cards in Room")
                InfoRow("Due for Revision", "${homeState.dueCount} cards")
                InfoRow("Memorized Ranges", "${homeState.rangeCount} active ranges")
                InfoRow("Outbox Pending Grades", "${homeState.pendingOutboxCount} unsynced")

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            homeViewModel.seedHifz {
                                Toast.makeText(context, "Seeded Al-Mulk cards to Room", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Seed Hifz", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            homeViewModel.gradeOffline { clientId ->
                                Toast.makeText(context, "Recorded grade offline: $clientId", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Grade Offline", fontSize = 11.sp)
                    }
                }
            }
        }

        // Delta Synchronization Engine Card (Issue #7)
        item {
            StatusCard(
                title = "WorkManager Delta Sync",
                badge = if (homeState.pendingOutboxCount > 0) "Needs Sync (${homeState.pendingOutboxCount})" else "In Sync"
            ) {
                InfoRow("Endpoint", "https://murajaah.sylergy-ltd.workers.dev")
                InfoRow("Sync Method", "Oldest-first replay (/grades/sync)")
                InfoRow("Background Worker", "WorkManager (Network Connected)")

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        SyncWorker.enqueue(context)
                        homeViewModel.triggerDeltaSync { res ->
                            when (res) {
                                is com.sylergy.murajaah.network.sync.SyncResult.Success -> {
                                    Toast.makeText(context, "Sync complete! Applied: ${res.applied}, Skipped: ${res.skipped}", Toast.LENGTH_LONG).show()
                                }
                                is com.sylergy.murajaah.network.sync.SyncResult.Error -> {
                                    Toast.makeText(context, "Outbox enqueued for WorkManager (${res.message})", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D))
                ) {
                    Text("Trigger Delta Sync", fontSize = 13.sp, color = Color.White)
                }
            }
        }

        // Qur'an Metadata Status Card (Issue #2)
        item {
            StatusCard(
                title = "Qur'an Domain Engine",
                badge = "114 Surahs · 6,236 Ayahs"
            ) {
                InfoRow("Ḥafṣ (Kufan)", "${QuranData.TOTAL_AYAHS} ayahs")
                InfoRow("Warsh & Qālūn (Madani)", "${NarrationData.totalAyahsIn(Narration.WARSH)} ayahs")
                InfoRow("Ad-Dūrī & As-Sūsī (Basran)", "${NarrationData.totalAyahsIn(Narration.DOORI)} ayahs")
                InfoRow("Al-Bazzī & Qunbul (Meccan)", "${NarrationData.totalAyahsIn(Narration.BAZZI)} ayahs")
                InfoRow("Partitions", "${QuranData.HIZB_COUNT} Ahzab · 30 Ajzāʾ")
            }
        }

        // FSRS-6 & Khatmah Scheduler Card (Issue #4 & Issue #5)
        item {
            StatusCard(
                title = "FSRS-6 & Khatmah Scheduler",
                badge = "21 Weights · Verified"
            ) {
                InfoRow("Request Retention", "90% target retrievability")
                InfoRow("Maximum Interval", "${MurajaahScheduler.MAX_STABILITY_DAYS} days")
                InfoRow("Khatmah Cycle (1-5 Juz)", "7 days")
                InfoRow("Khatmah Cycle (30 Juz)", "30 days")
                InfoRow("Teacher-Hold Safety Brake", "30 days active")
                InfoRow("Peer Growth Weight", "0.625 discount")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    badge: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF2F4A3D).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2F4A3D)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF222222)
        )
    }
}

@Composable
fun MurajaahTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
