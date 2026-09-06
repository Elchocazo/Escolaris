package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.domain.model.UserRole
import com.example.ui.theme.GoldStar
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RewardsCatalogScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val rewards by viewModel.rewards.collectAsState()
    val redemptions by viewModel.redemptions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCreateRewardDialog by remember { mutableStateOf(false) }
    var rewardToEdit by remember { mutableStateOf<RewardEntity?>(null) }
    var selectedRedemptionForQr by remember { mutableStateOf<RedemptionEntity?>(null) }

    val isTeacher = currentUser?.role == UserRole.TEACHER.code
    val isParent = currentUser?.role == UserRole.PARENT.code
    val userCredits = currentUser?.credits ?: 0
    val parentCredits = currentUser?.parentIncentiveCredits ?: 0

    var showGrantIncentiveDialog by remember { mutableStateOf(false) }

    val myRedemptions = remember(redemptions, currentUser) {
        if (isTeacher) redemptions
        else redemptions.filter { it.studentId == currentUser?.id }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("🎁 Catálogo Escolar", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("🎟️ Pases y Canjes (${myRedemptions.size})", fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // CATALOG TAB: GESTIÓN DE RECOMPENSAS & TIENDA ESCOLAR
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic Role Header Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when {
                                    isTeacher -> {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎁", fontSize = 28.sp)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Gestión de Recompensas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Crea y administra recompensas e incentivos canjeables con Escolaris para motivar a tus alumnos.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Button(
                                            onClick = { showCreateRewardDialog = true },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("create_reward_button")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Crear Nuevo Premio ✨", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    isParent -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "🪙 Saldo de Escolaris para Ceder",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "🪙 $parentCredits Escolaris",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFB45309)
                                                )
                                                Text(
                                                    text = "Tus Escolaris son exclusivamente para ceder a tu hijo/a como incentivo escolar.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = { showGrantIncentiveDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Ceder Escolaris 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    else -> {
                                        // Student view
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Tu Saldo Disponible",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "🪙 $userCredits",
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = "Gana monedas entregando tareas a tiempo y en evaluaciones.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (rewards.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🛍️", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No hay recompensas disponibles en el catálogo", fontWeight = FontWeight.Bold)
                                    Text("Pronto los docentes publicarán nuevos premios e incentivos escolares.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    } else {
                        items(rewards, key = { it.id }) { reward ->
                            val canAfford = userCredits >= reward.costCredits
                            RewardItemCard(
                                reward = reward,
                                canAfford = canAfford,
                                isTeacher = isTeacher,
                                isParent = isParent,
                                onRedeem = { viewModel.redeemReward(reward) },
                                onEdit = { rewardToEdit = reward },
                                onDelete = { viewModel.deleteReward(reward) }
                            )
                        }
                    }
                } else {
                    // REDEMPTIONS / PASSES TAB (Pases y Canjes exclusivamente)
                    if (myRedemptions.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎟️", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isTeacher) "No hay pases pendientes por validar" else "No tienes pases ni canjes activos",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isTeacher) "Los pases canjeados por tus estudiantes aparecerán aquí." else "Canjea beneficios escolares en la pestaña Catálogo Escolar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        items(myRedemptions, key = { it.id }) { redemption ->
                            RedemptionItemCard(
                                redemption = redemption,
                                isTeacher = isTeacher,
                                onShowQr = { selectedRedemptionForQr = redemption },
                                onValidate = { viewModel.validateStudentRedemption(redemption.id) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showCreateRewardDialog) {
        CreateRewardDialog(
            onDismiss = { showCreateRewardDialog = false },
            onCreate = { title, desc, cost, cat, stock ->
                viewModel.createNewTeacherReward(title, desc, cost, cat, "🎁", stock)
            }
        )
    }

    if (showGrantIncentiveDialog) {
        var transferAmount by remember { mutableStateOf("25") }
        var transferReason by remember { mutableStateOf("¡Reconocimiento por esfuerzo académico y apoyo en casa!") }

        AlertDialog(
            onDismissRequest = { showGrantIncentiveDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ceder Escolaris a tu Hijo/a", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Saldo disponible: 🪙 $parentCredits Escolaris",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )

                    Text("Selecciona una cantidad rápida:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10", "25", "50", "100").forEach { preset ->
                            val isSelected = transferAmount == preset
                            Button(
                                onClick = { transferAmount = preset },
                                colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black) else ButtonDefaults.filledTonalButtonColors(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$preset 🪙", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Cantidad personalizada (🪙 Escolaris)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = transferReason,
                        onValueChange = { transferReason = it },
                        label = { Text("Motivo del incentivo (Opcional)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val amt = transferAmount.toIntOrNull() ?: 0
                val canSubmit = amt in 1..parentCredits
                Button(
                    onClick = {
                        if (amt > 0) {
                            viewModel.transferParentPointsToChild(amt, transferReason)
                            showGrantIncentiveDialog = false
                        }
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldStar, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (amt <= 0) "Ingresa cantidad" else if (amt > parentCredits) "Saldo insuficiente" else "Ceder +$amt 🪙 Escolaris",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showGrantIncentiveDialog = false }) { Text("Cancelar") }
            }
        )
    }

    rewardToEdit?.let { targetReward ->
        EditRewardDialog(
            reward = targetReward,
            onDismiss = { rewardToEdit = null },
            onSave = { title, desc, cost, category, stock ->
                viewModel.updateTeacherReward(
                    rewardId = targetReward.id,
                    title = title,
                    desc = desc,
                    cost = cost,
                    category = category,
                    stock = stock,
                    icon = targetReward.iconKey
                )
            }
        )
    }

    selectedRedemptionForQr?.let { redemption ->
        PassQrDialog(
            redemption = redemption,
            onDismiss = { selectedRedemptionForQr = null }
        )
    }
}

@Composable
fun RewardItemCard(
    reward: RewardEntity,
    canAfford: Boolean,
    isTeacher: Boolean = false,
    isParent: Boolean = false,
    onRedeem: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("reward_item_${reward.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon + Title + Cost Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldStar.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎁", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val rawTeacher = reward.teacherName.trim()
                    val resolvedTeacher = if (rawTeacher.isBlank() || rawTeacher.equals("Docente Titular", ignoreCase = true) || rawTeacher.equals("Prof. Titular", ignoreCase = true)) {
                        "Manuel Alejandro Muñoz"
                    } else {
                        rawTeacher
                    }
                    val cleanTeacherLabel = if (resolvedTeacher.startsWith("Prof", ignoreCase = true)) resolvedTeacher else "Prof. $resolvedTeacher"
                    Text(
                        text = cleanTeacherLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldStar.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.7f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reward.costCredits}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB45309),
                            maxLines = 1
                        )
                    }
                }
            }

            if (reward.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Stock info + Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Disponibles: ${reward.stockAvailable} unidades",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reward.stockAvailable > 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTeacher) {
                        if (onEdit != null) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar recompensa",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (onDelete != null) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar recompensa",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (isParent) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "Para tu hijo/a 🎓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (!isTeacher) {
                        Button(
                            onClick = onRedeem,
                            enabled = canAfford && reward.stockAvailable > 0,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("redeem_reward_${reward.id}")
                        ) {
                            Text(
                                text = if (reward.stockAvailable <= 0) "Agotado" else if (canAfford) "Canjear" else "Faltan 🪙",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RedemptionItemCard(
    redemption: RedemptionEntity,
    isTeacher: Boolean,
    onShowQr: () -> Unit,
    onValidate: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val isUsed = redemption.status == "UTILIZADO"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().testTag("redemption_card_${redemption.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = redemption.rewardTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Estudiante: ${redemption.studentName} • ${dateFormat.format(Date(redemption.redeemedAtMillis))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val isPending = redemption.status == "PENDING_APPROVAL" || redemption.status == "PENDIENTE"
                val isApproved = redemption.status == "APROBADO" || redemption.status == "ACTIVO"
                val isRejected = redemption.status == "RECHAZADO"

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isApproved -> SuccessGreen.copy(alpha = 0.2f)
                        isPending -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        isRejected -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isApproved -> "🎟️ Aprobado / Listo para Usar"
                            isPending -> "⏳ Pendiente de Aprobación"
                            isRejected -> "❌ Rechazado (Reembolsado)"
                            else -> "✅ Utilizado en Clase"
                        },
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isApproved -> SuccessGreen
                            isPending -> Color(0xFFD97706)
                            isRejected -> Color(0xFFDC2626)
                            else -> MaterialTheme.colorScheme.outline
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Código: ${redemption.redemptionCode} • ${redemption.costCredits} 🪙",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row {
                        Button(onClick = onShowQr, modifier = Modifier.testTag("show_qr_button")) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver QR", fontSize = 11.sp)
                        }

                        if (isTeacher && !isUsed) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = onValidate,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.testTag("validate_pass_button")
                            ) {
                                Text("Aprobar", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassQrDialog(
    redemption: RedemptionEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pase de Beneficio Digital Escolar", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode, contentDescription = "QR Code", modifier = Modifier.size(110.dp), tint = Color.Black)
                            Text(redemption.redemptionCode, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(redemption.rewardTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Presenta este código QR a tu docente en el aula para hacer efectivo el beneficio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun CreateRewardDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, cost: Int, category: String, stock: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("150") }
    var stockStr by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Recompensa", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    placeholder = { Text("Ej: +0.5 en Examen") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción o reglas de uso") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Costo (🪙)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock disponible") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costStr.toIntOrNull() ?: 100
                    val stock = stockStr.toIntOrNull() ?: 10
                    if (title.isNotBlank()) {
                        onCreate(title, desc, cost, "ACADÉMICO", stock)
                        onDismiss()
                    }
                }
            ) { Text("Publicar Recompensa") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditRewardDialog(
    reward: RewardEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, cost: Int, category: String, stock: Int) -> Unit
) {
    var title by remember { mutableStateOf(reward.title) }
    var desc by remember { mutableStateOf(reward.description) }
    var costStr by remember { mutableStateOf(reward.costCredits.toString()) }
    var stockStr by remember { mutableStateOf(reward.stockAvailable.toString()) }
    var category by remember { mutableStateOf(reward.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✏️", fontSize = 18.sp)
                    }
                }
                Column {
                    Text("Editar Recompensa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Modifica nombre, descripción y valor", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la Recompensa") },
                    placeholder = { Text("Ej: Prórroga de Tarea (+24h)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción / Beneficio") },
                    placeholder = { Text("Describe en qué consiste el beneficio...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) costStr = it },
                        label = { Text("Valor (🪙 Escolaris)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) stockStr = it },
                        label = { Text("Stock Disponible") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costStr.toIntOrNull() ?: reward.costCredits
                    val stock = stockStr.toIntOrNull() ?: reward.stockAvailable
                    if (title.isNotBlank()) {
                        onSave(title, desc, cost, category, stock)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
