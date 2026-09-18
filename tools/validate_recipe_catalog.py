"""Check guide categorization and recipe coverage against the resolved Forge registry audit."""
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
BOOK = RES / 'assets/patchouli/patchouli_books/kncraft_guide/en_us'


def validate():
    audit = json.loads((ROOT / 'docs/Guide-Recipe-Index.json').read_text())
    recipes = {r['id']: r for r in audit['recipes']}
    foods = json.loads((RES / 'thermal-foods.json').read_text())
    child_counts = Counter()
    for path in (BOOK / 'categories').rglob('*.json'):
        category = json.loads(path.read_text(encoding='utf-8'))
        if 'parent' in category:
            child_counts[category['parent']] += 1
    assert all(n <= 16 for n in child_counts.values()), 'Category icons overflow the Patchouli page: ' + str(child_counts)
    seen_recipes, seen_foods, categories = set(), set(), Counter()
    for folder in ['recipes', 'cookbook']:
        for path in (BOOK / 'entries' / folder).rglob('*.json'):
            entry = json.loads(path.read_text(encoding='utf-8'))
            item = entry['icon']
            if item not in audit['items']:
                assert item in foods, 'Unclassified recipe result: ' + item
                food = True
            else:
                food = audit['items'][item]['food']
            assert entry['category'].startswith('patchouli:cookbook' if food else 'patchouli:recipes/'), 'Wrong food category: ' + str(path)
            categories['cookbook' if food else 'recipes'] += 1
            if food:
                amount = foods.get(item, {}).get('amount', 0)
                group = 'warm' if amount > 0 else 'cold' if amount < 0 else 'neutral'
                assert entry['category'] == 'patchouli:cookbook/' + group, 'Wrong temperature category: ' + item
                categories[group] += 1
            anchors = [p['anchor'] for p in entry['pages'] if 'anchor' in p]
            assert len(anchors) == len(set(anchors)), 'Duplicate recipe anchor: ' + str(path)
            for page in entry['pages']:
                for key in ['recipe', 'recipe2', 'kncraft_recipe']:
                    if isinstance(page.get(key), str):
                        assert page[key] in recipes, 'Recipe unavailable in the tested pack: ' + page[key]
                        seen_recipes.add(page[key])
                if item in foods and page['type'] in {'patchouli:crafting', 'patchouli:smelting', 'patchouli:smoking', 'patchouli:campfire', 'patchouli:blasting'}:
                    assert 'Cold Sweat:' in page.get('text', '') and 'pack default' in page['text'], 'Thermal summary absent from recipe face: ' + item
            if item in foods:
                effect = foods[item]
                from thermal_complexity import scale_effect
                assert (effect['amount'], effect['duration']) == scale_effect(effect['base_amount'], effect['base_duration'], effect['tier']), 'Invalid recipe tier: ' + item
                pages = [p for p in entry['pages'] if p.get('anchor') == 'kncraft_thermal_effect']
                assert len(pages) == 1, 'Missing or duplicated thermal page: ' + item
                text = f"{effect['amount']:+g} base temperature for {effect['duration']//20} seconds"
                assert text in pages[0]['fallback'] and pages[0]['reference'] == 'food/' + item, 'Stale thermal values: ' + item
                seen_foods.add(item)
            elif food:
                pages = [p for p in entry['pages'] if p.get('anchor') == 'kncraft_thermal_effect']
                assert len(pages) == 1 and 'no temperature effect' in pages[0]['fallback'], 'Neutral effect missing: ' + item
                assert pages[0]['reference'] == 'food/' + item, 'Neutral server reference missing: ' + item
    native = {'minecraft:crafting_shaped', 'minecraft:crafting_shapeless', 'minecraft:smelting', 'minecraft:smoking',
              'minecraft:blasting', 'minecraft:campfire_cooking', 'minecraft:stonecutting', 'minecraft:smithing_transform'}
    expected = {r['id'] for r in recipes.values() if r.get('result') and r['type'] in native}
    assert expected <= seen_recipes, 'Missing native recipes: ' + str(sorted(expected - seen_recipes))
    assert seen_foods == set(foods), 'Thermal food lacks guide entry'
    for item in audit['items']:
        if audit['items'][item]['food'] and ('smoothie' in item or 'soup' in item):
            assert item in foods, 'Soup or smoothie omitted from temperature catalog: ' + item
    print('RECIPE GUIDE:', dict(categories), len(expected), 'native recipes covered;', len(foods), 'thermal foods documented')


if __name__ == '__main__':
    validate()
