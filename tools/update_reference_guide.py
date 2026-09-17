"""Finish approved guide edits; retain explicit before/after provenance for original pages."""
import copy
import hashlib
import json
import re
from update_integrated_guide import BOOK, ROOT, RES, read, save, page, insert, edited


def digest(value):
    return hashlib.sha256(json.dumps(value, sort_keys=True, separators=(',', ':')).encode()).hexdigest()


def main():
    manifest = ROOT / 'docs/Guide-Editorial-Edits.json'
    edits = read(manifest) if manifest.exists() else {'reason': 'User-approved finishing pass: real BlueMap photo, player-facing instructions, complete short captions.', 'changes': []}
    def record(chapter, before, after):
        entry = {'chapter': chapter, 'before': before, 'after': after}
        if entry not in edits['changes']: edits['changes'].append(entry)
    replacements = {
        'shot_symbiont': 'Watch for the weakened posture, then look behind the Symbiont for its exposed purple weak point. Coordinate your attack window with nearby players. A boss bar shows remaining health; it does not by itself establish maximum-health scaling.',
        'shot_bowels': 'Look for the open entrance above the main head. Identify the whole opening before committing to an approach. Keep your group informed of its position; reaching the entrance does not make the approach safe from attacks.',
        'shot_weather': 'Place a Deflector as part of your shelter plan, and use the Weather2 instruments described in this chapter to watch changing conditions. A device does not replace a planned route back to shelter.',
        'shot_depths': 'Before activating the gateway, check your supplies and mark its position. On arrival, light the immediate footing and locate the return opening before exploring. Keep a clear route back to the gateway.',
        'shot_tent': 'A tent keeps its interior when packed and pitched again. Check the packed item\'s Tent ID so you move the same camp. Keep the door clear and inspect the new campsite climate after moving; furniture persistence does not guarantee a comfortable temperature.',
        'shot_food': 'Keep reusable tools near a supply of repeatable ingredients. Look up a final meal in JEI, then follow each crafted ingredient back to its recipe. The Cook Book lists thermal effects beside the affected meals, so your kitchen can support both hunger and climate.',
    }
    for path in sorted((BOOK / 'entries/chapters').glob('*.json')):
        data = read(path); pages = data['pages']; i = 0
        while i < len(pages):
            old = pages[i]; anchor = old.get('anchor', '')
            if anchor.startswith('shot_') and 'SCREENSHOT NEEDED' in old.get('body', ''):
                end = i + 1
                while end < len(pages) and not pages[end].get('anchor') and pages[end].get('heading') == old.get('heading'): end += 1
                before = copy.deepcopy(pages[i:end]); new = copy.deepcopy(old)
                if anchor == 'shot_map':
                    new.update(type='patchouli:kn_map', picture='kncraft:textures/guide/capture_bluemap_three_storms.jpg', credit='KNCraft', body='Three Wither Storm markers appear near the center of this BlueMap example. Check the live map before traveling; positions change.')
                    detail = copy.deepcopy(new)
                    detail.update(type='patchouli:kn_map_detail', heading='The three markers', anchor='kncraft_map_markers', body='Three markers in the same map. Check live positions before choosing a route.')
                    after = [new, detail]
                else:
                    new['body'] = replacements[anchor]; after = [new]
                record(path.stem, before, after); pages[i:end] = after; i += len(after); continue
            if i > 0 and not anchor and 0 < len(old.get('body', '')) < 70 and pages[i-1]['type'] in ('patchouli:kn_image', 'patchouli:kn_text'):
                prior = pages[i-1]; combined = prior.get('body', '') + ' ' + old['body']
                limit = 150 if prior['type'] == 'patchouli:kn_image' else 270
                if len(re.sub(r'\$\([^)]*\)', '', combined)) <= limit:
                    before = copy.deepcopy(pages[i-1:i+1]); new = copy.deepcopy(prior); new['body'] = combined
                    record(path.stem, before, [new]); pages[i-1:i+1] = [new]; continue
            i += 1
        save(path, data, 'Approved editorial finishing; original-page changes recorded in Guide-Editorial-Edits.json')
    manifest.write_text(json.dumps(edits, indent=2) + '\n', encoding='utf-8')

    insert('intro', 'controls', [
        ('guide', 'Your first Field Guide', 'You receive this guide once when joining the world. A carried copy counts; a full inventory delays delivery until space is free. Death and reconnecting do not grant another automatically. Lost it? Use /guide for a replacement.'),
        ('recover_guide', 'Recover your Field Guide', 'Use /guide or /kncraft guide to put this book in an empty inventory slot. Every player can use it. A carried copy, including your offhand, prevents duplicates. If your inventory is full, free a slot and retry. Crafting a book with one paper also still works.'),
        ('journal_links', 'From journal to chapter', 'Open a journal record, then select Open in Guide to jump to the relevant Field Guide chapter. All records stay independent, without rewards or recipe locks. Read at your own pace and return to the journal when ready.'),
        ('server_values', 'Values from your server', 'Thermal-food and selected insulation pages request current server definitions while open. They label pack defaults while waiting. Values describe a plain item under your current conditions; item data and other modifiers may change the result.'),
    ])
    for chapter, section in [('food', 'meal_plan'), ('travel', 'backpack_upgrades')]:
        insert(chapter, section, [
            ('reserved_food', 'Save food for deliberate use', 'Automatic backpack feeding reserves Golden Apple Stew and enchanted golden apples by default. You can still eat them yourself. Use /kncraft feeding reserved false to allow automatic use, or true to restore protection. The server controls the reserve list.'),
        ])

    # These custom components refresh from server packets even while a cached book page remains open.
    keys = {}
    recipes = {}
    for folder in ['altar_repairs', 'aether_freezer']:
        for recipe_path in (RES / f'packs/{folder}/data/kncraft/recipes').glob('*.json'):
            recipe = read(recipe_path)
            item = recipe['ingredient']['item'] if folder == 'altar_repairs' else recipe['result']
            recipes[item] = ('kncraft:' + recipe_path.stem, recipe.get('repairTime', recipe.get('cookingtime')) / 20)
    config = (ROOT / 'src/main/java/com/beautyinblocks/kncraft/core/ArchitectureConfig.java').read_text()
    food_ids = set(re.findall(r'"([a-z0-9_]+:[a-z0-9_]+)\|-?[0-9.]+\|\d+"', config))
    insulators = re.findall(r'"(?:cotton|wildlife)\|([^|]+)\|(item|armor)\|([0-9.]+)\|([0-9.]+)"', config)
    for path in (BOOK / 'entries').rglob('*.json'):
        data = read(path); icon = data.get('icon'); changed = False
        for entry in data['pages']:
            if entry.get('anchor') in ('kncraft_altar_repair', 'kncraft_freezer', 'kncraft_meals') and not entry.get('body', '').startswith('Pack defaults: '):
                entry['body'] = 'Pack defaults: ' + entry['body']; changed = True
        if isinstance(icon, str) and icon in recipes:
            recipe_id, seconds = recipes[icon]; key = 'recipe/' + recipe_id; keys[key] = recipe_id
            data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_server_recipe']
            entry = page('aether', 'server_recipe', 'Server machine recipe', f'{seconds:g} seconds with normal machine fuel. Use JEI for the current ingredients and result.')
            entry.update(type='patchouli:kn_value', reference=key, fallback=entry['body']); data['pages'].append(entry); changed = True
        if isinstance(icon, str) and icon in food_ids:
            for entry in data['pages']:
                if entry.get('anchor') == 'kncraft_thermal_effect':
                    entry.update(type='patchouli:kn_value', reference='food/' + icon)
                    entry['fallback'] = re.sub(r'\$\([^)]*\)', '', entry['body'])
                    keys[entry['reference']] = icon; changed = True
        for item, slot, cold, heat in insulators:
            if icon != item: continue
            key = slot + '/' + item; keys[key] = item
            data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_server_insulation']
            entry = page('cold', 'server_insulation', 'Server insulation', f'{cold} cold / {heat} heat insulation. Server settings and item conditions take precedence.')
            entry.update(type='patchouli:kn_value', reference=key, fallback=entry['body']); data['pages'].append(entry); changed = True
        if changed: save(path, data, 'Server-authoritative thermal food or insulation component beside its recipe')
    # Cotton and fur materials may lack individual recipe entries; put every material beside the sewing instructions too.
    path = BOOK / 'entries/chapters/cold.json'; data = read(path)
    data['pages'] = [p for p in data['pages'] if not p.get('anchor', '').startswith('kncraft_value_')]
    at = next(i+1 for i,p in enumerate(data['pages']) if p.get('anchor') == 'insulation')
    for item, slot, cold, heat in reversed(insulators):
        key = slot + '/' + item; keys[key] = item
        name = item.split(':')[1].replace('cottonitem', 'cotton').replace('_', ' ').title()
        entry = page('cold', 'value_' + item.split(':')[1], name, f'{cold} cold / {heat} heat insulation. Uses sewing capacity.')
        entry.update(type='patchouli:kn_value', reference=key, fallback=entry['body']); data['pages'].insert(at, entry)
    save(path, data, 'Server definitions beside sewing instructions')
    # Link to the existing recipe entries instead of repeating twelve value pages in the chapters.
    for chapter, ending, section in [('aether', '_altar_repair', 'aether_resources'), ('food', '_freezing', 'meal_plan')]:
        path = BOOK / f'entries/chapters/{chapter}.json'; data = read(path)
        data['pages'] = [p for p in data['pages'] if not p.get('anchor', '').startswith('kncraft_recipe_value_')]
        for entry in data['pages']:
            if entry.get('anchor') in ('kncraft_altar_times', 'kncraft_freezer') and not entry['body'].startswith('Pack defaults: '): entry['body'] = 'Pack defaults: ' + entry['body']
        save(path, data, 'Authoritative machine recipe times in the existing chapter')
    insert('aether', 'aether_resources', [
        ('machine_reference', 'Current repair times', 'Each equipment page shows server repair time:$(br)Goat fur: $(l:patchouli:recipes/cold_sweat/goat_fur_cap)cap$(/l), $(l:patchouli:recipes/cold_sweat/goat_fur_parka)parka$(/l), $(l:patchouli:recipes/cold_sweat/goat_fur_pants)pants$(/l), $(l:patchouli:recipes/cold_sweat/goat_fur_boots)boots$(/l).$(br)Hoglin: $(l:patchouli:recipes/cold_sweat/hoglin_headpiece)headpiece$(/l), $(l:patchouli:recipes/cold_sweat/hoglin_tunic)tunic$(/l), $(l:patchouli:recipes/cold_sweat/hoglin_trousers)trousers$(/l), $(l:patchouli:recipes/cold_sweat/hoglin_hooves)hooves$(/l).$(br)$(l:patchouli:recipes/alexsmobs/frontier_cap)Frontier Cap$(/l).'),
    ])
    insert('food', 'meal_plan', [
        ('machine_reference', 'Current freezing times', 'Prepared-juice Freezer recipes show current server times beside the result in the Cook Book: $(l:patchouli:cookbook/pamhc2foodcore/applesmoothieitem)apple smoothie$(/l), $(l:patchouli:cookbook/pamhc2foodcore/melonsmoothieitem)melon smoothie$(/l), and $(l:patchouli:cookbook/pamhc2foodcore/sweetberrysmoothieitem)sweetberry smoothie$(/l). Use JEI for the current ingredients.'),
    ])
    (RES / 'guide-values.json').write_text(json.dumps(keys, indent=2) + '\n', encoding='utf-8')
    template = read(BOOK / 'templates/kn_text.json')
    template['components'][-1] = {'type': 'patchouli:custom', 'class': 'com.beautyinblocks.kncraft.client.GuideValueComponent', 'x': 0, 'y': 30, 'reference': '#reference', 'fallback': '#fallback'}
    save(BOOK / 'templates/kn_value.json', template, 'Live server reference component')
    steps = read(BOOK / 'templates/kn_text.json'); steps['components'] = steps['components'][:3]
    for number, y in enumerate([33, 70, 107], 1):
        steps['components'] += [{'type': 'patchouli:item', 'x': 0, 'y': y, 'item': f'#icon{number}'},
                                {'type': 'patchouli:text', 'x': 22, 'y': y, 'text': f'#step{number}', 'max_width': 92, 'line_height': 9}]
    save(BOOK / 'templates/kn_steps.json', steps, 'Native item icons and numbered practical steps; no fabricated screenshots')
    for chapter, section, title, rows in [
        ('cold', 'hearth', 'Check the hearth', [('cold_sweat:hearth', '1. Supply fuel for the mode you need.'), ('minecraft:lever', '2. Enable heating or cooling. Allow warm-up time.'), ('minecraft:oak_door', '3. Check the air path and range with /kncraft camp.')]),
        ('food', 'meal_plan', 'From pantry to meal', [('pamhc2foodcore:potitem', '1. Gather reusable cooking tools.'), ('minecraft:carrot', '2. Follow intermediate ingredient recipes in JEI.'), ('pamhc2foodcore:stewitem', '3. Make the meal. Check its Cook Book thermal notes.')]),
    ]:
        path = BOOK / f'entries/chapters/{chapter}.json'; data = read(path)
        data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_practical_steps']
        entry = page(chapter, 'practical_steps', title, ''); entry['type'] = 'patchouli:kn_steps'
        for n, (icon, text) in enumerate(rows, 1): entry[f'icon{n}'] = icon; entry[f'step{n}'] = text
        at = next(i+1 for i,p in enumerate(data['pages']) if p.get('anchor') == section); data['pages'].insert(at, entry)
        save(path, data, 'Numbered hearth and kitchen sequences alongside their original chapters')
    for name, u, v, width, height, scale in [('kn_map', 0, 0, 1280, 595, 116/1280), ('kn_map_detail', 560, 250, 120, 90, 116/120)]:
        template = read(BOOK / 'templates/kn_image.json'); image = template['components'][4]
        image.update(u=u, v=v, width=width, height=height, texture_width=1280, texture_height=595, scale=scale)
        template['components'][5]['y'] = 29 + round(height*scale) + 5
        save(BOOK / f'templates/{name}.json', template, 'Native texture coordinates; original JPEG unchanged')
    declaration = RES / 'data/patchouli/patchouli_books/kncraft_guide/book.json'
    data = read(declaration); data['version'] = 13; declaration.write_text(json.dumps(data, indent=2) + '\n')
    manifest = ROOT / 'docs/Guide-Integration-Edits.json'; data = read(manifest); data['files'].update(edited)
    manifest.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')
    print('Reference guide:', len(keys), 'live keys;', len(edits['changes']), 'documented editorial replacements')


if __name__ == '__main__': main()
