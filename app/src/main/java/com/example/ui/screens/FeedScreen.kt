package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Send
import com.example.ui.components.ProfilePhotoViewerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.FeedPostEntity
import com.example.data.local.entity.PostCommentEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.PostType
import com.example.domain.model.UserRole
import com.example.ui.components.LateHelpRequestDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldStar
import com.example.ui.theme.StreakOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.feedPosts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val parentObligations by viewModel.parentObligations.collectAsState()

    var showLateHelpDialog by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val isTeacher = UserRole.isTeacherOrAdmin(currentUser?.role) || currentUser?.email == "moz658@gmail.com"
    val isParent = currentUser?.role == UserRole.PARENT.code
    val isParentLinked = isParent && !currentUser?.linkedStudentId.isNullOrBlank()

    val completedPensions = remember(parentObligations) {
        parentObligations.filter { it.category == "PENSION" && it.isCompleted }
    }
    val pendingPensions = remember(parentObligations) {
        parentObligations.filter { it.category == "PENSION" && !it.isCompleted }
    }

    val filteredPosts = remember(posts, selectedFilter, isParent) {
        val basePosts = if (isParent) {
            posts.filter {
                it.postType == PostType.ANNOUNCEMENT.code ||
                it.postType == PostType.EVENT.code ||
                it.postType == PostType.HOMEWORK_ALERT.code ||
                it.postType == PostType.EXAM_ALERT.code
            }
        } else {
            posts
        }

        when (selectedFilter) {
            "LATE_HELP" -> basePosts.filter { it.postType == PostType.LATE_HELP_REQUEST.code }
            "ANNOUNCEMENT" -> basePosts.filter { it.postType == PostType.ANNOUNCEMENT.code || it.postType == PostType.EVENT.code }
            "HOMEWORK" -> basePosts.filter { it.postType == PostType.HOMEWORK_ALERT.code || it.postType == PostType.EXAM_ALERT.code }
            else -> basePosts
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Cabecera y Filtros de Muro Escolar
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isParent) "Avisos & Comunicados" else "Muro Escolar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTeacher) "Tablón de anuncios y actividades" else if (isParent) "Información oficial para acudientes" else "Comunidad y vida escolar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (!isParent) {
                        Button(
                            onClick = { showCreatePostDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("create_post_button")
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTeacher) "Anunciar" else "Publicar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))



                // Filter chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChipItem(
                        label = "Todos",
                        isSelected = selectedFilter == "TODOS",
                        onClick = { selectedFilter = "TODOS" }
                    )
                    if (!isParent) {
                        FilterChipItem(
                            label = "🚨 Auxilio Apuntes",
                            isSelected = selectedFilter == "LATE_HELP",
                            onClick = { selectedFilter = "LATE_HELP" }
                        )
                    }
                    FilterChipItem(
                        label = "📢 Avisos",
                        isSelected = selectedFilter == "ANNOUNCEMENT",
                        onClick = { selectedFilter = "ANNOUNCEMENT" }
                    )
                    FilterChipItem(
                        label = "📚 Tareas & Exámenes",
                        isSelected = selectedFilter == "HOMEWORK",
                        onClick = { selectedFilter = "HOMEWORK" }
                    )
                }
            }

            // Mensaje informativo para padres no vinculados
            if (isParent && !isParentLinked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔒", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Vinculación Requerida",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Para ver los avisos y publicaciones del curso, ingresa el código único de tu hijo/a en la pestaña 'Padres'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Banner contextual solo cuando está en Auxilio Apuntes
            if (!isTeacher && !isParent && selectedFilter == "LATE_HELP") {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StreakOrange.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, StreakOrange.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showLateHelpDialog = true }
                            .testTag("late_help_emergency_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🚨", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "¿Faltaste a clase o estás atrasado?",
                                        color = StreakOrange,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "Pide fotos de apuntes y pizarras a tus compañeros",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StreakOrange,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "Pedir Apuntes",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Posts List
            if (filteredPosts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📭", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay publicaciones en esta categoría",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "¡Sé el primero en compartir un aviso o pedir apuntes!",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    FeedPostCard(
                        post = post,
                        viewModel = viewModel,
                        currentUser = currentUser,
                        allUsers = allUsers
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showLateHelpDialog) {
        LateHelpRequestDialog(
            onDismiss = { showLateHelpDialog = false },
            onSendRequest = { subject, dateStr, notes ->
                viewModel.requestLateHelp(subject, dateStr, notes)
            }
        )
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            currentUser = currentUser,
            onDismiss = { showCreatePostDialog = false },
            onCreate = { title, content, subject, type ->
                viewModel.createAnnouncement(title, content, subject, type)
            }
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun FeedPostCard(
    post: FeedPostEntity,
    viewModel: SchoolViewModel,
    currentUser: UserEntity?,
    allUsers: List<UserEntity> = emptyList()
) {
    var isCommentsExpanded by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    var attachedPhotoNote by remember { mutableStateOf<String?>(null) }

    val commentsFlow = remember(post.id) { viewModel.getCommentsForPost(post.id) }
    val comments by commentsFlow.collectAsState(initial = emptyList())

    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val isLateHelp = post.postType == PostType.LATE_HELP_REQUEST.code
    val isTeacher = UserRole.isTeacherOrAdmin(currentUser?.role) || currentUser?.email == "moz658@gmail.com"
    var showAuthorPhotoDialog by remember { mutableStateOf(false) }

    val author = if (post.authorId == currentUser?.id) currentUser else allUsers.find { it.id == post.authorId }
    val rawAuthorName = author?.name?.ifBlank { post.authorName } ?: post.authorName
    val liveAuthorName = com.example.domain.validation.ValidationUtils.formatProperNoun(
        rawAuthorName.replace(Regex("\\s*\\((Docente|Estudiante|Profesor|Familia)\\)"), "")
    )
    val isAuthorTeacherOrAdmin = UserRole.isTeacherOrAdmin(author?.role ?: post.authorRole) || author?.email == "moz658@gmail.com"
    val livePhotoUri = author?.photoUri
    val liveEmoji = author?.avatarEmoji?.ifBlank { if (isAuthorTeacherOrAdmin) "👨‍🏫" else "🎓" } ?: (if (isAuthorTeacherOrAdmin) "👨‍🏫" else "🎓")
    val liveColor = author?.avatarColorHex ?: post.authorAvatarColorHex
    val liveRole = if (isAuthorTeacherOrAdmin) UserRole.TEACHER.code else (author?.role ?: post.authorRole)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isLateHelp) StreakOrange.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feed_post_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Post Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showAuthorPhotoDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(liveColor),
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!livePhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = livePhotoUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                        } else if (!liveEmoji.isNullOrBlank()) {
                            Text(
                                text = liveEmoji,
                                fontSize = 20.sp
                            )
                        } else {
                            Text(
                                text = liveAuthorName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = com.example.domain.validation.ValidationUtils.formatProperNoun(liveAuthorName),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            val isAdmin = liveRole == UserRole.ADMIN.code || liveRole == "ADMIN" || liveRole == "SUPERADMIN"
                            val isDocente = liveRole == UserRole.TEACHER.code || liveRole == "DOCENTE"
                            val roleBadge = when {
                                isAdmin -> "Admin 👑"
                                isDocente -> "Docente"
                                liveRole == UserRole.PARENT.code -> "Familia"
                                else -> "Estudiante"
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isAdmin) Color(0xFFEDE9FE) else if (isDocente) GoldStar.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = roleBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAdmin) Color(0xFF6D28D9) else if (isDocente) Color(0xFFB45309) else MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = dateFormat.format(Date(post.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLateHelp) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreakOrange
                        ) {
                            Text(
                                text = "🚨 Auxilio Apuntes",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (isTeacher || post.authorId == currentUser?.id) {
                        IconButton(
                            onClick = { viewModel.deletePost(post.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Title & Content
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Attachment Banner (if any)
            if (post.attachmentsJson.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = post.attachmentsJson,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))

            // Post Actions (Social Network Style: Like, Comment, Notes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.togglePostLike(post) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("post_like_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (post.isLikedByMe) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (post.likesCount > 0) "${post.likesCount}" else "Me gusta",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (post.isLikedByMe) FontWeight.Bold else FontWeight.Medium,
                        color = if (post.isLikedByMe) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment Action Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isCommentsExpanded = !isCommentsExpanded }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentarios",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (comments.isNotEmpty()) "${comments.size} comentarios" else "Comentar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isLateHelp) {
                    Button(
                        onClick = { isCommentsExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.testTag("send_notes_help_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aportar (+25🪙)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Social Network Comments Section (Always interactive)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // List of Comments if expanded or if there are comments
                if (isCommentsExpanded && comments.isNotEmpty()) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                     comments.forEach { comment ->
                        CommentRowItem(comment = comment, allUsers = allUsers)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Attached Photo Chip (if attached)
                if (attachedPhotoNote != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Foto de apuntes / pizarra adjunta 📸", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { attachedPhotoNote = null }) {
                                Text("Quitar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Quick Comment Input Bar (Social Media Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current user avatar photo / emoji / initials
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        } else if (!currentUser?.avatarEmoji.isNullOrBlank()) {
                            Text(
                                text = currentUser?.avatarEmoji ?: "🎓",
                                fontSize = 18.sp
                            )
                        } else {
                            Text(
                                text = currentUser?.avatarInitials ?: (currentUser?.name?.take(2)?.uppercase() ?: "ES"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = {
                            Text(
                                text = "Escribe un comentario...",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    attachedPhotoNote = "Foto_Apuntes_${post.subject}_${System.currentTimeMillis()}.jpg"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Adjuntar foto de apuntes",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (commentInput.isNotBlank() || attachedPhotoNote != null) {
                                val textToSend = if (commentInput.isNotBlank()) commentInput else "¡Aquí tienes los apuntes de la clase!"
                                viewModel.addCommentToPost(post.id, textToSend, attachedPhotoNote)
                                commentInput = ""
                                attachedPhotoNote = null
                                isCommentsExpanded = true
                            }
                        },
                        enabled = commentInput.isNotBlank() || attachedPhotoNote != null,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (commentInput.isNotBlank() || attachedPhotoNote != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Publicar comentario",
                            tint = if (commentInput.isNotBlank() || attachedPhotoNote != null) Color.White else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAuthorPhotoDialog) {
        val displayGrade = if (isAuthorTeacherOrAdmin) "Docente Titular" else (if (post.subject.equals("General", ignoreCase = true) || post.subject.equals("Colegio general", ignoreCase = true)) "" else post.subject)
        ProfilePhotoViewerDialog(
            photoUri = livePhotoUri,
            avatarEmoji = liveEmoji,
            userName = liveAuthorName,
            userRole = liveRole,
            gradeSection = displayGrade,
            onDismiss = { showAuthorPhotoDialog = false }
        )
    }
}

@Composable
fun CommentRowItem(
    comment: PostCommentEntity,
    allUsers: List<UserEntity> = emptyList()
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val commenter = allUsers.find { it.id == comment.authorId }
    val commenterPhoto = commenter?.photoUri
    val commenterEmoji = commenter?.avatarEmoji?.ifBlank { if (comment.authorRole == UserRole.TEACHER.code) "👨‍🏫" else "🎓" } ?: (if (comment.authorRole == UserRole.TEACHER.code) "👨‍🏫" else "🎓")
    val commenterColor = commenter?.avatarColorHex ?: 0xFF2563EB
    val commenterName = commenter?.name?.ifBlank { comment.authorName } ?: comment.authorName
    val commenterRole = commenter?.role ?: comment.authorRole

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Commenter Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(commenterColor),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!commenterPhoto.isNullOrBlank()) {
                AsyncImage(
                    model = commenterPhoto,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            } else if (!commenterEmoji.isNullOrBlank()) {
                Text(
                    text = commenterEmoji,
                    fontSize = 16.sp
                )
            } else {
                Text(
                    text = commenterName.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Comment Bubble
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = com.example.domain.validation.ValidationUtils.formatProperNoun(commenterName),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val isCommentAdmin = commenterRole == UserRole.ADMIN.code || commenterRole == "ADMIN" || commenterRole == "SUPERADMIN"
                        val isCommentTeacher = commenterRole == UserRole.TEACHER.code || commenterRole == "DOCENTE"
                        val roleBadge = when {
                            isCommentAdmin -> "Admin 👑"
                            isCommentTeacher -> "Docente"
                            commenterRole == UserRole.PARENT.code -> "Familia"
                            else -> "Estudiante"
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCommentAdmin) Color(0xFFEDE9FE) else if (isCommentTeacher) GoldStar.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = roleBadge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                softWrap = false,
                                color = if (isCommentAdmin) Color(0xFF6D28D9) else if (isCommentTeacher) Color(0xFFB45309) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = dateFormat.format(Date(comment.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (comment.attachmentDescription != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, SuccessGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp), tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(comment.attachmentDescription, style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostDialog(
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onCreate: (title: String, content: String, subject: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf(PostType.ANNOUNCEMENT.code) }

    val postTypes = listOf(
        PostType.ANNOUNCEMENT.code to "📢 Aviso",
        PostType.EVENT.code to "📅 Evento",
        PostType.HOMEWORK_ALERT.code to "📝 Tarea"
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nueva Publicación", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Tipo de publicación:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    postTypes.forEach { (key, label) ->
                        val isSelected = postType == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { postType = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la publicación") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Mensaje o descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onCreate(title, content, subject, postType)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
