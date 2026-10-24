package com.sylergy.murajaah.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.sylergy.murajaah.settings.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanChips(
    viewModel: SettingsViewModel,
    onOpenPlanEditor: (planId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val plans by viewModel.plans.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val activeName = activePlan?.name ?: "All memorized · كل المحفوظ"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔖", fontSize = 16.sp)
                    Column {
                        Text(
                            text = "Revising now · الوِرد الحالي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7A6B56)
                        )
                        Text(
                            text = activeName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { expanded = !expanded },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (expanded) "Close · إغلاق" else if (plans.isNotEmpty()) "Change · تغيير" else "Plans · الخطط",
                        fontSize = 12.sp,
                        color = Color(0xFF2F4A3D)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Choose which surahs to revise today. The rest wait with dates untouched until chosen.",
                        fontSize = 11.sp,
                        color = Color(0xFF7A6B56)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // All memorized chip
                        val isAllActive = activePlan == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllActive) Color(0xFF2E6F40) else Color(0xFFFAF6EE))
                                .border(
                                    1.dp,
                                    if (isAllActive) Color(0xFF2E6F40) else Color(0xFFE2D9CC),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.activatePlan(null)
                                    expanded = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isAllActive) "✓ All memorized" else "All memorized · كل المحفوظ",
                                fontSize = 12.sp,
                                fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllActive) Color.White else Color(0xFF2F4A3D)
                            )
                        }

                        // Custom plans
                        plans.forEach { plan ->
                            val isActive = activePlan?.id == plan.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) Color(0xFF2E6F40) else Color(0xFFFAF6EE))
                                    .border(
                                        1.dp,
                                        if (isActive) Color(0xFF2E6F40) else Color(0xFFE2D9CC),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isActive) {
                                            // Click active plan to open editor
                                            onOpenPlanEditor(plan.id)
                                        } else {
                                            viewModel.activatePlan(plan.id)
                                            expanded = false
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isActive) "✓ ${plan.name} (Edit)" else plan.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) Color.White else Color(0xFF2F4A3D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            expanded = false
                            onOpenPlanEditor(null) // New plan
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAF6EE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E6F40), RoundedCornerShape(8.dp))
                            .height(36.dp)
                    ) {
                        Text(
                            text = "+ New Revision Plan · خطة وِرد جديدة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E6F40)
                        )
                    }
                }
            }
        }
    }
}
