package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ParentObligationEntity
import com.example.ui.theme.GoldStar
import java.util.Calendar

// WhatsApp Official Branding Colors
val WhatsAppGreenHeader = Color(0xFF075E54)
val WhatsAppGreenTeal = Color(0xFF128C7E)
val WhatsAppGreenAccent = Color(0xFF25D366)
val WhatsAppBubbleBgLight = Color(0xFFE7FFDB)
val WhatsAppDoubleCheckBlue = Color(0xFF34B7F1)

@Composable
fun WhatsAppPensionReminderCard(
    obligation: ParentObligationEntity,
    onNavigateToChecklist: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDismissed by remember { mutableStateOf(false) }

    if (isDismissed) return

    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val isWithinFirstFiveDays = currentDay <= 5

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(1.5.dp, WhatsAppGreenTeal.copy(alpha = 0.45f)),
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. WhatsApp Top Contact Bar
            Surface(
                color = WhatsAppGreenTeal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏫", fontSize = 18.sp)
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Colegio Escolaris • Tesorería",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verificado",
                                    tint = WhatsAppGreenAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "En línea • Recordatorio WhatsApp a las 2:00 PM",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isDismissed = true
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. WhatsApp Message Bubble Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    color = WhatsAppBubbleBgLight,
                    border = BorderStroke(1.dp, Color(0xFFC7EBB5)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💬 Mensaje de Recordatorio Oficial",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = WhatsAppGreenHeader
                            )
                            if (isWithinFirstFiveDays) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = WhatsAppGreenAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "⏰ Día $currentDay de 5 (Pago Oportuno)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppGreenHeader,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (obligation.whatsappMessage.isNotBlank()) obligation.whatsappMessage
                            else "¡Hola estimado acudiente! 👋 Les recordamos que a partir de octubre la pensión escolar se cancela dentro de los 5 primeros días del mes. ¡Cumple a tiempo para ganar la insignia de Pago Oportuno y 100 créditos Escolaris para tu hijo/a! ⭐",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF1F2937)
                        )

                        // Reward Tag Inside WhatsApp Message
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎁", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Recompensa para tu hijo/a:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "Insignia '${obligation.rewardBadgeTitle}' + ${obligation.rewardCredits} créditos Escolaris",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // WhatsApp Message Footer (Time & Blue Checkmarks)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "14:00",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = WhatsAppDoubleCheckBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // 3. Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToChecklist,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreenAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ver Checklist & Cumplir 📋",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
