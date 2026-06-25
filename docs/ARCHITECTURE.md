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
- `BirdListViewModel` combines Room flows with persistent display/filter
  settings and exposes one UI state.
- `BirdPreferences` stores display mode, active list, and sort order.
- `BirdListScreen` renders grid/list modes, details, settings, editing,
  arrangement, saved-list membership, and custom import.

## Persistence

Room schema version 4 contains:

- `birds`: metadata, image/audio paths, and custom sort order.
- `bird_list_memberships`: many-to-many-style membership between birds and
  built-in saved lists.

Bundled media uses Android asset paths. In-app imported media is copied under
the application's private `files/custom_birds/<id>/` directory and referenced
with file URIs. This avoids relying on long-term access to the source document.

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
