# Thermal food recipe tiers

The original Cook Book contains three child categories: Warm Food (90 entries), Cold
Food (188) and Neutral Food (924). These are pack-default classifications. Every item
also has a live server reference page, so administrator/native Cold Sweat overrides
remain visible. Category membership itself is static and does not move during play.

Only the curated 278 thermal foods receive bonuses. A complicated neutral dish remains
neutral. Fixed serving-temperature effects do not simulate cooling in storage.

| Tier | Recipe score | Bonus magnitude | Bonus time |
| --- | --- | --- | --- |
| Simple | 0–3 | None | None |
| Prepared | 4–5 | 0.05 | 15 seconds |
| Elaborate | 6+ | 0.10 | 30 seconds |

The score counts distinct consumed ingredient groups, excluding kitchen tools, containers,
water, ice/snow and sticks. Repeated slots count once; tag alternatives do not add points.
Up to two prepared edible components add one point each when even their simplest known
recipe needs at least two distinct ingredients. Only one ingredient layer is inspected;
recipe cycles cannot recurse. When there are alternative recipes or ingredients, the
least complex route determines the tier. Recipes without fixed results use the simple tier.

The result is a conservative preparation proxy, not a rarity or precise crafting-cost
calculation. It does not multiply by output count or assign extra value to expensive tools.
All routes to the same item share the same effect, including loot and machine alternatives.
The catalog records the selected recipe, score and tier for review. Runtime only reads the
explicit generated values; recipe graphs are not scanned during gameplay.

Current distribution: 213 simple, 42 prepared and 23 elaborate thermal foods. Examples:

| Food | Tier | Base-temperature effect | Duration |
| --- | --- | --- | --- |
| Carrot Soup | Simple | +0.20 | 90 seconds |
| Rabbit Stew | Prepared | +0.25 | 105 seconds |
| Deluxe Chicken Curry | Elaborate | +0.30 | 120 seconds |
| Melon Smoothie | Simple | -0.20 | 60 seconds |
| Caramel Ice Cream | Prepared | -0.25 | 75 seconds |
| Three Bean Salad | Elaborate | -0.20 | 60 seconds |

Bonuses increase magnitude while preserving direction and cap duration at 120 seconds.
Current catalog magnitudes remain at or below 0.30. The newest managed thermal meal
replaces the previous one. Neutral food neither adds an effect nor cancels an existing
meal. Native Cold Sweat definitions and explicit administrator rows retain precedence.
The original nine administrator-configurable defaults are unchanged.

Regenerate with `tools/update_recipe_catalog.py` using the isolated Forge registry export.
`tools/test_thermal_complexity.py` covers repeats, alternatives, cycles, bonus direction
and duration bounds. The artifact validator checks category membership, exact displayed
values and complete recipe coverage. The isolated runtime tests consume foods across
tiers, assert duration/replacement behavior, and verify neutral food and overrides.
