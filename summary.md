# Grimoire — Comprehensive Project Summary

**Grimoire** is an offline-first, privacy-centric habit tracking application for Android inspired by occult and arcane grimoire aesthetics. It couples strict on-device data sovereignty with a modern Android tech stack, featuring interactive Material 3 home-screen widgets, 120Hz fluid animations, localized temporal alarms, persistent streak telemetry, and local JSON backup/restore.

---

## 1. Project Overview & Core Objectives

The primary objective was to deliver a habit tracker fulfilling the **Foundational Scope (Pass Tier)** with uncompromising offline privacy:

1. **Room Habit Core**: Local habit inscription (title, sigil icon, resonance color, daily target, reminder schedule) with real-time streak telemetry and 30-day binary completion rates in SQLite/Room.
2. **Home-Screen App Widget**: Material 3 Glance interactive widget listing today’s habits in identical app order with instant 1-tap completions (`+` and `−`), Circadian phase badges, and reactive theme synchronization.
3. **Local Temporal Wake**: Zero-network daily reminder alarms scheduled through Android's `AlarmManager` (`RTC_WAKEUP`) and a midnight streak evaluation daemon powered by `WorkManager`, retaining alarms across device reboots (`RECEIVE_BOOT_COMPLETED`).
4. **Sovereign Vault**: Fully offline JSON export and import using Android's Storage Access Framework (SAF) and Kotlinx Serialization with atomic Room transaction rollback.

---

## 2. What Was Built & UI/UX Architecture

### 2.1 Aesthetic & Visual Design Philosophy
The user interface revolves around an arcane **Grimoire** theme:
- **Circadian Header**: Dynamically calculates the time of day and displays the active vigil:
  - *Dawn Vigil* (`✦`) (04:00 – 11:59)
  - *Solar Zenith* (`☀️`) (12:00 – 17:59)
  - *Dusk Ritual* (`🌙`) (18:00 – 21:59)
  - *Midnight Watch* (`🛡️`) (22:00 – 03:59)
- **Sigil Icons & Resonance Swatches**: Habits are styled with distinct sigil glyphs (📖 Study, 💧 Hydration, 🔥 Vitality, ⚔️ Discipline, 🧘 Mindfulness, ⚡ Craft, 🌙 Slumber, 🏃 Swiftness, 🛡️ Defense) and custom hex resonance glows.
- **Streak Flames & Freeze Shields**: Displays active streaks (`🔥 5d`), all-time longest streaks, and an interactive freeze shield (`🛡️`) that protects streaks against breaks.
- **30-Day Completion Matrix**: Interactive heatmap grid showing daily completion states over the trailing 30 days.

### 2.2 Screen Hierarchy & UI Components
- **`MainScreen.kt`**:
  - Circadian phase banner with daily completion counter pill (`✦ X/Y`).
  - Scrollable habit list using Material 3 elevated cards with completion progress bars and quick increment (`+`) / decrement (`−`) buttons.
  - Floating Action Button (FAB) inscribed with `+` to summon the Add/Edit Ritual modal.
  - Top app bar with access to Sovereign Vault (Export/Import) and the dedicated Settings page.
- **`AddHabitDialog.kt`**:
  - Modal bottom sheet / dialog with text input for habit name, sigil picker, color palette selector, daily target stepper (1 to 99), and reminder time picker.
- **`SettingsScreen.kt`**:
  - A dedicated full-page sliding view (replacing modals or popups).
  - **Header Controls**: Top-right cross icon (`✕`) to discard changes; bottom-right floating `Done` button to commit adjustments.
  - **Theme Gallery Section**: Dedicated section for theme modes:
    - **Dark** (Default mystical `#131316` surface, `#D0BCFF` violet, `#D4AF37` gold)
    - **Light** (`#FCF8FF` clean canvas, `#6750A4` deep purple)
    - **AMOLED** (`#000000` pitch black for battery saving on OLED panels)
    - **MD3 Expressive** (`#0E0A17` vibrant neon magenta and cyan highlights)
    - **Follow System** (Dynamically maps to system dark/light configuration)
  - **120Hz Animation Optimization**: Custom horizontal slide transitions (`slideInHorizontally` + `slideOutHorizontally`) paired with window background synchronization and strict z-index stacking to completely eliminate whitespace flashes and stutter on high-refresh-rate displays.

### 2.3 Interactive Material 3 Glance App Widget (`GrimoireGlanceWidget.kt`)
- **Jetpack Glance 1.1**: Renders native Android `RemoteViews` using Jetpack Compose syntax.
- **Live Theme Synchronization**: Adapts its background, cards, text, and button palette dynamically according to the app’s active theme mode.
- **One-Tap Counter Updates**: Direct `+` and `−` actions on widget rows update the Room database and change card state to "✓ Sealed" with emerald tinting.
- **Ordered Habits**: Always reflects the exact order of the app (`ORDER BY id DESC`).
- **Dynamic Resizing**: Configured with `resizeMode="horizontal|vertical"` from 1x1 to 4x4 grid cells (`100dp`–`600dp` width, `70dp`–`800dp` height).
- **Zero-Latency Sync**: Observes Room's reactive `Flow` inside `provideContent` via `collectAsState()` and uses a dual-trigger OS broadcast mechanism to synchronize launcher widgets immediately upon database mutations.

---

## 3. Technology Stack & Languages

### 3.1 Languages
- **Kotlin** (100% Kotlin codebase, Coroutines, Flow, Serialization, Jetpack Compose, Jetpack Glance)
- **XML** (Vector drawables, AppWidget provider metadata, manifest declarations, window style configurations)

### 3.2 Frameworks & Libraries
| Component | Library / Framework | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **UI Framework** | Jetpack Compose BOM / Material 3 | `2024.09.00` / `1.3.0` | Declarative UI, state management, animations |
| **App Widget** | Jetpack Glance AppWidget & Material 3 | `1.1.1` | Native Android home-screen interactive widgets |
| **Local Database** | AndroidX Room & Room KTX | `2.8.5` | SQLite persistence, reactive `Flow`, foreign keys, cascade deletes |
| **Annotation Processing** | Google KSP (Kotlin Symbol Processing) | Compatible | Fast compile-time DAO code generation |
| **Background Daemon** | AndroidX WorkManager | `2.9.1` | Midnight rollover worker for streak evaluation & widget updates |
| **Hardware Alarms** | Android `AlarmManager` | Native Android SDK | `setExactAndAllowWhileIdle()` for localized alarms |
| **Backup Serialization** | Kotlinx Serialization JSON | `1.7.3` | Schema validation, type-safe JSON export/import |
| **Storage Access** | Android Storage Access Framework (SAF) | Native Android SDK | User-directed file export/import via content URIs |
| **Theme Persistence** | Android DataStore / SharedPreferences | AndroidX | Persistent theme mode storage |

---

## 4. Skills & Engineering Practices

1. **Strict Zero-Network Privacy Architecture**:
   - `android.permission.INTERNET` is omitted from `AndroidManifest.xml`.
   - The OS hardware sandbox prevents all network calls.
   - Zero remote tracking, zero telemetry, zero analytics.
2. **Unidirectional Data Flow (UDF) & Clean Architecture**:
   - `HabitEntity` & `HabitLogEntity` (Data Layer)
   - `HabitDao` & `HabitRepository` (Domain/Data Layer with `transactionMutex` concurrency control)
   - `HabitViewModel` & `HabitUiModel` (State & Presentation Layer)
   - Compose & Glance Views (UI Layer)
3. **High-Refresh-Rate (120Hz) Display Optimization**:
   - Eliminated layout thrashing and alpha fade bleed-through.
   - Set window background to active theme surface color to prevent white-background flash during navigation transitions.
4. **Resilient Widget Data Binding**:
   - Moved Room data flow observation inside Glance’s `provideContent` scope (`collectAsState()`).
   - Integrated direct OS `AppWidgetManager.getAppWidgetIds()` updates and `ACTION_APPWIDGET_UPDATE` broadcasts to prevent stale state issues in launcher processes.

---

## 5. Prompts Given to Antigravity for Making the UI & Features

Below are the key prompts and instructions provided to Antigravity throughout the design, implementation, and refinement of the Grimoire UI and features:

```text
1. "how to raise pr for all these codes on github and only tell me commands do not do it"

2. "we need an app icon too"

3. "what is the command u use to compile an apk after changes has been made"

4. "remove the live theme preview bar from settings page and also if we press cross changes should not be applied and must be applied only when DONE is pressed in settings page (and rather make settings as a pop up keep it a dedicated slide or page)"

5. "remove cancel button instead keep the top right corner cross and remove done button instead keep the bottom right done button"

6. "add a theme section and keep the themes in that, remove the chronose rules from settings page"

7. "smoothen animations for 120Hz screen"

8. "the swap between settings page and home page feels jittery, the transtion adds a whitespace in between which makes it look ugly taccle that"

9. "make a material 3 ui based widget (should follow same theme pattern as selected in app), widget should display the habits as per app and in the same order,should be resizeable, update the changes made between app and widget instantly no lag or delay between them"

10. "widget is not syncing with app, it means if i add habit on app it doess not show up on widget, if clicked + or - from widget it updates in app but not in widget"

11. "this is our objective is the current app fulfilling it O B J E C T I V E
Deliver a privacy-centric offline habit tracker featuring interactive
home-screen App Widgets for instant 1-tap completions,
persistent streak telemetry, and localized alarm schedules.
F O U N D A T I O N A L S C O P E ( P A S S T I E R )
Room Habit Core: Create habits (name, icon, color, schedule)
with streak tracking and completion rates in Room.
✦
Home-Screen Widget: Interactive App Widget listing today's
habits with tap-to-complete updating instantly.
✦
Local Temporal Wake: Daily reminder alerts scheduled via
WorkManager / AlarmManager with zero network calls.
✦
Sovereign Vault: Fully offline operation with local JSON backup
and restore."
```
