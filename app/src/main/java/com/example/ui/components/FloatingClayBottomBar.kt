package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.UserEntity
import com.example.ui.navigation.Screen

@Composable
fun FloatingPillBottomNavBar(
    currentRoute: String,
    navItems: List<Screen>,
    currentUser: UserEntity?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val barBackground = MaterialTheme.colorScheme.surface
    val barBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main floating pill container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                )
                .background(barBackground, RoundedCornerShape(32.dp))
                .border(1.dp, barBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val isCenterProfile = screen is Screen.Profile

                    if (isCenterProfile) {
                        // Center item placeholder space
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                        )
                    } else {
                        StandardNavItem(
                            screen = screen,
                            isSelected = isSelected,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onClick = { onNavigate(screen) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Floating Elevated Center Avatar Button
        val centerProfileScreen = navItems.find { it is Screen.Profile }
        if (centerProfileScreen != null) {
            val isSelected = currentRoute == centerProfileScreen.route
            val avatarScale by animateFloatAsState(
                targetValue = if (isSelected) 1.08f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "avatarScale"
            )

            val avatarBgGradient = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )

            val avatarEmoji = currentUser?.avatarEmoji ?: "🎓"
            val photoUri = currentUser?.photoUri

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-14).dp)
                    .size(54.dp)
                    .scale(avatarScale)
                    .shadow(
                        elevation = if (isSelected) 12.dp else 8.dp,
                        shape = CircleShape,
                        spotColor = if (isSelected) activeColor.copy(alpha = 0.5f) else Color(0x35000000)
                    )
                    .background(avatarBgGradient, CircleShape)
                    .border(
                        width = if (isSelected) 3.dp else 2.5.dp,
                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onNavigate(centerProfileScreen)
                    }
                    .testTag("nav_bottom_profile_center"),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = avatarEmoji,
                        fontSize = 27.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardNavItem(
    screen: Screen,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "navItemScale"
    )

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag("nav_bottom_${screen.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
            contentDescription = screen.title,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = screen.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor,
            maxLines = 1,
            softWrap = false
        )
    }
}
