package com.sylergy.murajaah.session.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sylergy.murajaah.database.MurajaahDatabase
import com.sylergy.murajaah.database.toDomain
import com.sylergy.murajaah.mushaf.MushafCanvas
import com.sylergy.murajaah.mushaf.MushafRepository
import com.sylergy.murajaah.quran.QuranData
import com.sylergy.murajaah.scheduler.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MemorizedSurahItem(
    val surahNumber: Int,
    val name: String,
    val arabicName: String,
    val totalAyahs: Int,
    val memorizedCards: List<Card>
)

/**
 * Free Tasmīʿ (Free Test) as in a traditional majlis:
 * The listener / shaykh opens the Mushaf to any memorized surah and says "recite from here".
 * Tapping a memorized ayah launches a test sitting for that passage.
 */
@Composable
fun FreeTestScreen(
    database: MurajaahDatabase,
    onStartFreeSession: (Card) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loading by remember { mutableStateOf(true) }
    var memorizedSurahs by remember { mutableStateOf<List<MemorizedSurahItem>>(emptyList()) }
    var selectedSurah by remember { mutableStateOf<MemorizedSurahItem?>(null) }
    var selectedAyah by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val cards = database.cardDao().getAllCards().map { it.toDomain() }
            val bySurah = cards.groupBy { it.surah }
            val list = bySurah.map { (surahNum, surahCards) ->
                val meta = QuranData.surah(surahNum)
                MemorizedSurahItem(
                    surahNumber = surahNum,
                    name = meta?.name ?: "Surah $surahNum",
                    arabicName = meta?.arabicName ?: "",
                    totalAyahs = meta?.ayahCount ?: 0,
                    memorizedCards = surahCards.sortedBy { it.fromAyah }
                )
            }.sortedBy { it.surahNumber }

            withContext(Dispatchers.Main) {
                memorizedSurahs = list
                loading = false
            }
        }
    }

    if (loading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7F2)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF2F4A3D))
        }
        return
    }

    val currentSurah = selectedSurah
    if (currentSurah == null) {
        // Step 1: Pick a memorized Surah from the Fihris
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7F2))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back · عودة", color = Color(0xFF2F4A3D), fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تسميع حر · Free Tasmīʿ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pick a Surah · اختر السورة",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F4A3D)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose any memorized surah. Your listener can tap any ayah to test you from that point.",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (memorizedSurahs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No memorized ranges found in your library yet.",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memorizedSurahs) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSurah = item
                                    selectedAyah = item.memorizedCards.firstOrNull()?.fromAyah
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFE8F0EC), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${item.surahNumber}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2F4A3D)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = item.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2F4A3D)
                                        )
                                        Text(
                                            text = "${item.memorizedCards.size} memorized passages",
                                            fontSize = 11.sp,
                                            color = Color(0xFF888888)
                                        )
                                    }
                                }
                                Text(
                                    text = item.arabicName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2F4A3D)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Step 2: Browse the Surah and pick the Ayah to recite from
        val minAyah = currentSurah.memorizedCards.minOfOrNull { it.fromAyah } ?: 1
        val maxAyah = currentSurah.memorizedCards.maxOfOrNull { it.toAyah } ?: currentSurah.totalAyahs
        val ayahs = MushafRepository.getPassageText(currentSurah.surahNumber, minAyah, maxAyah)
        val activeAyah = selectedAyah ?: minAyah

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7F2))
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { selectedSurah = null }) {
                    Text("← All Surahs", color = Color(0xFF2F4A3D), fontSize = 13.sp)
                }
                Text(
                    text = "${currentSurah.arabicName} · ${currentSurah.name}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F4A3D)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guidance instruction
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tap any ayah to start from there",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2F4A3D)
                        )
                        Text(
                            text = "Selected start: Ayah $activeAyah",
                            fontSize = 11.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Ayah $activeAyah",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mushaf Canvas
            MushafCanvas(
                surahNumber = currentSurah.surahNumber,
                fromAyah = minAyah,
                toAyah = maxAyah,
                ayahs = ayahs,
                slippedAyahs = setOf(activeAyah),
                onAyahToggled = { tappedAyah ->
                    selectedAyah = tappedAyah
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CTA Button: Recite from here
            Button(
                onClick = {
                    // Locate candidate card in memorizedCards covering selectedAyah
                    val targetCard = currentSurah.memorizedCards.find {
                        activeAyah >= it.fromAyah && activeAyah <= it.toAyah
                    } ?: currentSurah.memorizedCards.firstOrNull()

                    if (targetCard != null) {
                        onStartFreeSession(targetCard)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4A3D)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Recite from Ayah $activeAyah · أسمِع من هنا",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
