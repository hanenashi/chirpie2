# Physical-device Test Checklist

Run this checklist on an Android phone before treating the current checkpoint
as release-ready.

## Current device status

- Pixel 8: original offline-library checkpoint tested.
- Pixel 10a: current APK installed through Kurochan ADB; launch, browsing,
  animated reordering, and pinch density smoke-tested.
- Full playback-stop, deletion, migration, rotation, accessibility, and
  provider-backed import regression testing remains recommended.

## Install and migration

- Install the previous version, launch it, then install the new APK over it.
- Confirm all 51 bundled birds remain available after the Room 2 → 4 migration.
- Cold-start the app and verify the grid appears without a blank intermediate
  state or crash.

## Display and playback

- Confirm the Chirpie launcher icon renders cleanly as circle, squircle, and
  rounded-square launcher masks.
- Switch between card grid and compact list; restart and confirm persistence.
- Open several bird details and play every available call.
- Confirm the active call shows a stop icon.
- Tap the active button again and confirm playback stops.
- Start a call, tap elsewhere in the app, and confirm playback stops.
- Start a call and use the system back gesture/button; confirm playback continues.
- Confirm starting another call stops the previous one.
- Let a call finish naturally and confirm its control returns to the play icon.
- Verify images fit correctly in grid, list, and detail views.

## Sorting and arrangement

- Exercise every name sort and confirm the expected language ordering.
- Reset to custom order.
- Long-press and drag cards and compact-list rows across several positions.
- Confirm the dragged item lifts and follows the finger while neighboring items
  animate aside.
- Drag near the top and bottom edges and confirm the collection auto-scrolls.
- Release and restart the app; confirm the order persists.
- Pinch inward and outward across all two-to-six-column density steps.
- Restart the app and confirm the selected grid density persists.

## Saved lists and text editing

- Add and remove birds from Summer, Winter, Favorites, and Study.
- Select each active list and confirm only its members appear.
- Edit all five name fields, save, restart, and confirm persistence.
- Reset a bundled bird's text and confirm the original TXT values return.

## Custom import

- Import a bird with JPG and one MP3.
- Import another bird with PNG and two MP3 files.
- Confirm imported images render in grid/list/detail views.
- Confirm all imported calls play after restarting the app.
- Move or delete the original source files and confirm playback still works.
- Delete an imported bird, cancel at the warning once, then confirm deletion.
- Restart and confirm the deleted bird and copied media stay removed.
- Try canceling each picker and confirm the form remains usable.
- Try a provider-backed document such as Google Drive if available.

## Responsive and failure checks

- Test portrait and landscape orientations.
- Test with large system font size.
- Verify dialogs scroll on a small display.
- Interrupt an import or select an unreadable document and confirm an error is
  shown without crashing.

Build artifact:

```text
app/build/outputs/apk/debug/app-debug.apk
```
