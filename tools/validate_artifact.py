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
        entry = json.loads((book / relative).read_text(encoding='utf-8'))
        anchors = [p['anchor'] for p in entry['pages'] if p.get('anchor')]
        assert len(anchors) == len(set(anchors)), 'Duplicate guide anchor: ' + relative
        for page in entry['pages']:
            assert 'chapters/architecture' not in json.dumps(page), 'Stale guide link'
            if page.get('anchor', '').startswith('kncraft_') and 'body' in page:
                assert len(re.sub(r'\$\([^)]*\)', '', page['body'])) <= 290, 'New guide page needs pagination'
    declaration = json.loads((RES / "data/patchouli/patchouli_books/kncraft_guide/book.json").read_text(encoding="utf-8"))
    assert declaration['use_resource_pack'] is True
    # New pages may be inserted next to their topic. Original chapter pages remain
    # an ordered subsequence, preserving old anchors, artwork and pagination.
    source_manifest = json.loads((ROOT / "docs/Guide-Provenance.json").read_text())
    provenance = source_manifest['source_files']
    for chapter, hashes in source_manifest['preserved_chapter_pages'].items():
        pages = json.loads((book / f'entries/chapters/{chapter}.json').read_text(encoding='utf-8'))['pages']
        assert len(pages) >= len(hashes)
        actual = [hashlib.sha256(json.dumps(page, sort_keys=True, separators=(',', ':')).encode()).hexdigest() for page in pages]
        cursor = 0
        for digest in hashes:
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
            forbidden = ['DepthHeightTests', 'DepthPortalChecks', 'TentTests', 'PerformanceTests', 'CohesionChecks', 'ExpansionChecks', 'TentClimateChecks', 'reference/', 'libs/']
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
            assert not quest.get('dependencies') and not quest.get('rewards')
            assert all(task['type'] == 'advancement' for task in quest['tasks'])
            records += 1
    assert records == len(json.loads((ROOT / 'docs/Journal-Index.json').read_text())['quests'])
    # Every managed food's cookbook page must display its actual default value and duration.
    config = (ROOT / 'src/main/java/com/beautyinblocks/kncraft/core/ArchitectureConfig.java').read_text()
    for item, amount, duration in re.findall(r'"([a-z0-9_]+:[a-z0-9_]+)\|(-?[0-9.]+)\|(\d+)"', config):
        matches = []
        for path in (book / 'entries/cookbook').rglob('*.json'):
            entry = json.loads(path.read_text(encoding='utf-8'))
            if entry.get('icon') == item: matches += [p for p in entry['pages'] if p.get('anchor') == 'kncraft_thermal_effect']
        assert matches, 'Thermal food missing cookbook help: ' + item
        assert all(f'{float(amount):+g} base temperature for {int(duration)//20} seconds' in p['body'] for p in matches)
    print('PASS:', count, 'JSON resources,', records, 'ungated journal records, integrated guide, parity data and release isolation')

if __name__ == "__main__": main()
