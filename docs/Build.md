# Build and test inputs

Pinned toolchain: Java 17, Gradle 8.8 (wrapper checksum), ForgeGradle 6.0.36,
Minecraft 1.20.1 official mappings and Forge 47.4.0. `tools/bootstrap_dependencies.py` copies
nine top-level inputs (including Patchouli 85's public guide API) and extracts Infiniverse from the verified Nomadic Tents parent archive.
It records hashes in ignored `libs/verified-inputs.json`. Build dependencies are local aliases
for verified artifacts, not machine-specific paths. All third-party JARs remain outside Git.

The owner's installed profile supplied the exact matching artifacts. Names alone were not used
to infer loader compatibility: Aether's filename includes `neoforge` but its metadata and this
Forge startup were checked. Pam's Food Extended reports internal version `0.0NONE`; preserve
the artifact hash rather than assuming the filename equals its internal version.

`gradlew build` runs deterministic tests and reobfuscates the runtime JAR. `harnessJars` makes
separate reobfuscated depth, tents, performance, cohesion and expansion harnesses. Main resources never
include test commands/classes. `tools/validate_artifact.py --jar PATH` checks this boundary,
JSON resources, guide assets, original gameplay data and fiber recipe costs.
The normal Gradle `check` task also runs this resource audit and writes
`docs/Guide-Asset-Report.json`. Documented photo requests may remain in the external
backlog; missing textures or unfinished author prompts inside the book fail the build.

CI builds on public GitHub-hosted runners. `bootstrap_dependencies.py --download` obtains
the exact inputs from the official URLs in `Build-Inputs.json` and checks their hashes.
Default-branch pushes publish incrementing versions; PR builds do not publish. See
[release behavior](Releases.md). No secrets or downloaded client profiles are embedded in CI.

## Isolated runtime procedure

Install the official Forge 47.4.0 server into a new directory inside this repository. Create
`KNCraft-ISOLATED-TEST-WORLD` there. Minecraft's EULA must be accepted by the owner before starting.
Use loopback-only binding, an unused port, a disposable `level-name`, Java 17 and sufficient RAM.
`tools/stage_test_mods.py` stages the gameplay subset plus dependencies, verifies captured hashes,
and records the resolved list. It requires Python 3.11+ or `tomli`. Depth has an undeclared
GeckoLib runtime requirement, so the staging list includes GeckoLib explicitly.

Copy the principal JAR and only the desired harnesses, then use `tools/smoke_server.py` with a
JSON list of `[delay_seconds, console_command]` pairs. The runner refuses an unmarked directory,
captures output and stops the server. A ready server/zero process status does not mean commands
passed: inspect their explicit PASS/FAIL messages, as documented in Validation.md.
The harnesses themselves also require the marker and `-Dkncraft.isolatedTests=true`; the runner
supplies that property after checking the directory.

For tent fixtures, run `forceload add 992 992 1135 1119` and wait before `kncrafttenttest`.
This makes newly added monster entities visible to the native nearby-monster query. The original
harness assumes entity-visible chunks; immediate creation in fresh unloaded chunks produced
a fixture failure, resolved by preparing chunks without changing the gameplay implementation.

Test commands include `kncraftperfprepare` (wait), `kncraftperftest`, `kncrafttentacktest`,
`kncrafttenttest`, `depthheighttests`, `nativeportalchecks`, `kncraftcohesiontest`, and the separate
tent persistence/traversal commands retained in source. The expansion adds `kncraftexpansiontest`
and `kncraftclimatetest`, plus `kncraftclimatepersist` after restarting their saved fixtures. They modify their world deliberately.
`kncraftreferencetest` exercises guide delivery, feeding reserves, persisted preferences,
server definition overrides, recipe replacement, packet data and native FTB guide routes.
Real multiplayer login and seamless rendering/traversal require a physical client afterward.

## Recipe guide authoring

The recipe catalog is checked in; a normal build does not need the owner's modpack.
To refresh it, stage every installed recipe-bearing mod in the marked test fixture
(including Macaw's Bridges/Stairs, JRFTL and Elytra Crafting), run `kncraftguidecatalog`,
then run `python tools/update_recipe_catalog.py --registry run-isolated/guide-recipe-registry.json --instance PATH`.
Run this generator after any older guide-authoring helpers. It references actual
loaded recipe IDs, groups non-food entries under Recipes, and retains existing entry IDs.
`docs/Guide-Recipe-Index.json` records the resolved recipe audit used by CI; the new
coverage validator checks every standard recipe and the food/non-food split.

`thermal-foods.json` is the explicit serving-temperature catalog, generated from reviewed
food families and the runtime food-item registry. Runtime code performs no name matching
or world scans. `kncraftthermaltest` verifies every catalog food and exercises replacement,
reloads, module switches, exclusions, explicit KNCraft rows and native Cold Sweat overrides.
`kncraftreferencetest` also checks that selected guide queries return only allowlisted keys.
