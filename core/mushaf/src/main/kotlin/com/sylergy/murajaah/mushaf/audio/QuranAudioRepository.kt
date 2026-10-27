package com.sylergy.murajaah.mushaf.audio

import android.content.Context
import com.sylergy.murajaah.quran.Narration
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads and caches narration audio indexes from bundled assets
 * (e.g. assets/audio/hafs.json).
 */
class QuranAudioRepository(private val context: Context) {

    private val cache = ConcurrentHashMap<Narration, JSONObject>()

    private fun getIndex(narration: Narration): JSONObject? {
        val cached = cache[narration]
        if (cached != null) return cached

        val fileName = when (narration) {
            Narration.HAFS -> "hafs.json"
            Narration.WARSH -> "warsh.json"
            Narration.QALOON -> "qaloon.json"
            Narration.DOORI -> "doori.json"
            Narration.SOOSI -> "soosi.json"
            Narration.BAZZI -> "bazzi.json"
            Narration.QUMBUL -> "bazzi.json" // Shares Ibn Kathir timing
            else -> "hafs.json"
        }

        return try {
            context.assets.open("audio/$fileName").use { stream ->
                val reader = InputStreamReader(stream, Charsets.UTF_8)
                val jsonStr = reader.readText()
                val json = JSONObject(jsonStr)
                cache[narration] = json
                json
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resolves the streaming audio clip and per-ayah timestamps for a passage.
     */
    fun loadClip(
        narration: Narration,
        surah: Int,
        fromAyah: Int,
        toAyah: Int
    ): AudioClip? {
        val index = getIndex(narration) ?: return null
        val base = index.optString("base")
        if (base.isEmpty()) return null

        val surahsObj = index.optJSONObject("surahs") ?: return null
        val timings = surahsObj.optJSONArray(surah.toString()) ?: return null

        val startIdx = (fromAyah - 1) * 2
        val endIdx = (toAyah - 1) * 2 + 1

        if (startIdx < 0 || endIdx >= timings.length()) return null

        val startMs = timings.optInt(startIdx, -1)
        val endMs = timings.optInt(endIdx, -1)
        if (startMs < 0 || endMs < 0) return null

        val ayahs = mutableListOf<AyahTiming>()
        for (ayah in fromAyah..toAyah) {
            val aStart = timings.optInt((ayah - 1) * 2, -1)
            val aEnd = timings.optInt((ayah - 1) * 2 + 1, -1)
            if (aStart >= 0 && aEnd >= 0) {
                ayahs.add(AyahTiming(ayahNumber = ayah, startMs = aStart, endMs = aEnd))
            }
        }

        val formattedSurah = surah.toString().padStart(3, '0')
        val url = "$base$formattedSurah.mp3"

        return AudioClip(
            url = url,
            startMs = startMs,
            endMs = endMs,
            ayahs = ayahs
        )
    }
}
