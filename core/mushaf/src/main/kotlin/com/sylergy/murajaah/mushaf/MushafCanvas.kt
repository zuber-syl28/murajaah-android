package com.sylergy.murajaah.mushaf

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.quran.Narration
import com.sylergy.murajaah.quran.QuranData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MushafCanvas(
    surahNumber: Int,
    fromAyah: Int,
    toAyah: Int,
    ayahs: List<Ayah>,
    narration: Narration = Narration.HAFS,
    slippedAyahs: Set<Int> = emptySet(),
    onAyahToggled: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fontSizeSp by remember { mutableFloatStateOf(26f) }
    var isTextHidden by remember { mutableStateOf(false) }

    val surahMeta = QuranData.surah(surahNumber)
    val fontFamily = remember(narration) { MushafTypography.getFontFamily(context, narration) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Surah metadata and toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = surahMeta?.arabicName ?: "سورة",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F4A3D)
                    )
                    Text(
                        text = "${surahMeta?.name ?: "Surah"} · Ayahs $fromAyah–$toAyah",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                // Font size controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { if (fontSizeSp > 20f) fontSizeSp -= 3f },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2F4A3D))
                    ) {
                        Text("A−", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${fontSizeSp.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666)
                    )
                    TextButton(
                        onClick = { if (fontSizeSp < 44f) fontSizeSp += 3f },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2F4A3D))
                    ) {
                        Text("A+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Basmala Banner if beginning of surah
            if (AyahFormatter.hasBasmalaHeader(surahNumber, fromAyah)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AyahFormatter.BASMALA_TEXT,
                        fontSize = (fontSizeSp * 0.95f).sp,
                        fontFamily = fontFamily,
                        color = Color(0xFF2F4A3D),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Hidden recitation toggle banner
            if (isTextHidden) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF8F7F2),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Mushaf Text Hidden",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recite this passage from memory, then reveal the text to inspect any slips.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF666666)
                        )
                    }
                }
            } else {
                // RTL Arabic Mushaf Ayahs Flow
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (ayah in ayahs) {
                            val isSlipped = slippedAyahs.contains(ayah.number)
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .background(
                                        color = if (isSlipped) Color(0xFFFFEEEE) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSlipped) 1.dp else 0.dp,
                                        color = if (isSlipped) Color(0xFFD32F2F) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onAyahToggled(ayah.number) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ayah.text,
                                        fontSize = fontSizeSp.sp,
                                        fontFamily = fontFamily,
                                        color = if (isSlipped) Color(0xFFB71C1C) else Color(0xFF1E2922),
                                        style = androidx.compose.ui.text.TextStyle(
                                            textDirection = TextDirection.Rtl,
                                            lineHeight = (fontSizeSp * 1.8f).sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AyahFormatter.formatAyahMarker(ayah.number),
                                        fontSize = (fontSizeSp * 0.75f).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSlipped) Color(0xFFD32F2F) else Color(0xFF2F4A3D)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer controls: Hide/Show Text & Slipped Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { isTextHidden = !isTextHidden },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isTextHidden) "Show Text" else "Hide Text",
                        fontSize = 11.sp,
                        color = Color(0xFF2F4A3D)
                    )
                }

                if (slippedAyahs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFEEEE),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${slippedAyahs.size} slipped ayah${if (slippedAyahs.size > 1) "s" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                } else {
                    Text(
                        text = "Tap any ayah to mark slip",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}
