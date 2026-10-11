# Modpack update compatibility - 2026-10-11

KNCraft **1.0.11** adds approval for the installed **Sophisticated Core
1.5.6.2383**. KNCraft 1.0.10 rejects that version at Forge dependency validation
and startup. No production gameplay-code or recipe changes were indicated by
the artifact/API review and completed runtime checks.

Both previously audited Core versions, 1.5.1.2335 and 1.5.2.2346, remain
supported. Cold Sweat remains on the already supported 2.4.3.2. Startup,
mixin selection, integration registration and status reporting share the
updated allowlist. Forge declares the same three Core versions as disjoint
singletons; intermediate and future releases remain unapproved. The pinned
build inputs retain the original API baseline and verified hashes.

## Installed pack comparison

Read the installed CurseForge **KNCraft** profile and each actual JAR's metadata
and SHA-256. Minecraft remains 1.20.1 and Forge remains 47.4.0. Compare against
the September 17 inventory with the [September 27 update](Modpack-Update-Audit.json)
applied. The profile contains 105 top-level JARs: 92 unchanged upstream artifacts,
12 upstream replacements and the installed KNCraft 1.0.10 JAR. No upstream
top-level mod IDs were added or removed. The review also inventoried 220
embedded JAR files, including Nomadic Tents' Infiniverse 1.0.0.5.

The [current inventory](Modpack-Update-Audit-2026-10-11.json) records every
top-level artifact, internal versions, hashes, changed dependencies and
the installed versions accepted by all 17 source dependency declarations.
All mandatory declared dependency versions are satisfied in the static
inventory. The installed KNCraft 1.0.10 Core pin is the sole present optional
dependency mismatch; the new source declaration resolves it.

| Updated mod | September 27 audit | Installed October 11 |
| --- | --- | --- |
| Collective | 8.40 | 8.45 |
| Distant Horizons | 3.3.2 | 3.3.3 |
| Entity Culling | 1.11.2 | 1.11.3 |
| Inventory Totem | 3.4 | 3.5 |
| JEI | 15.62.0.216 | 15.62.0.219 |
| Ksyxis | 1.4.5 | 1.4.6 |
| Mezz Config | 0.6.5 | 0.6.8 |
| ModernFix | 5.27.83+mc1.20.1 | 5.27.88+mc1.20.1 |
| Puzzles Lib | 8.1.33 | 8.1.36 |
| Sophisticated Backpacks | 3.26.4.2172 | 3.26.9.2194 |
| Sophisticated Core | 1.5.2.2346 | 1.5.6.2383 |
| Villager Names | 8.5 | 8.7 |

The official [Core release listing](https://www.curseforge.com/minecraft/mc-mods/sophisticated-core/files/all?gameVersionTypeId=1&page=1)
and [Backpacks file page](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks/files/9083935)
corroborate the installed artifacts. Compatibility approval uses the local
JAR metadata, bytecode and tests.

## Integration and data review

- Core's `FeedingUpgradeWrapper` and `MagnetUpgradeWrapper` are byte-for-byte
  identical to the original audited 1.5.1.2335 artifacts. The two `isEdible`
  mixins and native magnet-prevention callback retain their exact signatures.
  Storage-wrapper, upgrade-handler/type/base, inventory-handler, magnet-checker,
  magnet/feeding-item and tank-wrapper classes used by the existing tests also
  retain their exact bytes.
- Backpacks 3.26.9.2194 requires Core `[1.5.5.+,)`, satisfied by 1.5.6.2383.
  Its `BackpackWrapper` retains public signatures; normalized disassembly
  adds resetting `settingsHandler` in `onContentsNbtUpdated()`. Other changed
  linked-storage, UI and network surfaces are outside KNCraft's direct hooks.
  Backpacks has no separate KNCraft version pin; the feeding/magnet hooks use Core.
- All 6 Core and 240 Backpacks data JSON resources are byte-for-byte unchanged
  from the original gameplay fixture. Recipe implementation and item-registration
  classes are unchanged. All 77 Core/Backpacks guide recipe references still
  resolve to upstream recipe JSON; no guide catalog regeneration is indicated.
- Shared Backpacks Carry On blacklist and Forge loot-modifier lists retain
  additive `replace=false` definitions alongside KNCraft's bundled packs.
  No replacement tag, recipe or loot patch is indicated.
- Cold Sweat 2.4.3.2 matches the September 27 audited hash. The remaining
  explicitly pinned direct integrations retain their previously audited hashes,
  including Better Combat, Patchouli, Aether, tents, weather and Elytra Slot.
- Inventory Totem 3.5 renamed the callback package from `com.natamus` to
  `com.serilum`. KNCraft production code has no reference to this callback.
  The isolated camping harness now resolves the current class with a narrow
  class-not-found fallback to the legacy class, preserving every consumption
  assertion and 3.4 validation.

## Validation and boundaries

- Java 17 / Forge 47.4.0: `gradlew build expansionHarnessJar` passed all
  **22 Java unit tests**, four recipe-tier tests and the resource/guide audit
  (3,562 JSON resources, 344 journal records and 3,865 covered native recipes).
- Compatibility tests require an exact closed-singleton match between every
  runtime allowlist and Forge dependency range, including the new Core version,
  and reject neighboring, intermediate, future, absent and unknown versions.
- The reobfuscated KNCraft 1.0.11 JAR passed the original dedicated-server
  gameplay fixture's thermal, reference, polish, expansion and climate checks.
- The updated fixture loaded the installed Core/Backpacks/Cold Sweat versions
  and current server-side counterparts, including Collective, Inventory Totem,
  Ksyxis, ModernFix, Puzzles Lib, Balm and Noisiumed. Thermal, reference,
  polish and climate checks passed, including real backpack tank/waterskin
  use, placed/carried magnet offering protection and expiry, thermal feeding
  and reserved-food persistence. Thermal and reference checks passed again
  after `reload` in both fixtures.
- The initial updated expansion check failed solely on the test harness's
  renamed Inventory Totem class. Its original failed log is retained. After
  the harness correction, the complete expansion check passed in both fixtures,
  including empty-inventory rejection and exactly-once native totem consumption.
  Both follow-up servers reached ready state and exited cleanly. The production
  JAR retained SHA-256 `c8cc6c3a5711c02fab245fd0cb0f516f87fbbd77d2a2c5de9348be024e69cc85`
  across the full checks and both follow-up runs.

Evidence remains in `.cache/compat-update-2026-10-11/`: full inventory,
integration class/disassembly report, build log, fixture manifests, runtime
logs and status reports. Test servers are separate copies of the existing
marked disposable fixture; the original test fixture, installed profile,
live server and play worlds were not modified.

This covers artifact/metadata review of the full pack and dedicated-server
tests of its gameplay subset. It does not establish full-pack client rendering,
shader/animation performance, new linked-storage UI behavior or physical-client
multiplayer traversal. Existing upstream warnings/errors are retained in the
logs and are not counted as KNCraft test passes.

Install the same 1.0.11 release on the server and all clients together when
deploying this update, replacing the installed KNCraft 1.0.10 JAR. Follow
[Migration.md](Migration.md); do not install isolated-test harness JARs in a play world.
