# Grimoire — The Chronos Seal
**Code Sangam 2026 // Droidrush (Computer Club, MNNIT Allahabad)**  
**Team Name:** SORCERERS (Team ID: 66)  
**Problem Statement:** 03 — The Chronos Seal  

---

## 1. Architectural Blueprint
- **Core Pattern:** Offline-First Reactive MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
- **Data Persistence:** AndroidX Room ORM (v2.8.5 with KSP2) backed by SQLite with **Write-Ahead Logging (WAL)** enabled for non-blocking concurrent reads and writes.
- **Surface Presentation:** Jetpack Compose (Material 3) + Jetpack Glance for Home-Screen App Widgets. Features fully adaptive Light/Dark mode contrast scaling.
- **Temporal Engine:** AndroidX WorkManager for autonomous midnight rollover (`00:00:00`) + AlarmManager exact wake alarms for offline reminders.
- **Sovereign Vault:** Kotlinx Serialization JSON engine for 100% offline local backup and restore via Storage Access Framework (SAF).

---

## 2. Mystical Features & UX Elements
- **Circadian Ritual Phase Badge:** A dynamic, 100% offline TopAppBar badge that calculates the time of day and adjusts its icon, label, and Material 3 tonal colors (supporting both light and dark mode):
  - ✦ **Dawn Vigil** (04:00 – 11:59)
  - ☀️ **Solar Zenith** (12:00 – 17:59)
  - 🌙 **Dusk Ritual** (18:00 – 21:59)
  - 🛡 **Midnight Watch** (22:00 – 03:59)
- **Chronos Heatmap (30 Days):** An interactive 30-day activity grid for each habit. Cell colors dynamically match the user's chosen "Arcane Resonance Color". Features an animated, hovering tooltip displaying the exact date when tapped.
- **Habit Editing & Sigil Customization:** Tap a habit's title to summon the edit dialog and update the target, icon (Sigil), color scheme, or notification reminders on the fly, seamlessly rescheduling offline alarms in the background.
- **Chronos Seal Laws:** Accessible directly from the overflow menu, explaining the strict rules of the Temporal Daemon, Streak Progression, and Freeze Shields.

---

## 3. App Widget Synchronization & WorkManager Lifecycle
- **Widget Invalidation Pipeline:** When a habit is toggled via the home-screen widget or the in-app UI, `HabitRepository` commits the transaction to Room and immediately invokes `GrimoireGlanceWidget().updateAll(context)`. The Android Launcher re-renders the Glance surface with zero process-launch overhead.
- **WorkManager Midnight Engine:** `MidnightRolloverWorker` is registered as a unique periodic work request (`ExistingPeriodicWorkPolicy.KEEP`). At `00:00:00`, it queries yesterday's log entries:
  - If target was met $\rightarrow$ streak remains intact.
  - If target was missed and `isFrozen == true` $\rightarrow$ consumes the Freeze Shield, preserving the streak.
  - If target was missed and `isFrozen == false` $\rightarrow$ resets `currentStreak = 0`.
  - Concludes by calling `updateAll()` to reset the home-screen widget's daily checkboxes.

---

## 4. Analysis of 3 Edge-Case Failure Modes & Recovery Paths
1. **Edge Case 1: Device Power-Off / Reboot Across Midnight Boundary**
   - *Failure:* Alarms in `AlarmManager` are cleared by the OS on power-down, and `WorkManager` may miss the exact midnight execution window if the battery dies at 23:59.
   - *Recovery Path:* A static `BootReceiver` listening for `Intent.ACTION_BOOT_COMPLETED` automatically wakes on reboot, reschedules all exact notification timers, and triggers an immediate catch-up verification of missed dates before rendering.
2. **Edge Case 2: Concurrent Widget Taps & Race Conditions**
   - *Failure:* Rapidly spamming the widget completion button can cause read-modify-write collisions and duplicate streak counts.
   - *Recovery Path:* Database operations are serialized through a coroutine `Mutex` in `HabitRepository`. Each tap writes an append-only log entry (`HabitLogEntity`) within an atomic SQLite `@Transaction`, ensuring idempotency and zero lost updates.
3. **Edge Case 3: Corrupted or Tampered JSON Vault Import**
   - *Failure:* The user selects a malformed, modified, or foreign JSON file during restoration, which could crash the app or corrupt database tables.
   - *Recovery Path:* `SovereignVaultManager` parses the payload with strict schema validation. The restore runs inside `database.withTransaction { ... }`. If JSON parsing or entity insertion fails at any point, Room rolls back the entire database to its pre-import state, emits a `VaultNotification.Error`, and prevents partial data corruption.

---

## 5. Local Data Schema & Testing
- **Automated Testing Suite:** Features a robust Robolectric test suite (`BackendIntegrationTest.kt`) designed to validate the `HabitRepository` event streams, streak tracking logic, and `SovereignVaultManager` data recovery systems entirely offline.
- **`habits` Table:**
  - `id` (Long, Primary Key, AutoGenerate)
  - `title` (String), `iconName` (String), `colorHex` (String)
  - `targetPerDay` (Int), `currentStreak` (Int), `longestStreak` (Int)
  - `isFrozen` (Boolean), `reminderTime` (String?), `createdAt` (Long)
- **`habit_logs` Table:**
  - `logId` (Long, Primary Key, AutoGenerate)
  - `habitId` (Long, Foreign Key $\rightarrow$ `habits.id` ON DELETE CASCADE, Indexed)
  - `dateString` (String, Indexed with `habitId` as Unique), `completedCount` (Int), `updatedTimestamp` (Long)

---

## 6. Tool & Library Disclosures
- **Language & Runtime:** Kotlin 2.2.10, JDK 17/21.
- **Persistence & Serialization:** AndroidX Room 2.8.5 (KSP2), Kotlinx Serialization 1.7.3.
- **Testing:** JUnit4, Robolectric 4.17, Kotlinx Coroutines Test 1.7.3.
- **UI & Widget Framework:** Jetpack Compose (BOM), Material 3, Jetpack Glance 1.1.1.
- **System Engines:** AndroidX WorkManager 2.9.1, Android Core KTX.
- **AI Tooling Disclosure:** Architecture structuring, UI refinement, testing, and boilerplate prototyping assisted via LLM accelerators as disclosed per event rules.