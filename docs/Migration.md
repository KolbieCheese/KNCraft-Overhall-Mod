# Installation, migration and rollback

Validation scope and representative client checks are recorded in `Guide-Recipe-Validation.md`.
Do not put any `ISOLATED-TESTS` artifact on a play server. Client verification uses a separate
local test world; existing play worlds and the live multiplayer server are not test fixtures.

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
the KNCraft Guide Book; it is an upstream dependency, not embedded in Architecture.

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

### Recipe and thermal-food update (1.0.7)

An already migrated guide needs only the matching new JAR and a complete restart.
Its resource-backed declaration advances to book version 16 even if its title was
already renamed. Recipes now contains mod subcategories; all food/drink recipes
belong to Cook Book. Existing entry IDs and carried books remain valid.

Cook Book keeps its original top-level identity and gains Warm, Cold and Neutral Food
subcategories. Categories reflect pack defaults; every food page shows live server
effects, including native overrides or disabled integrations. The 278 thermal defaults
use simple, prepared and elaborate recipe tiers. Prepared adds 0.05 magnitude/15 seconds;
elaborate adds 0.10/30 seconds, capped at 120 seconds. The original nine configurable
foods remain simple and unchanged. Other explicit food rows still override catalog values.
Neutral food remains neutral regardless of recipe complexity and does not clear an active
thermal meal. Tiers are generated from the audited recipes at build authoring time, not
recomputed per server tick or item consumption. See `Thermal-Food-Tiers.md` for the policy.

`cohesion.expandedThermalFoods` defaults to true in `kncraft-common.toml` and adds the
curated food catalog without replacing the existing `foods` list. An explicit row
for an added item overrides that item's new default. `expandedThermalFoodExclusions`
removes selected item IDs from the expanded catalog. `thermalMeals=false` disables
all managed thermal meals; `expandedThermalFoods=false` retains only the original
administrator-owned `foods` rows. Cold Sweat's own definitions retain precedence.
Install 1.0.7 on both sides: the guide's smaller per-page network protocol has changed.

Versions through 1.0.6 incorrectly assumed Patchouli 85 honors `use_resource_pack`
for declarations in `patchouli_books/`. It still selected the external content loader,
so an updated title/version could appear above stale recipes. Version 1.0.7 corrects
the loader only for `patchouli:kncraft_guide` when that flag is true. Existing loose
pages remain untouched for rollback. Already migrated profiles need no additional
migration command, just the matching JAR and a full restart. Other external books
and KNCraft declarations with the flag false retain their original loading behavior.

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

## Updating 1.0.0 to 1.0.1

Install the matching JAR on server and clients. New controls remain opt-in; existing
`options.txt` assignments are untouched until `/kncraft controls preset` is used.
The preset saves only affected bindings to `config/kncraft-controls-backup.json`.
Restore keeps bindings customized after applying the preset and removes the consumed
backup. Review `/kncraft controls` afterward because unrelated/contextual conflicts
can still exist.

More Enchantments previously saved bonuses directly into base attributes. On the
first complete set of four native formula evaluations, KNCraft migrates those bases
only if **all four** match the currently equipped native formulas. This is a
conservative fingerprint, not historical proof of who wrote an attribute. A custom
setup deliberately using the identical four values is indistinguishable: set
`migrateMatchingLegacyAttributes=false` in `kncraft-polish.toml` before upgrading if
that applies. Nonmatching bases and unrelated modifiers are preserved. On a
nonmatching legacy save, review potential retained old bonuses rather than silently
resetting them. `/kncraft attributes` shows the recorded decision and original values.

The one-time record `ForgeData.KNCraftAttributeMigrationV1` contains the original four
bases and migration decision. Operators can use `/kncraft attributes restore <player>`
to restore the recorded bases; current enchantment modifiers remain active. The
record survives respawn and saved-player reload. New bonuses use stable transient
UUIDs and rebuild from equipped items. Disabling the attribute module restores the
upstream procedures. Returning to the old mod build likewise restores its native
per-tick base-writing behavior; keep a backup before any world rollback.

Regional defaults never rewrite Cold Sweat TOML. Its explicit JSON/TOML definitions
and absolute biome disables take precedence; existing offsets still apply. The two
obsolete biome strings noted by the audit are left in administrator-owned settings.
Only 20 deliberately selected gaps receive defaults; other regions retain native
fallbacks. Altar recipes use existing items and preserve complete stack data,
including capabilities. Journal IDs already present in 1.0.0 remain unchanged.

## Rollback and resets

### Field Guide finishing update (1.0.2)

Install the matching JAR on the server and clients; use the same external-guide migration
procedure above if the original loose guide is still installed. The bundled book is version
12 with the unchanged ID `patchouli:kncraft_guide`. Original artwork stays intact; approved
text replacements are recorded explicitly in `Guide-Editorial-Edits.json`.

`giveStarterGuide` in `kncraft-polish.toml` defaults to true. A player without a receipt
receives one guide on joining, or when a free inventory slot becomes available. An existing
carried guide (including offhand) marks the receipt without adding another. Stored copies
in remote chests are not scanned. Existing players without a receipt can receive this gift
on their first join after upgrading. `ForgeData.KNCraftGuideReceivedV1` survives death,
reconnect and save/load. Lost books can still be crafted; respawning does not replace them.
No other starter items, expedition loadouts or equipment profiles are provided.

`protectReservedFoods` and `reservedFoods` configure automatic-feeding reserves. The default
list is Golden Apple Stew and enchanted golden apples. An empty list is valid. The rule also
holds at emergency hunger; ordinary eligible food remains available. Player preference
`/kncraft feeding reserved false` allows automatic use, and `true` restores protection.
`ForgeData.KNCraftAllowReservedAutoFeed` preserves that choice through save/load and respawn.
Manual eating and upstream backpack filters keep their normal behavior.

The book requests its allowlisted server reference values while those pages are open.
It refreshes every five seconds, labels defaults while waiting, and clears its cache on
disconnect. Food/insulation values describe a plain item against the native server registry;
special item data, conditions and additional modifiers may differ. The displayed definition
is not a prediction of final body temperature. Machine times come from the server recipe
manager. Other prose explicitly marked as a pack default remains static.

Unmodified managed journal chapters gain Open in Guide links automatically. Administrator-edited
chapters remain preserved; add `guide_page: "kncraft/chapters/<chapter>"` there if desired.
This update never resets journal progress or replaces administrator quest files.

Photography is tracked in `Guide-Capture-Backlog.json` and `Guide-Finishing-Review.md`.
The BlueMap example is received; six groups/13 minimum captures remain. These requests
are documentation tasks, not unfinished prompts shown to players.

### Guide recovery (1.0.3)

Book version 13 documents `/guide` and its `/kncraft guide` alias. Both commands are
available to all players and bypass the one-time gift receipt and `giveStarterGuide`
switch. They recognize existing carried copies (including offhand) and use only an empty
main-inventory/hotbar slot. If all slots are occupied, no item is replaced or dropped;
the player gets a message to free space and retry. Console use reports that a player is
required. Recovery marks the normal receipt so it cannot trigger an extra automatic gift.

### Guide book name (1.0.4)

The book is now named **KNCraft Guide Book**. On startup, declarations still using
the original title receive the new name; recognized old subtitles update too. Other
settings and administrator-chosen titles are preserved. Resource-backed declarations
advance to at least book version 14; legacy content still requires the migration above.
The identity remains `patchouli:kncraft_guide`, so existing copies, links and `/guide`
keep working. Install the matching mod update on clients and server, then restart.

### Returning to the earlier helper arrangement

Stop cleanly. Restore the three old helpers and matching datapacks/configs together, remove
Architecture, and restore the backed-up guide declaration so its retained external chapters
load again. Never run old and new helpers together. Short meal modifiers use Cold Sweat's
existing type and expire normally. New recipes create existing items. Insulation uses upstream
NBT; behavior after removing its definitions still needs a saved-equipment rollback test.
Do not restore an old world over newer player progress without reviewing the consequences.

A world reset removes world-local datapacks/serverconfigs but leaves global configs, mod JARs
and the external guide declaration. Bundled data applies to new worlds automatically. Review
upstream defaults for the new world; this mod intentionally does not copy prior world settings.
