"""Build guide references from the isolated server's resolved registry; never edit the play instance.

Run kncraftguidecatalog in the marked test server before using --registry. Recipe JSON is
not copied from other mods: pages refer to recipe IDs resolved by the actual game.
"""
import argparse
from collections import Counter, defaultdict
import json
from pathlib import Path
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
BOOK = RES / 'assets/patchouli/patchouli_books/kncraft_guide/en_us'
NATIVE = {'minecraft:crafting_shaped': 'crafting', 'minecraft:crafting_shapeless': 'crafting',
          'minecraft:smelting': 'smelting', 'minecraft:smoking': 'smoking', 'minecraft:blasting': 'blasting',
          'minecraft:campfire_cooking': 'campfire', 'minecraft:stonecutting': 'stonecutting',
          'minecraft:smithing_transform': 'smithing'}


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes((json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode('utf-8'))


def thermal_group(item):
    """Authoring-time selection, materialized as an explicit, reviewable item allowlist."""
    mod, name = item.split(':')
    if name in {'currypowderitem', 'chilipepperitem', 'chilichocolateitem'}:
        return None
    if any(term in name for term in ('smoothie', 'icecream', 'popsicle', 'sorbet', 'sundae', 'gelato', 'slush')):
        return ('frozen_or_blended', -.2, 1200)
    if any(term in name for term in ('juiceitem', 'yogurt', 'milkshake', 'lemonade', 'icedtea', 'icedcoffee')) or name == 'sweetteaitem':
        return ('chilled_drink_or_yogurt', -.1, 600)
    if 'salad' in name and not any(term in name for term in ('dressing', 'sandwich')):
        return ('chilled_salad', -.1, 600)
    if any(term in name for term in ('soup', 'stew', 'chowder', 'curry', 'casserole', 'potpie')) or name in {
        'ramenitem', 'phoitem', 'chiliitem', 'cottagepieitem', 'shepardspieitem'}:
        return ('hot_meal', .2, 1800)
    if name in {'hotteaitem', 'hotcoffeeitem', 'hotnettleteaitem', 'hotchocolateitem', 'chaiteaitem',
                'coffeeconlecheitem', 'dandelionteaitem', 'earlgreyteaitem', 'rosepetalteaitem'}:
        return ('hot_drink', .2, 1200)
    if name.startswith('cooked_') or any(term in name for term in (
            'pizza', 'lasagna', 'spaghetti', 'macaroniandcheese', 'macandcheese', 'friedrice',
            'steamedrice', 'risotto', 'mashedpotato', 'bakedpotato', 'frenchfries')) or name == 'baked_potato':
        return ('warm_savory_food', .1, 1200)
    return None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--registry', type=Path, required=True)
    parser.add_argument('--instance', type=Path, required=True)
    args = parser.parse_args()
    registry = read(args.registry)
    lang = {}
    # The vanilla language file lives in the development runtime, not the mod directory.
    vanilla = Path.home() / '.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar'
    if vanilla.is_file():
        with zipfile.ZipFile(vanilla) as jar:
            lang.update(json.loads(jar.read('assets/minecraft/lang/en_us.json')))
    for path in sorted((args.instance / 'mods').glob('*.jar')):
        with zipfile.ZipFile(path) as jar:
            for name in jar.namelist():
                if re.fullmatch(r'assets/[^/]+/lang/en_us.json', name):
                    try:
                        lang.update(json.loads(jar.read(name)))
                    except (ValueError, UnicodeError):
                        pass
    config = (ROOT / 'src/main/java/com/beautyinblocks/kncraft/core/ArchitectureConfig.java').read_text()
    legacy = {item: (float(amount), int(duration)) for item, amount, duration in
              re.findall(r'"([a-z0-9_]+:[a-z0-9_]+)\|(-?[0-9.]+)\|(\d+)"', config)}
    foods = {}
    for item, info in registry['items'].items():
        if not info['food']:
            continue
        group = thermal_group(item)
        if item in legacy:
            amount, ticks = legacy[item]
            group = (group[0] if group else 'legacy', amount, ticks)
        if group:
            family, amount, ticks = group
            foods[item] = {'amount': amount, 'duration': ticks, 'group': family, 'legacy': item in legacy}
    write(RES / 'thermal-foods.json', foods)

    manifest = read(ROOT / 'docs/Guide-Integration-Edits.json')
    def save(path, data, reason):
        if path.exists() and read(path) == data:
            return
        write(path, data)
        manifest['files'][path.relative_to(BOOK).as_posix()] = reason
    def label(item):
        info = registry['items'].get(item, {})
        return lang.get(info.get('translation'), item.split(':')[1].replace('_', ' ').title())
    def category(item):
        # FoodProperties alone omits drinks, placeable cake and non-edible cooking ingredients.
        cooking_ingredients = {'aether:skyroot_milk_bucket', 'minecraft:cake', 'minecraft:sugar',
            'pamhc2foodcore:doughitem', 'pamhc2foodcore:flouritem', 'pamhc2foodextended:hotsauceitem',
            'pamhc2foodextended:ketchupitem', 'pamhc2foodextended:mustarditem', 'pamhc2foodextended:relishitem',
            'pamhc2foodextended:sesameoilitem', 'pamhc2foodextended:soysauceitem'}
        edible = registry['items'].get(item, {}).get('food', False) or item in cooking_ingredients
        return 'cookbook' if edible else 'recipes'

    entries = {}
    existing = defaultdict(list)
    represented = set()
    recipe_anchors = {'catalog_' + r['id'].replace(':', '_').replace('/', '_'): r['id'] for r in registry['recipes']}
    for folder in ['recipes', 'cookbook']:
        for path in sorted((BOOK / 'entries' / folder).rglob('*.json')):
            data = read(path)
            icon = data.get('icon', '')
            if not isinstance(icon, str):
                continue
            entries[path] = data
            existing[icon].append(path)
            for page in data['pages']:
                if page.get('anchor') in recipe_anchors:
                    page['kncraft_recipe'] = recipe_anchors[page['anchor']]
                represented.update(page[k] for k in ('recipe', 'recipe2', 'kncraft_recipe') if isinstance(page.get(k), str))

    prior_index = ROOT / 'docs/Guide-Recipe-Index.json'
    index = read(prior_index)['special_recipe_notes'] if prior_index.exists() else []
    skipped = Counter()
    for recipe in registry['recipes']:
        item = recipe.get('result')
        if not item or item == 'minecraft:air':
            skipped[recipe['type']] += 1
            continue
        if recipe['id'] in represented:
            continue
        kind = NATIVE.get(recipe['type'])
        # Environmental conversions and placement bans are not player crafting recipes.
        machine = recipe['type'] in {'aether:freezing', 'aether:enchanting'}
        custom = recipe['crafting'] or recipe['type'] in {'sophisticatedbackpacks:smithing_backpack_upgrade', 'witherstormmod:anvil'}
        if not kind and not machine and not custom:
            skipped[recipe['type']] += 1
            continue
        if existing[item]:
            path = existing[item][0]
            data = entries[path]
        else:
            path = BOOK / 'entries' / category(item) / item.replace(':', '/')
            path = path.with_suffix('.json')
            data = {'name': label(item), 'category': 'patchouli:' + category(item), 'icon': item,
                    'sortnum': 0, 'pages': [], 'extra_recipe_mappings': {item: 0}}
            entries[path] = data
            existing[item].append(path)
        anchor = 'catalog_' + recipe['id'].replace(':', '_').replace('/', '_')
        if kind:
            page = {'type': 'patchouli:' + kind, 'recipe': recipe['id'], 'anchor': anchor,
                    'text': '$(l:patchouli:' + category(item) + ')Index$(/l)'}
        elif machine and recipe.get('ingredients') and recipe['ingredients'][0]:
            freezer = recipe['type'] == 'aether:freezing'
            page = {'type': 'patchouli:kn_cooking', 'heading': 'Aether Freezer' if freezer else 'Aether Altar',
                    'anchor': anchor, 'input': recipe['ingredients'][0][0], 'output': item,
                    'station': 'aether:freezer' if freezer else 'aether:altar',
                    'body': 'Process the shown ingredient with normal machine fuel. Hover the result and use the JEI recipe key for current time, quantity and accepted alternatives.',
                    'navigation': '$(l:patchouli:' + category(item) + ')Index$(/l)'}
        else:
            # Dynamic upgrades preserve item data and cannot safely use a generic 3x3 diagram.
            page = {'type': 'patchouli:spotlight', 'item': item, 'anchor': anchor,
                    'text': 'This recipe uses a special crafting or upgrade operation. Hover this item and press $(k:key.jei.showRecipe) for its current ingredients and instructions. Existing item data follows the original mod rules.'}
        page['kncraft_recipe'] = recipe['id']
        data['pages'].append(page)
        represented.add(recipe['id'])
        index.append({'recipe': recipe['id'], 'result': item, 'entry': path.relative_to(BOOK / 'entries').as_posix()[:-5],
                      'display': 'native' if kind else 'machine' if machine else 'jei_special'})

    keys = read(RES / 'guide-values.json')
    for path, data in entries.items():
        item = data['icon']
        target = category(item) if item in registry['items'] else data['category'].split(':')[1]
        data['category'] = 'patchouli:' + target
        if target == 'recipes':
            data['category'] += '/' + item.split(':')[0]
        if target == 'recipes':
            for page in data['pages']:
                for field in ('text', 'navigation'):
                    if field in page:
                        page[field] = page[field].replace('$(l:patchouli:cookbook)Cook Book$(/l)', '$(l:patchouli:recipes)Recipes$(/l)')
        else:
            for page in data['pages']:
                for field in ('text', 'navigation'):
                    if field in page:
                        page[field] = page[field].replace('$(l:patchouli:recipes)Recipes$(/l)', '$(l:patchouli:cookbook)Cook Book$(/l)')
                        page[field] = page[field].replace('$(l:patchouli:recipes)Index$(/l)', '$(l:patchouli:cookbook)Index$(/l)')
        # Keep entry paths and anchors stable so old chapter links still resolve.
        if item in foods:
            food = foods[item]
            amount, seconds = food['amount'], food['duration'] // 20
            direction = 'Warms' if amount > 0 else 'Cools'
            short = f'Cold Sweat: {direction.lower()} {amount:+g} base temperature for {seconds}s (pack default).'
            detail = f'Pack default: {amount:+g} base temperature for {seconds} seconds. {direction} while active. The latest thermal meal replaces the previous effect. Server settings take precedence.'
            found = False
            for page in data['pages']:
                if page.get('anchor') == 'kncraft_thermal_effect':
                    page.update(type='patchouli:kn_value', body=detail, fallback=detail, reference='food/' + item)
                    found = True
                if page['type'] in {'patchouli:' + v for v in NATIVE.values()}:
                    # One recipe per page leaves room for a short native text summary.
                    nav = '$(l:patchouli:' + target + ')Index$(/l)'
                    page['text'] = short + '$(br)Turn pages for server values.$(br)' + nav
                if page['type'] == 'patchouli:kn_cooking':
                    page['body'] = short + ' Use JEI for current machine ingredients and time.'
            if not found:
                data['pages'].insert(1 if data['pages'] else 0, {'type': 'patchouli:kn_value', 'heading': 'Cold Sweat effect',
                    'body': detail, 'fallback': detail, 'reference': 'food/' + item, 'anchor': 'kncraft_thermal_effect',
                    'navigation': '$(l:patchouli:cookbook)Cook Book$(/l)'})
            keys['food/' + item] = item
        save(path, data, 'Complete resolved recipe catalog, food category and visible temperature summary')

    # Consumables without a fixed recipe (notably suspicious stew) still need an effect page.
    for item, food in foods.items():
        if existing[item]:
            continue
        detail = f"Pack default: {food['amount']:+g} base temperature for {food['duration']//20} seconds. The latest thermal meal replaces the previous effect. Server settings take precedence."
        path = BOOK / 'entries/cookbook' / (item.replace(':', '/') + '.json')
        data = {'name': label(item), 'category': 'patchouli:cookbook', 'icon': item, 'pages': [
            {'type': 'patchouli:spotlight', 'item': item, 'text': detail + '$(br)Use $(k:key.jei.showRecipe) over the item for available recipes.'},
            {'type': 'patchouli:kn_value', 'heading': 'Cold Sweat effect', 'anchor': 'kncraft_thermal_effect',
             'reference': 'food/' + item, 'body': detail, 'fallback': detail, 'navigation': '$(l:patchouli:cookbook)Cook Book$(/l)'}]}
        save(path, data, 'Thermal food reference for a dynamic recipe or non-craftable consumable')
        entries[path] = data
        existing[item].append(path)
        keys['food/' + item] = item
    write(RES / 'guide-values.json', keys)
    mod_names = {'minecraft': 'Minecraft', 'aether': 'The Aether', 'alexsmobs': "Alex's Mobs", 'biomesoplenty': 'Biomes O Plenty',
        'callfromthedepth_': 'Call from the Depth', 'cold_sweat': 'Cold Sweat', 'mcwbridges': "Macaw's Bridges", 'mcwstairs': "Macaw's Stairs",
        'nomadictents': 'Nomadic Tents', 'sophisticatedbackpacks': 'Sophisticated Backpacks', 'sophisticatedcore': 'Sophisticated Core',
        'waystones': 'Waystones', 'weather2': 'Weather2', 'witherstormmod': 'Wither Storm', 'ropebridge': 'Rope Bridge',
        'pamhc2crops': "Pam's Crops", 'pamhc2foodcore': "Pam's Kitchen Tools", 'pamhc2foodextended': "Pam's Ingredients",
        'pamhc2trees': "Pam's Trees", 'comforts': 'Comforts', 'betterminecarts': 'Better Minecarts', 'ironchest': 'Iron Chests',
        'superbarrels': 'Super Barrels', 'ftbquests': 'FTB Quests', 'patchouli': 'Guide Books'}
    nonfood = defaultdict(list)
    for data in entries.values():
        if data['category'].startswith('patchouli:recipes/'):
            nonfood[data['icon'].split(':')[0]].append(data['icon'])
    category_icons = {'minecraft': 'minecraft:crafting_table', 'aether': 'aether:altar', 'cold_sweat': 'cold_sweat:hearth'}
    for path in (BOOK / 'categories/recipes').glob('*.json'):
        relative = path.relative_to(BOOK).as_posix()
        if path.stem not in nonfood and manifest['files'].get(relative) == 'Group non-food recipes by mod under Recipes':
            path.unlink()
            del manifest['files'][relative]
    for mod, icons in sorted(nonfood.items()):
        title = mod_names.get(mod, mod.replace('_', ' ').title())
        save(BOOK / 'categories/recipes' / (mod + '.json'), {'name': title, 'parent': 'patchouli:recipes',
            'description': title + ' equipment, materials and crafting. Food and drink recipes are in Cook Book.', 'icon': category_icons.get(mod, sorted(icons)[0]), 'sortnum': 0},
            'Group non-food recipes by mod under Recipes')
    for chapter, anchor, body in [
        ('food', 'kncraft_thermal_meals', 'Smoothies, frozen desserts, juices and chilled foods cool you. Soups, stews, hot drinks and selected cooked meals warm you. Each affected recipe prints its default strength and duration; turn pages for current server values. These change base temperature, not instant body temperature.'),
        ('cold', 'kncraft_meals', 'Pack defaults: soups and stews warm for 90 seconds; hot drinks for 60. Smoothies and frozen desserts cool for 60; juices, yogurt and salads for 30. Selected cooked meals warm gently for 60. Read each Cook Book recipe for strength and current server values.'),
        ('intro', 'kncraft_server_values', 'Recipes groups non-food crafting by mod. Cook Book holds food and drinks. Thermal recipes print pack defaults below the diagram; turn pages for current server values. Server settings, special item data and conditions can change the effect.'),
    ]:
        path = BOOK / 'entries/chapters' / (chapter + '.json')
        data = read(path)
        for page in data['pages']:
            if page.get('anchor') == anchor:
                page['body'] = body
        save(path, data, 'Expanded thermal meals and clear recipe navigation in existing chapters')
    for name, description in [('recipes', 'Equipment, tools, machines, building materials and other non-food recipes from the pack. Food and drinks are in Cook Book.'),
                              ('cookbook', 'Food, drinks and edible ingredients from across the pack. Affected recipes show Cold Sweat temperature effects; turn pages for alternatives and server values.')]:
        path = BOOK / 'categories' / (name + '.json')
        data = read(path); data['description'] = description
        save(path, data, 'Food and non-food category scope')
    write(ROOT / 'docs/Guide-Integration-Edits.json', manifest)
    declaration = RES / 'data/patchouli/patchouli_books/kncraft_guide/book.json'
    data = read(declaration); data['version'] = 15; write(declaration, data)
    # Coverage is validated independently against this resolved reference, including pre-existing entries.
    write(ROOT / 'docs/Guide-Recipe-Index.json', {'source': 'Isolated Forge 1.20.1 registry with every installed recipe-bearing mod',
        'items': {item: {'food': category(item) == 'cookbook'} for item in sorted(existing)},
        'recipes': [{k: r[k] for k in ('id', 'type', 'result') if k in r} for r in registry['recipes']],
        'special_recipe_notes': index, 'not_a_fixed_player_recipe': dict(sorted(skipped.items()))})
    print('Thermal foods:', len(foods), dict(Counter(f['group'] for f in foods.values())))
    print('Added recipe references:', len(index), 'catalog entries:', len(entries), 'live keys:', len(keys))


if __name__ == '__main__':
    main()
