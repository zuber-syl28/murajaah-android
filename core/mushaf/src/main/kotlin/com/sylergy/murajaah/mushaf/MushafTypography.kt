package com.sylergy.murajaah.mushaf

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import com.sylergy.murajaah.quran.Narration
import java.util.concurrent.ConcurrentHashMap

object MushafTypography {
    private val fontCache = ConcurrentHashMap<Narration, FontFamily>()

    fun getFontFamily(context: Context, narration: Narration): FontFamily {
        return fontCache.getOrPut(narration) {
            val assetPath = when (narration) {
                Narration.WARSH -> "fonts/warsh.ttf"
                Narration.QALOON -> "fonts/qaloon.ttf"
                Narration.DOORI -> "fonts/doori.ttf"
                Narration.SOOSI -> "fonts/soosi.ttf"
                Narration.SHOUBA -> "fonts/shouba.ttf"
                Narration.BAZZI -> "fonts/bazzi.ttf"
                Narration.QUMBUL -> "fonts/qumbul.ttf"
                Narration.HAFS -> null
            }

            if (assetPath != null) {
                try {
                    val typeface = Typeface.createFromAsset(context.assets, assetPath)
                    FontFamily(typeface)
                } catch (_: Exception) {
                    FontFamily.Serif
                }
            } else {
                FontFamily.Serif
            }
        }
    }
}
