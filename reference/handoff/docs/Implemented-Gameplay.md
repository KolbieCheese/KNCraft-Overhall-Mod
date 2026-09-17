# Implemented gameplay baseline

These features are deployed. This document describes the starting point, not additional proposed work. Source snapshots and live-reference files accompany the descriptions. No source changes or gameplay deployments were made while preparing this handoff.

## Native dimensional portals

**Artifacts:** KNCraftNativePortals 1.1.0; Aether immersive datapack 1.1.0; Depth immersive datapack 1.2.0.

Native Aether water-bucket lighting and the native Depth igniter create their normal portals, then the integration converts successful lighting into paired Immersive Portals. Stick activation remains a datapack fallback for existing opaque portals. Preserve frame-size rules and normal item consumption. The JSON portal-generation forms and Java adapter are coupled; copy both when consolidating.

Call From The Depth 1.22.1 declares a 256-block dimension but its generated terrain occupies Y=0..127 with bedrock at the floor and roof. Generic portal placement could previously pick empty space above that roof. The custom `kncraft:bounded_depth` form confines the full destination frame, floor and arrival room to **Y=5..122 inclusive**. Arrival space extends two blocks to either side of the portal plane. It prefers open space, can excavate recognized natural terrain, and refuses fluids, bedrock, block entities and occupied/unsupported spaces. It excludes out-of-bounds existing destination frames and rejects unsafe Depth source frames. Failure restores the native source portal. Reverse Overworld placement retains upstream behavior; Aether placement is unchanged.

Existing generated portal entities were not migrated. A broken pair may need deliberate breaking/rebuilding; consolidation must not silently recreate or relocate player portals.

Historical isolated tests covered small and 20x6 interior frames, both axes, extreme mapped heights, untouched bedrock, native ignition and actual generated arrival rooms. Dedicated startup passed. Physical-client traversal was not established by those height tests.

## Nomadic Tents and Immersive Portals

**Artifact:** KNCraftTentPortals 1.0.1. **Core versions:** Nomadic Tents 20.1.1; Immersive Portals 3.0.7; Infiniverse 1.0.0.5 build dependency.

Nomadic Tents dynamically adds interior dimensions. Immersive Portals previously retained stale integer dimension mappings, causing missing-dimension failures during travel/login. The addon synchronizes that registry when a new level is loaded, sends the synchronization packet, and then waits for an existing vanilla ping/pong acknowledgement from connected players before publishing the dimension event and creating entrances. Sending the registry packet alone is insufficient because client processing is queued. Wrong/stale acknowledgements do not release the barrier; disconnected clients are removed from it. A connected client that never acknowledges can delay creation and needs a diagnostic, not an unsafe timeout bypass.

Completed tents near players receive paired entrances, checked once per second in already-loaded nearby chunks. Right-clicking a complete door also initializes a link. Tent identity, contents and native fallback return points remain under Nomadic Tents' control. Travel preserves ownership, completeness, nearby-monster and entity restrictions. Packing or damaging a tent invalidates its link; a token on both door block entities prevents old entrances from reconnecting to moved tents. Cross-portal interaction is disabled so the physical door remains available for packing.

Seamless travel does not impose Nomadic Tents' native door cooldown: an earlier implementation rejected quick return crossings and caused client/server position disagreement. Native door fallback remains available, with synchronization safeguards. `immersiveTentEntrances=false` disables immersive entrances but keeps the dimension synchronization repair active.

Preserve the `KNCraftTentLink` persistent compound, portal metadata/tags, link tokens and existing dimensions. Read all serialization code before renaming anything. The included test harness covers directions, ownership, packing, persistent contents, acknowledgement behavior and server-side round trips; client prediction still needs a real client validation pass for the merged release.

## Conservative performance integrations

**Artifact:** KNCraftPerformance 1.0.0. This changes search implementation while preserving gameplay.

| Module | Existing behavior to preserve |
|---|---|
| Wither Storm flee-goal portal search | Inspect loaded section palettes to skip sections without possible Nether portal blocks. Preserve upstream coordinate truncation, inclusive bounds and X/Y/Z order. Fall back on missing chunks, unusual coordinates/build heights, nonportal requests, off-thread calls, debug worlds and insufficient pruning. No stale persistent block cache. |
| Alex's Mobs dropped-item targeting | Use a linear stable minimum instead of sorting the whole candidate list when only the first item is used. Preserve ties, eligibility, range, activation scheduling, callback and navigation. |
| Weather2 tornado query | Query living entities for normal rotation and item entities for pet storms, matching the original loop's applicable types. Preserve original distance, shelter, grab rules and physics. |

Independent config keys: `portalSearch`, `itemSelection`, `tornadoQuery`. Exact audited mod versions: Wither Storm 4.2.1, Alex's Mobs 1.22.9, Weather2 **1.20.1-2.8.3**. The internal Weather2 version is not simply `2.8.3`. Legacy dependencies intentionally reject unaudited upgrades.

Historical harness results: 10,000 stable-selection equivalence cases; 135 actual portal-search equivalence calls; real dropped-item targeting; 64 tornado-target equivalence calls. Local operation timings are not proof of overall server TPS improvement. Do not add AI throttling, reduced mob counts or storm nerfs under the label of optimization.

## Gameplay rules and balancing

`live-reference/world/datapacks/kncraft-rules` is the exact currently deployed rule set:

- Natural Waystone network: recipe overrides disable crafting of network components, selected block loot tables suppress drops, and the live Waystones config restricts placement/editing/breaking to creative behavior while protecting generated stones. Preserve the precise combined config/data behavior, not only one recipe.
- Carry On's block blacklist includes Waystones. Wither Storm block/cluster/cave-in blacklists and vanilla dragon/wither immunity tags also protect the network. These are gameplay protections, not permission-system features.
- A scheduled `kncraft:encounter` function checks selected dimensions once per second. Unmarked Wither Storms receive +50% base max-health, +50% base attack damage and +4 armor; the command-block encounter receives +100% base max-health. The functions initialize health once and add persistent tags. Vanilla caps and upstream phase behavior still apply.
- Preserve tags `kncraft_storm_balanced`, `kncraft_balanced` and the modifier UUIDs in `scale_storm.mcfunction` / `scale_core.mcfunction`. Applying equivalent Java logic without recognizing those tags can double-buff or repeatedly heal an existing encounter.
- Existing Weather2 settings reduce severe-weather frequency and use block-grab policy intended to protect stone infrastructure. Do not silently reset weather frequency, storm path targeting or destruction policy during consolidation.
- Existing Wither Storm server settings tune pursuit, healing, group scaling, phase-five bomb progression and chunk-loading behavior. Use the live TOML as the value reference; avoid copying older baseline defaults.

Existing Cold Sweat configuration has custom biome/dimension entries and thermal-source settings. In particular, the Aether has a +0.7 dimension offset. Review actual resulting temperatures before changing that design. Cold Sweat's disabled-mod list contains no active exclusions in this snapshot. Weather2 integration already exists upstream.

ModernFix/Distant Horizons and other performance settings are included only as compatibility context. They are not a request to absorb those mods or reimplement their configuration systems.
