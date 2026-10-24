package com.sylergy.murajaah.friends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.network.model.InvitePreviewDto

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)
private val CrimsonError = Color(0xFF8B261D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrDialog(
    isLoading: Boolean,
    preview: InvitePreviewDto?,
    errorMessage: String?,
    onScan: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputUrl by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ParchmentBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "مسح رمز أو انضمام",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )
            Text(
                text = "Scan QR Code or Enter Companion Link",
                fontSize = 13.sp,
                color = MutedInk,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            if (preview != null) {
                // Show confirmation card to join this companion
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightParchment)
                        .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "انضمام إلى مراجعة",
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${preview.studentName} invites you to mutual Quran revision.",
                            fontSize = 14.sp,
                            color = DeepBrown,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Both of you will be able to recite to each other and assess retention.",
                            fontSize = 12.sp,
                            color = MutedInk,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onAccept(preview.token) },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("قبول والاتصال / Connect Now", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Scanner viewfinder frame representation
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, ForestGreen, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Align companion's QR code in this frame",
                            fontSize = 12.sp,
                            color = MutedInk,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "— OR PASTE LINK / TOKEN —",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("https://murajaah.../join/... or token", fontSize = 13.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightParchment,
                        unfocusedContainerColor = LightParchment,
                        focusedIndicatorColor = ForestGreen,
                        unfocusedIndicatorColor = BorderParchment
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = CrimsonError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onScan(inputUrl) },
                    enabled = inputUrl.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Verify & Review", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onDismiss) {
                Text("إغلاق / Close", color = MutedInk)
            }
        }
    }
}
