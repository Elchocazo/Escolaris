package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Feed : Screen("feed", "Inicio", Icons.Filled.Home, Icons.Outlined.Home)
    object Tasks : Screen("tasks", "Tareas", Icons.Filled.Assignment, Icons.Outlined.Assignment)
    val Homework = Tasks
    object Profile : Screen("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
    object Schedule : Screen("schedule", "Horario", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Scanner : Screen("scanner", "Registro", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt)

    object Rewards : Screen("rewards", "Tienda", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)
    object Gamification : Screen("gamification", "Ranking", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard)
    object ParentDashboard : Screen("parent", "Padres", Icons.Filled.FamilyRestroom, Icons.Outlined.FamilyRestroom)
    object TeacherAdmin : Screen("teacher_admin", "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)

    companion object {
        fun getNavItemsForRole(role: String?): List<Screen> {
            return when (role) {
                "TEACHER" -> listOf(Feed, TeacherAdmin, Profile, Tasks, Rewards)
                "PARENT" -> listOf(Feed, ParentDashboard, Profile, Tasks, Schedule)
                else -> listOf(Feed, Scanner, Profile, Tasks, Gamification)
            }
        }
    }
}
