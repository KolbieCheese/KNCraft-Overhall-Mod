"""Generate a stable, reward-free journal from audited installed advancement/structure IDs."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
NATIVE = [
    ('aether', 'aether-*', 'Aether', 'aether'),
    ('callfromthedepth_', 'callfrom*', 'The Depths', 'depths'),
    ('witherstormmod', 'witherstormmod*', 'Wither Storm', 'wither'),
    ('alexsmobs', 'alexsmobs*', 'Wildlife', 'animals'),
    ('cold_sweat', 'ColdSweat*', 'Climate & Shelter', 'cold'),
    ('dungeons_enhanced', 'dungeons_enhanced*', 'Explorer Milestones', 'travel'),
    ('biomesoplenty', 'BiomesOPlenty*', "Biomes O' Plenty", 'explore'),
    ('ropebridge', 'RopeBridge*', 'RopeBridge', 'travel'),
]
STRUCTURES = [
    ('dungeons_enhanced', 'dungeons_enhanced*', 'Dungeons Enhanced'),
    ('structory', 'Structory*', 'Structory'),
    ('t_and_t', 'Towns*', 'Towns & Towers'),
    ('abridged', 'abridged*', 'Bridges'),
    ('callfromthedepth_', 'callfrom*', 'Depths Landmarks'),
]

def stable(key):
    return '4' + hashlib.sha256(('kncraft:journal/' + key).encode()).hexdigest()[:15].upper()

def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes((json.dumps(value, ensure_ascii=False, indent=2) + '\n').encode('utf-8'))

def advancement(mod, name, criteria):
    pack = RES / 'packs' / ('journal_' + mod)
    save(pack / 'pack.mcmeta', {'pack': {'pack_format': 15, 'description': 'KNCraft accomplishment detection: ' + mod}})
    save(pack / f'data/kncraft/advancements/journal/{mod}/{name}.json', {'criteria': criteria})
    return f'kncraft:journal/{mod}/{name}'

def quest(key, title, description, advancement_id, chapter, number, icon='minecraft:book'):
    guide = {'Travel & Exploration': 'explore', 'Combat & Exploration': 'explore', 'Travel & Transport': 'travel', "Pam's HarvestCraft": 'food'}.get(chapter, chapter)
    chapter = {'aether': 'The Aether', 'depths': 'Call from the Depths', 'wither': 'Wither Storm', 'animals': 'Animals & Companions', 'cold': 'Cold Sweat', 'travel': 'Travel & Logistics', 'food': "Pam's HarvestCraft", 'explore': 'Exploration'}.get(chapter, chapter)
    return {'id': stable('quest/' + key), 'title': title, 'icon': icon, 'guide_page': 'kncraft/chapters/' + guide,
            'x': float(number % 7 * 2), 'y': float(number // 7 * 2),
            'description': [description, '', f'KNCraft Guide Book: {chapter}. Explore at your own pace; this record grants no reward and unlocks no recipes.'],
            'dependencies': [], 'rewards': [], 'disable_toast': True,
            'tasks': [{'id': stable('task/' + key), 'type': 'advancement', 'advancement': advancement_id, 'criterion': ''}]}

def chapter(name, title, quests, mods, catalog, index):
    filename = 'kncraft_' + name + '.snbt'
    save(RES / 'journal' / filename, {'id': stable('chapter/' + name), 'filename': 'kncraft_' + name,
         'title': title, 'icon': 'minecraft:writable_book', 'order_index': len(catalog) + 100,
         'subtitle': ['An accomplishment journal. No required order, item turn-ins, or rewards.'],
         'default_hide_dependency_lines': True, 'quests': quests})
    catalog[filename] = mods
    index.extend({'chapter': filename, 'quest': q['id'], 'title': q['title'], 'advancement': q['tasks'][0]['advancement']} for q in quests)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--instance', required=True, type=Path)
    args = parser.parse_args()
    catalog, index = {}, []
    for mod, pattern, title, guide in NATIVE:
        with zipfile.ZipFile(next((args.instance / 'mods').glob(pattern))) as jar:
            languages = {}
            for path in jar.namelist():
                if path.startswith('assets/') and path.endswith('/lang/en_us.json'):
                    languages.update(json.loads(jar.read(path)))
            quests = []
            for path in sorted(jar.namelist()):
                if not path.startswith('data/') or '/advancements/' not in path or not path.endswith('.json'): continue
                try: data = json.loads(jar.read(path))
                except (ValueError, UnicodeDecodeError): continue
                if 'display' not in data or path.endswith('/root.json'): continue
                display = data['display']
                def text(value):
                    return languages.get(value.get('translate'), value.get('text', '')) if isinstance(value, dict) else str(value)
                label = text(display.get('title', ''))
                if not label: continue
                parts = path.split('/')
                id = parts[1] + ':' + '/'.join(parts[3:])[:-5]
                quests.append(quest('native/' + id, label, text(display.get('description', 'Complete the native advancement.')), id, guide, len(quests), display.get('icon', {}).get('item', 'minecraft:book')))
            chapter(mod, title, quests, [mod], catalog, index)
    for mod, pattern, title in STRUCTURES:
        with zipfile.ZipFile(next((args.instance / 'mods').glob(pattern))) as jar:
            quests = []
            for path in sorted(jar.namelist()):
                if not path.startswith('data/') or '/worldgen/structure/' not in path or '/tags/' in path or not path.endswith('.json'): continue
                parts = path.split('/')
                structure = parts[1] + ':' + '/'.join(parts[4:])[:-5]
                name = '/'.join(parts[4:])[:-5]
                id = advancement(mod, 'structures/' + name, {'visit': {'trigger': 'minecraft:location', 'conditions': {'player': [{'condition': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'location': {'structure': structure}}}]}}})
                label = name.split('/')[-1].replace('_', ' ').title()
                quests.append(quest('structure/' + structure, label, 'Enter this landmark to record your discovery. Previously visited places can be recorded by visiting again.', id, 'Travel & Exploration', len(quests), 'minecraft:filled_map'))
            chapter(mod + '_landmarks', title + ' Landmarks' if mod != 'callfromthedepth_' else title, quests, [mod], catalog, index)
    quests = []
    for entity, label in [('minecraft:ender_dragon', 'Defeat the Ender Dragon'), ('minecraft:wither', 'Defeat the Wither'), ('minecraft:elder_guardian', 'Defeat an Elder Guardian')]:
        id = advancement('minecraft', 'bosses/' + entity.split(':')[1], {'defeat': {'trigger': 'minecraft:player_killed_entity', 'conditions': {'entity': {'type': entity}}}})
        quests.append(quest(entity, label, 'Land the defeating blow. This record starts tracking when KNCraft is installed.', id, 'Combat & Exploration', len(quests), 'minecraft:diamond_sword'))
    chapter('bosses', 'Vanilla Bosses', quests, [], catalog, index)
    for mod, entries, title, guide in [
        ('pamhc2crops', [('cottonitem', 'Grow a fiber supply'), ('tealeafitem', 'Harvest tea'), ('riceitem', 'Harvest rice')], 'Farming', "Pam's HarvestCraft"),
        ('pamhc2foodcore', [('potitem', 'Equip a kitchen'), ('stewitem', 'Prepare warming food'), ('icecreamitem', 'Prepare cooling food')], 'Cooking', "Pam's HarvestCraft"),
        ('betterminecarts', [('bio_diesel_fuel', 'Stock railway fuel')], 'Railway Supplies', 'Travel & Transport'),
    ]:
        quests = []
        for item, label in entries:
            full_id = mod + ':' + item
            id = advancement(mod, 'supplies/' + item, {'obtain': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': [full_id]}]}}})
            quests.append(quest('supplies/' + full_id, label, 'Have this supply in your ordinary inventory. The item is not consumed.', id, guide, len(quests), full_id))
        chapter(mod + '_supplies', title, quests, [mod], catalog, index)
    save(RES / 'journal/catalog.json', catalog)
    save(ROOT / 'docs/Journal-Index.json', {'format': 13, 'stable_id_namespace': 'kncraft:journal/', 'quests': index})
    print(f'Generated {len(catalog)} chapters and {len(index)} independent accomplishment records')

if __name__ == '__main__': main()
