# KNCraft integration audit

Reviewed 17 September 2026 against the 102 top-level mod JARs in the installed
KNCraft instance. This is a proposal, not an implementation or a release acceptance
report. No installed mods, settings, quests or worlds were changed for this audit.

The review covers JAR metadata and packaged resources, relevant local configs,
selected installed-version bytecode, the current KNCraft Architecture source, and
upstream documentation. The installed instance is not proof of the live server's
configuration. New behavior below still needs implementation and gameplay tests.
The complete artifact inventory and review disposition are in
[Installed-Mod-Audit.json](Installed-Mod-Audit.json).

## Recommended next scope

The best return is a small compatibility and content expansion: ingredient bridges,
the Elytra Slot enchantment adapter, agricultural rail fuel, selected Aether thermal
equipment, themed loot, and a Field Guide-linked quest path. Add explicit combat
profiles only where the existing fallback gives the wrong result.

| Order | Proposal | Player benefit | Work and confidence |
|---|---|---|---|
| 1 | Cross-mod ingredients and recipe cleanup | Food and resources work together as players expect | Mostly data; several confirmed resource gaps |
| 2 | More Enchantments + Elytra Slot | Flight enchantments recognize the equipped elytra | Java adapter; strong static evidence, reproduce in client first |
| 3 | Pam's cooking oil + Better Minecarts diesel | Farms supply a practical transport network | Small recipe addition; exact ingredient and fuel mechanism confirmed |
| 4 | Aether equipment + Cold Sweat | Dimensional exploration yields useful climate equipment | Small registry/data extension; equipment balance needs tuning |
| 5 | Structure loot and selected village trades | Exploration supplies farming, camping and transport | Data plus trade events; curate per location |
| 6 | FTB Quests + Field Guide | One understandable journey through the pack | Content and packaging; preserve existing server quest progress |
| 7 | Better Combat profiles and companion handling | Special weapons feel intentional; fewer accidental animal hits | Data/config first; special attacks require client tests |
| 8 | Regional ecology and campsite polish | Terrain, gathering and camping tell a consistent story | Targeted coverage review; avoid duplicating built-in support |

## 1. Ingredient bridges and recipe cleanup

**Confirmed:** Pam's uses `forge:egg`; Aether and Alex's Mobs contribute animal eggs
to `forge:eggs`. Across Food Core and Food Extended, 53 recipe JSONs directly use
the singular tag. No installed top-level JAR supplies a bridge between these tags.
Pam's also uses bananas and avocados as egg substitutes. Add selected real animal
eggs to Pam's ingredient path in one direction; do not make fruit a valid animal
egg for other mods. Moa eggs are a useful candidate, with a guide warning that
cooking consumes an egg that could otherwise be hatched.

**Confirmed:** Pam's milk ingredient contains the vanilla bucket but not
`aether:skyroot_milk_bucket`. Its eight-fresh-milk recipe explicitly requires the
vanilla bucket. Add the skyroot bucket as a milk ingredient and a matching fresh
milk recipe, preserving its correct empty-container return.

**Confirmed:** Aether's ordinary `blue_berry` is absent from Pam's berry tags.
Alex's raw moose ribs, kangaroo meat and catfish are absent from Pam's `rawmeats`
ingredient family. Add them only to appropriate general meat/fish/berry recipes;
avoid making moose ribs count as chicken or making magical/enchanted berries
cheap generic ingredients. Pam's already has vegetarian substitutes; retain them.

**Repair before expanding:** the earlier isolated startup reports three Food Core
recipes with missing result items: `caramelcupcakeitem`, `honeymuffinitem` and
`melonpieitem`. The packaged JSONs still name those results. Resolve each against
the actual registered items, then correct or deliberately disable it. Do not invent
replacement items merely to silence a log. Pam's malformed seed loot modifiers
also need a targeted cleanup; the installed crop config intentionally disables
grass/fern seed drops, so a repair must not silently enable them.

Implementation: conditional, additive tags and recipes in the mod, narrow recipe
overrides only for verified errors. Acceptance: JEI alternatives, actual crafting,
bucket/tool remainders, recipe availability with each optional mod absent, and no
resource multiplication through reverse recipes.

## 2. Flight enchantments in Elytra Slot

The installed More Enchantments classes `GlidingProcProcedure`,
`WingingProcProcedure` and `ActionKeyPressedProcedure` directly read
`EquipmentSlot.CHEST`. Gliding also reads Ender Gliding from that slot. The action
key dispatcher checks the chest stack before invoking Ascending, Beat of Wings and
Gust of Wind. Elytra Slot has an equipped-elytra abstraction, but its inspected
integration list does not include More Enchantments.

This is a strong candidate for an actual compatibility defect when a chestplate
occupies the armor slot and enchanted wings occupy Curios. It has not been
reproduced in a physical client during this audit.

Implement a narrow adapter that selects the active equipped elytra for the relevant
enchantment checks. Review the downstream effect procedures as well as the
dispatcher. Do not globally pretend a chestplate is an elytra or apply an effect
twice when both slots contain equipment. Verify movement, damage/durability,
server authority and Do a Barrel Roll together.

Separately, Elytra Crafting currently makes an elytra from eleven phantom membranes
and one stick. This is already an accessible flight route. Document that route and
its enchantments; an Aether-themed alternative recipe could be optional, but I
would not impose a new boss gate on the existing recipe.

## 3. Farming supports the railway

Better Minecarts' installed `bio_diesel_fuel` recipe uses a sunflower and a glass
bottle. Pam's already produces `pamhc2foodcore:cookingoilitem` from vegetables or
nuts. Add a balanced cooking-oil-and-bottle recipe for the existing diesel fuel.
This creates a farm -> processing -> railway loop using existing items and GUIs.

Keep Better Minecarts' existing fuel accounting and empty-bottle behavior. Its
local fuel config defines 200 mB per fuel item and 10 energy per mB. The upstream
[fuel configuration documentation](https://github.com/Xannosz/BetterMinecarts/wiki/Configuration)
also supports custom item fuels, but producing the existing fuel avoids another
consumable and duplicate configuration ownership.

Cold Sweat already supplies an insulated minecart. Better Minecarts links carts
through its `AbstractMinecart` machinery. Verify the existing insulated cart can
join a train before writing a new passenger-car integration. A station guide can
combine insulated passenger travel, ordinary freight carts, Iron Chests/SuperBarrels
at depots, and Sophisticated Backpacks for the last leg of a trip.

Weather2's wind turbine exposes Forge Energy, but the installed electric locomotive
refills its internal power on powered rails. A turbine-to-locomotive charger is a
larger new system, not an existing plug-and-play connection. Defer it. Testing a
placed battery-upgraded backpack beside a turbine is a smaller investigation of
existing capabilities, not a promised new feature.

## 4. Aether thermal equipment and utility

The local Cold Sweat config has no insulating Curios and no Aether-specific fuel.
Its only bundled Aether JSON found in this scan concerns vertical temperature
regions. Good additions are:

- Icestone as a measured icebox/hearth cooling fuel.
- An ice ring or pendant that slows overheating, with a combined cap so multiple
  accessory slots do not erase the survival mechanic.
- Modest insulation for selected ordinary capes, preserving their other effects.
- Aether Freezer recipes for a small number of prepared Pam's desserts, provided
  the input includes the food ingredients and retains the cost of existing recipes.

The Aether Freezer already makes ice, packed ice and blue ice, so it already has an
indirect Cold Sweat supply role. The missing work is direct equipment/fuel tuning
and teaching that connection. Leave the pack's existing Aether temperature offset
unchanged. These additions extend the current opt-in cohesion module.

## 5. Themed exploration rewards and trade

There are concrete loot extension points: Dungeons Enhanced has 51 base loot tables,
Structory has 39 base tables, Towns and Towers has 14, and Abridged has two bridge
chest tables. Towns and Towers' inspected tables use the `kaisyn` namespace;
assuming a `t_and_t` loot namespace would miss them. Structory also has resources
for newer-version overlays, which must not be mistaken for the 1.20.1 base data.

Add small supplementary pools using an explicit table allowlist:

- Kitchens/cellars: Pam's provisions and cooking supplies.
- Farms/village supplies: locally appropriate seeds or small crop bundles.
- Camps/ruins: cotton, rope, insulation materials and modest travel supplies.
- Mining bridges/outposts: rail repair supplies or a small amount of diesel fuel.

Selected farmer/butcher trades can give crops and wildlife products another use.
Use finite stocks and balanced prices; check buy/sell cycles. Do not put dimension
keys, full high-tier equipment, protected Waystones or large backpacks everywhere.
Existing Sophisticated Backpacks chest loot is enabled, so account for it first.
Loot changes affect future generation of unopened loot, not already opened chests.

## 6. A quest path that uses the existing Field Guide

FTB Quests, Teams, Library and XMod Compat are installed. No pack-level quest
chapters were found under `config/ftbquests`; `defaultconfigs/ftbquests` contains
only a client config. This does not establish whether the live server has quests
stored elsewhere.

Build a small optional path around practical milestones: establish a camp, prepare
for heat/cold, grow provisions, connect settlements, explore the Aether, prepare for
the Depths, then undertake late-game encounters. Use inventory/advancement/visit
tasks appropriate to the installed FTB version. Link or clearly reference the
relevant Field Guide entries instead of duplicating their explanations. Reward
supplies or recognition; keep the world open rather than enforcing recipe locks.

FTB describes Quests as a team-based system in its
[official documentation](https://docs.feed-the-beast.com/mod-docs/mods/suite/Quests/).
Those current docs target 1.21, so author against the installed 2001.4.22 format.
Use stable quest IDs and a migration-aware import. Ship quest definitions as a
separate pack override or deliberate installer step; do not assume placing SNBT
inside a mod JAR makes FTB discover it. Existing progress must survive updates.

## 7. Combat and animal interaction

Better Combat's installed fallback already recognizes swords, axes, claymores,
scythes, claws, lances and many other names. Depth's `soulclaymore` and
`dark_abyssscythe` therefore already have plausible fallback matches. Individual
weapon files are useful for tuning reach, handedness and animation, not evidence
that those weapons currently have no support. Better Combat's
[1.20.1 integration documentation](https://raw.githubusercontent.com/ZsoltMolnarrr/BetterCombat/1.20.1/README.md)
supports dedicated data files and explains the fallback system.

Review unusual Aether/Alex/Depth/Wither Storm weapons for lost right-click effects,
special reach, shield interaction and enchantment attack speed. Alex's nunchaku,
for example, deserves behavior inspection before assigning a generic sword combo.
Check More Enchantments' Furor alongside Better Combat timing. Do not turn a
weapon-by-weapon compatibility pass into blanket damage inflation.

The local Better Combat config treats passives as `HOSTILE` for cleave selection;
players and villagers have explicit `NEUTRAL` entries. An optional companion-aware
policy could prevent accidental area hits on owned/tamed creatures. Avoid changing
an entire hostile species to friendly just because some individuals can be tamed.

Pam's bananas already reach Alex's monkey-taming tags, and Pam's seeds/rice already
have several Alex's tag consumers. Do not reimplement those. A useful smaller
addition is selected fresh berries as appropriate wildlife food, keeping ordinary
feeding, breeding and rare taming ingredients separate.

## 8. Regional ecology, building and camps

Cold Sweat already has 97 explicit biome temperature entries across vanilla, BOP
and Terralith. Alex's spawn configs already include many BOP/Terralith biomes.
Pam's gardens and trees use biome tags; Abridged also uses common biome tags.
These are existing connections, not blank systems needing wholesale replacement.

Audit uncovered biomes against their intended climate, crop access and wildlife.
In particular, Tectonic's terrain height can make Cold Sweat altitude effects more
noticeable. A light touch is preferable to making every mountain lethally cold or
adding another Weather2 temperature penalty. Fix only demonstrated tag/coverage
gaps; worldgen additions generally require new chunks.

FallingTree currently has whole-tree breaking enabled, a 100-log limit, leaf
breaking enabled, and sneak-to-disable. Pam's fruit has right-click harvest enabled.
Teach orchard harvesting and inspect giant BOP/Aether trees against these limits.
Do not globally raise the scan cap or change every fruit block into a tree leaf.

Macaw's installed bridge/stair recipes cover their existing materials, with no
Skyroot/BOP-named variants found. Matching Skyroot and selected BOP timber variants
would be attractive but requires real blocks, models and recipes, or a compatible
addon; a recipe alone cannot give an oak bridge a new wood appearance. Lower
priority than ingredient and equipment compatibility.

Comforts already separates sleeping bags and hammocks by time of day, and its
config can add effects after sleeping. First verify Cold Sweat's sleep checks in
these beds and in tents. A campsite climate inherited from the tent's exterior is
a distinctive future feature, but it needs persisted entrance context, sensible
behavior when the exterior chunk is unloaded, and shelter mitigation. Keep it
separate from the existing tent synchronization fixes.

## Later features and compatibility work

- **Temperature-aware feeding:** Sophisticated's inspected feeding path already
  calls the food-finish event used by thermal meals. Teach filters now. An optional
  temperature-aware selector can avoid eating cooling food during hypothermia,
  but must honor hunger, cooldowns, filters and container returns. The earlier
  runtime check showed Pam's hot tea cannot be consumed at full hunger.
- **Evacuation and storage:** verify Carry On with tent doors, multiblock climate
  appliances, placed backpacks and storage contents. Carry On already blacklists
  Waystones and recognizes immovable/relocation tags; do not duplicate that policy
  or claim it is currently bypassed. Add exclusions only for confirmed unsafe moves.
- **Recovery rules:** document Inventory Totem's actual inventory scope and test
  death/recovery with backpacks, Curios, tent travel and the Depths. Do not assume a
  totem deep inside a backpack is eligible, or replace the native death system.
- **Repair and gear maintenance:** Easy Anvils already removes the too-expensive
  limit and fixes prior-work penalties. JRFTL already supplies a rotten-flesh-to-
  leather route. Guide these alternatives before adding duplicate salvage recipes.
- **Presentation compatibility:** Immersive Portals, Distant Horizons, Oculus,
  Embeddium, Euphoria, culling mods and first-person/animation mods need a real-client
  test matrix. Simple Voice Chat and Sound Physics need portal/tent sound checks.
  Do not promise that remote-dimension sound or voice travels through portals.
- **Performance:** profile the full pack before another optimization patch or mod
  removal. Installed filenames alone do not establish conflicts. The apparently
  Fabric-named API JAR identifies itself as Forgified Fabric API; Embeddium also
  advertises a Rubidium compatibility identity. These are not by themselves errors.

## Scope boundaries

Existing KNCraft work already covers portal conversion, Depth placement, tent
synchronization, three pinned performance fixes, encounter scaling, protected
Waystone data, optional cotton/wildlife insulation, nine thermal foods, cotton
canvas/rope recipes, and the preserved Field Guide. They are not new discoveries.

Prefer conditional data and supported APIs. Keep balance changes independently
configurable and preserve administrator overrides. The best first expansion does
not require new dimensions, mandatory hunting, rewritten weather, broad global
item aliases, progression locks, a new power network, or automatic world regeneration.
Those changes would add substantially more design and migration work than the
confirmed compatibility gaps above.
