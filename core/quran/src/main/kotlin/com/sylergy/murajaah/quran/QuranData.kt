package com.sylergy.murajaah.quran

object QuranData {
    const val TOTAL_AYAHS = 6236

    val SURAHS: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fātiḥah", 7),
        Surah(2, "البقرة", "Al-Baqarah", 286),
        Surah(3, "آل عمران", "Āl ʿImrān", 200),
        Surah(4, "النساء", "An-Nisāʾ", 176),
        Surah(5, "المائدة", "Al-Māʾidah", 120),
        Surah(6, "الأنعام", "Al-Anʿām", 165),
        Surah(7, "الأعراف", "Al-Aʿrāf", 206),
        Surah(8, "الأنفال", "Al-Anfāl", 75),
        Surah(9, "التوبة", "At-Tawbah", 129),
        Surah(10, "يونس", "Yūnus", 109),
        Surah(11, "هود", "Hūd", 123),
        Surah(12, "يوسف", "Yūsuf", 111),
        Surah(13, "الرعد", "Ar-Raʿd", 43),
        Surah(14, "إبراهيم", "Ibrāhīm", 52),
        Surah(15, "الحجر", "Al-Ḥijr", 99),
        Surah(16, "النحل", "An-Naḥl", 128),
        Surah(17, "الإسراء", "Al-Isrāʾ", 111),
        Surah(18, "الكهف", "Al-Kahf", 110),
        Surah(19, "مريم", "Maryam", 98),
        Surah(20, "طه", "Ṭāhā", 135),
        Surah(21, "الأنبياء", "Al-Anbiyāʾ", 112),
        Surah(22, "الحج", "Al-Ḥajj", 78),
        Surah(23, "المؤمنون", "Al-Muʾminūn", 118),
        Surah(24, "النور", "An-Nūr", 64),
        Surah(25, "الفرقان", "Al-Furqān", 77),
        Surah(26, "الشعراء", "Ash-Shuʿarāʾ", 227),
        Surah(27, "النمل", "An-Naml", 93),
        Surah(28, "القصص", "Al-Qaṣaṣ", 88),
        Surah(29, "العنكبوت", "Al-ʿAnkabūt", 69),
        Surah(30, "الروم", "Ar-Rūm", 60),
        Surah(31, "لقمان", "Luqmān", 34),
        Surah(32, "السجدة", "As-Sajdah", 30),
        Surah(33, "الأحزاب", "Al-Aḥzāb", 73),
        Surah(34, "سبأ", "Sabaʾ", 54),
        Surah(35, "فاطر", "Fāṭir", 45),
        Surah(36, "يس", "Yā-Sīn", 83),
        Surah(37, "الصافات", "Aṣ-Ṣāffāt", 182),
        Surah(38, "ص", "Ṣād", 88),
        Surah(39, "الزمر", "Az-Zumar", 75),
        Surah(40, "غافر", "Ghāfir", 85),
        Surah(41, "فصلت", "Fuṣṣilat", 54),
        Surah(42, "الشورى", "Ash-Shūrā", 53),
        Surah(43, "الزخرف", "Az-Zukhruf", 89),
        Surah(44, "الدخان", "Ad-Dukhān", 59),
        Surah(45, "الجاثية", "Al-Jāthiyah", 37),
        Surah(46, "الأحقاف", "Al-Aḥqāf", 35),
        Surah(47, "محمد", "Muḥammad", 38),
        Surah(48, "الفتح", "Al-Fatḥ", 29),
        Surah(49, "الحجرات", "Al-Ḥujurāt", 18),
        Surah(50, "ق", "Qāf", 45),
        Surah(51, "الذاريات", "Adh-Dhāriyāt", 60),
        Surah(52, "الطور", "Aṭ-Ṭūr", 49),
        Surah(53, "النجم", "An-Najm", 62),
        Surah(54, "القمر", "Al-Qamar", 55),
        Surah(55, "الرحمن", "Ar-Raḥmān", 78),
        Surah(56, "الواقعة", "Al-Wāqiʿah", 96),
        Surah(57, "الحديد", "Al-Ḥadīd", 29),
        Surah(58, "المجادلة", "Al-Mujādilah", 22),
        Surah(59, "الحشر", "Al-Ḥashr", 24),
        Surah(60, "الممتحنة", "Al-Mumtaḥanah", 13),
        Surah(61, "الصف", "Aṣ-Ṣaff", 14),
        Surah(62, "الجمعة", "Al-Jumuʿah", 11),
        Surah(63, "المنافقون", "Al-Munāfiqūn", 11),
        Surah(64, "التغابن", "At-Taghābun", 18),
        Surah(65, "الطلاق", "Aṭ-Ṭalāq", 12),
        Surah(66, "التحريم", "At-Taḥrīm", 12),
        Surah(67, "الملك", "Al-Mulk", 30),
        Surah(68, "القلم", "Al-Qalam", 52),
        Surah(69, "الحاقة", "Al-Ḥāqqah", 52),
        Surah(70, "المعارج", "Al-Maʿārij", 44),
        Surah(71, "نوح", "Nūḥ", 28),
        Surah(72, "الجن", "Al-Jinn", 28),
        Surah(73, "المزمل", "Al-Muzzammil", 20),
        Surah(74, "المدثر", "Al-Muddaththir", 56),
        Surah(75, "القيامة", "Al-Qiyāmah", 40),
        Surah(76, "الإنسان", "Al-Insān", 31),
        Surah(77, "المرسلات", "Al-Mursalāt", 50),
        Surah(78, "النبأ", "An-Nabaʾ", 40),
        Surah(79, "النازعات", "An-Nāziʿāt", 46),
        Surah(80, "عبس", "ʿAbasa", 42),
        Surah(81, "التكوير", "At-Takwīr", 29),
        Surah(82, "الانفطار", "Al-Infiṭār", 19),
        Surah(83, "المطففين", "Al-Muṭaffifīn", 36),
        Surah(84, "الانشقاق", "Al-Inshiqāq", 25),
        Surah(85, "البروج", "Al-Burūj", 22),
        Surah(86, "الطارق", "Aṭ-Ṭāriq", 17),
        Surah(87, "الأعلى", "Al-Aʿlā", 19),
        Surah(88, "الغاشية", "Al-Ghāshiyah", 26),
        Surah(89, "الفجر", "Al-Fajr", 30),
        Surah(90, "البلد", "Al-Balad", 20),
        Surah(91, "الشمس", "Ash-Shams", 15),
        Surah(92, "الليل", "Al-Layl", 21),
        Surah(93, "الضحى", "Aḍ-Ḍuḥā", 11),
        Surah(94, "الشرح", "Ash-Sharḥ", 8),
        Surah(95, "التين", "At-Tīn", 8),
        Surah(96, "العلق", "Al-ʿAlaq", 19),
        Surah(97, "القدر", "Al-Qadr", 5),
        Surah(98, "البينة", "Al-Bayyinah", 8),
        Surah(99, "الزلزلة", "Az-Zalzalah", 8),
        Surah(100, "العاديات", "Al-ʿĀdiyāt", 11),
        Surah(101, "القارعة", "Al-Qāriʿah", 11),
        Surah(102, "التكاثر", "At-Takāthur", 8),
        Surah(103, "العصر", "Al-ʿAṣr", 3),
        Surah(104, "الهمزة", "Al-Humazah", 9),
        Surah(105, "الفيل", "Al-Fīl", 5),
        Surah(106, "قريش", "Quraysh", 4),
        Surah(107, "الماعون", "Al-Māʿūn", 7),
        Surah(108, "الكوثر", "Al-Kawthar", 3),
        Surah(109, "الكافرون", "Al-Kāfirūn", 6),
        Surah(110, "النصر", "An-Naṣr", 3),
        Surah(111, "المسد", "Al-Masad", 5),
        Surah(112, "الإخلاص", "Al-Ikhlāṣ", 4),
        Surah(113, "الفلق", "Al-Falaq", 5),
        Surah(114, "الناس", "An-Nās", 6)
    )

    fun surah(number: Int): Surah? =
        if (number in 1..SURAHS.size) SURAHS[number - 1] else null

    val HIZB_STARTS: List<Pair<Int, Int>> = listOf(
        1 to 1, 2 to 75, 2 to 142, 2 to 203, 2 to 253, 3 to 15, 3 to 93, 3 to 171, 4 to 24, 4 to 88,
        4 to 148, 5 to 27, 5 to 82, 6 to 36, 6 to 111, 7 to 1, 7 to 88, 7 to 171, 8 to 41, 9 to 34,
        9 to 93, 10 to 26, 11 to 6, 11 to 84, 12 to 53, 13 to 19, 15 to 1, 16 to 51, 17 to 1, 17 to 99,
        18 to 75, 20 to 1, 21 to 1, 22 to 1, 23 to 1, 24 to 21, 25 to 21, 26 to 111, 27 to 56, 28 to 51,
        29 to 46, 31 to 22, 33 to 31, 34 to 24, 36 to 28, 37 to 145, 39 to 32, 40 to 41, 41 to 47, 43 to 24,
        46 to 1, 48 to 18, 51 to 31, 55 to 1, 58 to 1, 62 to 1, 67 to 1, 72 to 1, 78 to 1, 87 to 1
    )

    val HIZB_COUNT: Int = HIZB_STARTS.size

    fun hizbStart(hizb: Int): Pair<Int, Int>? =
        if (hizb in 1..HIZB_COUNT) HIZB_STARTS[hizb - 1] else null

    fun surahSpans(from: Int, to: Int): List<AyahSpan> {
        val spans = mutableListOf<AyahSpan>()
        for (n in from..to) {
            val meta = surah(n) ?: continue
            spans.add(AyahSpan(n, 1, meta.ayahCount))
        }
        return spans
    }

    fun hizbSpans(from: Int, to: Int): List<AyahSpan> {
        if (from !in 1..HIZB_COUNT || to < from || to > HIZB_COUNT) return emptyList()
        val start = HIZB_STARTS[from - 1]
        val next = if (to < HIZB_COUNT) HIZB_STARTS[to] else null

        var endSurah = next?.first ?: 114
        var endAyah = if (next != null) next.second - 1 else surah(114)!!.ayahCount
        if (endAyah == 0) {
            endSurah -= 1
            endAyah = surah(endSurah)!!.ayahCount
        }

        val spans = mutableListOf<AyahSpan>()
        for (n in start.first..endSurah) {
            val fromA = if (n == start.first) start.second else 1
            val toA = if (n == endSurah) endAyah else surah(n)!!.ayahCount
            spans.add(AyahSpan(n, fromA, toA))
        }
        return spans
    }

    fun uncoveredSpans(span: AyahSpan, existing: List<AyahSpan>): List<AyahSpan> {
        var pieces = listOf(span)
        for (range in existing) {
            if (range.surah != span.surah) continue
            pieces = pieces.flatMap { piece ->
                if (range.toAyah < piece.fromAyah || range.fromAyah > piece.toAyah) {
                    listOf(piece)
                } else {
                    val left = if (range.fromAyah > piece.fromAyah) {
                        listOf(piece.copy(toAyah = range.fromAyah - 1))
                    } else emptyList()
                    val right = if (range.toAyah < piece.toAyah) {
                        listOf(piece.copy(fromAyah = range.toAyah + 1))
                    } else emptyList()
                    left + right
                }
            }
        }
        return pieces
    }

    fun subtractSpans(ranges: List<AyahSpan>, cuts: List<AyahSpan>): List<AyahSpan> {
        return ranges.flatMap { range -> uncoveredSpans(range, cuts) }
    }
}
