# Build KNCraft Architecture: a unified gameplay integration mod

You are my local development partner. I am creating a GitHub repository to consolidate the gameplay customizations previously developed directly on my KNCraft server. Use the attached handoff as the baseline, inspect the included implementations, and build a maintainable unified mod instead of continuing to scatter custom addons across separate projects.

## Objective and scope

Create **KNCraft Architecture**, a Forge **Minecraft 1.20.1 / Forge 47.4.0 / Java 17** gameplay integration mod. Target one principal distributable JAR, a coherent configuration model, a reproducible build, organized optional integrations and a documented migration from the existing addons/datapacks.

Focus on gameplay cohesion: farming should support clothing, cooking and camping; exploration should supply useful materials; dimensions should reward preparation; portals and tents should feel naturally connected. Preserve working custom behavior before adding new mechanics.

Do not incorporate website services, map integrations, Discord, server branding, rank/store benefits, shared-account services or server administration tooling. Do not port those features into this mod merely because they existed on the source server. Existing Java package names in legacy sources are historical identifiers, not a request to add branding features.

This is an implementation request for the local project. Make progress through working vertical slices, produce code and tests, and explain material design decisions. Do not silently turn every proposed feature into an enabled gameplay rule. First establish parity, then implement the first integration batch behind explicit, documented configuration. Later experimental ideas can remain clearly labeled roadmap items. Do not deploy to a live server or change live worlds as part of this prompt.

## Evidence and order of authority

Read the attached documents, the three source snapshots, original metadata and live-reference configs/datapacks before coding. The source and live snapshot are authoritative about existing behavior; proposals are design intent, not evidence that features already exist. File names alone are not proof of loader compatibility; inspect mod metadata.

Required baseline integrations:

- KNCraftNativePortals 1.1.0, Aether immersive datapack 1.1.0 and Depth immersive datapack 1.2.0.
- KNCraftTentPortals 1.0.1, including dynamic-dimension synchronization and the acknowledgement barrier.
- KNCraftPerformance 1.0.0, including all conservative fallback behavior and exact upstream version checks.
- `kncraft-rules`: natural Waystone network restrictions/protection and idempotent Wither Storm/command-block encounter scaling.
- Selected gameplay policy in the live-reference configs. Keep upstream configs externally owned unless a specific integration requires a documented change. Do not overwrite whole files on every startup.

Important installed versions include Cold Sweat 2.4.3, Pam's HarvestCraft 2 crops 1.0.3, food core 1.0.5, food extended 1.0.1, trees 1.0.2, Alex's Mobs 1.22.9, Aether 1.5.2, Nomadic Tents 20.1.1, Immersive Portals Forge 3.0.7, Call From The Depth 1.22.1, Weather2 1.20.1-2.8.3 and Wither Storm 4.2.1. Resolve exact artifacts from `live-reference/gameplay-mod-inventory.json` and legacy dependencies. Infiniverse is a transitive/nested dependency and will not necessarily have its own top-level JAR.

Keep Cold Sweat at 2.4.3 initially: the previously tested 2.4.3.1 Forge artifact was missing required classes and failed startup. A future corrected release needs separate verification, not an automatic upgrade during consolidation.

## Architecture requirements

Use one public mod identity, preferably `kncraft`, while retaining legacy registry/data identifiers wherever existing saved content depends on them. Package modularity does not require multiple deployed mods. A reasonable organization is:

```text
src/main/java/.../kncraft/
  core/                 lifecycle, config, compatibility report, migrations
  integration/portals/  native Aether/Depth conversion and bounded placement
  integration/tents/    lifecycle, dynamic dimensions, travel checks
  integration/climate/ Cold Sweat food/material/accessory integration
  integration/crafting/
  integration/exploration/
  integration/encounters/
  integration/performance/
src/main/resources/     metadata, mixins, recipes, tags, loot, guide assets
src/test/               deterministic unit tests
src/gametest/           isolated integration harnesses; never bundled in release
pack-overrides/         only data/config that cannot safely live in the JAR
docs/                   architecture, features, migration, troubleshooting
.github/workflows/      build and appropriate validation
```

Adjust that structure if the pinned Forge toolchain requires it. Inspect existing mappings/reobfuscation and mixin build setup before moving classes. Keep implementation modules independently configurable. Optional integrations must not eagerly reference absent classes; gate mixins and registration appropriately. If a pinned optimization cannot be proven safe against a new upstream version, disable it with a clear diagnostic or fail explicitly according to the chosen compatibility policy. Do not silently apply an incompatible patch.

Favor supported APIs, recipes, tags and datapack registries over invasive patches. Register data once, use named/versioned values, preserve user overrides and avoid duplicate Cold Sweat entries on reload. Do not globally retag cotton as wool just to make a few recipes work. Prefer a small set of intentional compatibility tags and targeted recipes.

World/entity work belongs on the logical server thread. No asynchronous chunk access, whole-world scans every tick or repeated command dispatch to mutate unchanged attributes. Use events and bounded scheduled work, with caches whose invalidation behavior is explicit. Preserve entity selection order where behavior depends on it. Keep new diagnostics local and rate-limited.

Existing gameplay helpers are server-only for multiplayer and have dedicated-server validation. Do not advertise the merged mod as client-optional until tested. Integrated single-player needs separate validation. If custom items, screens, packets or a bundled guide require client installation, document that requirement and test it; reuse existing items for the first integration batch to keep deployment simpler.

## Phase 1: preserve existing gameplay

1. Import the source snapshots with attribution and establish a reproducible Java 17 build. Replace machine-specific dependency paths with documented resolvable dependencies or a verification-based local dependency bootstrap.
2. Preserve native Aether/Depth lighting and fallback behavior. Keep `kncraft:bounded_depth`; all Depth frame and arrival-space edits must stay inside Y=5..122. Do not move existing portal pairs automatically.
3. Preserve tent ownership/content/packing behavior, dynamic dimension identity, vanilla ping/pong acknowledgement gate, native door fallback and rapid seamless return travel. Never reuse a stale packed tent portal link.
4. Preserve performance optimizations exactly, with independent switches, upstream pins, fallback paths and existing regression coverage.
5. Consolidate the gameplay datapacks without running old and new rules simultaneously. Preserve the encounter attribute UUIDs and marker tags to prevent double scaling or repeated healing. Preserve natural Waystone restrictions and protections unless I explicitly change that design.
6. Add a compatibility/status report suitable for debugging locally: module state, dependency/version issues, data migration state, missing item IDs and rejected config entries. Avoid exposing secrets or unrelated service information.

## Phase 2: implement the first cohesion batch

Use `docs/Proposed-Integrations.md` for item IDs, balance notes and priorities.

- Pam's cotton as a Sewing Table insulation ingredient. Proposed starting balance: 1 cold / 0.5 heat insulation; this is a tuning hypothesis, not a fixed requirement or real-world thermal simulation.
- A curated selection of Pam's hot drinks/soups/stews and cold desserts/drinks affecting Cold Sweat. Limit durations and stacking; keep waterskins, insulation and shelter useful. Verify behavior when hunger is full and with automatic feeding.
- Alex's Mobs bear/bison fur and kangaroo hide as differentiated insulation materials, plus sensible insulation for the Frontier Cap. Avoid making easily farmed drops best-in-slot automatically.
- Balanced plant-fiber recipes for tent canvas/rope and, if useful, sleeping bags. Existing cotton already crafts into string; compare material costs and avoid reversible recipe exploits.

Proposed next increments: Aether icestone as Icebox fuel and cooling accessories through Curios; thematic survival loot; optional FTB Quests expedition chapters; a Patchouli field guide. Test the current Aether warmth offset before changing it. Cold Sweat already has Weather2 support; do not double-apply storm temperature effects.

Larger optional projects: campsite-aware tent temperature and temperature-aware backpack consumables. Design and prototype these separately, with explicit performance and multiplayer behavior. They are not part of the existing parity baseline.

## Migration and validation

Follow `docs/Migration-and-Acceptance.md`. Produce an exact replace/retain manifest: which three old helper JARs and which old datapacks are replaced, which upstream dependencies remain, what config/NBT IDs are preserved and which settings are imported once. Detect old-plus-new installations early to avoid duplicate hooks. Keep tests and synthetic-world creation out of production artifacts.

Validate dedicated startup, actual multiplayer login and portal traversal, integrated single-player if supported, restart/persistence, optional-mod absence, data reload and safe rollback. Preserve tent dimensions/contents, existing encounter modifiers, existing player equipment and existing portal links. Do not equate a startup log with a successful client traversal test.

For recipes and survival integrations, verify consumption, resulting items, containers, JEI display, insulation slots/tooltips, Curios behavior and food-effect stacking. Test API behavior against the actual pinned JARs. If a client test cannot be run, document it as outstanding rather than marking it passed.

## Deliverables

- One organized repository and reproducible build producing a reobfuscated Forge JAR.
- A parity release before broad gameplay changes, followed by the first cohesion batch in reviewable commits.
- A module/status matrix marking implemented, tested, proposed and blocked features distinctly.
- External override files only where needed, with install paths and world-reset instructions.
- Migration/rollback guides and a minimal required-client-mod explanation.
- Automated tests for consequential logic and retained isolated integration harnesses.
- A changelog, dependency/version manifest, source attribution and an explicit list of anything not verified.

Start by inspecting the attachment, writing a concise architecture/migration plan grounded in the source, and then implementing the parity build. Ask only for consequential missing decisions; choose routine implementation details yourself. Do not replace gameplay with placeholders or invent validation results to make the migration appear complete.
