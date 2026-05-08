# Chirpie 2

Chirpie 2 is the native Android port of the web-based Chirpie bird guide at
https://github.com/hanenashi/chirpie.

The app currently ships the Chirpie bird library offline using Android assets:
bird illustrations, metadata text files, and MP3 bird calls are bundled into
the app and loaded into a local Room database on first launch.

## Current State

This checkpoint runs on Android and has been tested on a Pixel 8.

- Real Chirpie web assets imported:
  - 51 bird images
  - 79 MP3 bird calls
  - 51 metadata text files
  - `birds.json`
- Room database seeded from bundled assets.
- Jetpack Compose dark UI.
- Square image grid of bird cards.
- Bird detail dialog with Japanese, romanized, English, scientific, and Czech names.
- Multiple MP3 playback buttons per bird using Android `MediaPlayer`.
- Gradle wrapper added for repeatable builds.

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- Coil
- Android `MediaPlayer`
- Gradle wrapper

## Build

Open the repository folder in Android Studio:

```text
C:\GIT\chirpie2
```

Then run the `app` configuration on an emulator or physical Android device.

Command-line debug build:

```powershell
.\gradlew.bat :app:assembleDebug
```

The current Gradle setup uses Android Gradle Plugin 9.x and has a few warning
flags in `gradle.properties` from the initial Android Studio/Gemini upgrade.
The app builds successfully, but those warnings should be cleaned up later.

## Asset Layout

Bird data is intentionally kept close to the original web Chirpie layout:

```text
app/src/main/assets/birds.json
app/src/main/assets/birds/kogera/kogera.txt
app/src/main/assets/birds/kogera/kogera.jpg
app/src/main/assets/birds/kogera/kogera_ch.mp3
app/src/main/assets/birds/kogera/kogera_d.mp3
```

Each text file uses simple key/value metadata:

```text
id=7
romanized=kogera
kanji=コゲラ
scientific=Yungipicus kizuki
english=Japanese Pygmy Woodpecker
czech=Strakapoud japonský
```

This should make future bird additions straightforward: add a folder with a
text file, image, and MP3 files, then rebuild the app. A helper script for this
workflow is planned.

## Roadmap

### Near Term

- Polish the current grid:
  - white square card backgrounds
  - cleaner detail dialog sizing
  - better image fit consistency
- Add display modes:
  - card grid
  - compact list with names and play buttons in each row
- Add settings:
  - display mode
  - active list
  - sort/order options
  - reset order
- Add local ordering:
  - long press to enter arrange mode
  - wiggle state
  - drag/rearrange cards
  - confirm/cancel
  - save order locally

### Mid Term

- Saved bird lists:
  - all birds
  - summer birds
  - winter birds
  - favorites or study lists
- Sorting presets:
  - custom order
  - Japanese
  - English
  - Czech
  - scientific name
- In-app text editing for bird metadata.
- Reset edited text back to bundled source data.

### Later

- Easier project-side bird import helper for JPG/PNG, MP3, and TXT files.
- Optional in-app media import.
- Custom transparent PNG graphics for playback buttons and other controls.
- More complete parity with the original web Chirpie settings/edit behavior.

## Notes

- The bundled asset data comes from the web Chirpie repository at commit
  `77717442bb1cf9373dbc5a0b253615a533fe9338`.
- Generated Android build output under `app/build/` is ignored.
