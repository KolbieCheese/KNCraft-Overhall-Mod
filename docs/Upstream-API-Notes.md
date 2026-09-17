# Version-specific API observations

These notes summarize inspection of the hash-verified installed artifacts. Decompiled upstream
source is not included in the repository or release.

- Cold Sweat 2.4.3 clears dynamic settings, collects config/data registries, posts
  `LoadRegistriesEvent.Pre`, installs entries, then loads TOML settings. Consequently the adapter
  checks both event entries and `ItemSettingsConfig` before adding anything. Repeated registry
  loads were tested; entries did not accumulate.
- `InsulatorData.fromToml` provides the upstream schema and normal slot behavior.
  `FoodData.fromToml` with a positive duration feeds the BASE trait, not instantaneous CORE.
- Upstream food equality includes the consumed item ID. Equal-strength different foods can
  coexist. The KNCraft adapter uses `TempModifierEvent.Add` and `Temperature.removeModifiers`
  to keep just the latest managed meal while retaining sibling notifications and upstream types.
- Cold Sweat receives food use through `LivingEntityUseItemEvent.Finish`. Sequential real Forge
  finish events were exercised in a running server, including opposite temperatures and repeats.
- Sophisticated Core 1.5.1.2335's `FeedingUpgradeWrapper.tryFeedingStack` calls
  `ForgeEventFactory.onItemUseFinish`, so its inspected code path should reach that same hook.
  It still selects by hunger/filter rules and returns containers through its inventory logic.
  **This is source-level compatibility evidence, not a completed backpack gameplay test.**
- Pam's hot tea returned a non-consuming use result at full hunger in the server harness.
  Architecture deliberately leaves that native rule unchanged.
- Patchouli 85-FORGE only scans each mod's own namespace for book declarations before loading
  external books. The historical external book ID therefore requires a resource-backed external
  declaration. The registered `patchouli:kncraft_guide` ID was asserted at runtime.
- Nomadic Tents' monster check queries entities in loaded, visible chunks. The retained harness
  needs an explicit chunk-preparation step before fixture creation and again before persistence
  checks. No production permission logic was weakened to make those tests pass.
