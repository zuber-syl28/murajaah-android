# Murajaah Android · مراجعة

*Structured Qur'an revision with the people who help you retain it.*

Murajaah Android is the native Android implementation of the Murajaah two-person revision loop: **prompt → mushaf → grade → reschedule**. Built with 100% Kotlin, Jetpack Compose, and an offline-first Room database, it brings the FSRS-6 spaced repetition engine, authentic King Fahd Mushaf typography, and mutual Tasmīʿ companion reviewing to mobile.

---

## Key Features

### 1. Revision Engine & Schedulers
* **Pure Kotlin FSRS-6.0:** Implements the Free Spaced Repetition Scheduler algorithm with status-weighted intervals, review counts, and 90% target retrievability.
* **Khatmah Cycle Bounds:** Enforces traditional revision cycles (7 days for 1–5 Ajzāʾ, 14 days for 6–15, 21 days for 16–25, and 30 days beyond) so no memorized passage goes unheard past its cycle.
* **Two-Lane Sitting Queue:** Balances each daily Wird between *Strengthening* (recent weak/hesitant passages) and *Rotation* (broader review).
* **Sequential Wird (الوِرد المرتب):** Optional traditional sequential reading in Mushaf order continuing from where yesterday stopped, automatically inserting up to 2 due weak cards at the start.

### 2. Live Session & Tathbeet (Spot Repair)
* **Active Revision Player:** Focus mode with floor timer, hint steps (first word, then half), and in-session slipped ayah tagging.
* **Spot Repair (تثبيت على الفور):** Immediate intervention when a recitation slips (3 repetitions for *Review*, 5 for *Weak*, 7 for stubborn slips) with Mushaf text and audio read-along.
* **Final Retest:** Slipped passages are recited from memory once more before session wrap-up (*It Held Firm* vs *Still Slipped*).
* **Free Tasmīʿ (تسميع حر):** Open any memorized surah and tap any ayah to launch an instant test on that passage.

### 3. Mushaf Typography & Audio Playback
* **KFGQPC Fonts:** Bundled authentic Uthmanic fonts across 7 distinct Riwayat (Ḥafṣ, Warsh, Qālūn, Ad-Dūrī, As-Sūsī, Al-Bazzī, and Qunbul).
* **Synchronized Audio Recitations:** Streaming reciter audio synchronized to each ayah's millisecond timestamps, supporting playback speeds (`1.0x`, `1.25x`, and `1.5x`).
* **Interactive Canvas:** Tap ayahs to toggle slips during review or select starting points in Free Tasmīʿ.

### 4. Mutual Tasmīʿ & Social Circle
* **Two Roles, One Account:** Every user can recite to a companion and listen to their friends.
* **One-Tap Invites & QR Scanner:** Deep-link invite URLs (48-hour expiration) and in-app camera QR code scanner.
* **Examiner Mode:** Live listening screen allowing a listener or teacher to follow along, tag slips in real time, and record peer/teacher weighted grades.

### 5. Progress, Plans & Widgets
* **Retention Dashboard:** Itqan ring gauge, weekly consistency histogram, and interactive 30-Juz mastery map.
* **Custom Revision Plans:** Create and switch named plans (e.g., *Juz' 'Amma*, *Al-Baqarah*, *Ramadan Wird*).
* **Glance AppWidget:** Android Home Screen widget displaying active plan, due card counts, and 1-tap Wird launching.
* **Non-Shaming Reminders:** WorkManager-scheduled notifications that adjust intelligently based on revision gaps.

---

## Architecture

The project follows a clean, decoupled multi-module architecture:

```
├── app/                  # Main Application, Compose Theme & Activity Host
├── core/
│   ├── fsrs/             # Pure Kotlin FSRS-6.0 Spaced Repetition Engine
│   ├── quran/            # Static Quran metadata, 114 Surahs, Hizb Spans & Riwayat
│   ├── scheduler/        # Khatmah Cycle, Two-Lane Queue & Sequential Schedulers
│   ├── database/         # Room Database, SQLite DAOs & Entities
│   ├── network/          # Retrofit API Client, Cloudflare D1 Models & WorkManager Sync
│   └── mushaf/           # Canvas Rendering, Typography & Reciter Audio Engine
└── feature/
    ├── session/          # Wird Player, Free Tasmi, Spot Repair & Retest
    ├── reviewing/        # Examiner Live Listening & Slip Marking
    ├── friends/          # Companion Management, Deep Linking & QR Scanner
    ├── progress/         # Mastery Dashboard, Itqan Gauge & 30-Juz Map
    ├── achievements/     # Permanent Non-Lossy Milestone Badges
    ├── settings/         # Revision Plans & Qira'at Riwayah Switcher
    ├── reminders/        # Intelligent Push Notification Scheduler
    ├── voice/            # P2P Audio Calls & WebRTC Signaling
    └── widget/           # Android Glance Home Screen AppWidget
```

---

## Getting Started

### Prerequisites
* **Android Studio:** Iguana / Ladybug or newer
* **JDK:** Version 17 or 21
* **Android SDK:** Compile SDK 35 (Min SDK 26)

### Build & Run
```bash
# Clone the repository
git clone https://github.com/zuber-syl28/murajaah-android.git
cd murajaah-android

# Run all unit tests across all modules
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug

# Install and launch on connected device or emulator
./gradlew installDebug
adb shell am start -n com.sylergy.murajaah/.MainActivity
```

---

## Design System

* **Warm Parchment (`#F8F7F2`):** Soft cream background mirroring traditional Mushaf paper to minimize eye strain.
* **Deep Forest Green (`#2F4A3D`):** Primary branding color representing classical Islamic bindings.
* **Typography:** KFGQPC Uthmanic Arabic typography paired with clean Material 3 typography.

---

## License

This project is part of the Murajaah ecosystem. All Qur'an text and typography assets follow the King Fahd Glorious Quran Printing Complex guidelines.
