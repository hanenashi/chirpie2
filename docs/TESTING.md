# Physical-device Test Checklist

Run this checklist on an Android phone before treating the current checkpoint
as release-ready.

## Install and migration

- Install the previous version, launch it, then install the new APK over it.
- Confirm all 51 bundled birds remain available after the Room 2 → 4 migration.
- Cold-start the app and verify the grid appears without a blank intermediate
  state or crash.

## Display and playback

- Switch between card grid and compact list; restart and confirm persistence.
- Open several bird details and play every available call.
- Confirm starting another call stops the previous one.
- Verify images fit correctly in grid, list, and detail views.

## Sorting and arrangement

- Exercise every name sort and confirm the expected language ordering.
- Reset to custom order.
- Long-press a card, drag it across rows, then cancel; confirm no order change.
- Repeat and save; restart the app and confirm the order persists.

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
