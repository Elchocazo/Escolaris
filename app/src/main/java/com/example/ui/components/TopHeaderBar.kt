package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.DarkThemeMode
import com.example.ui.theme.StreakOrange
import com.example.ui.viewmodel.SyncState

@Composable
fun TopHeaderBar(
    currentUser: UserEntity?,
    allUsers: List<UserEntity> = emptyList(),
    syncState: SyncState,
    notifications: List<NotificationEntity>,
    themeMode: DarkThemeMode,
    colorTheme: AppColorTheme,
    onSwitchUser: (String) -> Unit = {},
    onTriggerSync: () -> Unit,
    onOpenNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onThemeChange: (DarkThemeMode, AppColorTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var syncMenuExpanded by remember { mutableStateOf(false) }
    var userMenuExpanded by remember { mutableStateOf(false) }
    var showLevelAchievementsDialog by remember { mutableStateOf(false) }
    var showMyPhotoDialog by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { !it.isRead }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showMyPhotoDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(currentUser?.avatarColorHex ?: 0xFF2563EB),
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentUser?.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = currentUser?.photoUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                        } else if (!currentUser?.avatarEmoji.isNullOrBlank()) {
                            Text(
                                text = currentUser?.avatarEmoji ?: "🎓",
                                fontSize = 18.sp
                            )
                        } else {
                            Text(
                                text = currentUser?.avatarInitials ?: "ES",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = com.example.domain.validation.ValidationUtils.formatProperNoun(currentUser?.name ?: ""),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val roleLabel = when (currentUser?.role) {
                            UserRole.PARENT.code -> "👨‍👩‍👧 Tutor • ${currentUser.gradeSection.ifBlank { "Familia" }}"
                            UserRole.TEACHER.code -> {
                                val grade = currentUser.gradeSection.trim()
                                if (grade.isBlank() || grade.equals("Docente Titular", ignoreCase = true)) {
                                    "👨‍🏫 Docente Titular"
                                } else {
                                    "👨‍🏫 Docente • $grade"
                                }
                            }
                            UserRole.STUDENT.code -> {
                                val cleanGrade = currentUser.gradeSection.replace(" - Sección A", "").ifBlank { "Estudiante" }
                                "🎓 $cleanGrade"
                            }
                            else -> if (currentUser != null) "🎓 ${currentUser.gradeSection}" else ""
                        }

                        if (roleLabel.isNotBlank()) {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Action icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentUser?.role == UserRole.STUDENT.code) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showLevelAchievementsDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("⭐ Nv. ${currentUser.level}", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔥 ${currentUser.streakDays}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StreakOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🪙 ${currentUser.credits}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    // Cloud Sync Button
                    IconButton(
                        onClick = onTriggerSync,
                        modifier = Modifier.size(36.dp).testTag("cloud_sync_button")
                    ) {
                        when (syncState) {
                            SyncState.SYNCING -> Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sincronizando",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            SyncState.OFFLINE -> Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Modo sin conexión",
                                tint = MaterialTheme.colorScheme.error
                            )
                            SyncState.ERROR -> Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Error de sincronización",
                                tint = MaterialTheme.colorScheme.error
                            )
                            SyncState.SYNCED -> Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Sincronizado",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Notification Bell
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.size(36.dp).testTag("notification_bell_button")
                    ) {
                        if (unreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge { Text("$unreadCount") }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notificaciones",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMyPhotoDialog && currentUser != null) {
        ProfilePhotoViewerDialog(
            photoUri = currentUser.photoUri,
            avatarEmoji = currentUser.avatarEmoji,
            userName = currentUser.name,
            userRole = currentUser.role,
            gradeSection = currentUser.gradeSection,
            onEditProfile = {
                showMyPhotoDialog = false
                onNavigateToProfile()
            },
            onDismiss = { showMyPhotoDialog = false }
        )
    }

    if (showLevelAchievementsDialog && currentUser != null) {
        LevelAchievementsDialog(
            currentLevel = currentUser.level,
            currentXp = currentUser.xp,
            onDismiss = { showLevelAchievementsDialog = false }
        )
    }
}
