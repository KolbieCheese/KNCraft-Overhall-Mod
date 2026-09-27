# Modpack update compatibility — 2026-09-27

KNCraft 1.0.10 needs compatibility declarations, not gameplay changes, for the
installed updates reviewed here. Both old and new audited versions remain supported.

| Integration | Previous version | Newly audited version |
| --- | --- | --- |
| Cold Sweat | 2.4.3 | 2.4.3.2 |
| Sophisticated Core | 1.5.1.2335 | 1.5.2.2346 |
| Sophisticated Backpacks | 3.26.3.2157 | 3.26.4.2172 |

Cold Sweat and Sophisticated Core have explicit allowlists shared by startup
validation, mixin selection, integration registration and status reporting.
Forge metadata accepts the same two versions using disjoint singleton ranges;
intermediate or future versions are not implicitly approved. Backpacks has no
KNCraft version pin: its feeding and magnet integration uses Sophisticated Core.
Patchouli's existing 85-only runtime check now also has a Forge declaration.
Build dependencies stay on the older versions so this JAR retains the older API baseline.

## Evidence

Compared the installed KNCraft profile (105 top-level JARs) with the September 17
[inventory](Installed-Mod-Audit.json). Unchanged filenames retain their recorded
SHA-256 hashes. The [update inventory](Modpack-Update-Audit.json) records the 17
new or replaced upstream artifacts, resolved internal versions, dependencies,
hashes and shared data paths. The eighteenth new filename is KNCraft 1.0.9 itself.

- Cold Sweat's `WorldHelper` (both tent-enclosure injection targets),
  `HearthBlockEntity`, temperature API/modifiers, event types, `ConfigSettings`,
  `ItemSettingsConfig` and food/insulator/fuel/biome codecs imported by KNCraft
  are byte-for-byte unchanged. `WorldSettingsConfig` has equivalent disassembly
  after constant-pool index normalization. `ModRegistries` retains its API;
  its executable differences are string conversion in exception messages.
  All upstream `data/**/*.json` values are unchanged after JSON parsing.
- Sophisticated Core's `FeedingUpgradeWrapper` and `MagnetUpgradeWrapper` are
  byte-for-byte unchanged. These supply the `isEdible` mixins and magnet
  prevention callback. Updated Backpacks retains its data resources unchanged;
  runtime checks also exercise the actual backpack wrapper and upgrade behavior.
- Shared Carry On tags and Forge loot-modifier lists use additive definitions.
  No new recipe, tag or loot override is needed.

Official release listings corroborate the target artifacts:
[Cold Sweat](https://www.curseforge.com/minecraft/mc-mods/cold-sweat/files/all?gameVersionTypeId=1&page=1)
and [Sophisticated Core](https://www.curseforge.com/minecraft/mc-mods/sophisticated-core/files/all?gameVersionTypeId=1&page=1&pageSize=20).
Compatibility decisions use the installed JARs and tests, not filenames alone.

## Other installed changes

Balm 7.3.44, Collective 8.40 and Ksyxis 1.4.5 were included in the updated
server fixture. Noisium 2.3.0 was replaced by Noisiumed 3.0.6, also tested there;
these have different mod IDs and KNCraft has no direct API integration with either.

Metadata and integration-surface review also covered Cupboard 4.2, Distant
Horizons 3.3.2, Entity Culling 1.11.2, First Person 2.7.3, Not Enough Animations
1.12.6, 3D Skin Layers 1.11.3, JEI 15.62.0.216, Mezz Config 0.6.5,
ImmediatelyFast 1.5.5+1.20.4 and the replacement of BorderlessWindow by Cubes
Without Borders 3.0.0+mc1.20. KNCraft has no direct mixin/API targets in these
mods. JEI's new required Mezz Config dependency is present and satisfies
`[0.6.3,1.0.0)`. ImmediatelyFast's metadata accepts Minecraft 1.20.1 despite
its filename suffix. These reviews do not establish client rendering compatibility.

## Validation

- Java 17 / Forge 47.4.0: `gradlew build expansionHarnessJar` passed,
  including all 22 unit tests and the resource/guide audit.
- New regression tests check every supported version against Forge's parsed
  dependency ranges and reject absent, unknown, intermediate and future versions.
- The same reobfuscated KNCraft 1.0.10 JAR passed two dedicated-server runs:
  the original gameplay fixture and the updated Cold Sweat/Core/Backpacks set
  plus Balm, Collective, Ksyxis and Noisiumed above.
- Both runs explicitly passed `kncraftthermaltest` (278 foods),
  `kncraftreferencetest`, `kncraftpolishtest`, `kncraftexpansiontest` and
  `kncraftclimatetest`. Thermal and reference checks passed again after `reload`.
  Both servers reached ready state and exited cleanly. Status reports showed
  the loaded old/new versions and both supported alternatives.
- Existing upstream Immersive Portals mixin metadata messages and the Depth
  biome JSON error remain. No KNCraft test failure occurred.

Local evidence is retained in `.cache/compat-update/`: `build.log`,
`old-runtime.log`, `updated-runtime.log`, both status reports, class disassemblies
and the fixture runner. The runner restored the original isolated-test JARs.
The installed client profile and live server were not modified. Client rendering,
multiplayer traversal and the entire 105-mod client were not exercised in this audit.
