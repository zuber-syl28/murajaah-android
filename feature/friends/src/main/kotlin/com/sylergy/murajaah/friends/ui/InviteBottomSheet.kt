package com.sylergy.murajaah.friends.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.friends.qr.QrCodeGenerator
import com.sylergy.murajaah.network.model.InviteDto

private val ParchmentBg = Color(0xFFFBF8F1)
private val DeepBrown = Color(0xFF2C2416)
private val MutedInk = Color(0xFF7A6B56)
private val ForestGreen = Color(0xFF2E6F40)
private val LightParchment = Color(0xFFF3ECE0)
private val BorderParchment = Color(0xFFE2D6C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteBottomSheet(
    invite: InviteDto?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEnlarged by remember { mutableStateOf(false) }

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
                text = "دعوة رفيق للمراجعة",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )
            Text(
                text = "Companion Invite Link",
                fontSize = 13.sp,
                color = MutedInk,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Text(
                text = "Whoever opens this link connects with you both ways: you will each be able to hear and review the other's recitation.",
                fontSize = 13.sp,
                color = DeepBrown,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (invite == null) {
                CircularProgressIndicator(
                    color = ForestGreen,
                    modifier = Modifier.padding(32.dp)
                )
            } else {
                val qrSize = if (isEnlarged) 280 else 180
                val qrImage = remember(invite.url, isEnlarged) {
                    QrCodeGenerator.generateQrImageBitmap(invite.url, if (isEnlarged) 700 else 450)
                }

                Box(
                    modifier = Modifier
                        .size((qrSize + 20).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, BorderParchment, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrImage,
                        contentDescription = "Invite QR Code",
                        modifier = Modifier.size(qrSize.dp)
                    )
                }

                TextButton(
                    onClick = { isEnlarged = !isEnlarged },
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = if (isEnlarged) "Show Smaller Code" else "Enlarge Code for Scanning",
                        fontSize = 13.sp,
                        color = ForestGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(LightParchment, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderParchment, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = invite.url,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = DeepBrown,
                        maxLines = 2
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Murajaah Invite", invite.url)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("نسخ الرابط / Copy", color = DeepBrown, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Join me for Quran revision on Murajaah:\n${invite.url}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Murajaah Invite"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("مشاركة / Share", color = Color.White, fontSize = 13.sp)
                    }
                }

                Text(
                    text = "Expires in 48 hours · Cancel anytime from companion settings",
                    fontSize = 11.sp,
                    color = MutedInk,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
