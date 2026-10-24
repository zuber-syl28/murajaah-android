package com.sylergy.murajaah.reviewing.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sylergy.murajaah.reviewing.model.SlipTag

private val DeepBrown = Color(0xFF2C221E)
private val Parchment = Color(0xFFFAF6F0)
private val CoralRed = Color(0xFFC2410C)
private val ForestGreen = Color(0xFF2F4A3D)
private val MutedInk = Color(0xFF6B5E55)

@Composable
fun SlipTagDialog(
    ayahNumber: Int,
    currentTag: SlipTag?,
    onSelectTag: (SlipTag?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Parchment),
            border = BorderStroke(1.dp, Color(0xFFE5DDD0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تعثر في الآية $ayahNumber",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
                Text(
                    text = "Slip on Ayah $ayahNumber · Select Reason",
                    fontSize = 12.sp,
                    color = MutedInk
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SlipTag.entries.forEach { tag ->
                        val isSelected = currentTag == tag
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) Color(0xFFFFECE0) else Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectTag(tag) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CoralRed else DeepBrown
                                )
                                Text(
                                    text = tag.arabicLabel,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CoralRed else MutedInk
                                )
                            }
                        }
                    }

                    // Option for generic slip without a tag
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (currentTag == null) Color(0xFFFFECE0) else Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTag(null) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "General slip (no specific tag)",
                                fontSize = 13.sp,
                                color = DeepBrown
                            )
                            Text(
                                text = "تعثر عام",
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp,
                                color = MutedInk
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done / تم", color = ForestGreen, fontSize = 13.sp)
                }
            }
        }
    }
}
