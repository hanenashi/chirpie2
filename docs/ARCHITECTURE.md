# Architecture

Chirpie 2 is a single-activity Compose application with Room as the persistent
source of bird data.

## Data flow

```text
Bundled assets / custom document picker
                  |
                  v
          BirdRepository
          /             \
         v               v
    Room database   app-private media
         |
         v
 BirdListViewModel + BirdPreferences
         |
         v
    Compose screens
```

- `BirdAssetSeeder` reads the bundled `birds.json`, TXT, JPG, and MP3 assets.
- `BirdRepository` owns database writes, metadata reset, and custom media copy.
  It also deletes imported records and their private media after confirmation
  from the UI.
- `BirdListViewModel` combines Room flows with persistent display/filter
  settings and exposes one UI state.
- `BirdPreferences` stores display mode, active list, sort order, and grid
  column density.
- `BirdListScreen` renders grid/list modes, details, settings, editing,
  arrangement, saved-list membership, custom import, playback controls, and
  imported-bird deletion.

## Playback

`MainActivity` owns the single `MediaPlayer` and the currently playing asset
path. Starting another call releases the existing player first. The UI uses
that path to render play/stop state.

Pointer-down events inside app content stop active playback. Audio-button taps
are disambiguated so tapping the currently active button stops rather than
immediately restarting it. System back navigation is not intercepted and
therefore does not stop playback.

## Reordering and grid density

Grid and compact-list layouts share the same draft order while a long-press
drag is active. The dragged item receives lift, scale, alpha, shadow, and
haptic feedback; lazy-layout placement animation moves neighboring items.
Dragging near a vertical edge scrolls the collection. Releasing persists the
new order through Room.

Two-finger pinch gestures adjust the grid between two and six fixed columns.
Crossing a density step gives haptic feedback and writes the value to
`BirdPreferences`.

## Persistence

Room schema version 4 contains:

- `birds`: metadata, image/audio paths, and custom sort order.
- `bird_list_memberships`: many-to-many-style membership between birds and
  built-in saved lists.

Bundled media uses Android asset paths. In-app imported media is copied under
the application's private `files/custom_birds/<id>/` directory and referenced
with file URIs. This avoids relying on long-term access to the source document.
Deleting an imported bird removes its Room row, cascades saved-list
memberships, and deletes that private media directory. Bundled birds cannot be
deleted.

## Database migrations

- 2 → 3: adds `sortIndex` and initializes it from the bird ID.
- 3 → 4: adds the saved-list membership table and bird ID index.

Destructive migration is disabled, so future schema changes require explicit
migrations.

## Validation

The standard local validation command is:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

The project-side asset importer has separate Python tests:

```bash
PYTHONPATH=tools python3 -m unittest -v tools/test_import_bird.py
```
