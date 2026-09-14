# GameDeck

An Armoury Crate / Ultra Game Mode style gaming console for Android, focused on PUBG.
Built with Kotlin + Jetpack Compose. Min SDK 26, target SDK 34.

## Features

- **Console dashboard** — CPU temperature, RAM, battery, storage, performance mode presets
  (Balanced / Performance / Ultra / Esports)
- **Game library** — auto-detects installed games (PUBG variants, CoD Mobile, Free Fire,
  MLBB, etc.) with one-tap launch
- **PUBG game profile** — touch / aim / gyro sensitivity sliders, launch-time automations
- **Game Genie overlay** (floating bubble, draggable):
  - Live FPS meter (Choreographer frame timing)
  - Do Not Disturb toggle
  - Brightness slider (needs *Write Settings*)
  - Screen recording (MediaProjection, H.264 + optional mic audio)
  - Direct PUBG launch
  - L/R **shoulder triggers** that dispatch real screen taps via the accessibility service
    (AirTrigger / Monster Touch style)
- Per-launch automation: auto DND, auto-start overlay

## Honest limitations

CPU/GPU clock control, real touch-sampling-rate changes, and kernel thermal tuning are
firmware-level features. Only the phone manufacturer (ASUS / vivo-iQOO) can implement
them. No third-party app — this one included — can do that without root and a custom
kernel. GameDeck implements every gaming feature that is legally accessible to
third-party apps.

## Build & install

1. Install Android Studio (Hedgehog or newer).
2. Open this folder as a project and let Gradle sync (needs internet once).
3. Build > Build Bundle(s)/APK(s) > Build APK(s).
4. Copy `app-debug.apk` to your phone and install it (allow "install from unknown sources").

Or from a terminal with Gradle and the Android SDK installed:

```bash
gradle assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

(This project has no Gradle wrapper committed, so use a locally installed
Gradle — matching the 8.7 version used in CI — rather than `./gradlew`.
If you'd rather have a wrapper, open the project in Android Studio and it
will offer to generate one, or run `gradle wrapper --gradle-version 8.7`
yourself once you have any Gradle install available.)

## First-run setup (grant these when asked, or from the Settings tab)

| Permission | Why |
|---|---|
| Display over other apps | Game Genie bubble & shoulder triggers |
| Do Not Disturb access | Silence notifications while gaming |
| Write Settings | Panel brightness slider |
| Accessibility ("GameDeck Trigger") | Shoulder trigger taps |
| Notifications + Microphone | Foreground service + recorded audio |
| Screen capture consent | Shown every recording start (Android requirement) |

## Usage

1. Open GameDeck → **Settings** tab → grant all permissions.
2. **Profile** tab → enable *Shoulder triggers* and configure sensitivities.
3. **Console** tab → tap **GAME GENIE OVERLAY**, then launch PUBG from the app.
4. In game: tap the FPS bubble to open the panel; use the floating L / R buttons as
   shoulder triggers. They tap the configured screen positions through the accessibility
   service.

## Project structure

- `data/ProfileStore.kt` — persisted profile/settings
- `core/SystemStats.kt`, `core/GameDetector.kt` — device stats & game detection
- `service/OverlayService.kt` — Game Genie overlay (FPS, panel, triggers)
- `service/ScreenRecorderService.kt` — MediaProjection screen recorder
- `service/TriggerService.kt` — accessibility tap dispatcher
- `ui/` — Compose screens (Console, Library, Profile, Settings)
- `MainActivity.kt` — navigation + permission flows + recording consent
