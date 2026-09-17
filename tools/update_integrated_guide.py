"""Weave integration help into the existing KNCraft Guide Book and individual Cook Book entries."""
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
BOOK = RES / 'assets/patchouli/patchouli_books/kncraft_guide/en_us'
edited = {}

def save(path, data, reason):
    path.write_bytes((json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode('utf-8'))
    edited[path.relative_to(BOOK).as_posix()] = reason

def read(path): return json.loads(path.read_text(encoding='utf-8'))

def page(chapter, key, heading, body):
    assert len(re.sub(r'\$\([^)]*\)', '', body)) <= 290, (heading, len(body))
    return {'type': 'patchouli:kn_text', 'heading': heading, 'body': body, 'anchor': 'kncraft_' + key,
            'navigation': f'$(l:patchouli:guide)Book$(/l) $(l:patchouli:chapters/{chapter})Chapter$(/l)'}

def insert(chapter, section, pages):
    path = BOOK / f'entries/chapters/{chapter}.json'
    data = read(path)
    pages = [page(chapter, *p) for p in pages]
    keys = {p['anchor'] for p in pages}
    data['pages'] = [p for p in data['pages'] if p.get('anchor') not in keys and p.get('anchor') != 'architecture']
    start = next(i for i,p in enumerate(data['pages']) if p.get('anchor') == section)
    end = start + 1
    while end < len(data['pages']) and not data['pages'][end].get('anchor'): end += 1
    data['pages'][end:end] = pages
    save(path, data, 'Integration pages inserted beside existing topics; original pages retained')

def main():
    insert('intro', 'controls', [
        ('journal', 'Your accomplishment journal', 'FTB Quests records bosses, discoveries and mod advancements. Choose any entry, in any order. There are no reward chains, recipe locks or item turn-ins. Existing native advancements can count; revisit landmarks to record new discoveries.'),
        ('guide', 'Keep this guide', 'Craft a book with one paper for this KNCraft Guide Book. Its chapters explain how the pack works together. Food effects appear beside their Cook Book recipes. Values shown are pack defaults; the server may adjust them. Check item tooltips for active effects.'),
    ])
    insert('food', 'gardens', [
        ('cotton', 'A crop for more than food', "Cotton can line clothing in Cold Sweat's Sewing Table: 1 cold / 0.5 heat insulation by default. Eight cotton around one wool make tent canvas. Six cotton and three vines make eight RopeBridge ropes. Cotton keeps its ordinary farming uses."),
        ('supplies', 'Seeds on your travels', 'Selected farm and garden chests may contain cotton, rice or tea seeds. Farmers sell small stocks of cotton and tea seeds. Save planting stock before cooking. These supplies help start a garden; exploration is never required to unlock farming.'),
        ('savanna_gardens', 'Gardens across the terrain', "Pam's windy gardens now recognize savanna biomes classified by the terrain mods. Explore newly generated land to find this coverage. Existing chunks are not replanted, and the pack's biome temperatures and wildlife spawn rules still apply."),
    ])
    insert('food', 'orchard', [
        ('orchard', 'Harvest before felling', 'Pick ripe fruit with its normal right-click harvest. FallingTree can fell a tree and clear leaves; sneak to use ordinary chopping. The pack limits whole-tree scans to 100 logs, so very large trees may need several cuts. Keep saplings and planting stock.'),
    ])
    insert('food', 'meal_plan', [
        ('thermal_meals', 'Food for the climate', 'Some meals help Cold Sweat as well as hunger. Hot drinks and soups warm you; selected chilled foods cool you. Each affected Cook Book entry lists its strength and duration. These are changes to base temperature, not instant body-temperature points.'),
        ('meal_limits', 'One thermal meal at a time', 'KNCraft foods share one thermal effect. The latest meal replaces the previous one, including warming versus cooling; eating it again refreshes the time. Normal hunger rules still apply. Clothing, shelter and waterskins remain useful.'),
        ('ingredients', 'Ingredients across worlds', "Real Aether and Alex's Mobs eggs work in Pam's egg recipes. Moa eggs are consumed when cooked, so keep those you want to hatch. Banana and avocado substitutes stay available in Pam's cooking; they do not become animal eggs."),
        ('milk_berries', 'Milk and berries', "Skyroot milk buckets work in Pam's milk recipes, or make eight fresh milk while returning the Skyroot bucket. Ordinary Aether blue berries work as blueberries. Enchanted berries keep their special role."),
        ('meat', 'Wild ingredients', "Moose ribs and kangaroo meat work in general raw-meat recipes. Raw catfish works in general meat and fish recipes; cooked catfish works in cooked-fish recipes. These additions do not turn every meat into chicken or beef."),
        ('freezer', 'Juice from the Freezer', 'An Aether Freezer turns prepared apple, melon or sweetberry juice into its matching smoothie in 10 seconds, using normal Freezer fuel. Prepare the juice first. This replaces the ice step; it does not create extra servings or cooking experience.'),
    ])
    insert('cold', 'insulation', [
        ('fibers', 'Farm and wildlife linings', 'Cotton gives 1 cold / 0.5 heat insulation. Bear fur gives 1.5 cold, bison fur 1.75 cold, and kangaroo hide 0.75 cold / 0.75 heat. Use normal Sewing Table slots and removal rules. Cotton offers a renewable alternative to hunting.'),
        ('cap', 'Frontier Cap', 'The worn Frontier Cap provides 3 cold / 1 heat insulation by default. Its built-in insulation uses sewing capacity. Read the Cold Sweat tooltip before adding linings; your existing sewn equipment keeps its native rules.'),
        ('aether_accessories', 'Aether accessories', 'An equipped Ice Ring or Ice Pendant adds 2 heat insulation. Ordinary colored capes add 0.5 cold / 0.5 heat. Their combined added heat insulation is capped at 4. They slow temperature change; they do not make you immune to heat.'),
    ])
    insert('cold', 'hearth', [
        ('tent_hearth', 'A hearth inside a tent', 'Nomadic Tent interiors count as enclosed for Cold Sweat, including their expandable floors. A hearth still needs fuel, warm-up and a clear route for its effect to spread. Large tents may need careful placement or additional climate equipment.'),
        ('tent_temperature', 'The camp sets the climate', 'A tent inherits ambient temperature from its outside entrance, including local climate and time of day. Interior hearths, heat sources, clothing and food still affect you. Packing and pitching elsewhere gives the tent its new campsite climate.'),
        ('unloaded_camp', 'A distant campsite', 'When the outside area is unloaded, the tent retains its last sampled outdoor temperature. It updates when that area loads again. Rain is not simulated falling inside the tent. If you entered while wet, your existing wetness still dries normally.'),
    ])
    insert('cold', 'boiler', [
        ('icestone', 'Icestone cooling fuel', 'Aether Icestone supplies 100 cooling fuel to an Icebox or hearth. This is less than ordinary ice. The Aether Freezer also makes ice, packed ice and blue ice, linking an Aether workshop to your climate supplies.'),
        ('meals', 'Carry climate provisions', "Hot tea, coffee and nettle tea warm for 60 seconds; carrot soup, potato soup and stew for 90. Melon smoothie and ice cream cool for 60; apple juice for 30. See $(l:patchouli:chapters/food#kncraft_thermal_meals)Pam's chapter$(/l) and each Cook Book page."),
    ])
    insert('travel', 'tents', [
        ('climate', 'Choose the campsite', "Your tent takes its outdoor entrance's ambient temperature. A snowy campsite stays cold inside; a hot campsite still needs cooling. Tent interiors count as enclosed for hearths. Read $(l:patchouli:chapters/cold#kncraft_tent_hearth)tent climate care$(/l) before a long stay."),
        ('crossings', 'Entering the tent', 'Completed tents support seamless entrances and retain their ownership and contents. If a new entrance stays closed, a connected client may still be loading its dimension. Reconnect a stalled client or use the normal tent door.'),
        ('canvas', 'Canvas from cotton', 'Surround one wool with eight cotton for tent canvas. This saves crafting steps while retaining the original fiber cost. Plant cotton for a renewable camping supply; find the canvas pattern in JEI.'),
    ])
    insert('travel', 'tent_move', [
        ('packing', 'Use the tent door', 'Pack and upgrade through the normal Nomadic Tents door. Packing or moving clears the old entrance links; pitching again updates the outside climate source. Keep your route home marked before moving a shared campsite.'),
    ])
    insert('travel', 'carryon', [
        ('safe_carry', 'Move the whole structure', 'Carry On leaves tent doors and both hearth halves in place. Pack tents through their normal door and dismantle hearths normally. Placed Sophisticated Backpacks retain their own pickup rules. Ordinary permitted chests still move with Carry On.'),
    ])
    insert('travel', 'backpack_care', [
        ('totems', 'Keep a totem within reach', 'Inventory Totem can consume a totem in your normal player inventory. A totem stored inside a backpack or Curios slot is not part of that search. It prevents a qualifying death; it does not preserve every item after a death that still occurs.'),
    ])
    insert('cold', 'hearth', [
        ('comforts', 'Sleep at camp', 'Comforts uses the normal Cold Sweat sleep check. This pack exempts sleeping bags through its existing Cold Sweat setting; other beds retain temperature danger checks. A server can change that policy. Warm or cool your shelter before a long stay.'),
    ])
    insert('explore', 'return_trip', [
        ('maintenance', 'Maintain your equipment', 'Easy Anvils removes this pack\'s Too Expensive limit and softens repeated repair penalties. JRFTL supplies the existing rotten-flesh-to-leather route. Use these workshop options and JEI before discarding useful equipment; no extra salvage recipe is needed.'),
    ])
    insert('travel', 'backpack_upgrades', [
        ('feeding', 'Food and automatic feeding', 'A feeding upgrade avoids KNCraft warming foods while your body is too hot, and cooling foods while too cold. It still honors its food filters and hunger rules. At three hunger icons or fewer, it permits available food to prevent starvation.'),
        ('feeding_limits', 'Keep deliberate provisions', 'Automatic feeding does not choose the best climate treatment or eat at full hunger. Carry both ordinary food and chosen hot or cold provisions. The default cutoff is body temperature +20 or -20. Check each thermal food in the Cook Book.'),
    ])
    insert('travel', 'engines', [
        ('oil_fuel', 'Farms can fuel the railway', "Two Pam's cooking oil and one glass bottle make one Better Minecarts bio-diesel fuel. The locomotive uses its usual fuel and bottle rules. Grow oil crops, process them in your kitchen, then stock a depot beside the railway."),
        ('rail_climate', 'A prepared passenger', 'Cold Sweat already provides insulated minecarts. Carry climate supplies for the station and the walk beyond the rails. Use freight storage at depots and a backpack for the final part of the trip.'),
        ('electric', 'Electric locomotive supply', "This pack's electric locomotive refills on powered rails. Weather2's wind turbine supplies Forge Energy to compatible equipment, but it is not a direct train charger. Plan the track power before starting a long electric route."),
    ])
    insert('aether', 'aether_start', [
        ('portal', 'Light the normal frame', 'Use the normal Aether activation item to light the frame. Successful conversion gives an immersive portal pair; a stick remains a fallback. If conversion cannot complete, the normal portal remains available.'),
    ])
    insert('aether', 'aether_resources', [
        ('thermal', 'Bring cooling home', 'Icestone fuels Cold Sweat cooling equipment. Ice accessories and ordinary capes add modest insulation, with a combined heat limit. The Freezer can chill prepared Pam\'s juices. See $(l:patchouli:chapters/cold#kncraft_aether_accessories)climate equipment$(/l).'),
        ('lance', 'The Valkyrie Lance', 'The Valkyrie Lance uses Better Combat thrusts with a 6.5-block reach, matching its normal survival reach. Better Combat otherwise misses its special Forge reach bonus. The weapon keeps its normal attack damage and enchantment rules.'),
    ])
    insert('depths', 'depths_start', [
        ('portal', 'Safe new arrivals', 'The normal igniter can create an immersive Depths route. New destination frames and landing rooms stay between Y=5 and Y=122 and avoid occupied or unsafe space. Existing portals stay where they were built; failed conversion keeps normal travel.'),
    ])
    insert('animals', 'animals_care', [
        ('berries', 'Fresh berry food', "Pam's blueberries, blackberries, raspberries and strawberries count as food for Alex's crows and raccoons. Their special taming, breeding and teaming ingredients stay the same. Food is not automatically a taming gift."),
        ('companions', 'Companions near combat', 'Better Combat protects owned companions from accidental area hits. KNCraft also recognizes their ownership when the owner is not loaded. Still aim carefully: direct attacks and other damage sources retain their own rules.'),
        ('materials', 'Wildlife and cold weather', 'Bear fur, bison fur and kangaroo hide can supply Sewing Table insulation; the Frontier Cap helps when worn. Read $(l:patchouli:chapters/cold#kncraft_fibers)lining values$(/l). Cotton offers another route, so hunting is a choice.'),
    ])
    insert('explore', 'return_trip', [
        ('journal', 'Record the journey', 'The FTB journal tracks individual landmarks, bosses and native mod advancements. Structure records update when you enter a landmark after this update. They grant no loot or recipe unlocks. Use them as a travel checklist, at your own pace.'),
        ('loot', 'Supplies with a place', 'Kitchens and cellars may hold prepared food, farms may hold seeds, camps may hold camping supplies, and mining sites may hold rail supplies. Small additions accompany the normal chest loot. Opened chests are not restocked.'),
        ('wings', 'Enchanted travel wings', 'More Enchantments flight effects recognize usable wings in Elytra Slot, including Iron and Leaden Wings. If usable wings also occupy your chest slot, those take priority. Each check uses one wing stack; durability stays on the selected wings.'),
    ])
    insert('wither', 'wither_after', [
        ('journal', 'Keep the accomplishment', "The FTB journal follows the Wither Storm mod's own advancements, including its final defeat. There is no extra reward or forced route. KNCraft encounter scaling applies once; returning to an already balanced encounter does not heal it again."),
    ])
    architecture = BOOK / 'entries/chapters/architecture.json'
    if architecture.exists(): architecture.unlink()
    meals = [
        ('pamhc2crops:hotteaitem', .2, 60), ('pamhc2crops:hotcoffeeitem', .2, 60), ('pamhc2crops:hotnettleteaitem', .2, 60),
        ('pamhc2foodcore:carrotsoupitem', .2, 90), ('pamhc2foodcore:potatosoupitem', .2, 90), ('pamhc2foodcore:stewitem', .2, 90),
        ('pamhc2foodcore:melonsmoothieitem', -.2, 60), ('pamhc2foodcore:icecreamitem', -.2, 60), ('pamhc2foodcore:applejuiceitem', -.1, 30),
    ]
    for path in (BOOK / 'entries/cookbook').rglob('*.json'):
        data = read(path)
        effect = next((m for m in meals if data.get('icon') == m[0]), None)
        if effect:
            _, amount, seconds = effect
            data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_thermal_effect']
            data['pages'].append(page('food', 'thermal_effect', 'Cold Sweat effect',
                f'Pack default: {amount:+g} base temperature for {seconds} seconds. '+ ('Warms' if amount > 0 else 'Cools') + ' while the effect lasts. The latest KNCraft thermal meal replaces the previous effect. Normal hunger rules apply. Server settings and tooltips take precedence.'))
            save(path, data, 'Thermal effect beside the affected food recipe')
        if data.get('icon') in ['pamhc2foodcore:' + fruit + 'smoothieitem' for fruit in ['apple', 'melon', 'sweetberry']]:
            fruit = data['icon'].split(':')[1].removesuffix('smoothieitem')
            data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_freezer']
            data['pages'].append({'type': 'patchouli:kn_cooking', 'heading': 'Aether Freezer', 'anchor': 'kncraft_freezer',
                'input': f'pamhc2foodcore:{fruit}juiceitem', 'output': data['icon'], 'station': 'aether:freezer',
                'body': 'Freeze prepared juice for 10 seconds. Makes one smoothie; uses normal Freezer fuel.',
                'navigation': '$(l:patchouli:cookbook)Cook Book$(/l)'})
            save(path, data, 'Freezer alternative beside the smoothie recipe')
    path = BOOK / 'entries/cookbook/pamhc2foodcore/freshmilk_x8.json'
    data = read(path)
    data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_skyroot_milk']
    data['pages'].append({'type': 'patchouli:crafting', 'recipe': 'kncraft:skyroot_fresh_milk', 'anchor': 'kncraft_skyroot_milk', 'text': 'Skyroot milk also makes eight fresh milk. The empty Skyroot bucket is returned.'})
    save(path, data, 'Skyroot bucket alternative beside fresh milk')
    for name in ['caramelcupcakeitem_x4', 'honeymuffinitem', 'melonpieitem']:
        path = BOOK / f'entries/cookbook/pamhc2foodcore/{name}.json'
        data = read(path)
        data['icon'] = 'minecraft:wheat'
        data['pages'] = [page('food', 'recipe_note', 'Recipe unavailable', "This Food Core recipe names an item that is absent from the installed version. It has been disabled. Choose another recipe from the Cook Book or JEI; no replacement ingredient or result is required.")]
        save(path, data, 'Replace invalid recipe and missing-item icon with an accurate note')
    declaration = RES / 'data/patchouli/patchouli_books/kncraft_guide/book.json'
    data = read(declaration); data['version'] = 10; data['name'] = 'KNCraft Guide Book'; data['subtitle'] = 'KNCraft Guide Book'
    declaration.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    manifest = ROOT / 'docs/Guide-Integration-Edits.json'
    # Preserve provenance of all approved edits when this authoring tool is rerun.
    if manifest.exists(): edited.update({k: v for k,v in read(manifest)['files'].items() if k not in edited})
    manifest.write_text(json.dumps({'removed': ['entries/chapters/architecture.json'], 'files': edited}, indent=2) + '\n')
    print('Integrated guide updates:', len(edited), 'files')

if __name__ == '__main__': main()
