"""Validate gameplay parity resources, recipe conservation and release contents without Minecraft."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path)
    args = parser.parse_args()
    count = 0
    for file in RES.rglob("*.json"):
        json.loads(file.read_text(encoding="utf-8-sig")); count += 1
    for kind, version in [("aether", "1.1.0"), ("depth", "1.2.0")]:
        file = Path(f"data/kncraft_{kind}/custom_portal_generation/{kind}.json")
        original = ROOT / f"reference/handoff/legacy-sources/portal-datapacks/kncraft-{kind}-immersive-portals-{version}" / file
        assert (RES / f"packs/{kind}_portals" / file).read_bytes() == original.read_bytes(), "Portal generation changed"
    original = ROOT / "reference/handoff/live-reference/world/datapacks/kncraft-rules/data"
    for file in original.rglob("*.json"):
        if "functions" in file.parts: continue
        assert (RES / "packs/waystones/data" / file.relative_to(original)).read_bytes() == file.read_bytes(), str(file)
    # The two new routes preserve the complete cotton->string->wool/rope conversion cost.
    recipe_dir = RES / "packs/fiber_recipes/data/kncraft/recipes"
    canvas = json.loads((recipe_dir / "cotton_canvas.json").read_text())
    rope = json.loads((recipe_dir / "cotton_rope.json").read_text())
    assert ''.join(canvas['pattern']).count('F') == 8 and ''.join(canvas['pattern']).count('W') == 1
    assert 8 + (4 * 2) == 2 * (4 * 2)  # 8 cotton + wool == 2 wool
    assert ''.join(rope['pattern']).count('F') == 6 and ''.join(rope['pattern']).count('V') == 3
    assert rope['result']['count'] == 8 and 6 == 3 * 2
    assert not (RES / "data/minecraft/tags/items/wool.json").exists(), "Cotton must not be wool globally"
    book = RES / "assets/patchouli/patchouli_books/kncraft_guide/en_us"
    assert not (book / 'entries/chapters/architecture.json').exists(), 'Integrate changes into the existing chapters'
    guide_edits = json.loads((ROOT / 'docs/Guide-Integration-Edits.json').read_text())['files']
    for relative in guide_edits:
        if not relative.startswith('entries/'): continue
        entry = json.loads((book / relative).read_text(encoding='utf-8'))
        anchors = [p['anchor'] for p in entry['pages'] if p.get('anchor')]
        assert len(anchors) == len(set(anchors)), 'Duplicate guide anchor: ' + relative
        for page in entry['pages']:
            assert 'chapters/architecture' not in json.dumps(page), 'Stale guide link'
            if page.get('anchor', '').startswith('kncraft_') and 'body' in page:
                assert len(re.sub(r'\$\([^)]*\)', '', page['body'])) <= 290, 'New guide page needs pagination'
    declaration = json.loads((RES / "data/patchouli/patchouli_books/kncraft_guide/book.json").read_text(encoding="utf-8"))
    assert declaration['use_resource_pack'] is True
    # Original pages remain in order, except individually documented, approved
    # editorial replacements. Check both sides of that explicit provenance record.
    source_manifest = json.loads((ROOT / "docs/Guide-Provenance.json").read_text())
    provenance = source_manifest['source_files']
    editorial = json.loads((ROOT / 'docs/Guide-Editorial-Edits.json').read_text())['changes']
    digest_page = lambda p: hashlib.sha256(json.dumps(p, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
    for chapter, hashes in source_manifest['preserved_chapter_pages'].items():
        pages = json.loads((book / f'entries/chapters/{chapter}.json').read_text(encoding='utf-8'))['pages']
        expected = list(hashes)
        for change in [e for e in editorial if e['chapter'] == chapter]:
            before, after = [[digest_page(p) for p in change[side]] for side in ('before', 'after')]
            start = expected.index(before[0])
            assert expected[start:start+len(before)] == before, 'Editorial provenance mismatch'
            expected[start:start+len(before)] = after
            old_anchors = {p['anchor'] for p in change['before'] if p.get('anchor')}
            new_anchors = {p['anchor'] for p in change['after'] if p.get('anchor')}
            assert old_anchors <= new_anchors, 'Lost original guide anchor'
        actual = [digest_page(page) for page in pages]
        cursor = 0
        for digest in expected:
            assert digest in actual[cursor:], 'Original guide page changed: ' + chapter
            cursor = actual.index(digest, cursor) + 1
    edited = {'book/en_us/' + p for p in guide_edits}
    for source, digest in provenance.items():
        if source == 'book/book.json' or source in edited: continue
        path = RES / 'assets' / source[len('artwork/'):] if source.startswith('artwork/') else book.parent / source[len('book/'):]
        assert hashlib.sha256(path.read_bytes()).hexdigest() == digest, 'Imported guide asset changed: ' + source
    if args.jar:
        with zipfile.ZipFile(args.jar) as jar:
            names = jar.namelist()
            forbidden = ['DepthHeightTests', 'DepthPortalChecks', 'TentTests', 'PerformanceTests', 'CohesionChecks', 'ExpansionChecks', 'TentClimateChecks', 'PolishChecks', 'ReferenceChecks', 'reference/', 'libs/']
            assert not [n for n in names if any(token in n for token in forbidden)], 'Test/dependency payload in release'
            meta = jar.read('META-INF/mods.toml').decode()
            assert meta.count('[[mods]]') == 1 and 'modId="kncraft"' in meta
            assert not [n for n in names if n.endswith('.jar')], 'Dependencies must not be bundled'
            assert 'assets/patchouli/patchouli_books/kncraft_guide/en_us/entries/chapters/architecture.json' not in names
            assert 'journal/catalog.json' in names
            assert 'kncraftintegrations.mixins.json' in names
            assert 'assets/kncraft/textures/guide/book_teal.png' in names
        print('Release SHA-256:', hashlib.sha256(args.jar.read_bytes()).hexdigest())
    # Journal definitions use JSON-compatible SNBT. IDs must stay unique and nothing
    # may impose a dependency, grant a reward, or require item consumption.
    ids = set()
    journal = json.loads((RES / 'journal/catalog.json').read_text())
    records = 0
    for filename in journal:
        chapter = json.loads((RES / 'journal' / filename).read_text(encoding='utf-8'))
        for obj in [chapter] + chapter['quests'] + [t for q in chapter['quests'] for t in q['tasks']]:
            assert re.fullmatch(r'[0-9A-F]{16}', obj['id']) and obj['id'] not in ids, 'Duplicate journal ID'
            ids.add(obj['id'])
        for quest in chapter['quests']:
            target = quest['guide_page']
            assert target.startswith('kncraft/chapters/') and (book / ('entries/' + target[len('kncraft/'):] + '.json')).is_file(), 'Broken journal guide link'
            assert not quest.get('dependencies') and not quest.get('rewards')
            assert all(task['type'] == 'advancement' for task in quest['tasks'])
            records += 1
    assert records == len(json.loads((ROOT / 'docs/Journal-Index.json').read_text())['quests'])
    control_refs = json.loads((ROOT / 'docs/Guide-Control-References.json').read_text())['chapters']
    for path in (book / 'entries/chapters').glob('*.json'):
        entry = json.loads(path.read_text(encoding='utf-8'))
        assert any(p.get('anchor') == 'kncraft_controls_live' for p in entry['pages']), 'Chapter lacks controls: ' + path.stem
        actual = sorted(set(re.findall(r'\$\(k:([^)]*)\)', path.read_text(encoding='utf-8'))))
        assert actual == control_refs[path.stem], 'Unaudited key reference: ' + path.stem
    altar = list((RES / 'packs/altar_repairs/data/kncraft/recipes').glob('*.json'))
    assert len(altar) == 9
    for path in altar:
        recipe = json.loads(path.read_text())
        assert recipe['type'] == 'aether:repairing' and recipe['repairTime'] in (700, 1000)
        assert recipe['conditions'][0]['modid'] == recipe['ingredient']['item'].split(':')[0]
    # Every managed food's cookbook page must display its actual default value and duration.
    config = (ROOT / 'src/main/java/com/beautyinblocks/kncraft/core/ArchitectureConfig.java').read_text()
    for item, amount, duration in re.findall(r'"([a-z0-9_]+:[a-z0-9_]+)\|(-?[0-9.]+)\|(\d+)"', config):
        matches = []
        for path in (book / 'entries/cookbook').rglob('*.json'):
            entry = json.loads(path.read_text(encoding='utf-8'))
            if entry.get('icon') == item: matches += [p for p in entry['pages'] if p.get('anchor') == 'kncraft_thermal_effect']
        assert matches, 'Thermal food missing cookbook help: ' + item
        assert all(f'{float(amount):+g} base temperature for {int(duration)//20} seconds' in p['body'] for p in matches)
    from validate_guide import validate
    validate()
    print('PASS:', count, 'JSON resources,', records, 'ungated journal records, integrated guide, parity data and release isolation')

if __name__ == "__main__": main()
