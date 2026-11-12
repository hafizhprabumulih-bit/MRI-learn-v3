package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.animation.LearningAnimationScreen
import com.example.ui.character.CharacterRoomScreen
import com.example.ui.dialog.AvatarDialog
import com.example.ui.dialog.InfoDialog
import com.example.ui.dialog.LoadingScreen
import com.example.ui.dialog.StreakDialog
import com.example.ui.glossary.GlossaryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.module.ModuleDetailScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.saved.SavedScreen
import com.example.ui.simulators.SimulatorsScreen
import com.example.ui.theme.BelajarMriTheme
import com.example.ui.theme.CardSlateBorder
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanDark
import com.example.ui.theme.DarkNavySlate
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.TextMuted
import com.example.viewmodel.MainTab
import com.example.viewmodel.MriViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MriViewModel by viewModels()

    private var lastCheckedDate: java.time.LocalDate = java.time.LocalDate.now()

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    lastCheckedDate = java.time.LocalDate.now()
                    viewModel.refreshStreak()
                }
                Intent.ACTION_TIME_TICK -> {
                    val today = java.time.LocalDate.now()
                    if (today != lastCheckedDate) {
                        lastCheckedDate = today
                        viewModel.refreshStreak()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lastCheckedDate = java.time.LocalDate.now()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
        }
        ContextCompat.registerReceiver(
            this,
            dateChangeReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        runCatching { unregisterReceiver(dateChangeReceiver) }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        lastCheckedDate = java.time.LocalDate.now()
        viewModel.refreshStreak()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BelajarMriTheme {
                val state by viewModel.uiState.collectAsState()

                // P1: Saat loading, Back mempersingkat loading, bukan menutup aplikasi.
                androidx.activity.compose.BackHandler(enabled = state.isLoading) {
                    viewModel.finishLoading()
                }

                androidx.activity.compose.BackHandler(
                    enabled = !state.isLoading && (state.canNavigateBack || state.selectedModule != null || state.currentTab != MainTab.BERANDA)
                ) {
                    viewModel.navigateBack()
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepObsidian),
                    contentWindowInsets = WindowInsets.statusBars,
                    bottomBar = {
                        // Tampilkan bottom navigation di semua layar termasuk di dalam simpul materi (selama tidak loading)
                        if (!state.isLoading) {
                            val savedCount = state.bookmarkedNodeCodes.size + state.bookmarkedGlossaryTerms.size
                            MriBottomNavigationBar(
                                currentTab = state.currentTab,
                                isModuleDetailOpen = state.selectedModule != null,
                                savedCount = savedCount,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    val baseModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                    val contentModifier = if (state.isLoading) {
                        baseModifier.windowInsetsPadding(WindowInsets.navigationBars)
                    } else {
                        baseModifier
                    }
                    Box(
                        modifier = contentModifier
                    ) {
                        if (state.isLoading) {
                            LoadingScreen(
                                customMessage = state.loadingMessage,
                                onFinished = { viewModel.finishLoading() }
                            )
                        } else if (state.selectedModule != null) {
                            ModuleDetailScreen(
                                module = state.selectedModule!!,
                                state = state,
                                viewModel = viewModel
                            )
                        } else {
                            when (state.currentTab) {
                                MainTab.BERANDA -> HomeScreen(state = state, viewModel = viewModel)
                                MainTab.ANIMASI -> LearningAnimationScreen(state = state, viewModel = viewModel)
                                MainTab.SIMULASI -> SimulatorsScreen(state = state, viewModel = viewModel)
                                MainTab.TERSIMPAN -> SavedScreen(state = state, viewModel = viewModel)
                                MainTab.CATATAN -> NotesScreen(state = state, viewModel = viewModel)
                                MainTab.GLOSARIUM -> GlossaryScreen(state = state, viewModel = viewModel)
                                MainTab.KUIS -> QuizScreen(state = state, viewModel = viewModel)
                                MainTab.KARAKTER -> CharacterRoomScreen(state = state, viewModel = viewModel)
                            }
                        }

                        // Modal Info & Disclaimer
                        if (state.showInfoDialog) {
                            InfoDialog(onDismiss = { viewModel.setInfoDialogVisible(false) })
                        }

                        // Modal Streak Api 🔥
                        if (state.showStreakDialog) {
                            StreakDialog(
                                streakDays = state.studyStreakDays,
                                onDismiss = { viewModel.setStreakDialogVisible(false) }
                            )
                        }

                        // Modal Avatar Pengguna 👨‍⚕️
                        if (state.showAvatarDialog) {
                            AvatarDialog(
                                currentAvatarId = state.userAvatarId,
                                currentName = state.userName,
                                onDismiss = { viewModel.setAvatarDialogVisible(false) },
                                onSave = { avatarId, name -> viewModel.updateAvatar(avatarId, name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MriBottomNavigationBar(
    currentTab: MainTab,
    isModuleDetailOpen: Boolean = false,
    savedCount: Int = 0,
    onTabSelected: (MainTab) -> Unit
) {
    val items = listOf(
        NavigationItem(
            tab = MainTab.BERANDA,
            label = "Beranda",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        NavigationItem(
            tab = MainTab.SIMULASI,
            label = "Simulasi",
            selectedIcon = Icons.Filled.Tune,
            unselectedIcon = Icons.Outlined.Tune
        ),
        NavigationItem(
            tab = MainTab.CATATAN,
            label = "Catatan",
            selectedIcon = Icons.Filled.EditNote,
            unselectedIcon = Icons.Outlined.EditNote
        ),
        NavigationItem(
            tab = MainTab.KUIS,
            label = "Kuis",
            selectedIcon = Icons.Filled.Quiz,
            unselectedIcon = Icons.Outlined.Quiz
        ),
        NavigationItem(
            tab = MainTab.KARAKTER,
            label = "Karakter",
            selectedIcon = Icons.Filled.Face,
            unselectedIcon = Icons.Outlined.Face
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = CardSlateBorder)
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = DarkNavySlate,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = !isModuleDetailOpen && currentTab == item.tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(vertical = 3.dp, horizontal = 2.dp)
                        .testTag("nav_tab_${item.tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyanDark else Color.Transparent)
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (isSelected) CyanBright else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        if (item.tab == MainTab.TERSIMPAN && savedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (savedCount > 9) "9+" else "$savedCount",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepObsidian,
                                    lineHeight = 8.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanBright else TextMuted,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val tab: MainTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
