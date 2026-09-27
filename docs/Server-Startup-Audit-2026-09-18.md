# Server startup audit — 2026-09-18

Reviewed the supplied startup log (17:47:22–17:47:45), the current KNCraft
Compatibility 1.0.9 sources and bundled packs, and upstream JARs staged in
`run-isolated/mods`. No logged failure was traced to this repository's mod.
The server reports `Done (3.584s)!` at 17:47:40; BlueMap finishes loading at
17:47:45. Successful startup does not establish that every gameplay system works.

## Confirmed upstream data problems

- **97 Pam's crop loot-table warnings:** Every crop table named in the log was
  checked against `pamhc2crops-1.20-1.0.3.jar`. Each contains an unconditional
  child before the last child of a `minecraft:alternatives` entry. That earlier
  fallback prevents the later child from being reached. For example,
  `data/pamhc2crops/loot_tables/blocks/pamlettucecrop.json` repeats the lettuce
  fallback. KNCraft does not override these block loot tables. Its three Pam's
  seed-drop **loot modifier** repairs are separate resources. A future upstream
  repair can remove unreachable alternatives while preserving the existing
  maturity, Fortune and explosion behavior; changing crop yields is unnecessary.
- **Biome feature decode error:**
  `callfromthedepth_-1.22.1-forge-1.20.1.jar` contains a trailing comma after
  `minecraft:ore_copper` in
  `data/callfromthedepth_/worldgen/biome/strongsculkswamp.json` (line 62), followed
  by the closing array bracket. Its ore sequence matches the logged list ending
  in `null`. Strict JSON parsing rejects this upstream file at line 64. KNCraft
  supplies no biome-definition override. The targeted upstream correction is
  to remove the trailing comma; do not invent an additional ore feature.

## Other warnings and errors

| Log item | Attribution / next action |
| --- | --- |
| `kncraft (version 1.0.8 -> 1.0.9)` | Forge notices the saved world's previous mod version. This is upgrade bookkeeping, not a KNCraft exception or evidence of a client/server mismatch. |
| Missing metadata/data packs for `kncraftdiscordlink` and `witherstormbluemap` | Separate helper mods, not this repository's `kncraft` mod. Inspect their JAR packaging and add valid root `pack.mcmeta` if missing. Their JARs were not inspected in this audit. |
| Missing `noisium` mod/data pack | The saved world references `noisium`, while this startup loads `noisiumed`. Reconcile the installed replacement and saved pack selection; the log alone cannot establish migration compatibility. |
| `MouseHandler` and `Screen` loaded on a dedicated server | The adjacent messages identify `fabric-screen-api-v1.mixins.json`. Inspect the installed Fabric compatibility/API components' server-side mixin selection. No KNCraft mixin is named. |
| Discord `Cannot build an empty message` | Discord Integration 3.0.7.1 attempts to build an empty outgoing message after startup. Inspect its effective startup-message configuration and any helper that changes it. The stack trace alone does not identify which configuration or helper produced the empty message. This repository contains no Discord Integration code. |
| ModernFix/Radium/ServerCore overwrite or redirect conflicts | Overlapping upstream optimizations; the loader reports which competing implementation it skips. No KNCraft mixin is named. |
| Distant Horizons unknown chunk generators | Warnings name Nomadic Tents' `EmptyChunkGenerator` and Immersive Portals' `ErrorTerrainGenerator`. These are compatibility warnings, not demonstrated crashes. If generation fails, the log recommends disabling distant generation or using `PRE_EXISTING_ONLY`. |
| Cold Sweat's absent Spoiled class; CristelLib refmap; upstream mixin metadata/shift warnings | Messages identify upstream mixins/resources. They did not prevent this startup. |
| Cold Sweat `required mods not met` INFO messages | Optional integrations being skipped, not load errors. |
| ModernFix startup-duration warnings | Timing reports, not exceptions. |

## Scope and verification

No gameplay code or data was changed because this audit found no demonstrated
KNCraft Compatibility defect to fix. The analysis inspected all WARN/ERROR and
exception lines, compared all 97 named crop tables with the upstream archive,
and checked Depth biome JSON directly. Existing local validation logs also contain
the identical biome error, although they are not a controlled run without KNCraft.

The live server, its configuration, and its installed JARs were not modified.
No new server run was performed. Any upstream repair should be verified against
the actual deployed JARs and then tested in an isolated server before rollout.
