# Grimoire — The Chronos Seal

**Code Sangam 2026 // Droidrush (Computer Club, MNNIT Allahabad)**  
**Team Name:** SORCERERS (Team ID: 66)  
**Problem Statement:** 03 — The Chronos Seal  

Grimoire is an offline-first, privacy-respecting habit tracker designed with robust architectural patterns, providing seamless synchronization between your home-screen widget and the main app. 

---

## 🏗️ 1. Structured & Modular Codebase Architecture
The project adheres to modern Android development practices, ensuring scalability, testability, and clarity.

- **Architecture Pattern:** Offline-First Reactive MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
- **Presentation Layer (`com.sorcerers.grimoire.ui` & `com.example.grimoire.widget`):** Jetpack Compose for declarative UI, alongside Jetpack Glance for real-time App Widgets.
- **Domain & ViewModel Layer (`com.example.grimoire.viewmodel`):** Manages UI states and orchestrates events between the UI and repository.
- **Data Layer (`com.example.grimoire.data`):**
  - **Local Persistence (`data.local`):** AndroidX Room ORM backed by SQLite with **Write-Ahead Logging (WAL)** enabled for non-blocking concurrent reads and writes.
  - **Vault Management (`data.vault`):** Local JSON backup and restore logic using Kotlinx Serialization.
  - **Background Work (`data.worker`):** Autonomous midnight rollover logic using WorkManager and AlarmManager.

This modular structure ensures that the UI, business logic, and data storage are fully decoupled.

---

## ✨ 2. Mystical Features & UX Elements
- **Circadian Ritual Phase Badge:** A dynamic TopAppBar badge that calculates the time of day and adjusts its icon, label, and colors:
  - ✦ **Dawn Vigil** (04:00 – 11:59)
  - ☀️ **Solar Zenith** (12:00 – 17:59)
  - 🌙 **Dusk Ritual** (18:00 – 21:59)
  - 🛡 **Midnight Watch** (22:00 – 03:59)
- **Chronos Heatmap (30 Days):** An interactive 30-day activity grid for each habit. Cell colors dynamically match the chosen "Arcane Resonance Color."
- **Habit Editing & Sigil Customization:** Edit targets, icons (Sigils), and notification reminders on the fly.
- **Chronos Seal Laws:** Built-in rules outlining Streak Progression and Freeze Shields.
- **Fully Adaptive UI:** Material 3 integration with seamless Light/Dark mode contrast scaling.

---

## ⚙️ 3. Advanced Synchronization & WorkManager Lifecycle
- **Widget Invalidation Pipeline:** When a habit is toggled, `HabitRepository` commits the transaction and triggers `GrimoireGlanceWidget().updateAll(context)`. The Android Launcher re-renders the Glance surface instantly.
- **WorkManager Midnight Engine:** `MidnightRolloverWorker` queries yesterday's log entries at `00:00:00`:
  - Target met $\rightarrow$ streak remains intact.
  - Target missed + Freeze Shield active $\rightarrow$ consumes the shield, preserving the streak.
  - Target missed + no shield $\rightarrow$ resets `currentStreak = 0`.
  - Resets the home-screen widget's daily checkboxes.

---

## 🛠 4. Setup & Build Instructions

### Prerequisites
- **Android Studio:** Ladybug Feature Drop | 2024.2.2 (or newer recommended)
- **JDK:** Java 17 (or newer)
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 35 (Android 15)

### Build & Run
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-repo/grimoire.git
   cd grimoire
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select **Open an existing project** and choose the `grimoire` folder.
   - Let Gradle sync completely.
3. **Run the App:**
   - Connect an emulator or physical device.
   - Click the **Run** button (▶) or execute:
     ```bash
     ./gradlew assembleDebug
     ```
4. **Run Unit Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

### Sovereign Vault (Backup & Restore)
- Navigate to **Settings** -> **Sovereign Vault**.
- Use **Export** to save a `.json` backup of your habits and logs to your device.
- Use **Import** to restore from an existing JSON backup.

---

## 🛡️ 5. Edge-Case Resilience
1. **Device Reboot Across Midnight:** A static `BootReceiver` reschedules exact notification timers and triggers catch-up verification.
2. **Widget Race Conditions:** Database operations are serialized via Coroutine `Mutex`, using atomic `@Transaction`s to prevent lost updates.
3. **Corrupted JSON Vault Import:** Restore operations run inside `database.withTransaction`. Failures trigger an automatic rollback to prevent partial corruption.

---

## 📚 6. Tech Stack & Library Disclosures
- **Language:** Kotlin 2.2.10
- **UI & Widget Framework:** Jetpack Compose (BOM), Material 3, Jetpack Glance 1.1.1
- **Persistence & Serialization:** AndroidX Room 2.8.5 (KSP2), Kotlinx Serialization 1.7.3
- **System Engines:** AndroidX WorkManager 2.9.1, AlarmManager, Android Core KTX
- **Testing:** JUnit4, Robolectric 4.17, Kotlinx Coroutines Test 1.7.3
- **AI Tooling Disclosure:** Architecture structuring, UI refinement, and testing prototyping assisted via LLM accelerators as disclosed per event rules.