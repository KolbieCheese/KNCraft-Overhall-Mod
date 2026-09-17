"""Author the small, additive KNCraft data packs (no installed files are modified)."""
import json
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / 'src/main/resources'

def write(pack, path, value):
    root = RES / 'packs' / pack
    root.mkdir(parents=True, exist_ok=True)
    (root / 'pack.mcmeta').write_text(json.dumps({'pack': {'pack_format': 15, 'description': 'KNCraft ' + pack}}, indent=2) + '\n')
    target = root / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes((json.dumps(value, indent=2) + '\n').encode('utf-8'))

def tag(pack, id, values):
    namespace, path = id.split(':')
    write(pack, f'data/{namespace}/tags/items/{path}.json', {'replace': False, 'values': [{'id': value, 'required': False} for value in values]})

def recipe(pack, name, ingredients, result, mods, count=1):
    write(pack, f'data/kncraft/recipes/{name}.json', {
        'type': 'minecraft:crafting_shapeless',
        'conditions': [{'type': 'forge:mod_loaded', 'modid': mod} for mod in mods],
        'ingredients': [{'item': value} for value in ingredients],
        'result': {'item': result, 'count': count}})

def main():
    # Deliberately one-way: fruit-based egg substitutes never enter forge:eggs.
    tag('ingredients', 'forge:egg', ['#aether:moa_eggs'] + ['alexsmobs:' + x + '_egg' for x in ['emu', 'crocodile', 'platypus', 'terrapin', 'caiman']])
    tag('ingredients', 'forge:milk/milk', ['aether:skyroot_milk_bucket'])
    tag('ingredients', 'forge:berries', ['aether:blue_berry'])
    tag('ingredients', 'forge:berries/blueberry', ['aether:blue_berry'])
    tag('ingredients', 'forge:crops/blueberry', ['aether:blue_berry'])
    tag('ingredients', 'forge:fruits/blueberry', ['aether:blue_berry'])
    tag('ingredients', 'forge:fruits/blueberries', ['aether:blue_berry'])
    tag('ingredients', 'forge:rawmeats', ['alexsmobs:moose_ribs', 'alexsmobs:kangaroo_meat', 'alexsmobs:raw_catfish'])
    for family in ['rawfish', 'fishes']:
        tag('ingredients', 'forge:' + family, ['alexsmobs:raw_catfish'])
    tag('ingredients', 'forge:cookedfish', ['alexsmobs:cooked_catfish'])
    doors = ['nomadictents:' + size + '_' + shape + '_door' for size in ['tiny', 'small', 'medium', 'large', 'giant', 'mega'] for shape in ['yurt', 'tepee', 'bedouin', 'indlu', 'shamiyana']]
    write('camp_safety', 'data/carryon/tags/blocks/block_blacklist.json', {'replace': False, 'values': [
        {'id': id, 'required': False} for id in doors + ['nomadictents:door_frame', 'cold_sweat:hearth_bottom', 'cold_sweat:hearth_top']]})
    # Pam's windy-garden modifier requests an absent Forge savanna tag. Reuse the
    # terrain mods' own classifications rather than guessing individual climates.
    write('ecology', 'data/forge/tags/worldgen/biome/is_savanna.json', {'replace': False, 'values': [
        {'id': '#minecraft:is_savanna', 'required': False}, {'id': '#c:savanna', 'required': False}]})
    recipe('ingredients', 'skyroot_fresh_milk', ['aether:skyroot_milk_bucket'], 'pamhc2foodcore:freshmilkitem', ['aether', 'pamhc2foodcore'], 8)
    recipe('rail_fuel', 'cooking_oil_diesel', ['pamhc2foodcore:cookingoilitem'] * 2 + ['minecraft:glass_bottle'], 'betterminecarts:bio_diesel_fuel', ['pamhc2foodcore', 'betterminecarts'])
    # Prepared juice retains the fruit/juicer step; the Freezer and its native fuel
    # replace only the ice and reusable mixing bowl of the crafting recipe.
    for fruit in ['apple', 'melon', 'sweetberry']:
        write('aether_freezer', f'data/kncraft/recipes/{fruit}_smoothie_freezing.json', {
            'type': 'aether:freezing', 'category': 'freezable_misc', 'cookingtime': 200, 'experience': 0.0,
            'conditions': [{'type': 'forge:mod_loaded', 'modid': mod} for mod in ['aether', 'pamhc2foodcore']],
            'ingredient': {'item': f'pamhc2foodcore:{fruit}juiceitem'}, 'result': f'pamhc2foodcore:{fruit}smoothieitem'})
    for name in ['caramelcupcakeitem_x4', 'honeymuffinitem', 'melonpieitem']:
        write('recipe_repairs', f'data/pamhc2foodcore/recipes/{name}.json', {
            'type': 'minecraft:crafting_shapeless', 'conditions': [{'type': 'forge:false'}],
            'ingredients': [{'item': 'minecraft:barrier'}], 'result': {'item': 'minecraft:barrier'}})
    # The crop JAR references these missing modifiers. Preserve the pack's disabled seed drops.
    for name in ['fern_drops', 'grass_drops', 'tall_grass_drops']:
        write('recipe_repairs', f'data/pamhc2crops/loot_modifiers/{name}.json', {'type': 'kncraft:supplies', 'profile': 'none', 'conditions': []})
    berries = ['pamhc2crops:' + x + 'item' for x in ['blueberry', 'blackberry', 'raspberry', 'strawberry']]
    for animal in ['crow', 'raccoon']:
        tag('wildlife_food', f'alexsmobs:{animal}_foodstuffs', berries)
    # Better Combat 1.9 does not read Forge ENTITY_REACH. Preserve the lance's
    # survival reach (3 base + 3.5 native bonus) in its own hitbox data.
    write('combat', 'data/aether/weapon_attributes/valkyrie_lance.json', {'parent': 'bettercombat:lance', 'attributes': {'attack_range': 6.5}})
    profiles = {
        'pantry': ['dungeons_enhanced:chests/black_citadel/kitchen', 'dungeons_enhanced:chests/castle/kitchen', 'dungeons_enhanced:chests/castle/cellar', 'dungeons_enhanced:chests/pillager_camp/kitchen', 'dungeons_enhanced:chests/tree_house/kitchen', 'dungeons_enhanced:chests/ice_pit/food', 'kaisyn:outpost/common/food', 'kaisyn:village/exclusives/village_mediterranean_house'],
        'farm': ['dungeons_enhanced:chests/hay_storage', 'dungeons_enhanced:chests/ice_pit/garden', 'structory:mood/farmer', 'structory:outcast/farm_ruin'],
        'camp': ['dungeons_enhanced:chests/pillager_camp/general', 'dungeons_enhanced:chests/ruined/house', 'structory:mood/taiga', 'structory:mood/snowy', 'structory:outcast/settlement'],
        'rail': ['dungeons_enhanced:chests/miners_house', 'structory:mood/miner', 'structory:outcast/generic/miner', 'structory:outcast/mine/loot', 'abridged:chests/badlands_mining'],
    }
    entries = []
    for profile, tables in profiles.items():
        entries.append('kncraft:' + profile)
        write('exploration', f'data/kncraft/loot_modifiers/{profile}.json', {
            'type': 'kncraft:supplies', 'profile': profile,
            'conditions': [{'condition': 'minecraft:any_of', 'terms': [{'condition': 'forge:loot_table_id', 'loot_table_id': table} for table in tables]}]})
    write('exploration', 'data/forge/loot_modifiers/global_loot_modifiers.json', {'replace': False, 'entries': entries})

if __name__ == '__main__':
    main()
