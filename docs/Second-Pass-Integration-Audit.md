# Second integration audit after 1.0.0

Reviewed 17 September 2026 against the installed KNCraft CurseForge instance and
KNCraft Compatibility source commit `542481d`. This preserves the original
recommendation report. Its seven proposals were subsequently implemented for 1.0.1;
see [the implementation and validation record](Polish-Validation.md) for completed
checks and remaining client acceptance. Statements below describe the audit baseline.

All **102 top-level installed JARs** still match their earlier audited SHA-256
hashes. The new inventory covers 12,074 parsed, root-level `data/` JSON resources,
including 4,133 recipes, 2,523 tags, 1,687 loot tables and 1,230 advancements.
These counts include upstream resources that can be replaced or disabled at runtime;
they are not counts of successfully loaded game content. Relevant configs, the
1.0.0 implementation and selected installed-version classes were examined more
deeply. Nested dependencies are recorded in the original inventory; versioned
resource overlays and every class were not individually re-audited.

[Second-Pass-Mod-Coverage.json](Second-Pass-Mod-Coverage.json) records every
artifact, its hash, review group and disposition. Installed settings and play
worlds were read only. No new server or physical-client tests ran for this audit.
The earlier runtime evidence and remaining client work are in
[Validation.md](Validation.md).

## Recommended priorities

| ID | Proposal | Why it belongs | Evidence / effort |
|---|---|---|---|
| P1 | Resolve everyday control collisions | Train, backpack, Curios and voice controls should be usable together | Confirmed installed bindings; small pack configuration and guide work, then client testing |
| P2 | Make More Enchantments attribute changes cooperate | Preserve other attribute changes and avoid repeated command dispatch | Confirmed installed implementation; medium Java work with migration tests |
| P3 | Tune uncovered regional climates | Terrain appearance, travel preparation and campsite temperature should agree | Confirmed table gaps; sample actual climate before selecting values |
| P4 | Protect deliberately dropped wildlife food from magnets | Feeding an animal should not immediately put its food back in a backpack | Strong code-path evidence; reproduce before a narrow adapter |
| P5 | Add selected equipment to Aether altar repair | Exploration gains another practical connection to expedition equipment | Confirmed recipe gap; small data addition, balance and NBT tests |
| P6 | Finish native exploration/construction journal coverage | Track existing accomplishments without gates, rewards or a prescribed route | Five confirmed omitted non-root advancements; small content update |
| P7 | Show useful campsite status on demand | Players can understand saved outside climate and room-conditioning behavior | New usability feature; small/medium work using native state |

My implementation order would be P1/P2/P4/P6, then P3/P5/P7. P3 should include
actual temperature sampling before broadening the temperature table. Existing
Skyroot/BOP building variants remain outside this scope.

## P1 — One coherent control scheme

The installed `options.txt` binds:

| Key | Assigned functions |
|---|---|
| N | Curios inventory, train redstone, disable voice chat, voice group |
| V | Aether invisibility toggle, train lamp, voice menu |
| B | Backpack, train whistle |
| H | Train slowdown, hide voice icons |

Better Minecarts' `ClientEvents.ClientForgeEvents.onKeyInput` handles its bindings
while riding a minecart; Simple Voice Chat's `KeyEvents` independently handles its
bindings. These are concrete overlaps, although the exact resulting behavior
depends on mapping registration, screen and input context. They need a real client
reproduction; this audit does not claim every mapped action fires together.

Provide an optional, documented pack control preset and a conflict report. Preserve
players' customized controls. Show the current binding in guide instructions where
possible. FTB Quests has no assigned hotkey in this profile, but its inventory
sidebar button is enabled, so the journal is already accessible. Give it an easy
documented entry point. Ordinary context-specific overlaps such as JEI recipe
keys and world actions should not be treated as automatic bugs.

Guide placement: existing Getting Started, Travel, Aether and multiplayer pages.
Acceptance: riding a train while opening a backpack, accessing Curios and using
voice controls; verify rebound keys and the journal entry point.

## P2 — Attribute changes that cooperate

In installed More Enchantments 1.4.3, `UpdateTickProcedure` calls four procedures at
the end of every player tick. Their server paths execute commands setting these
attribute base values, including when the relevant enchantment level is zero:

| Procedure | Attribute | Written value |
|---|---|---|
| Furor | Attack speed | `4 + level * configured_strength` |
| Agility | Movement speed | `0.1 + level * configured_strength` |
| Range | Block reach | `4.5 + level * configured_strength` |
| Armoring | Armor | `computed_enchantment_total * 0.5` |

This amounts to four command dispatches per player per server tick. It overwrites
other changes to those **base values**; it does not by itself erase normal
attribute modifiers. Better Combat reads attack speed, so preserve the intended
Furor effect and test its timing. There is no measured performance claim here.

Replace the managed contributions with stable, entity-scoped modifiers that update
when needed and are removed correctly when equipment changes. Preserve configured
strengths and the existing Elytra Slot adapter. The commands use a nearest-player
selector at the ticking player's location; this is not evidence that they normally
target another player. Direct entity updates would remove that unnecessary selector.

Acceptance: equip/unequip, death, relog, dimension changes, co-located players,
armor plus Curios wings, administrator attributes and other mods' modifiers.
Previously saved base values require an explicit migration strategy; do not
indiscriminately reset legitimate custom player attributes.

## P3 — Regional climate that matches the terrain

The installed Cold Sweat `world.toml` has 97 explicit biome temperature entries.
Comparing its biome section with packaged biome definitions gives:

| Biomes | Installed definitions | Explicit temperature entries | Without explicit entries |
|---|---:|---:|---:|
| Biomes O' Plenty | 63 | 34 | 29 |
| Terralith namespace | 99 | 34 | 65 |

Two explicit BOP IDs, `highland_moor` and `wooded_wasteland`, have no matching
packaged biome JSON in this installed BOP version. Confirm the loaded registry
before retiring those entries. This comparison excludes Terralith's overrides of
vanilla biomes and does not mistake leaf-block or dimension settings for biomes.

Missing explicit entries are **not missing temperature support**. Cold Sweat's
`WorldHelper.getBiomeTemperatureRange` falls back to the biome's native climate,
and its cave modifier blends underground biome temperatures separately. For
example, Terralith's `cave/ice_caves` has native temperature `1.0`, while
`cave/thermal_caves` has `0.8`. These are useful sampling targets, not proof of the
final temperature a player experiences after all modifiers.

Sample cold gardens, tundra, cold desert, tropical biomes, high peaks, ice caves
and thermal caves at representative times/heights. Introduce narrowly justified
defaults, respecting administrator overrides and Cold Sweat's existing Weather2
support. Tent climate should continue to inherit the same result without applying
the weather or biome contribution twice. Add regional preparation advice to the
existing climate/exploration chapters; do not add dozens of mandatory biome quests.

## P4 — Wildlife offerings and backpack magnets

Sophisticated Core's installed magnet path collects eligible dropped items without
waiting for an ordinary finite pickup delay. Alex's Mobs' item-targeting AI must
reach a dropped item to eat or collect it. This creates a plausible race when a
player drops food within magnet range; no physical reproduction ran in this audit.

Use a short, bounded grace period for intentional feeding/taming offerings near an
eligible animal. Keep ordinary loot collection unchanged and preserve existing
magnet filters. Sophisticated Core exposes an `addMagnetPreventionChecker` hook,
also visible in its [official 1.20.x source](https://raw.githubusercontent.com/P3pp3rF1y/SophisticatedCore/1.20.x/src/main/java/net/p3pp3rf1y/sophisticatedcore/upgrades/magnet/MagnetUpgradeWrapper.java).
The installed Alex's JAR contains neither of that magnet's remote-movement marker
strings; that alone is not a complete runtime conflict test.

Acceptance: crow/raccoon offerings, another dropped-item feeding interaction,
ordinary mob loot, full backpacks, multiple nearby players and both carried and
placed magnets. Avoid permanently excluding all food from magnets or bypassing
animal ownership/taming rules. Explain the behavior in Animals and Backpacks.

## P5 — Aether altar maintenance for expedition gear

Across the scanned root-level data, the 97 `aether:repairing` recipes are all from
Aether itself. There is no repair recipe for Cold Sweat or Alex's equipment, and
1.0.0 does not add one. A small allowlist could cover goat-fur/hoglin clothing and
the Frontier Cap, after checking actual durability and existing repair routes.

Retain native fuel and time costs. The altar's same-item output path copies input
NBT and resets damage; the [official repair recipe implementation](https://raw.githubusercontent.com/The-Aether-Team/The-Aether/1.20.1-develop/src/main/java/com/aetherteam/aether/recipe/recipes/item/AltarRepairRecipe.java)
also provides the recipe format. The installed furnace implementation, rather
than a fresh crafting recipe that recreates the item, is the important path to test.

Acceptance: named and enchanted gear, sewn Cold Sweat insulation, repair cost,
damage and item count, plus actual fuel/time consumption. Start with practical
travel equipment; review powerful boss gear individually. Document both the
relevant equipment page and the existing Aether chapter.

## P6 — Five native accomplishments missing from the journal

The journal generator imports displayed advancements from six mod namespaces;
BOP and RopeBridge are not among them. Add these existing non-root records:

- `biomesoplenty:biomesoplenty/all_biomes` — its native 63-biome achievement.
- `ropebridge:main/craft_bridge_builder` and `craft_ladder_builder`.
- `ropebridge:main/build_bridge` and `build_ladder`.

The two build records use `minecraft:impossible` criteria because native bridge
and ladder handlers explicitly award them after successful construction. They
are usable achievements, not broken criteria. Reference the native advancements
so their actual completion rules remain authoritative.

Keep stable IDs, independent records and existing administrator chapter ownership.
Do not import hidden recipe-unlock advancements as achievements. A later handful
of campsite or railway records could track real actions, but should remain optional
and avoid adding dozens of inventory checklists. Place guidance in Exploration and
Travel, with no dedicated custom-integration chapter.

## P7 — Understandable campsite feedback

The current `/kncraft status` reports global integration state; the guide already
explains saved exterior samples and hearth requirements. An optional, on-demand
campsite readout could show the particular tent's source dimension, whether its
climate is live or saved, and the relevant heating/cooling equipment's native state.

Use existing state to explain warming up, missing fuel or redstone, and whether
the player's position actually receives conditioning. Do not force-load the
outside world to display this information. Surface it quietly through inspection
or a command, with a link to the existing Cold Sweat guide page. Report only states
that the integration can determine reliably; do not guess why a hearth is inactive.

## Useful connections to verify and document before writing more adapters

- **Waterskin refill from a placed backpack tank:** installed Cold Sweat's
  `WaterskinItem.useOn` already looks for a block fluid capability, requires at
  least 250 mB of water and calls its native filling routine. The placed backpack
  exposes that capability. Its normal click opens a GUI, so test sneak-use and
  Forge interaction order, actual draining, insufficient water, two tanks and
  preservation of the item's data. This is strong evidence for an existing route,
  not a verified end-to-end interaction or proof the tank GUI accepts waterskins.
- **Existing combinations already covered in 1.0:** insulated minecart linking,
  Comforts sleep checks, native totem inventory behavior, cooking-oil diesel,
  cotton tent/rope supplies, flight-slot support and thermal-food tooltips do not
  need duplicate implementations. Preserve their guide explanations.
- **Weather and portal sound:** Cold Sweat already accounts for supported Weather2
  conditions. The installed Immersive Portals config already enables cross-portal
  sound. Evaluate actual Sound Physics/voice behavior in a client before designing
  another sound bridge or stacking another weather penalty.

## Remaining acceptance work with the highest payoff

Run one representative multiplayer expedition on a copied world: obtain food and
insulated gear, travel by train, pitch a tent in an extreme biome, condition the
room, use enchanted Curios wings, feed wildlife, repair gear and check the journal.
Include the physical-client combinations of Immersive Portals, Oculus/Embeddium,
Distant Horizons, culling, dynamic lights, first-person animations, Sound Physics
and voice. The 1.0 server tests cannot certify those graphics/input/audio paths.

The library and performance groups were reconsidered as dependencies rather than
given artificial gameplay features. No evidence here justifies changing their
settings or adding blanket compatibility patches. Resolve any reproducible
client issue narrowly, with guide/JEI presentation checked using the actual pack.
