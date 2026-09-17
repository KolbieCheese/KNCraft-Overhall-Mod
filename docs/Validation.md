# Validation record — 2026-09-17

**Development candidate, not production acceptance.** Tests used disposable loopback-only Forge
servers inside this repository, with the owner's permission to accept the Minecraft EULA for
those tests. The installed KNCraft profile, live server and play worlds were not changed.
This is a gameplay dependency subset (24 top-level upstream JARs plus nested dependencies),
not a launch of every client/visual mod in the complete CurseForge profile.

## Passed

- Java 17 compilation and Forge reobfuscation of the principal artifact and four separate
  harness artifacts. A forced repeat of the full build produced a byte-identical principal JAR.
  SHA-256: `8e272d5381d32361c0b539a868bf67dcbf1d5f2cc3887217f8696f903ce21ffd`.
- Six JUnit tests: 10,000 stable-selection comparisons including ties, empty candidates,
  NaN and signed zero; encounter scope/tags; legacy config preservation/repeated import;
  bounded config parsing and valid tuning.
- 1,307 JSON resource checks, byte-identical legacy portal/Waystone data, guide asset hashes,
  original content retention, recipe material accounting and no test/dependency payload in
  the principal JAR.
- Dedicated startup, status command, `/reload` and clean stop with **all optional mods absent**.
- Dedicated startup/reload/restart with the pinned gameplay subset. Original generator IDs
  were loaded once each. Cold Sweat remained 2.4.3.
- Runtime performance harness: 10,000 stable-selection cases, 135 actual portal-search
  comparisons plus block removal/fallback checks, actual Alex's Mobs item targeting, and
  64 normal/pet Weather2 comparisons under grab-rule combinations.
- Four native ignition/conversion cases (small/large Aether and Depth), normal bucket handling,
  four converted source areas and eight portal faces.
- Four large Depth placement cases across both axes and negative/high mapped heights;
  frame/arrival bounds, bedrock preservation and unsafe source rejection.
- Correct, wrong, stale, missing and disconnected-client acknowledgement behavior using
  the retained mocked packet harness. No timeout bypass was introduced.
- Four tent facings, ownership, native monster restrictions, packing/moving, stale-link rejection
  and content markers. Restart verified saved paired entrances and the interior diamond-block
  content markers. An actual server-controlled entity completed a two-way portal trip.
- Cotton and all wildlife registry values; nine managed meal entries; Cold Sweat registry
  reload without duplicate entries; hot/cold meal replacement and refresh through real Forge
  consumption-finish events. Hot tea does not consume at full hunger in the tested pack.
- Actual canvas/rope recipe matching and output yields, encounter UUID/tag idempotence and
  no repeated healing, registration of the preserved Patchouli book ID, and the crafted guide's
  correct `patchouli:book` NBT identity.
- Guide migration preview/backup/apply/repeat-apply on a temporary copy. The original installed
  guide was only previewed. Content changes/deletions cause migration refusal.
- Valid synthetic legacy helper ID rejected by Architecture with a remove-the-old-helper error.
  A renamed old encounter scheduler rejected before resource apply. Forge rejected a synthetic
  Cold Sweat 2.4.3.1 identity against the exact 2.4.3 dependency range.

## Fixture corrections and upstream observations

The first tent safety check ran immediately after loading fresh chunks, before spawned entities
were visible to the native query. Explicit `forceload` and waiting resolved it without changing
production permission logic. The first restart check likewise needed the documented chunk
preparation before looking up saved portal entities; the prepared repeat passed. These failures
were not counted as passes or fixed by weakening assertions.

A migration unit test found that NightConfig rejected a temporary filename ending in `.importing`.
The importer now uses `.importing.toml`; the test passes. Two early negative-test fixture formats
were invalid Forge mods; only the corrected packaged @Mod fixture's rejection is counted above.

The pinned upstream set logs pre-existing data warnings: three Pam's Food Core recipes reference
unknown items (`caramelcupcakeitem`, `melonpieitem`, `honeymuffinitem`), several Depth tags/biome
features are malformed, and Pam's crop loot modifiers emit decode warnings. These are attributed
by the loader to upstream resources; Architecture does not silently replace them. Full-pack
behavior/loot should be checked before rollout. Fresh default Waystones config lacks the captured
natural-network restrictions; Architecture reports this and the migration guide lists the keys.

## Outstanding acceptance

- Physical multiplayer join/leave, late login into a saved tent, client dimension application,
  actual player rapid two-way traversal and visual/position agreement.
- Integrated single-player launch and gameplay; server-only installation is not advertised.
- In-game guide typography, page clipping, link/recipe display, custom teal textures, and
  existing player bookmarks. Static asset checks are not a rendered-client test.
- Real Sewing Table capacity/tooltips/removal, previously insulated equipped armor, and rollback
  with saved equipment. Registry registration alone does not prove these UI/persistence paths.
- Full manual consumption/containers, all foods at full hunger, real backpack feeding with
  inventory/container conservation, JEI display, and Curios interactions.
- Natural Waystone protection through actual player break/carry/explosion paths, full boss phases,
  and a migration/rollback rehearsal on a backed-up copy of the real server world.

These are required before calling the mod production-ready. Larger roadmap features in Features.md
are not implemented. Historical handoff benchmarks were not substituted for this build's results.

Concise pass evidence is retained in `Validation-Evidence.txt`. Full local test logs are ignored
under `.cache/` and `run-isolated/`; no test worlds or runtime dependencies are shipped.
