# Chirpie 2

Chirpie 2 is the native Android port of the web-based Chirpie bird guide at
https://github.com/hanenashi/chirpie.

The app currently ships the Chirpie bird library offline using Android assets:
bird illustrations, metadata text files, and MP3 bird calls are bundled into
the app and loaded into a local Room database on first launch.

## Current State

The original offline-library checkpoint was tested on a Pixel 8. The features
added in this checkpoint build and pass automated checks, but the interaction
flows listed in [docs/TESTING.md](docs/TESTING.md) still need device testing.

- Real Chirpie web assets imported:
  - 51 bird images
  - 79 MP3 bird calls
  - 51 metadata text files
  - `birds.json`
- Room database seeded from bundled assets.
- Jetpack Compose dark UI.
- Square image grid of bird cards.
- White card backgrounds and consistent image fitting.
- Switchable card grid and compact bird list.
- Per-row bird names and playback controls in compact list mode.
- Persistent display mode and name-sorting settings.
- Settings dialog with active-list placeholder and order reset.
- Long-press grid arrangement with wiggle feedback, drag reordering, and save/cancel.
- Custom bird order persisted in Room with a non-destructive database migration.
- Persistent Summer, Winter, Favorites, and Study bird lists.
- Active-list filtering and per-bird list membership controls.
- In-app editing of Japanese, romanized, English, scientific, and Czech names.
- One-tap reset of edited names to bundled TXT source data.
- In-app custom bird import with JPG/PNG, one or two MP3 files, and text fields.
- Imported media copied into app-private storage for reliable offline use.
- Bird detail dialog with Japanese, romanized, English, scientific, and Czech names.
- Responsive, scrollable bird detail dialog for smaller screens.
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
workflow is included:

```bash
python3 tools/import_bird.py \
  --slug sample \
  --metadata /path/to/sample.txt \
  --image /path/to/sample.jpg \
  --audio /path/to/sample_call.mp3 \
  --dry-run
```

Remove `--dry-run` after validation. The importer rejects duplicate slugs and
IDs, copies the media into the asset tree, and updates `birds.json` atomically.

## Development Notes

- [Architecture and data flow](docs/ARCHITECTURE.md)
- [Physical-device test checklist](docs/TESTING.md)

## Roadmap

### Later

- Custom transparent PNG graphics for playback buttons and other controls.
- More complete parity with the original web Chirpie settings/edit behavior.
- Split the current screen implementation into smaller focused Compose files.
- Add database migration and custom media import integration tests.

## Notes

- The bundled asset data comes from the web Chirpie repository at commit
  `77717442bb1cf9373dbc5a0b253615a533fe9338`.
- Generated Android build output under `app/build/` is ignored.
