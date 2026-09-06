package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.BadgeEntity

/**
 * Modern Claymorphism Card with soft inflated feel and dynamic theme support.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "clay_press_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else elevation,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                spotColor = accentColor.copy(alpha = 0.15f)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.background(backgroundColor)
        ) {
            content()
        }
    }
}

/**
 * 3D Claymorphic Avatar with glowing clay ring and customizable profile emoji / photo.
 */
@Composable
fun ClayAvatar(
    modifier: Modifier = Modifier,
    emoji: String = "🎓",
    initials: String = "ES",
    colorHex: Long = 0xFF2563EB,
    photoUri: String? = null,
    size: Dp = 72.dp,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val baseColor = Color(colorHex)
    val clayGradient = Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.9f),
            baseColor
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(8.dp, CircleShape, spotColor = baseColor.copy(alpha = 0.5f)),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(
                3.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        baseColor.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier.background(clayGradient),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                    )
                } else if (emoji.isNotBlank()) {
                    Text(
                        text = emoji,
                        fontSize = (size.value * 0.48f).sp
                    )
                } else {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.36f).sp
                    )
                }
            }
        }

        if (showEditBadge) {
            Surface(
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.BottomEnd)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📷", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 3D Claymorphic Badge Tile with vibrant clay styling and theme adaptability.
 */
@Composable
fun ClayBadgeItem(
    badge: BadgeEntity,
    isUnlocked: Boolean = true,
    onClick: () -> Unit
) {
    val clayColor = Color(badge.clayColorHex)
    val backgroundBrush = if (isUnlocked) {
        Brush.verticalGradient(
            colors = listOf(
                clayColor.copy(alpha = 0.16f),
                clayColor.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            )
        )
    }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        accentColor = if (isUnlocked) clayColor else Color.Gray,
        elevation = if (isUnlocked) 6.dp else 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = if (isUnlocked) 8.dp else 2.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = if (isUnlocked) clayColor.copy(alpha = 0.5f) else Color.Transparent
                    ),
                shape = RoundedCornerShape(18.dp),
                color = if (isUnlocked) clayColor else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.8f),
                            if (isUnlocked) clayColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                        )
                    )
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isUnlocked) {
                        Text(text = badge.emoji, fontSize = 28.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueada",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isUnlocked) clayColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isUnlocked) clayColor.copy(alpha = 0.5f) else Color.Transparent)
                    ) {
                        Text(
                            text = badge.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) clayColor else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (isUnlocked && badge.teacherNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "👑 ${badge.unlockedByTeacher}: \"${badge.teacherNote}\"",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClayStatPill(icon = "⭐", text = "+${badge.xpReward} XP", color = Color(0xFFF59E0B))
                    ClayStatPill(icon = "🪙", text = "+${badge.creditReward} Pts", color = Color(0xFF10B981))
                }
            }
        }
    }
}

/**
 * Tactile Clay Pill for stats.
 */
@Composable
fun ClayStatPill(
    icon: String,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

/**
 * 3D Claymorphic Tactile Action Button.
 */
@Composable
fun ClayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    gradientColors: List<Color> = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        label = "button_press_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = gradientColors.first().copy(alpha = 0.5f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.8f),
                    gradientColors.first().copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (enabled) Brush.horizontalGradient(gradientColors)
                    else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(text = icon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}
