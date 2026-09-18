# Guide layout validation: 1.0.9

Verified on 2026-09-18 in the installed KNCraft client, Forge 47.4.0, Minecraft 1.20.1,
Patchouli 85, with the existing resource packs and shaders. Startup confirmed KNCraft
1.0.9. Testing used the local creative `KNCraft 1.0.6 Guide Verification` world.

Patchouli's original entry buttons draw one line and paginate by a fixed entry count.
The KNCraft book now wraps full titles and allocates rows by their measured height.
The implementation applies to its index, categories, search and history; other books
continue using native Patchouli layout. Rules retain their complete names.

## Actual client audit

The separate temporary client harness reported:

> GUIDE CLIENT LAYOUT PASSED: 3270 entries, 276 wrapped titles, 149 index spreads,
> 34 categories / 180 category spreads; full names, normal font size, width/height
> bounds, no overlapping rows, exact ordered coverage, search/clamping/empty results,
> resize and 1st Edition.

This checks the transformed GUI and real Minecraft font, not an approximate character
count. It walks every spread, then repeats index coverage after searches and resizing.
It checks the shipped English book and installed font; future resource-pack changes
should rerun this audit.

## Visual checks

- All four Rules titles occupy their own wrapped, fully clickable rows. Clicking the
  second line of rule 2 opened the correct rule.
- Red Nether Brick and Red Sandstone names fit on both Entry Index pages, including
  Skyline Stairs, Terrace Stairs, Bridge Support and Compact Stairs.
- All five new campsite photographs render with complete captions. Both enlarged HUD
  views retain their body/environment readings and fit within the page.
- The home page displays KNCraft Guide Book, 1st Edition.
- The Symbiont weak-point and Bowels entrance directions render as complete text without
  photo placeholders.

Minecraft F2 evidence remains in the profile's screenshots directory and the ignored
local `.cache/client-verification/` directory:

| Screenshot | Evidence |
| --- | --- |
| `2026-09-18_02.09.17.png` | Four full rule titles |
| `2026-09-18_02.11.37.png` | Long entries on both index pages |
| `2026-09-18_02.12.40.png` | First campsite exterior/interior |
| `2026-09-18_02.13.15.png` | Hearth-on photograph and hearth-off HUD detail |

## Build and server checks

- Twenty Java tests passed, including page-boundary and 5,000-entry pagination checks.
- Four thermal recipe tests and the complete resource/guide audit passed.
- The isolated dedicated server started, completed `kncraftreferencetest` (reference and
  guide-edition assertions), and stopped cleanly. No live server was used.
- Production artifact validation excludes every test harness, including the new client
  layout audit. The temporary client harness is removed from the installed profile after testing.

The book remains on 1st Edition. Its internal content revision is retained separately.
