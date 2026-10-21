# Grimoire — The Chronos Seal
**Code Sangam 2026 // Droidrush (Computer Club, MNNIT Allahabad)**  
**Team Name:** SORCERERS (Team ID: 66)  
**Problem Statement:** 03 — The Chronos Seal  

---

## 1. Architectural Blueprint
- **Core Pattern:** Offline-First Reactive MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
- **Data Persistence:** AndroidX Room ORM backed by SQLite with **Write-Ahead Logging (WAL)** enabled for non-blocking concurrent reads and writes.
- **Surface Presentation:** Jetpack Compose (Material 3) + Jetpack Glance for Home-Screen App Widgets.
- **Temporal Engine:** AndroidX WorkManager for autonomous midnight rollover (`00:00:00`) + AlarmManager exact wake alarms for offline reminders.
- **Sovereign Vault:** Kotlinx Serialization JSON engine for 100% offline local backup and restore via Storage Access Framework (SAF).

---

## 2. App Widget Synchronization & WorkManager Lifecycle
- **Widget Invalidation Pipeline:** When a habit is toggled via the home-screen widget or the in-app UI, `HabitRepository` commits the transaction to Room and immediately invokes `GrimoireGlanceWidget().updateAll(context)`. The Android Launcher re-renders the Glance surface with zero process-launch overhead.
- **WorkManager Midnight Engine:** `MidnightRolloverWorker` is registered as a unique periodic work request (`ExistingPeriodicWorkPolicy.KEEP`). At `00:00:00`, it queries yesterday's log entries:
  - If target was met $\rightarrow$ streak remains intact.
  - If target was missed and `isFrozen == true` $\rightarrow$ consumes the Freeze Shield, preserving the streak.
  - If target was missed and `isFrozen == false` $\rightarrow$ resets `currentStreak = 0`.
  - Concludes by calling `updateAll()` to reset the home-screen widget's daily checkboxes.

---

## 3. Analysis of 3 Edge-Case Failure Modes & Recovery Paths
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

## 4. Local Data Schema Documentation
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

## 5. Tool & Library Disclosures
- **Language & Runtime:** Kotlin 2.1.0, JDK 17/21.
- **Persistence & Serialization:** AndroidX Room 2.6.1 (KSP), Kotlinx Serialization 1.7.3.
- **UI & Widget Framework:** Jetpack Compose (BOM), Material 3, Jetpack Glance 1.1.1.
- **System Engines:** AndroidX WorkManager 2.9.1, Android Core KTX.
- **AI Tooling Disclosure:** Architecture structuring and boilerplate prototyping assisted via LLM accelerators as disclosed per event rules.
