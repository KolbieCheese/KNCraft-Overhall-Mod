# Setup and performance audit — 17 September 2026

The compatibility mod passes the dedicated-server regression suite. The installed
CurseForge profile is **not yet the finished deployment**: it lacks the compatibility
JAR, still loads the legacy guide, and has unresolved client rendering diagnostics.
Full-pack visual acceptance and representative multiplayer profiling remain necessary.
This audit does not certify maximum FPS or a finished live-server rollout.

## Installation findings

| Priority | Verified finding | Required follow-through |
|---|---|---|
| High | All 102 installed top-level JARs still match the recorded SHA-256 baseline. No `KNCraftCompatibilityMod` JAR is present. | Install the same release on the server and every client together, following [Migration.md](Migration.md). The new integrations are not active in this local profile yet. |
| High | The external guide still has `use_resource_pack=false`, book version 8. | The migration preview passes against the current files. With Minecraft closed, apply the included guide migration after installing the matching mod. Existing book IDs and original files are retained. |
| High | Local Waystones config has `restrictToCreative=false` and `generatedWaystonesUnbreakable=false`. | Merge the four keys from `pack-overrides/waystones-policy.toml.fragment` into the existing restrictions table for the rollout. Verify the authoritative server config too; these local values do not establish the remote server's policy. |
| Medium | The latest client log reports shader uniforms missing, Immersive Portals clipping-uniform errors and Distant Horizons client-data update exceptions. | Test the exact graphics stack in a disposable profile, first with shaders off, then with shaders on and DH independently enabled. Verify both sides of tent/Aether/Depth portals. |

The dependency scan covers nested JAR metadata as well as top-level mods: 105 top-level
declared IDs and 160 distinct IDs including nested providers. There are no duplicate
top-level mod IDs or missing declared mandatory dependency IDs. This checks presence;
it is not a blanket certification of every declared version range or client combination.
The actual Forge gameplay fixture checks the pinned gameplay versions at startup.

No installed mods, play worlds, resource packs, client settings or live-server files were
changed during this audit. Coordinated deployment matters because the mod adds a
client/server network channel and expects matching installations.

## Client configuration and tuning

Observed hardware is an i7-14700K, RTX 4090 and 64 GiB RAM. The profile currently allocates
32,000 MB to Java. Vanilla render distance is 10, local simulation distance 15, and the
frame cap is 260. DH uses 14 workers at runtime ratio 1.0 with a 256-chunk LOD radius.
Immersive Portals permits five nested layers, 200 rendered portals and an indirect
loading radius of eight. These are significant workloads, even on this hardware.

Use a repeatable route through a base, a tent, a portal and fresh terrain. Record frame
times and server tick times before changing one setting at a time. Reasonable trial
settings are 12–16 GiB heap, 128-chunk DH radius with a lower CPU-load preset, and two
portal layers. These are starting points for measurement, not measured optimums or
automatically applied settings. A larger heap alone is not evidence of better performance.
Local simulation distance only controls a local/integrated world; a dedicated server
has its own setting. Preserve gameplay reach and required chunk loading while tuning.

For clipping artifacts with shaders, test Immersive Portals' compatibility renderer.
Its documented tradeoff is losing nested portal rendering. Portal rendering also loads
and draws additional world views, so layer and indirect-loading limits affect real work.
See the [official configuration documentation](https://qouteall.fun/immptl/wiki/Config-Options).
The Forge project's [shader interaction report](https://github.com/iPortalTeam/ImmersivePortalsModForForge/issues/183)
supports treating this as a graphics compatibility test, not assuming a gameplay patch
will repair the renderer.

Keep the existing performance stack while establishing that baseline. No additional
optimization mods or replacement rendering libraries were added during this audit.
ModernFix is already mitigating the logged Sodium Dynamic Lights reload-listener
threading warning. Removing it would remove that mitigation.

Other client log items need targeted follow-up: Cold Sweat armor IDs were rejected by
a config reader during JEI ingredient registration, and the client received an unknown
`ftbessentials:update_tab_name` message. The server fixtures successfully use the
Cold Sweat equipment, so those messages alone do not justify deleting the armor or its
recipes. Recheck client/server mod and config alignment during the deployment rehearsal.

## Runtime, performance and release checks

The 53-upstream-JAR dedicated-server fixture exercises the gameplay integrations with
loopback networking and disposable worlds. The broad suite was also repeated with 13
additional performance/runtime JARs, for 66 upstream JARs: AI Improvements, Clumps,
FastSuite, Placebo, Ksyxis, Multiplayer Server Pause, iChunUtil, FastBoot, FerriteCore,
Memory Leak Fix, ModernFix, Noisium and Radium. Their installed hashes are recorded in
the evidence file. This tests the server performance stack together with the gameplay
mods; client-only rendering mods still require a physical client.

The retained implementation passed:

- Guide delivery and recovery, full inventories, duplicate prevention, saved receipts,
  authoritative guide values, feeding reserves and the native FTB guide-route bridge.
- Enchantment attributes, external overrides, wildlife offerings and altar maintenance.
- Crafting, food/climate integrations, Freezer recipes, loot/trades, Carry On, native
  water/fuel handling, equipment slots, companion protection and journal installation.
- All 30 tent style/size climate cases and actual powered-hearth conditioning; enclosure
  respects native fuel, range and warm-up. Reload checks preserve the integrations.
- Native tent acknowledgement gating, packing/moving, saved contents, process-restart
  persistence, round-trip entity traversal and saved exterior climate.
- Portal search parity, wildlife targeting and tornado-query parity, plus bounded
  Depth-height placement. Optional-mods-absent startup, reload and shutdown also pass.

The existing portal-search benchmark compared 300 empty searches at approximately
32.9 ms for the original loop versus 0.60 ms for the optimized loop in the gameplay
fixture, and 16.8 ms versus 0.58 ms with the expanded performance stack. Both preserved
135 live comparison cases. These are individual operation timings, not whole-pack
FPS, a controlled comparison of the two mod sets, or a multiplayer speedup.

A proposed event-maintained tent-portal index was evaluated to eliminate the periodic
all-entity scan. One restart comparison failed despite subsequent passes. The experiment
was removed; the release retains the established cleanup behavior. Avoiding an
unverified shortcut takes precedence over an unmeasured performance gain.

Build verification includes 16 JUnit tests, three release-version tests, 1,482 JSON
resources, 344 independent journal records, 26 live guide references, 42 local textures
and release payload isolation. The guide audit reports no missing image, broken link
or player-facing placeholder. Six photography groups remain on the existing capture list.

The release workflow pins and verifies build inputs, runs validation/tests, produces
the JAR and installation ZIP with checksums, and automatically publishes default-branch
pushes. Test harnesses and third-party dependencies are excluded from the production JAR.
Selected local runtime output is retained in [Setup-Audit-Evidence.txt](Setup-Audit-Evidence.txt).

## Acceptance still open

Use a copy of the real server world for the coordinated installation rehearsal. Confirm
`/kncraft status`, the effective Waystones restrictions, the updated guide and existing
quest progress. Then test the guide UI and live key labels, shaders/DH/culling, portal
views, voice/audio and representative multiplayer exploration in a physical client.
Existing upstream startup warnings are recorded separately from passed feature checks;
this is not a claim that the entire pack has an error-free log.
