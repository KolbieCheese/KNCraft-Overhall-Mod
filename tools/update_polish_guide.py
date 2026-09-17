"""Integrate follow-up behavior and live key references into the original guide chapters."""
import json
import re
from update_integrated_guide import BOOK, ROOT, RES, read, save, page, insert, edited


CONTROLS = {
    'intro': [
        ('controls_live', 'Your current controls', 'Bindings below are read from your Controls menu. Unbound means you need to assign a key; N/A means the binding is unavailable. Rebinding updates these references when the page opens. Context-specific overlaps can be intentional.'),
        ('controls_basics', 'Everyday controls', 'Inventory: $(k:key.inventory)$(br)Use / interact: $(k:key.use)$(br)Sneak: $(k:key.sneak)$(br)Chat: $(k:key.chat)$(br)Journal: $(k:key.ftbquests.quests)$(br)The journal is also in the inventory sidebar.'),
        ('controls_voice', 'Voice and company', 'Voice menu: $(k:key.voice_chat)$(br)Group: $(k:key.voice_chat_group)$(br)Mute microphone: $(k:key.mute_microphone)$(br)Disable voice: $(k:key.disable_voice_chat)$(br)Push to talk: $(k:key.push_to_talk)$(br)Select devices in the voice menu.'),
        ('controls_preset', 'Optional pack controls', 'Run /kncraft controls for possible overlaps. Use /kncraft controls preset to apply the optional pack layout. It saves your old bindings. /kncraft controls restore restores unchanged preset bindings and keeps your later customizations.'),
    ],
    'wither': [
        ('controls_live', 'Encounter controls', 'Use held equipment: $(k:key.use)$(br)Attack: $(k:key.attack)$(br)Sprint: $(k:key.sprint)$(br)Swap offhand: $(k:key.swapOffhand)$(br)Journal: $(k:key.ftbquests.quests)$(br)This chapter uses normal item controls, not a separate Wither Storm hotkey.'),
    ],
    'cold': [
        ('controls_live', 'Climate controls', 'Use waterskin / open machine: $(k:key.use)$(br)Inventory: $(k:key.inventory)$(br)Curios equipment: $(k:key.curios.open.desc)$(br)Recipe in JEI: $(k:key.jei.showRecipe)$(br)Use / pour preference follows Cold Sweat settings.'),
        ('controls_camp', 'Inspect your campsite', 'Inside a tent, enter /kncraft camp in chat. It shows exterior climate, live or saved sampling, and nearby climate equipment. Read fuel, heating/cooling enablement, current conditioning levels and whether the air path reaches you.'),
    ],
    'weather': [
        ('controls_live', 'Weather controls', 'Use weather instruments: $(k:key.use)$(br)Recipes: $(k:key.jei.showRecipe)$(br)Recipe uses: $(k:key.jei.showUses)$(br)Weather devices use their normal item and block interfaces. This pack has no separate Weather2 key binding.'),
    ],
    'aether': [
        ('controls_live', 'Aether controls', 'Aether accessories: $(k:key.aether.open_accessories.desc)$(br)Curios: $(k:key.curios.open.desc)$(br)Invisibility toggle: $(k:key.aether.invisibility_toggle.desc)$(br)Gravitite jump ability: $(k:key.aether.gravitite_jump_ability.desc)$(br)Abilities still require the appropriate equipment.'),
        ('controls_equipment', 'Equipment abilities', 'More Enchantments action: $(k:key.more_enchantments.action_key)$(br)Use held item / altar: $(k:key.use)$(br)Journal: $(k:key.ftbquests.quests)$(br)Flight abilities read your equipped usable wings, including Elytra Slot.'),
    ],
    'depths': [
        ('controls_live', 'Depths controls', 'Use a key or held item: $(k:key.use)$(br)Attack: $(k:key.attack)$(br)Swap offhand: $(k:key.swapOffhand)$(br)Journal: $(k:key.ftbquests.quests)$(br)This chapter uses ordinary item interactions. Check individual equipment descriptions for its behavior.'),
    ],
    'animals': [
        ('controls_live', 'Wildlife controls', 'Interact / offer held item: $(k:key.use)$(br)Drop an offering: $(k:key.drop)$(br)Sneak: $(k:key.sneak)$(br)Journal: $(k:key.ftbquests.quests)$(br)Some animals collect dropped food; others require direct interaction or special ingredients.'),
    ],
    'travel': [
        ('controls_live', 'Camp and storage controls', 'Use tent door / storage: $(k:key.use)$(br)Backpack: $(k:key.sophisticatedbackpacks.open_backpack)$(br)Inventory interaction: $(k:key.sophisticatedbackpacks.inventory_interaction)$(br)Carry On modifier: $(k:key.carry.desc)$(br)Carry eligible blocks with empty hands and Use.'),
        ('controls_train', 'Train controls', 'Increase power: $(k:key.betterminecarts.increase)$(br)Decrease power: $(k:key.betterminecarts.decrease)$(br)Lamp: $(k:key.betterminecarts.lamp)$(br)Whistle: $(k:key.betterminecarts.whistle)$(br)These controls apply while riding a minecart.'),
        ('controls_train_extra', 'Train and inventory tools', 'Train redstone: $(k:key.betterminecarts.redstone)$(br)Train data: $(k:key.betterminecarts.data)$(br)Sort storage: $(k:key.sophisticatedcore.sort)$(br)To storage: $(k:key.sophisticatedcore.transfer_to_storage)$(br)To inventory: $(k:key.sophisticatedcore.transfer_to_inventory)'),
        ('controls_building', 'Building a route', 'Aim / use a bridge or ladder builder: $(k:key.use)$(br)Sneak: $(k:key.sneak)$(br)Recipes: $(k:key.jei.showRecipe)$(br)RopeBridge uses the builder item. Macaw\'s bridges and stairs use normal block placement. Waystones open through Use.'),
    ],
    'food': [
        ('controls_live', 'Kitchen and garden controls', 'Use / eat / harvest: $(k:key.use)$(br)Recipe: $(k:key.jei.showRecipe)$(br)Uses: $(k:key.jei.showUses)$(br)JEI search: $(k:key.jei.focusSearch)$(br)Bookmark: $(k:key.jei.bookmark)$(br)Hover the item before requesting recipes or uses.'),
        ('controls_harvest', 'Harvest with care', 'Sneak: $(k:key.sneak)$(br)Attack / break: $(k:key.attack)$(br)Use / harvest: $(k:key.use)$(br)Pick ripe Pam\'s fruit before felling its tree. Sneaking uses ordinary chopping with this pack\'s FallingTree settings. Follow the normal crop or fruit interaction.'),
    ],
    'explore': [
        ('controls_live', 'Exploration controls', 'Journal: $(k:key.ftbquests.quests)$(br)Native advancements: $(k:key.advancements)$(br)Equipment action: $(k:key.more_enchantments.action_key)$(br)Curios: $(k:key.curios.open.desc)$(br)Records are optional and grant no rewards or recipe unlocks.'),
        ('controls_flight', 'Flight and combat controls', 'Barrel Roll toggle: $(k:key.do_a_barrel_roll.toggle_enabled)$(br)Attack: $(k:key.attack)$(br)Use item: $(k:key.use)$(br)Better Combat feint: $(k:keybinds.bettercombat.feint)$(br)Unbound actions are optional; assign them in Controls if wanted.'),
        ('controls_view', 'View and presentation', 'Perspective: $(k:key.togglePerspective)$(br)First-person body: $(k:key.firstperson.toggle)$(br)Shader selection: $(k:iris.keybind.shaderPackSelection)$(br)Toggle shaders: $(k:iris.keybind.toggleShaders)$(br)Visual settings are personal client choices.'),
    ],
    'catalog': [
        ('controls_live', 'Catalog controls', 'Use / interact: $(k:key.use)$(br)Drop item: $(k:key.drop)$(br)Recipe: $(k:key.jei.showRecipe)$(br)Uses: $(k:key.jei.showUses)$(br)An animal\'s page explains its specific interaction. Food is not automatically a taming ingredient.'),
    ],
}


def main():
    for chapter, entries in CONTROLS.items():
        path = BOOK / f'entries/chapters/{chapter}.json'
        data = read(path)
        anchors = {'kncraft_' + row[0] for row in entries}
        data['pages'] = [p for p in data['pages'] if p.get('anchor') not in anchors]
        # The original contents/artwork remain intact; controls sit near each chapter opening.
        position = next((i + 1 for i, p in enumerate(data['pages']) if p.get('anchor', '').endswith('_contents')), 1)
        data['pages'][position:position] = [page(chapter, *row) for row in entries]
        save(path, data, 'Live key references and relevant controls within the original chapter')
    insert('animals', 'animals_care', [
        ('offerings', 'Give animals time to collect', 'Player-dropped food accepted by a nearby Alex\'s animal gets a 10-second grace period from backpack magnets. Stay within eight blocks with a clear view. Ordinary loot is unaffected. The animal still follows its own eating and taming rules.'),
        ('frontier_recipe', 'A cap for expeditions', 'The $(l:patchouli:recipes/alexsmobs/frontier_cap)Frontier Cap$(/l) has a recipe page with its climate and maintenance notes. It keeps its native abilities and can be repaired at an Aether Altar. Cotton and other linings remain useful options.'),
    ])
    insert('travel', 'backpack_upgrades', [
        ('offering_magnets', 'Magnets near wildlife', 'Deliberate offerings can briefly stay on the ground for a nearby animal. The grace period ends; magnets then collect normally. Filters still apply. See $(l:patchouli:chapters/animals#kncraft_offerings)wildlife offerings$(/l).'),
        ('tank_refill', 'Water at a portable camp', 'Place a backpack with a water-filled tank upgrade. Hold an empty waterskin, then sneak ($(k:key.sneak)) and Use ($(k:key.use)) on the backpack. Filling consumes 250 mB of water. Normal clicking opens the backpack; tank capacity and upgrades still matter.'),
    ])
    insert('cold', 'waterskins', [
        ('tank_refill', 'Refill from a backpack tank', 'A placed backpack tank can refill an empty waterskin. Hold it, sneak ($(k:key.sneak)) and Use ($(k:key.use)) on the backpack. You need at least 250 mB of water. Filling uses native local temperature; warm or chill the skin afterward as needed.'),
    ])
    insert('aether', 'aether_resources', [
        ('altar_repairs', 'Maintain expedition gear', 'An Altar repairs Cold Sweat goat-fur and hoglin armor, plus Alex\'s Frontier Cap. Supply normal altar fuel. Repairing keeps enchantments, names and sewn insulation. This is another workshop option alongside your normal repair routes.'),
        ('altar_times', 'Altar repair times', 'Goat-fur armor and the Frontier Cap take 35 seconds per piece; hoglin armor takes 50 seconds. Repair one damaged piece at a time with normal Ambrosium fuel. Powerful boss equipment has not been added to this repair list.'),
    ])
    insert('cold', 'insulation', [
        ('altar_maintenance', 'Repair your climate gear', 'Cold Sweat goat-fur and hoglin armor can be repaired at an Aether Altar. The Frontier Cap can too. Keep your sewn insulation and enchantments while restoring durability. See $(l:patchouli:chapters/aether#kncraft_altar_repairs)altar maintenance$(/l).'),
    ])
    insert('cold', 'cold_destinations', [
        ('regional_climate', 'Prepare for the region', 'Selected BOP and Terralith regions now have deliberate climate defaults. Ice caves are cold; thermal and mantle caves are warm or hot. Campsite inheritance uses the same climate. Time, elevation, weather and nearby blocks still affect the result.'),
        ('regional_examples', 'Regional starting points', 'Default biome ranges: auroral garden 18-36 F; tundra 28-46 F; cold desert 28-64 F; tropics 76-88 F. These are ambient biome contributions, not guaranteed thermometer readings. The server may override them.'),
        ('cave_examples', 'Underground contrasts', 'Terralith ice caves default to 32 F, frostfire caves 48 F, thermal caves 86 F and mantle caves 100 F before other effects. Pack both insulation and useful provisions. A tent preserves its campsite climate rather than guaranteeing comfort.'),
    ])
    insert('explore', 'return_trip', [
        ('journal_complete', 'More accomplishments', 'The journal now includes BOP\'s native all-biomes achievement and RopeBridge\'s two builder-item and two construction achievements. All 344 records remain independent. Build or explore at your own pace; nothing requires completing the whole journal.'),
        ('attribute_equipment', 'Equipment bonuses travel with you', 'Furor, Agility, Range and Armoring keep their native strengths while contributing alongside other attribute bonuses. Changing or removing equipment updates the bonus. Wing armor effects still recognize your active Elytra Slot wings.'),
    ])
    frontier_path = BOOK / 'entries/recipes/alexsmobs/frontier_cap.json'
    if not frontier_path.exists():
        frontier_path.parent.mkdir(parents=True, exist_ok=True)
        save(frontier_path, {'name': 'Frontier Cap', 'category': 'patchouli:recipes', 'icon': 'alexsmobs:frontier_cap',
             'pages': [{'type': 'patchouli:crafting', 'recipe': 'alexsmobs:frontier_cap', 'anchor': 'frontier_cap_recipe'},
                       page('animals', 'frontier_insulation', 'Cold Sweat insulation', 'Worn pack default: 3 cold / 1 heat insulation. Built-in insulation uses sewing capacity. Read the live item tooltip before adding linings; server settings take precedence.')],
             'extra_recipe_mappings': {'alexsmobs:frontier_cap': 0}}, 'Frontier Cap recipe, insulation and altar maintenance in the existing recipe category')
    for path in (BOOK / 'entries').rglob('*.json'):
        if '/chapters/' in path.as_posix(): continue
        data = read(path)
        icon = data.get('icon', '')
        if icon == 'alexsmobs:frontier_cap' or (isinstance(icon, str) and icon.startswith('cold_sweat:') and any(icon.endswith('_' + slot) for slot in ('helmet', 'chestplate', 'leggings', 'boots')) and ('goat_fur' in icon or 'hoglin' in icon)):
            data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_altar_repair']
            seconds = 50 if 'hoglin' in icon else 35
            data['pages'].append(page('aether', 'altar_repair', 'Aether Altar repair', f'Repair this damaged item in an Aether Altar in {seconds} seconds with normal altar fuel. Its name, enchantments and sewn insulation are retained. See $(l:patchouli:chapters/aether#kncraft_altar_repairs)altar maintenance$(/l).'))
            save(path, data, 'Altar repair information beside the relevant equipment')
        if icon == 'cold_sweat:waterskin':
            data['pages'] = [p for p in data['pages'] if p.get('anchor') != 'kncraft_tank_refill']
            data['pages'].append(page('cold', 'tank_refill', 'Portable water supply', 'Sneak and Use an empty waterskin on a placed backpack with a water tank upgrade. Filling consumes 250 mB. It keeps the waterskin name and uses native fill-temperature rules. Heat or cool the filled waterskin normally.'))
            save(path, data, 'Verified native backpack-tank refill beside the waterskin recipe')
    declaration = RES / 'data/patchouli/patchouli_books/kncraft_guide/book.json'
    data = read(declaration); data['version'] = 11
    declaration.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    manifest = ROOT / 'docs/Guide-Integration-Edits.json'
    previous = read(manifest)
    previous['files'].update(edited)
    manifest.write_text(json.dumps(previous, indent=2) + '\n', encoding='utf-8')
    refs_path = ROOT / 'docs/Guide-Control-References.json'
    refs = read(refs_path)
    audited = set(refs.get('known_binding_ids', [k for values in refs['chapters'].values() for k in values]))
    refs['chapters'] = {p.stem: sorted(set(re.findall(r'\$\(k:([^)]*)\)', p.read_text(encoding='utf-8')))) for p in sorted((BOOK / 'entries/chapters').glob('*.json'))}
    assert {k for values in refs['chapters'].values() for k in values} <= audited, 'A new key ID needs an installed-pack audit'
    refs['known_binding_ids'] = sorted(audited)
    refs_path.write_text(json.dumps(refs, indent=2) + '\n', encoding='utf-8')
    print('Polished', len(edited), 'guide entries; controls in all', len(CONTROLS), 'chapters')


if __name__ == '__main__': main()
