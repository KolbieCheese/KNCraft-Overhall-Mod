# Proposed gameplay integrations

Everything below is a proposal, not deployed functionality. Values are initial tuning candidates. The first batch should reuse existing items and supported configuration/data mechanisms. Do not add a new required client content mod unless it provides a clear gameplay benefit.

## First batch

### Cotton insulation

Confirmed item: `pamhc2crops:cottonitem`. Existing tags include `forge:crops/cotton` and `forge:fiber/cotton`. Cold Sweat's current `item.toml` exposes static and adaptive Sewing Table insulation ingredients. Cotton is not presently listed.

Propose static 1 cold / 0.5 heat insulation as an initial balance candidate. Current references are leather 1/1, wool 1.5/0, goat fur 2/0, hoglin hide 0/2 and rabbit hide 0/1.5. Chameleon molt is adaptive. Preserve a reason to seek specialized materials rather than making an easy crop replace every option. Verify slot consumption, removability, tooltips and existing insulated items. Decide whether the integration uses Cold Sweat data registries or one-time configuration import; never append duplicate entries on each reload.

### Temperature-aware cooking

Confirmed examples:

- `pamhc2crops:hotteaitem`, `pamhc2crops:hotcoffeeitem`, `pamhc2crops:hotnettleteaitem`.
- `pamhc2foodcore:carrotsoupitem`, `pamhc2foodcore:potatosoupitem`, `pamhc2foodcore:stewitem`.
- `pamhc2foodcore:melonsmoothieitem`, `pamhc2foodcore:icecreamitem` and `pamhc2foodcore:applejuiceitem`.

Warm meals/drinks give short warming effects; cold desserts/smoothies give cooling. Ordinary juice could receive a weaker effect. A curated list is preferable to classifying every item whose name happens to contain a keyword. Cold Sweat already supports amount, duration and stack limits; test their exact semantics in 2.4.3 before choosing values. Keep effects bounded, avoid cross-food stacking exploits, and do not turn a bowl of soup into permanent climate immunity. Test consumption at full hunger, bowl/bottle returns, effect reapplication and interactions with waterskins.

### Wildlife insulation

Confirmed materials: `alexsmobs:bear_fur`, `alexsmobs:bison_fur`, `alexsmobs:kangaroo_hide`. The existing `alexsmobs:frontier_cap` uses bear fur, a leather helmet and a raccoon tail.

Give fur a clear cold-protection role and hides modest mixed protection. Add appropriate worn insulation to the Frontier Cap. Check existing Alex's Mobs fur-related item/NBT behavior before adding a second overlapping effect. Preserve dedicated hot-climate and adaptive Cold Sweat gear niches. No new hunting requirement should replace the farming alternative.

### Plant fibers and camping supplies

Pam's already supplies a recipe from two `forge:fiber` ingredients to one string. Vanilla string can already become wool. Nomadic Tents' `nomadictents:tent_canvas` currently consumes two wool blocks. Comforts' ordinary sleeping bags use matching wool. RopeBridge has rope/string recipes that must be considered together.

Add direct plant-fiber routes to canvas and rope; optionally add a sleeping-bag alternative. Set yields by comparing the entire existing conversion chain. Do not accidentally permit cotton -> rope -> string -> cotton duplication. Do not add cotton to the global wool tag, which could change unrelated recipes and thermal behavior. Recipes can output existing items; new cloth variants are optional future content, not required for this integration.

## Follow-up integrations

| Feature | Proposed player experience | Implementation and checks |
|---|---|---|
| Aether thermal resources | Icestone supplies cooling fuel; Ice Ring/Pendant helps in hot regions after returning from the Aether. | `aether:icestone`, `aether:ice_ring`, `aether:ice_pendant`; Cold Sweat Icebox fuel and insulating Curios settings exist. Test Curios registration, multiple accessory stacking and fuel value against ordinary ice. |
| Expedition backpack provisions | Players pack hot or cold meals appropriate to their trip; feeding remains useful with the new meals. | Sophisticated Backpacks already has Feeding/Advanced Feeding upgrades. Test whether the installed consumption path triggers Cold Sweat; hunger-based automatic feeding is not temperature-aware selection. Do not assume waterskins nested inside packs behave like hotbar waterskins. |
| Thematic exploration loot | Village stores contain useful seeds/ingredients; ruins contain provisions and sewing materials; difficult expeditions reward specialized supplies. | Target actual Dungeons Enhanced, Towns and Towers and Structory loot tables. Prefer additive loot modifiers where supported. Check fixed structure inventories and loot resolved on chest opening. Already looted chests should not refill. No existing-world regeneration required. |
| Optional FTB Quests progression | Grow cotton -> sew a lining -> cook provisions -> prepare camp -> visit a demanding biome/dimension. | Installed FTB Quests supports item, biome, structure, location, advancement and dimension tasks. Use optional guidance and modest rewards initially; do not introduce mandatory progression locks without a separate design decision. Verify shared-team progress and reward duplication. |
| Patchouli field guide | Players learn why materials, meals and shelter matter and can find the relevant recipes. | Existing Patchouli is installed. Add practical entries tied to enabled modules, with matching JEI recipes/tooltips. Verify resource distribution; client-visible book/assets may require a client copy or pack override. |
| Climate consistency | Weather, terrain and dimensions feel intentional together. | Cold Sweat already integrates Weather2 and supports biome/dimension temperature rules. Check Terralith/Biomes O' Plenty regions and altitude behavior before adding modifiers. Review Aether's existing +0.7 offset rather than blindly treating every sky biome as cold. |

## Larger custom-code projects

### Campsite-aware tent climate

Use the existing exterior/interior tent link to derive a buffered interior climate from its actual campsite. Shelter moderates exposure; heaters/cooling provide improvement. A tent in a snowy mountain region should not automatically inherit an unrelated default dimension climate.

Prototype handling of moved/packed tents, offline owners, multiple tents, unloaded exterior chunks, broken doors, cross-dimension links and temperature transitions after travel. Do not force-load arbitrary exterior chunks just to compute temperature. Avoid recursive temperature sampling across portals. Preserve the existing acknowledgement barrier and native fallback. Test whether Cold Sweat's hearth/icebox already handles interior rooms before implementing additional effects.

### Temperature-aware backpack consumables

An opt-in controller selects an allowed warming/cooling consumable only when needed. It must conserve inventory, return containers, honor cooldowns, cooperate with the existing hunger feeder, avoid consuming rare ingredients indiscriminately and stop safely when the backpack is removed. Use event-driven or low-frequency work. This is a distinct custom integration, not a promised capability of the current Feeding Upgrade.

## Design priorities

Provide alternate useful paths, not a single mandatory material. Let a farmer, explorer and camper support each other. Favor understandable effects over dozens of invisible bonuses. Keep food, clothing, shelter and rare equipment complementary. Show current effects in tooltips or the guide and let pack maintainers tune values without recompiling.
