# KNCraft Gameplay Architecture — local Codex handoff

This is a development handoff, not an installable modpack or a completed unified mod.

Extract this ZIP into your new local GitHub project directory. Give your local Codex the entire `KNCraft-Gameplay-Architecture` folder and ask it to follow **`KNCraft-Architecture-Prompt.md`**. That document is also usable as a standalone prompt, but the source snapshots and current configuration provide important implementation details.

The scope is gameplay only, following the owner's correction: portal and tent integration, survival, crafting, exploration, progression, encounter balance, and behavior-preserving mod optimizations. Website, map integrations, Discord, branding, rank benefits and administration tooling are excluded.

## Read in this order

1. `KNCraft-Architecture-Prompt.md` — the actual instruction to the implementing agent.
2. `docs/Implemented-Gameplay.md` — current behavior that must survive consolidation.
3. `docs/Proposed-Integrations.md` — ideas and their implementation boundaries; not already deployed.
4. `docs/Migration-and-Acceptance.md` — save compatibility, test requirements and rollout.
5. `docs/Build-and-Sources.md` — source coverage and missing build inputs.

## Included evidence

- Source snapshots for KNCraft Native Portals 1.1.0, Tent Portals 1.0.1 and Performance 1.0.0, including available isolated harness sources.
- The currently deployed Aether/Depth portal datapacks, extracted authoring copies, and `kncraft-rules` gameplay datapack.
- Selected current gameplay configurations and a scoped installed-mod inventory with SHA-256 hashes.
- Original custom-mod metadata, source provenance and archive checksums.

`live-reference/` is a reference snapshot, not an instruction to overwrite a local profile. `legacy-sources/` contains separate historical projects, not the finished monorepo. Tests included there may modify their test world and must never run on a real play world.

No worlds, player data, credentials, third-party dependency JARs or compiled mod releases are included. The only nested ZIPs are the two gameplay datapacks. The snapshot was captured on September 17, 2026 UTC (September 16 local evening). Source declarations and metadata describe their own authorship/licenses; do not infer ownership of the upstream mods.

The requested deliverable from local Codex is one maintainable gameplay repository and one principal Forge runtime JAR, with organized source modules and data assets. It is not a rewrite of every dependency.
