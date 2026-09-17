# Changelog

## 1.0.5 - setup audit

- Document the installed-pack audit, coordinated installation requirements, local
  Waystones policy mismatch and client rendering/performance follow-up.
- Recheck the existing gameplay and performance integrations. Gameplay behavior and
  client settings remain unchanged; no additional performance mods are introduced.

## 1.0.4 - KNCraft Guide Book

- Rename the Patchouli book to **KNCraft Guide Book**, including command messages,
  campsite help and journal references. The bundled book advances to version 14.
- Update existing declarations that still use the old title on startup, preserving
  custom settings and the `patchouli:kncraft_guide` identity. Existing books and
  `/guide` continue to work.

## 1.0.3 - recover a lost guide

- Added `/guide` and `/kncraft guide` for every player, without operator permission.
  Recovery uses an empty inventory slot, skips copies already carried (including offhand),
  and reports when the inventory is full without replacing or dropping items.
- Explicit recovery works after the first-join gift and when automatic gifts are disabled.
  The Welcome chapter explains the commands; the bundled book advances to version 13.

## 1.0.2 - connected Field Guide

- Added configurable automatic-feeding reserves for Golden Apple Stew and enchanted
  golden apples, with a persistent per-player opt-out and unchanged manual consumption.
- Give one Field Guide per player, recognizing carried copies and waiting for inventory
  space. Preserve the receipt through respawn, reconnect and saved-player reload.
- Connect all 344 journal records to their existing guide chapters through FTB's native
  Open in Guide action; retain stable IDs, administrator edits and reward-free tracking.
- Display 26 authoritative server references beside food/equipment recipes and sewing
  instructions: nine thermal foods, five insulation materials/items and twelve machine recipes.
- Include the supplied BlueMap photo with three Wither Storm markers, an overview and
  a native detail view. Preserve its original JPEG bytes and all prior artwork.
- Replace author capture prompts with useful player instructions, complete short captions,
  add illustrated hearth/kitchen steps and audit unfinished text/images during builds.
  Keep the six remaining photo groups (13 minimum shots) in the external capture checklist.
- No expedition loadouts or equipment profiles are included.

## 1.0.1 - expedition polish

- Added an opt-in control preset with backup/restore and conflict reporting; all eleven
  guide chapters now display relevant live key bindings.
- Changed four More Enchantments attribute contributions to cooperative modifiers,
  with bounded legacy migration and an operator-accessible backup restore.
- Added twenty selected regional climate defaults while preserving explicit Cold Sweat
  settings, and on-demand campsite/hearth inspection without loading distant chunks.
- Protected deliberate wildlife offerings briefly from backpack magnets.
- Added nine Aether altar repair recipes and preserved complete equipment data,
  including sewn Cold Sweat insulation, through those repairs.
- Added BOP and RopeBridge accomplishments, bringing the journal to 344 independent
  records; retained all prior quest/task IDs and administrator-owned files.
- Integrated the changes into existing guide chapters and equipment recipe entries.


## 1.0.0 â€” cohesion expansion

- Added campsite-derived tent climate and bounded enclosure support for Cold Sweat.
- Connected cooking ingredients, agricultural rail fuel, prepared juices and Aether freezing,
  Aether climate equipment, and temperature-aware backpack feeding.
- Added Elytra Slot flight-enchantment support, a Valkyrie Lance reach profile,
  absent-owner companion protection, themed exploration supplies, and limited-stock trades.
- Repaired known Pam's recipes/loot definitions and its missing savanna garden biome tag.
- Restored merged rail/Depth tags and added Carry On exclusions for tent doors/hearth halves.
- Added a stable FTB accomplishment journal with no progression gates or rewards.
- Moved integration help into the existing Field Guide chapters and affected Cook Book pages.
- Added automatic public-runner builds, sequential release versioning, downloadable JAR/ZIP
  assets, checksum verification, and expanded isolated test tools.

## 0.2.0 â€” development candidate

- Consolidated three legacy helpers under one Forge identity with version-gated mixins,
  one-time config import, local status diagnostics and duplicate-install/reload protection.
- Preserved portal generators, Depth bounds, tent link serialization and conservative
  performance algorithms. Added a diagnostic for indefinitely pending tent acknowledgements.
- Bundled natural Waystone data. Replaced the repeating encounter function with a bounded
  entity-join queue preserving dimension scope, UUIDs, tags and one-time health initialization.
- Added opt-in cotton/wildlife insulation, nine bounded thermal meal definitions with a
  shared replacement rule, and cost-equivalent cotton canvas/rope recipes.
- Imported the complete existing Field Guide and teal resources; added 17 gameplay pages,
  cross-links, a book recipe and a migration bridge retaining its historical identity.
- Added deterministic tests, retained isolated legacy harnesses, a cohesion runtime harness,
  checksum-based dependency bootstrap, asset/release validation and CI workflows.
- Fixed a config-import temporary filename discovered by a new migration test: NightConfig
  requires the temporary file to retain a `.toml` extension.

Initial parity sources/build were established before survival additions. The 0.2 candidate
started in parity mode with cohesion switches off; 1.0 enables cohesive defaults for fresh installs. Runtime and physical-client results are
tracked separately in `docs/Validation.md`; this is not a claim of production acceptance.
