package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.components.FloatingPillBottomNavBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AuthFlowScreen
import com.example.ui.screens.ExamScannerHistoryScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.GamificationLeaderboardScreen
import com.example.ui.screens.HomeworkExamsScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.RewardsCatalogScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TeacherAdminDashboardScreen
import com.example.ui.theme.EscolarisTheme
import com.example.ui.viewmodel.SchoolViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val colorTheme by viewModel.colorTheme.collectAsState()
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()
            val userMessage by viewModel.userMessage.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(userMessage) {
                userMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearUserMessage()
                }
            }

            EscolarisTheme(
                themeMode = themeMode,
                colorTheme = colorTheme
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = isAuthenticated,
                            label = "auth_gate_transition"
                        ) { authenticated ->
                            if (!authenticated) {
                                AuthFlowScreen(
                                    authService = viewModel.authService,
                                    onAuthSuccess = { user ->
                                        viewModel.onUserAuthenticated(user)
                                    },
                                    onShowMessage = { msg ->
                                        viewModel.showMessage(msg)
                                    }
                                )
                            } else {
                                EscolarisApp(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EscolarisApp(viewModel: SchoolViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val switchedFromParentId by viewModel.switchedFromParentId.collectAsState()

    // Role-dependent navigation items
    val currentRole = currentUser?.role ?: UserRole.STUDENT.code
    val isTeacherOrAdmin = UserRole.isTeacherOrAdmin(currentRole) || (currentUser?.email?.equals("moz658@gmail.com", ignoreCase = true) == true)
    val navItems = remember(currentRole, isTeacherOrAdmin) {
        when {
            isTeacherOrAdmin -> listOf(
                Screen.Feed,
                Screen.TeacherAdmin,
                Screen.Profile,
                Screen.Tasks,
                Screen.Schedule
            )
            currentRole.equals(UserRole.PARENT.code, ignoreCase = true) -> listOf(
                Screen.Feed,
                Screen.ParentDashboard,
                Screen.Profile,
                Screen.Tasks,
                Screen.Schedule
            )
            else -> listOf(
                Screen.Feed,
                Screen.Schedule,
                Screen.Profile,
                Screen.Tasks,
                Screen.Gamification
            )
        }
    }

    val defaultScreen = navItems.first()
    var currentScreen by remember { mutableStateOf<Screen>(defaultScreen) }
    val screenBackStack = remember { mutableStateListOf<Screen>() }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Interceptar botón atrás del sistema para volver a la pantalla anterior o principal
    BackHandler(enabled = currentScreen.route != defaultScreen.route || screenBackStack.isNotEmpty()) {
        if (screenBackStack.isNotEmpty()) {
            currentScreen = screenBackStack.removeAt(screenBackStack.lastIndex)
        } else {
            currentScreen = defaultScreen
        }
    }

    // Ensure active screen is allowed for role
    LaunchedEffect(currentRole) {
        if (!navItems.any { it.route == currentScreen.route }) {
            screenBackStack.clear()
            currentScreen = navItems.first()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        Scaffold(
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopHeaderBar(
                        currentUser = currentUser,
                        allUsers = allUsers,
                        syncState = syncState,
                        notifications = notifications,
                        themeMode = themeMode,
                        colorTheme = colorTheme,
                        onSwitchUser = { viewModel.switchUser(it) },
                        onTriggerSync = { viewModel.triggerCloudSync() },
                        onOpenNotifications = { showNotificationsDialog = true },
                        onNavigateToProfile = { currentScreen = Screen.Profile },
                        onThemeChange = { mode, cTheme ->
                            viewModel.setThemeMode(mode)
                            viewModel.setColorTheme(cTheme)
                        }
                    )

                    if (!switchedFromParentId.isNullOrBlank()) {
                        val parentUser = allUsers.find { it.id == switchedFromParentId }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📱", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Modo Estudiante (Teléfono de ${com.example.domain.validation.ValidationUtils.formatProperNoun(parentUser?.name?.split(" ")?.firstOrNull() ?: "Acudiente")})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.switchBackToParentProfile() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Volver a Acudiente ↩️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    FloatingPillBottomNavBar(
                        currentRoute = currentScreen.route,
                        navItems = navItems,
                        currentUser = currentUser,
                        onNavigate = { targetScreen ->
                            if (currentScreen.route != targetScreen.route) {
                                if (screenBackStack.lastOrNull()?.route != currentScreen.route) {
                                    screenBackStack.add(currentScreen)
                                }
                                currentScreen = targetScreen
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Wide Screen Tablet Navigation Rail
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { screen ->
                            NavigationRailItem(
                                selected = currentScreen.route == screen.route,
                                onClick = {
                                    if (currentScreen.route != screen.route) {
                                        if (screenBackStack.lastOrNull()?.route != currentScreen.route) {
                                            screenBackStack.add(currentScreen)
                                        }
                                        currentScreen = screen
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen.route == screen.route) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                modifier = Modifier.testTag("nav_rail_${screen.route}")
                            )
                        }
                    }
                }

                // Main Dynamic Content Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Feed -> FeedScreen(viewModel = viewModel)
                            is Screen.Tasks -> HomeworkExamsScreen(viewModel = viewModel)
                            is Screen.Schedule -> ScheduleScreen(viewModel = viewModel)
                            is Screen.Scanner -> ExamScannerHistoryScreen(viewModel = viewModel)
                            is Screen.Gamification -> GamificationLeaderboardScreen(viewModel = viewModel)
                            is Screen.Rewards -> RewardsCatalogScreen(viewModel = viewModel)
                            is Screen.ParentDashboard -> ParentDashboardScreen(viewModel = viewModel)
                            is Screen.TeacherAdmin -> TeacherAdminDashboardScreen(viewModel = viewModel)
                            is Screen.Profile -> ProfileSettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onMarkRead = { viewModel.markNotificationRead(it) },
            onMarkAllRead = { viewModel.markAllNotificationsRead() }
        )
    }
}
