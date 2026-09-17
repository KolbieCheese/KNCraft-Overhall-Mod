# KNCraft Compatibility 1.0.1 implementation and validation

Implemented on 17 September 2026 against the exact installed KNCraft versions.
All 102 installed top-level JAR hashes still match the second audit. Testing used
disposable, loopback-only Forge 47.4.0 servers with Java 17 and previously authorized
EULA acceptance. The installed profile, live server and play worlds were not changed.

## Implemented audit proposals

| Proposal | Delivered behavior | Verification |
|---|---|---|
| P1 Controls | Optional 14-binding client preset, original/applied backup, conditional restore, conflict report; live key references in all 11 original guide chapters | Compilation and static validation of 42 distinct installed binding IDs; physical input/preset exercise remains open |
| P2 Attributes | Four native enchantment formulas contribute through stable transient modifiers; preserve unrelated bases/modifiers; fingerprint-based legacy migration with backup | Actual upstream procedures, repeated updates, unequip, matching legacy values, custom base plus external modifier, saved-player NBT reload and backup restore |
| P3 Regional climate | Twenty selected BOP/Terralith defaults, explicit Cold Sweat definitions take precedence, existing climate pipeline retained | Native biome ranges with module disabled/enabled; administrator TOML override and registry reload; biome/shade/elevation/cave samples at two heights and two times |
| P4 Wildlife offerings | Nearby animals receive a bounded opportunity to collect accepted player-thrown items before backpack magnets collect them | Native magnet path reproduced collection with feature disabled; crow/raccoon protection, carried and placed magnet calls, expiry and ordinary loot |
| P5 Altar repairs | Nine expedition-equipment repairs using normal fuel and 700/1000 native ticks; complete stack data survives | All nine actual furnace operations preserve name, enchantment, arbitrary NBT and sewn Cold Sweat cotton capability, repair damage and consume exactly one input |
| P6 Journal | Five BOP/RopeBridge native achievements added; 344 independent records across 17 chapters | FTB resolves all advancement references, icons and IDs; original 339 IDs retained; repeat installation and administrator edits preserved |
| P7 Campsite status | Source, live/saved temperature, enclosure and nearby appliances' native state; loaded chunks only | Status agrees with working hearth paths in all five tent styles and does not increase loaded chunk count |

The Aether repair test found that its ordinary repair output loses serialized item
capabilities. A narrowly scoped output-copy fix applies only to the nine KNCraft
repair recipes and retains sewn insulation. Native fuel consumption, timing and
input/output handling still run.

The existing native backpack interaction also passed: a survival player sneaks and
uses an empty waterskin on a placed tank backpack; exactly 250 mB is drained and its
custom name survives. With only 150 mB available, neither water nor the empty skin
is consumed. This needed guide documentation, not another fluid-transfer adapter.

## Climate sampling scope

The sampler uses a controlled seven-by-seven loaded chunk biome fixture. Each of
auroral garden, cold desert, tropics, emerald peaks, ice caves and thermal caves is
sampled at Y=-32 and Y=300, at times 6000 and 18000. It runs Cold Sweat's native
biome, shade, elevation and cave modifiers. It deliberately does not claim to model
every weather, equipment, nearby-block or mixed-biome situation.

The four surface biomes preserve their expected native day/night contributions and
day/night variation above ground. Deep underground, Cold Sweat's existing elevation
rules can moderate surface-biome temperatures. Actual ice caves sample 32 F and
thermal caves 86 F in the underground fixture; cave rules do not apply to open sky.
An initial Y=128 fixture was still underground in the generated terrain, so its
uniform readings were traced to the native elevation modifier before moving the
above-ground probe to Y=300. No production rule was weakened to satisfy the fixture.

Values in `kncraft-polish.toml` are biome contributions, not promised thermometer
readings. Guide pages make that distinction. The defaults are tunable preparation
choices; a real expedition remains the appropriate way to assess difficulty.

## Build and regression checks

- Java 17 compilation, Forge reobfuscation and 10 JUnit tests passed.
- Three release-version allocation tests passed.
- Static validation passed for 1,477 JSON resources, 344 ungated journal records,
  nine conditional repair recipes, all chapter control references, thermal-food
  cookbook values, original guide artwork/pages and release payload isolation.
- The new polish harness and existing expansion harness both passed before and
  after `/reload` in the 53-upstream-JAR gameplay fixture.
- All 30 tent shape/size combinations passed climate/enclosure checks, including
  expanded floors, changing source dimension and unloaded-source fallback.
- All five tent styles warmed a player through the native powered hearth. Each
  fixture used 500 native ticks, reduced fuel from 500 to 488, applied warmth and
  kept its spread within the tent bounds. Inspection reported that conditioning
  reached the player without loading more chunks.
- Cohesion, original performance, tent acknowledgement and four Depth-height
  regression checks passed again. Native portal comparisons, weather force rules,
  wildlife selection and earlier recipe/equipment integrations remain covered.
- Startup, status, reload and clean stop passed with every optional mod absent and
  with just Nomadic Tents plus Cold Sweat and their required dependencies. This
  checks classloading isolation, not every possible optional-mod combination.
- The smoke runner now requires requested success markers as well as a clean exit;
  a command failure followed by a normal server stop cannot pass silently.

Selected runtime output is retained in
[Polish-Validation-Evidence.txt](Polish-Validation-Evidence.txt). The fixture inputs
remain recorded in [Testing-Mods.json](Testing-Mods.json); ignored logs and worlds
are not shipped. Earlier results and upstream warnings are in
[Validation.md](Validation.md).

## Remaining physical-client and play acceptance

The client preset/restore and actual simultaneous train, backpack, Curios and voice
input have not been exercised in a physical client. Guide text layout, live key
rendering, links and JEI presentation likewise need a visual pass with the full pack.
Rebinding is opt-in and stores a backup; installation does not change controls.

The wildlife fixture covers crows and raccoons, not every animal, full-backpack
combination or concurrent multiplayer interaction. Migration was tested using saved
player NBT, not a rehearsal on a copy of the real server world. The four-value legacy
fingerprint has an explicit opt-out because coincidentally identical administrator
values cannot be distinguished from old enchantment writes; see
[Migration.md](Migration.md).

A representative multiplayer expedition, boss playthroughs, graphics/shaders,
distant terrain, culling, first-person animations, portal rendering, voice and
cross-dimension sound remain player acceptance work. Passing dedicated-server
fixtures does not certify those client paths.
