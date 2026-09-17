# Migration and acceptance

## Release stages

1. **Parity build:** merge the three existing helpers and existing gameplay data with no intentional gameplay changes.
2. **Cohesion build:** add cotton, selected food effects, wildlife insulation and fiber recipes behind clear settings.
3. **Later increments:** accessories, loot, quests/guide, then optional tent-climate/backpack automation prototypes.

Keep old and new builds available for controlled testing. A ZIP handoff or successful compilation is not a production-ready migration.

## Replace/retain manifest

| Existing component | Planned treatment |
|---|---|
| KNCraftNativePortals-1.1.0.jar | Replace only once native portal parity is tested. |
| KNCraftTentPortals-1.0.1.jar | Replace only once dynamic dimension and ownership/travel/persistence parity is tested. |
| KNCraftPerformance-1.0.0.jar | Replace with the same three independent optimizations and compatibility checks. |
| Aether datapack 1.1.0 / Depth datapack 1.2.0 | Bundle equivalent resources or deliver managed external packs. Verify actual resource precedence and generator IDs; never run duplicate generators. |
| kncraft-rules datapack | Migrate rules once; ensure only one encounter scheduler/scaling mechanism owns the behavior. |
| Upstream gameplay mods | Retain as dependencies. Consolidating glue does not eliminate the upstream mods. |
| Existing upstream configuration | Import only specific settings when necessary; preserve user edits. Ship a documented optional pack preset instead of overwriting all config. |

Detect old helper mod IDs alongside the unified replacement and stop with an actionable conflict message, or disable only equivalent merged modules according to an explicit policy. Never allow both copies to register hooks silently.

## Persistent state

- Preserve dimension keys, tent identities, `KNCraftTentLink`, portal token fields and entity tags. Inventory every serialized key in the actual source before assigning a new public mod ID.
- Retain `kncraft:bounded_depth` and the exact portal-generation resource IDs unless a tested migration translates existing references.
- Preserve encounter modifier UUIDs and already-balanced entity tags. Do not heal every loaded boss again after a reload/restart.
- Do not delete or regenerate any world, tent dimension, existing equipment or portal entity as a migration shortcut.
- Keep source/import configuration backups and a schema version for any one-time migrations. Re-running import must be harmless.
- Existing external data packs can override bundled mod data. Verify enabled packs and priorities in a copy of the real mod environment.
- World resets remove world-local datapacks/serverconfigs but not all global configs. Document exactly what is bundled, generated for new worlds and installed manually.

## Required checks

### Portals and tents

- Both frame axes and all tent facing directions; small and large frames; native Aether water use and Depth ignition.
- Full Depth frame/arrival bounds inside Y=5..122; no bedrock or block-entity destruction; safe failure and reverse travel.
- New dimension creation while players are connected; correct, stale, missing and disconnected-client acknowledgements; late login into an existing tent.
- Rapid two-way seamless traversal with a real client, not only server-side teleport assertions.
- Ownership, monsters, entity restrictions, door packing, damage, moved tents, contents persistence, fallback exit and restart.

### Performance

- Retain stable selection tests including ties, empty candidates, NaN and signed zero.
- Compare actual old/new portal search at negative fractional coordinates, boundaries, missing chunks and immediate block changes.
- Compare tornado normal/pet affected entities and ordering under grab-rule combinations.
- Test each module disabled and verify unsupported upstream versions cannot receive unchecked transformations.

### Data and survival

- Confirm IDs/tags resolve against pinned mods; disabled/missing integrations do not fail unrelated startup.
- Recipes consume correct quantities, return containers, show in JEI and have no conversion duplication cycles.
- Existing insulated armor survives migration; new ingredients respect slot counts and removable insulation behavior.
- Food duration, caps, full-hunger consumption and auto-feeder behavior; Curios stacking and inventory conservation.
- Natural Waystones cannot be crafted, carried or dropped through newly introduced alternate routes; protected blocks remain protected.
- An already-tagged boss is not buffed/healed again; new bosses receive the intended modifiers once; reload schedules do not duplicate.
- Optional quests correctly share progress/rewards; guide entries describe actual enabled settings.

### Deployment environments

Dedicated server startup, multiplayer join/leave and physical-client rendering/traversal are separate checks. Integrated single-player is another separate target. Test server-only installation where advertised. If client assets require installation, test both missing-client failure behavior and the matching client setup.

Use isolated worlds and mock services; this gameplay project requires no real network service credentials. Build test harnesses as separate artifacts and assert their classes/commands are absent from the release JAR. Historical `BenefitChecks` and service tests are deliberately not included because they are outside scope.

## Rollback

Before deployment, snapshot the world plus relevant configs and the exact addon/datapack set. Stop cleanly, replace only the documented components, and check the parity release before enabling new mechanics. To roll back, stop cleanly and restore the matching old helper JARs, datapacks and their configuration together. Preserve player progress made since the snapshot when possible; any migration that prevents that must be documented before rollout. Do not blindly restore an old world over newer play sessions.

Record test evidence as actual outcomes: automated passes, manual passes and outstanding checks. Historical operation benchmarks and old server boot logs cannot stand in for testing the unified artifact.
