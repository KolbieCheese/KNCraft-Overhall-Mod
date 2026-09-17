# Installation, migration and rollback

This candidate has not passed physical-client acceptance. Test a copy first. Do not put any
`ISOLATED-TESTS` artifact on a play server. No live installation was changed during this work.

## Exact replacement

With the server/game stopped, snapshot the world, `config/`, `defaultconfigs/`, world
`serverconfig/`, external guide declaration, and exact mod/datapack set. Store backups outside
active `mods/` and `datapacks/` directories.

Replace these three JARs with **one** `KNCraftCompatibilityMod-<version>.jar`:

- `KNCraftNativePortals-1.1.0.jar`
- `KNCraftTentPortals-1.0.1.jar`
- `KNCraftPerformance-1.0.0.jar`

Remove these external datapacks from the world and any global datapack loader:

- `kncraft-aether-immersive-portals-1.1.0.zip` (or its extracted folder)
- `kncraft-depth-immersive-portals-1.2.0.zip` (or its extracted folder)
- `kncraft-rules` (or its ZIP)

Architecture bundles equivalent portal/Waystone data and owns encounter scaling. Duplicate
helper IDs and known old pack names fail with a diagnostic; renamed encounter schedulers are
also rejected on startup/reload. Do not use a different name to work around these checks.

Retain all upstream gameplay/dependency mods and existing configs. Infiniverse stays nested
inside Nomadic Tents; do not install the bootstrap's extracted build JAR separately. Use the
handoff inventory and `docs/Testing-Mods.json` to compare actual artifacts. Keep Cold Sweat 2.4.3.
Retain Waystones' creative-only and generated-stone protection settings; the exact four-key
fragment is in `pack-overrides/`.

Install the matching Compatibility JAR on clients too. Patchouli must remain in the pack for
the Field Guide; it is an upstream dependency, not embedded in Architecture.

## Guide migration

Fresh installations get the small external declaration automatically. Existing guide content
is preserved; to switch the old declaration to the bundled updated chapters, close Minecraft
and run on each relevant server/client profile:

```powershell
python tools/migrate_guide.py --instance "C:\path\to\KNCraft"
python tools/migrate_guide.py --instance "C:\path\to\KNCraft" --apply
```

The first command previews. The second saves the old declaration under `kncraft-guide-backups/`,
sets `use_resource_pack=true`, and preserves the historical `patchouli:kncraft_guide` ID. It refuses
unrecognized content edits so they can first be ported to a resource-pack override. No chapter,
cookbook entry, artwork or player book is deleted. Existing guide textures can remain installed.
The new book recipe is one book plus one paper. The old external chapters remain for rollback.

## Config and parity first

If `config/kncraft-common.toml` does not exist, the four old helper boolean settings are imported
once. Originals remain untouched and backups go into `config/kncraft-migration-v1/`, with schema
version 1. If the unified config already exists, it wins. No upstream configuration is rewritten.

Version 1.0 enables cohesive defaults on fresh installations. An existing 0.2 config is
preserved: review its cotton, wildlife, meals and fiber switches if those features should
now be enabled. New switches are in `kncraft-integrations.toml`. Inspect `/kncraft status`
after restarting; then verify travel, tent contents, equipment and the guide on a test copy.
Changing a switch while running is not a supported pack-switching workflow.

## Upgrading 0.2 and the accomplishment journal

Replace the single 0.2 Architecture JAR with the matching Compatibility JAR on both sides;
do not reinstall the three superseded helpers. Internal mod/book/world IDs remain unchanged.
The guide now puts integration help in its existing chapters and thermal food pages.
Already resource-backed declarations continue to load bundled content; legacy external
ones need the migration tool above.

On server start, when FTB Quests is installed, the journal installer adds `kncraft_*.snbt`
chapters under `config/ftbquests/quests/chapters`. It records owned file hashes in
`config/kncraft-journal-manifest.json`, preserves edited chapters/root settings, and skips
conflicting IDs. Player/team progress is never written by the installer. Read its status
message if a chapter was skipped. Journal files use stable IDs across updates.

`installAccomplishmentJournal=false` stops future installation and the new detection packs.
It does not delete chapters already installed or erase their progress. To retire a chapter,
back up the FTB quest folder and remove only the intended `kncraft_*.snbt` while stopped.
The same applies when removing an optional upstream mod. Existing native advancements can
count immediately; new structure/kill/inventory detection starts when the update is active.
FTB's normal team sharing remains in effect.

Tent climate snapshots add only `KNCraftExteriorClimate` to interior door persistent data.
Unloaded camps keep the last known sample; normal placement updates the entrance source.
New loot affects unopened/generated loot, and garden coverage applies to newly generated
chunks. The update does not refill opened chests or regenerate existing terrain.

Serialized fields/IDs and modifier UUIDs are enumerated in `Migration-Manifest.json`. Existing
portals are never relocated. Existing tent worlds, identities, equipment and inventories are
never regenerated. The obsolete encounter timer is removed; the old marker tags remain valid.

## Rollback and resets

Stop cleanly. Restore the three old helpers and matching datapacks/configs together, remove
Architecture, and restore the backed-up guide declaration so its retained external chapters
load again. Never run old and new helpers together. Short meal modifiers use Cold Sweat's
existing type and expire normally. New recipes create existing items. Insulation uses upstream
NBT; behavior after removing its definitions still needs a saved-equipment rollback test.
Do not restore an old world over newer player progress without reviewing the consequences.

A world reset removes world-local datapacks/serverconfigs but leaves global configs, mod JARs
and the external guide declaration. Bundled data applies to new worlds automatically. Review
upstream defaults for the new world; this mod intentionally does not copy prior world settings.
