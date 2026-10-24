package com.sylergy.murajaah.mushaf

object AyahFormatter {
    const val BASMALA_TEXT = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"

    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun toArabicDigits(number: Int): String {
        val s = number.toString()
        val sb = java.lang.StringBuilder(s.length)
        for (ch in s) {
            if (ch in '0'..'9') {
                sb.append(ARABIC_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats an ayah number marker using the standard Quranic ornate brackets: ﴿ ١ ﴾
     */
    fun formatAyahMarker(ayahNumber: Int): String {
        return "\uFD3E${toArabicDigits(ayahNumber)}\uFD3F"
    }

    /**
     * Surah 9 (At-Tawbah) does not start with Basmala.
     * All other surahs start with Basmala when beginning from Ayah 1.
     */
    fun hasBasmalaHeader(surah: Int, fromAyah: Int): Boolean {
        return fromAyah == 1 && surah != 1 && surah != 9
    }
}
