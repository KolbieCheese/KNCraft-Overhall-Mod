# Recipe guide and thermal food validation (1.0.6)

The reported profile had successfully migrated: `use_resource_pack=true`, mod 1.0.5,
and nine managed thermal foods active. Its JAR contained the old limited Recipes
catalog. Thermal values were on separate custom reference pages; the recipe faces
themselves contained navigation links only. Version 1.0.6 puts a short, explicitly
labeled default below each affected native recipe diagram, independent of the live
reference component. Live server values remain on a separate page.

The isolated Forge 47.4.0 fixture includes all installed recipe-bearing mods. Export:
4,630 registered items and 4,292 resolved recipes. The guide covers all 3,865 resolved
standard crafting, furnace, smoking, blasting, campfire, stonecutting and smithing
transform recipes. It contains 1,963 non-food entries grouped by mod and 1,202 Cook
Book entries. Existing entry IDs remain stable even when a food changes category.
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

Validation completed:

- Java 17 build, reobfuscation, release ZIP and 17 JUnit tests (zero failures/errors).
- 3,554 JSON resources, all guide links, category assignment, recipe coverage,
  thermal summaries and defaults, 295 reference keys, original artwork and photo provenance.
- Isolated runtime: all 278 food definitions, newest-meal replacement across hot/cold
  foods, reloads, master/expanded switches, exclusions, explicit rows and native overrides.
- Guide recovery, full inventory handling, per-page reference allowlisting, native
  server overrides, packet data, FTB guide routes, original cohesion and expansion tests.

An initial new test could not read a resource across Forge's mod classloader boundary;
it was corrected to read via the production mod's class. The runtime pass below is
from the corrected harness. No play-world files or installed client JARs were changed.
Rendered client page layout still needs an in-game check; server tests cannot establish
visual correctness or client frame-time performance.

Runtime success markers are retained in `Guide-Recipe-Validation-Evidence.txt`.
