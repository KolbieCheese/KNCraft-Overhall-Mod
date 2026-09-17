# Architecture

## Lifecycle and dependency boundaries

`core/Architecture` is the only public Forge mod (`kncraft`). It validates versions/conflicts,
imports old helper settings, installs a missing guide declaration and registers the applicable
modules. Optional classes are instantiated only after their complete dependency set is found.
`IntegrationMixinPlugin` gates transformation during early loading without loading upstream
game classes. Audited mixins retain their explicit 1.20.1 SRG targets and fail on missing
injections; Forge reobfuscates all normal Minecraft references in the release.

Legacy packages under `com.beautyinblocks.portals`, `.tents` and `.performance` remain recognizable
for comparison against `reference/handoff/legacy-sources`. This is deliberate source continuity,
not three separately deployed mods. Orchestration, climate and encounters use the new package.

Unaudited versions of patched integrations are rejected, even when a runtime switch is off.
Missing integrations are skipped. The policy is conservative: upgrade pins only after auditing
targets and rerunning regressions. Cold Sweat remains exactly 2.4.3.

## Saved data and server work

Portal generator IDs, bounded form ID, tent NBT fields, link tokens and portal tags are unchanged.
No existing portal is relocated. The tent acknowledgement gate remains independent of visible
entrances; a rate-limited warning reports stalled acknowledgements without bypassing the barrier.
Existing conservative portal/item/tornado algorithms and fallback paths are retained.

Encounter functions became an entity-join queue drained once per second on the server thread.
Only the original entity types/dimensions qualify. Weak pending references do not keep unloaded
entities alive. The old UUIDs and marker tags prevent double modifiers/healing, including the
partial state where a health UUID exists but its marker does not. The old `kncraft:encounter`
timer is removed on startup. A reload listener rejects external legacy schedulers before apply.
This preserves intended encounter behavior while avoiding missing optional dimensions and
repeated command dispatch. A newly joined encounter is processed at the next 20-tick boundary.

## Resource ownership

Bundled server packs register only when their upstream mods/config allow them. Portal JSON and
Waystone data are byte-identical to the handoff. External datapacks can override resources;
the old packs must be removed rather than relying on precedence. The same original resource
IDs mean a renamed equivalent portal JSON overrides the definition instead of creating another
ID; pack maintainers are still responsible for intentional higher-priority changes.

Fiber recipes use an intentional `kncraft:camping_fiber` tag, never global wool retagging.
Cold Sweat entries are added through its 2.4.3 `LoadRegistriesEvent.Pre`. Each load starts from
fresh upstream registries. Existing JSON/TOML definitions win; duplicate custom rows are rejected.
The food modifier event removes the previous managed meal through Cold Sweat's own API before
adding the newest one. No custom client packet, item registry, eating loop or armor NBT is added.

## Guide distribution

Patchouli 85 scans `data/<owning-mod-id>/patchouli_books` for bundled book declarations. The
historical external guide belongs to `patchouli`, so a declaration simply placed under that
namespace in our JAR is insufficient. `GuideBootstrap` creates the missing external declaration
before common setup. The declaration uses resource-backed content in
`assets/patchouli/patchouli_books/kncraft_guide`; artwork remains under `assets/kncraft`.
Existing external declarations are preserved and diagnosed until explicitly migrated by the tool.
This retains old book items, page IDs and original page ordering. All clients need the JAR assets.

Official format references: [Patchouli book JSON](https://vazkiimods.github.io/Patchouli/docs/reference/book-json/),
[1.20 resource layout](https://vazkiimods.github.io/Patchouli/docs/upgrading/upgrade-guide-120/).
Version-specific implementation choices were checked against the installed 85-FORGE JAR.
