"""Authoring-time recipe tiers. Runtime consumes the resulting explicit item catalog."""
from collections import defaultdict

UTILITIES = {
    'minecraft:bowl', 'minecraft:glass_bottle', 'minecraft:bucket',
    'minecraft:water_bucket', 'minecraft:ice', 'minecraft:packed_ice',
    'minecraft:blue_ice', 'minecraft:snowball', 'minecraft:stick',
    'pamhc2foodcore:freshwateritem', 'aether:skyroot_water_bucket',
}
TOOLS = {'bakewareitem', 'cuttingboarditem', 'juiceritem', 'mixingbowlitem',
         'mortarandpestleitem', 'potitem', 'rolleritem', 'saucepanitem', 'skilletitem'}


def complexity_catalog(registry):
    recipes = defaultdict(list)
    for recipe in registry['recipes']:
        if recipe.get('result') and (recipe['crafting'] or recipe['type'] in {
            'minecraft:smelting', 'minecraft:smoking', 'minecraft:campfire_cooking',
            'aether:freezing', 'aether:enchanting'}):
            recipes[recipe['result']].append(recipe)

    def ingredients(recipe):
        # Sets prevent six identical beetroots or large ingredient tags earning a bonus.
        groups = set()
        for choices in recipe['ingredients']:
            if not choices:
                continue
            if any(c in UTILITIES or (c.startswith('pamhc2foodcore:') and c.split(':')[1] in TOOLS) for c in choices):
                continue
            groups.add(tuple(sorted(set(choices))))
        return groups

    def prepared(item):
        # Only one ingredient layer: bounded, deterministic, immune to recipe cycles.
        options = recipes.get(item, [])
        return registry['items'].get(item, {}).get('food', False) and options and min(len(ingredients(r)) for r in options) >= 2

    result = {}
    for item, options in recipes.items():
        candidates = []
        for recipe in options:
            groups = ingredients(recipe)
            steps = min(2, sum(all(prepared(c) for c in choices) for choices in groups))
            score = len(groups) + steps
            candidates.append((score, recipe['id'], len(groups), steps))
        score, recipe, distinct, steps = min(candidates)
        tier = 'elaborate' if score >= 6 else 'prepared' if score >= 4 else 'simple'
        result[item] = {'tier': tier, 'score': score, 'recipe': recipe,
                        'ingredients': distinct, 'prepared_components': steps}
    return result


def scale_effect(amount, ticks, tier):
    bonus = {'simple': 0, 'prepared': 1, 'elaborate': 2}[tier]
    return round(amount + (1 if amount > 0 else -1) * .05 * bonus, 2), min(2400, ticks + 300 * bonus)
