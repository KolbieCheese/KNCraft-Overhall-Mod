"""Generate narrowly scoped follow-up recipes; upstream items, fuel and processing remain native."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / 'src/main/resources/packs/altar_repairs'
GEAR = [('cold_sweat:' + material + '_' + slot, 700 if material == 'goat_fur' else 1000)
        for material in ('goat_fur', 'hoglin') for slot in ('helmet', 'chestplate', 'leggings', 'boots')]
GEAR.append(('alexsmobs:frontier_cap', 700))

def main():
    PACK.mkdir(parents=True, exist_ok=True)
    (PACK / 'pack.mcmeta').write_bytes((json.dumps({'pack': {'pack_format': 15, 'description': 'KNCraft expedition equipment altar repairs'}}, indent=2) + '\n').encode('utf-8'))
    directory = PACK / 'data/kncraft/recipes'
    directory.mkdir(parents=True, exist_ok=True)
    for item, ticks in GEAR:
        mod, name = item.split(':')
        recipe = {'type': 'aether:repairing', 'conditions': [{'type': 'forge:mod_loaded', 'modid': mod}],
                  'ingredient': {'item': item}, 'repairTime': ticks}
        (directory / (name + '_altar_repair.json')).write_bytes((json.dumps(recipe, indent=2) + '\n').encode('utf-8'))
    print('Generated', len(GEAR), 'conditional altar recipes')

if __name__ == '__main__': main()
