# Validation record - KNCraft Compatibility 1.0

Tests ran on 17 September 2026 in disposable, loopback-only Forge 47.4.0 servers
inside this repository. The owner authorized EULA acceptance for these tests.
The installed CurseForge profile, live server and play worlds were not changed.

The expansion server loads 53 top-level gameplay/dependency JARs plus nested
libraries and isolated test harnesses. Exact hashes are in [Testing-Mods.json](Testing-Mods.json).
This is not a full graphics/client launch of all 102 installed pack JARs.

## Build and resource checks

- Java 17 compilation, Forge reobfuscation and 10 JUnit tests passed, including
  selection parity, migration preservation, feeding boundaries and managed-file ownership.
- Three version-allocation tests passed: initial/incremented releases, retry reuse,
  and a new major/minor series.
- All eight public compile downloads matched pinned hashes; nested Infiniverse is
  derived from the verified Nomadic Tents archive. No third-party JAR is redistributed.
- 1,467 JSON resources, stable/unique journal IDs, no quest rewards or dependencies,
  original guide asset/content preservation, thermal-food page values and pagination
  budgets passed static validation. The release validator excludes test/dependency payloads.

## Expanded dedicated-server checks

- All 30 native tent style/size combinations, including two expanded floor layers,
  inherited exterior climate and bounded enclosure. Moving the source from Overworld
  to Nether changed climate; unloaded sources retained the saved sample without being loaded.
- All five tent styles, at the largest size, warmed an occupant through a native powered
  hearth. Over 500 native hearth ticks, fuel decreased from 500 to 488, warmth was applied,
  and every air path remained inside the tent bounds. This does not claim a single hearth
  reaches every position in a large tent.
- Climate/enclosure opt-out checks and saved door climate after a real process restart.
- Actual crafting matches/yields, Skyroot bucket return, two-oil minimum, three disabled
  invalid recipes, three Freezer inputs/results and Icestone fuel values.
- Real Curios slots, chest-elytra precedence, non-flight armor preservation and opt-out.
  All eight patched More Enchantments procedures transformed without injection errors.
- Cold Sweat's actual equipment pipeline capped three managed ice accessories at four
  heat insulation. Other armor remained intact; an administrator Ice Ring override
  survived registry reload and was excluded from KNCraft's cap.
- Sophisticated's native food-eligibility path rejected a warming meal while hot,
  accepted cooling food and retained the low-hunger emergency exception.
- Owned-companion relations, the native Valkyrie Lance weapon profile and modded savanna tags.
- 128 real curated loot rolls preserved original stacks and added at most one small supply
  stack per selected table. All four native villager trade registrations were verified.
- Vanilla rails tag restored; an administrator tag contributor stayed unchanged.
- Carry On's actual permission queries excluded tent doors/hearth halves and the upstream
  backpack blacklist, while ordinary chests remained permitted. Native list caches now
  refresh after tags load.
- Native diesel bottle/amount rules, insulated-minecart parent linking, Comforts sleep
  checks with/without the upstream sleeping-bag exemption, and consumption of an ordinary
  inventory totem without an extra recovery system.
- FTB loaded every stable quest/task ID and resolved all 339 referenced advancements.
  Its native advancement task recognized a Wither kill criterion, and every journal icon
  resolved to a registered item. Installer repeat runs retained IDs;
  administrator chapter/root edits and duplicate-ID conflicts were preserved.
- The original performance, acknowledgement-barrier and four Depth-height regressions
  passed again with the expanded gameplay subset.
- The full expansion harness passed before and after `/reload`. The original cohesion
  harness still passed, including registry idempotence, recipes, encounter markers and guide ID.
- Startup, status, reload and clean stop passed with every optional mod absent, and with
  only Nomadic Tents + Cold Sweat (no Immersive Portals, Weather2, Aether or Curios).
  Those reduced sets are startup checks, not a full gameplay matrix for every subset.

## Observed limits and upstream messages

Forge intentionally refuses advancement awards to FakePlayer; the journal fixture therefore
uses a normal ServerPlayer with a no-op connection. Cold Sweat hearths in the pack require
redstone when automatic mode is off, so the fixture powers the real hearth. Neither test
was made to pass by removing a gameplay requirement.

The new recipe/tag fixes remove the specific Pam's and missing rail/Depth-tag errors they
address. Existing upstream warnings remain for Depth biome features, several crop loot
branches, Immersive Portals mixin metadata and Cold Sweat's absent optional Spoiled class.
These do not stop the tested server, but this work does not certify upstream world generation.

Physical-client acceptance remains outstanding: guide typography/links/textures and JEI;
Sewing Table and Curios UI; flight movement/durability with Do a Barrel Roll; multiplayer
portal rendering/rapid crossings; nunchaku and Furor combat feel; shaders, distant terrain,
culling, voice and cross-dimension sound. A server-controlled fixture cannot prove those.
Full boss playthroughs and a migration/rollback rehearsal on a copy of the real world also
remain player acceptance tasks. This is a tested development release, not a claim that the
entire modpack has passed a multiplayer/graphics certification.

The earlier consolidation tests and their original hashes are preserved separately in
[Validation-0.2.md](Validation-0.2.md). Concise new evidence is in
[Expansion-Validation-Evidence.txt](Expansion-Validation-Evidence.txt); full local logs and
worlds remain ignored and are not shipped.
