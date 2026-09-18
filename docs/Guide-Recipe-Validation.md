# Recipe guide and thermal food validation (1.0.7)

The reported profile had `use_resource_pack=true`, mod 1.0.5, and nine managed thermal
foods active. Client testing exposed a loading bug that the earlier file/server checks
missed: Patchouli 85 always selected its external loader for declarations discovered
in `patchouli_books/`, irrespective of this flag. The old loose pages were still shown.
An updated title/version and the Rules category therefore did not prove bundled content
was loaded. Version 1.0.7 changes that loader selection only for the migrated KNCraft
book. It retains the old files and ID and leaves other external books and legacy opt-outs
unchanged. The new runtime regression asserts the actual Patchouli Book loading flag.

The expanded catalog also puts a short, explicitly labeled default below each affected
native recipe diagram. Live server values remain on a separate page. Both render in
the real installed client after the loading correction; no change to the custom
temperature renderer was necessary.

The isolated Forge 47.4.0 fixture includes all installed recipe-bearing mods. Export:
4,630 registered items and 4,292 resolved recipes. The guide covers all 3,865 resolved
standard crafting, furnace, smoking, blasting, campfire, stonecutting and smithing
transform recipes. It contains 1,963 non-food entries grouped by mod and 1,202 Cook
Book entries: 90 warm, 188 cold and 924 neutral. Existing entry IDs remain stable even when a food changes category.
Special dynamic upgrades receive JEI instructions, not an invented crafting grid.
Aether machine pages show example ingredients and point to JEI for current alternatives,
quantities and times. Environmental conversions, placement restrictions and dynamic
recipes without a fixed result are not represented as ordinary crafting diagrams.

The explicit thermal catalog contains 278 foods: 50 soups/stews and other hot meals,
9 hot drinks, 31 gently warming cooked meals, 65 smoothies/frozen desserts,
109 chilled drinks/yogurts and 14 salads. These are fixed serving-temperature bonuses,
not a simulation of food cooling in storage. All installed smoothies and soups are
covered. Raw peppers, coffee beans, tea leaves and curry powder are not treated as hot
prepared food solely because of their names. Native Cold Sweat definitions take priority.

Recipe tiers keep 213 thermal foods at their simple baseline, add modest bonuses to
42 prepared foods and 23 elaborate foods, and leave neutral foods unchanged. Tiers
are materialized in the catalog with the chosen recipe/score; gameplay performs no
recipe graph calculations. The isolated consumption checks cover tier durations and
neutral-food behavior as well as all catalog magnitudes. See `Thermal-Food-Tiers.md`.

Validation completed:

- Java 17 build, reobfuscation and 17 JUnit tests (zero failures/errors), plus four
  recipe-tier policy tests.
- 3,560 JSON resources, all guide links, category assignment, recipe coverage,
  thermal summaries and defaults, 1,219 reference keys, original artwork and photo provenance.
- Isolated runtime: all 278 food definitions, newest-meal replacement across hot/cold
  foods, reloads, master/expanded switches, exclusions, explicit rows and native overrides.
- Guide recovery, full inventory handling, per-page reference allowlisting, native
  server overrides, packet data, FTB guide routes, original cohesion and expansion tests.

Client checks used a newly created local world, `KNCraft 1.0.6 Guide Verification`,
in the installed KNCraft profile. No existing play worlds or live multiplayer server
were opened. The original client 1.0.5 JAR was backed up outside the mods folder.
Melon Smoothie renders -0.2/60s on its recipe face and -0.200/60.0s on its live server
page. Carrot Soup renders +0.2/90s and +0.200/90.0s. The Aether category, Altar diagram,
Skyroot Stick diagram, type-to-search and Enter-to-open were checked in the client.
Screenshots were captured with Minecraft F2 (2026-09-17 at 22:01:26, 22:02:38 and
22:04:22 local time) and retained locally under `.cache/client-verification/`.

The final client pass also verified the three Cook Book child categories, warm/cold
search, Deluxe Chicken Curry (+0.30/120s), Caramel Ice Cream (-0.25/75s) and
Apple Jelly (no matching server effect). Defaults and authoritative values agree.
F2 captures from 22:24 through 22:27 are retained with the earlier evidence.

The first expanded category grid overflowed the right page. Related building,
storage/transport and guide mods are now grouped, with a validation limit of sixteen
children per category. The final fourteen-icon Recipes grid and nested Building &
Materials category were checked in-game. Recipe IDs and existing links stay unchanged.

These are representative UI checks plus exhaustive recipe-reference and thermal-catalog
checks, not a claim that every recipe was manually crafted or every page visually
inspected. This pass is not a client frame-time benchmark. The installed profile also
reported an existing Euphoria/Complementary shader version mismatch; graphics settings
were left unchanged during guide verification.

Runtime success markers are retained in `Guide-Recipe-Validation-Evidence.txt`.

## Edition-label follow-up (1.0.8)

The installed client displays **1st Edition** on the title page. Patchouli keeps
content revision 16 internally. The isolated reference check verifies the public
label, unchanged content revision and untouched unrelated-book subtitles. Build,
unit/resource checks and the reference runtime harness pass. The existing local
verification world was used again; no live server or existing play world was opened.
