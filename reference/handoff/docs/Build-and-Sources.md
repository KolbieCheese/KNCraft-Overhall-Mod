# Build inputs and source coverage

## Included projects

| Snapshot | Current artifact | Included |
|---|---|---|
| legacy-sources/native-portals | KNCraftNativePortals 1.1.0 | Main source/resources, bounded Depth form, available isolated harness, build configuration. |
| legacy-sources/tent-portals | KNCraftTentPortals 1.0.1 | Main source/resources, portal/door/pong mixins, regression harness, build configuration. |
| legacy-sources/performance | KNCraftPerformance 1.0.0 | Three optimization implementations/mixins, deterministic helpers, regression harness, build configuration. |
| legacy-sources/portal-datapacks | Aether 1.1.0 / Depth 1.2.0 | Extracted current portal-generation data. Original ZIPs are also in live-reference. |
| live-reference/world/datapacks/kncraft-rules | Current gameplay rules | Recipes, block loot/tag protection and encounter functions. |

`SOURCE-PROVENANCE.json` hashes the copied source snapshots. The scoped live mod inventory hashes installed top-level JARs. The three custom-mod metadata files were extracted from local release copies whose hashes matched the live installed files during capture. This establishes artifact identity, not a fresh source rebuild or reproducibility guarantee.

## Toolchain and missing inputs

- Java 17; official Minecraft 1.20.1 mappings; Forge 47.4.0; ForgeGradle 6; historical Gradle wrapper version 8.8.
- Original build files and wrapper scripts/properties are included. The standard Gradle wrapper JAR is not included; generate a standard wrapper using a trusted Gradle 8.8 installation and retain its normal licensing. Do not expect `./gradlew` to work until this is done.
- Native portals uses a local deobfuscated dependency named `local:immersiveportals:3.0.7`.
- Tent portals uses local `nomadictents:20.1.1`, `immersiveportals:3.0.7` and `infiniverse:1.0.0.5` inputs.
- Performance uses local `weather2:2.8.3`; its runtime metadata pins the internal version `1.20.1-2.8.3`. Reflection/mixin targets may still require the other runtime mods even when the compile file does not list all of them.
- Upstream dependency JARs, mappings caches, Gradle caches, compiled artifacts and decompiled upstream source are deliberately omitted. Resolve dependencies through official repositories/releases where possible, pin the exact versions and verify their hashes against the inventory. If a dependency must be installed locally, document its source and filename instead of embedding a developer's absolute path.
- The original project mixin/refmap/reobfuscation settings must be inspected before merging. New source layout and public mod ID can change generated metadata even if Java compiles.

## Client distribution

The three old helper builds were used on a dedicated server without adding those helpers to multiplayer clients. Their upstream content mods still belong in the normal client modpack. That does not prove an integrated single-player environment works or that every proposed feature remains server-only. Guide assets, custom registries and any future networking need an explicit distribution decision and matching tests.

## Configuration reference

Included upstream TOML/JSON files are captured gameplay settings. They are not templates to overwrite indiscriminately. Version-specific config schemas may normalize or move values. In particular, world/serverconfig values belong to that world; config values are generally global. Document changes and import only relevant keys.

The Cold Sweat/Pam/Alex's/Aether IDs in the proposals were checked against installed configuration or same-version local JAR resources. Their proposed interactions have not been play-tested or implemented. Cold Sweat 2.4.3 supports insulation ingredients, worn insulation, insulating Curios, consumable temperature effects and Icebox fuels in its current configuration.

## Source boundaries and attribution

The three included helper metadata declarations identify their license as MIT. Preserve original notices and verify the desired repository license before publishing. Upstream Minecraft and mod implementations are dependencies, not newly owned KNCraft source. A consolidated mod should use compatible APIs or carefully scoped patches and retain attribution.

No custom map, website, Discord, rank or service source is part of this gameplay handoff. The full server is not reproduced by this archive. Gameplay development should not depend on credentials, another game server, production permission databases or browser assets.

## Suggested first build procedure

1. Inventory the three projects and compare code paths to the implemented-feature document.
2. Resolve the exact dependencies and make the separate snapshots build before changing behavior, where practical.
3. Scaffold the unified mod; migrate one module at a time; preserve required registry/data IDs and tested fallbacks.
4. Build a reobfuscated release JAR and separate test artifacts. Inspect their contents.
5. Exercise an isolated full gameplay pack, then real client traversal. Record limitations honestly.
6. Consolidate datapack/config installation with explicit ownership and precedence; ship a replacement manifest.
