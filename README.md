# Infinity Rush

Infinity Rush is a production-ready 2D endless runner built with Kotlin, `SurfaceView`, and the Android Canvas API. The project is designed to open directly in Android Studio and serves as a Play Store-ready starting point for a polished minimal runner.

## Features

- Endless auto-run gameplay with tap-to-jump and swipe-down-to-slide controls
- Randomized obstacle system with blocks, spikes, and moving barriers
- Curated obstacle pattern spawning for fairer, more readable runs
- Dynamic difficulty that ramps speed and spawn pressure every 10 seconds
- Buffered jump and slide input with short coyote-time forgiveness
- Start, pause, resume, and game-over flows
- Distance-based scoring with persistent high score using `SharedPreferences`
- In-game audio settings for music and sound effects
- Impact polish with particle bursts and screen shake feedback
- Background music via `MediaPlayer`
- Jump and crash sound effects via `SoundPool`
- No external art dependencies: visuals are rendered with Canvas shapes

## Tech Stack

- Kotlin
- Android Canvas + `SurfaceView`
- Minimum SDK 24
- Target SDK 36
- Package name: `com.infinityrush.game`

## Open In Android Studio

1. Launch Android Studio.
2. Select **Open**.
3. Choose the project root folder: `infinityrush`.
4. Allow Android Studio to sync Gradle.
5. If prompted, use JDK 17 and install Android SDK Platform 36.

## Run The Game

1. Connect an Android device or start an emulator.
2. Click **Run 'app'** in Android Studio.
3. From the start screen, press **Play** to begin.

## Generate A Debug APK

1. Open the project in Android Studio.
2. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
3. Android Studio will output the APK under:
   `app/build/outputs/apk/debug/`

## Generate A Release Build

1. Open **Build > Generate Signed Bundle / APK**.
2. Create or choose a keystore.
3. Select **Android App Bundle** for Google Play, or **APK** for side loading.
4. Complete the signing flow and build the release artifact.

## Controls

- Tap: Jump
- Swipe down: Slide
- Tap pause icon: Pause
- Settings button on menus: Toggle music and sound effects
- Tap Resume: Continue the run

## Project Structure

- `MainActivity.kt`: Activity host, immersive mode, lifecycle handoff
- `GameView.kt`: Game loop, state machine, update/render pipeline, input
- `Player.kt`: Player movement, collision box, and rendering
- `Obstacle.kt`: Obstacle definitions, animation, and rendering
- `Constants.kt`: Tuning values and configuration
- `Utils.kt`: Shared preferences and view helpers
- `SoundManager.kt`: SoundPool and MediaPlayer orchestration

## Notes For Publishing

- Replace the placeholder launcher icon if needed.
- Update `versionCode` and `versionName` in `app/build.gradle.kts` before release.
- Add your signing config and Play Store listing assets before publishing.
