"""Build-time image/reference audit; pending photography lives outside the player book."""
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
BOOK = RES / 'assets/patchouli/patchouli_books/kncraft_guide/en_us'


def validate():
    pictures, references = set(), set()
    keys = json.loads((RES / 'guide-values.json').read_text())
    entries = {p.relative_to(BOOK / 'entries').as_posix()[:-5]: json.loads(p.read_text(encoding='utf-8')) for p in (BOOK / 'entries').rglob('*.json')}
    categories = {p.relative_to(BOOK / 'categories').as_posix()[:-5] for p in (BOOK / 'categories').rglob('*.json')}
    for path in BOOK.rglob('*.json'):
        text = path.read_text(encoding='utf-8')
        assert not re.search(r'SCREENSHOT NEEDED|TODO|PLACEHOLDER', text, re.I), 'Unfinished guide text: ' + str(path)
        for target in re.findall(r'\$\(l:patchouli:([^)]*)\)', text):
            name, _, anchor = target.partition('#')
            if name in categories: continue
            assert name in entries, 'Missing guide destination: ' + target
            assert not anchor or any(p.get('anchor') == anchor for p in entries[name]['pages']), 'Missing guide anchor: ' + target
        for texture in re.findall(r'kncraft:textures/[a-zA-Z0-9_./-]+', text):
            assert (RES / 'assets/kncraft' / texture.split(':')[1]).is_file(), 'Missing guide image: ' + texture
            pictures.add(texture)
        data = json.loads(text)
        for page in data.get('pages', []):
            if page.get('type') == 'patchouli:kn_value':
                assert page['reference'] in keys and page.get('fallback'), 'Missing live reference or fallback'
                references.add(page['reference'])
    assert references == set(keys), 'Unreachable server reference key'
    photo = json.loads((ROOT / 'docs/Guide-Photo-Provenance.json').read_text())
    assert hashlib.sha256((RES / photo['resource']).read_bytes()).hexdigest() == photo['sha256'], 'Original BlueMap image changed'
    backlog = json.loads((ROOT / 'docs/Guide-Capture-Backlog.json').read_text())
    pending = [row for row in backlog['groups'] if row['status'] == 'pending']
    report = {'missing_images': [], 'broken_guide_links': [], 'player_facing_placeholders': [], 'local_texture_references': len(pictures), 'live_reference_keys': len(keys),
              'pending_photo_groups': len(pending), 'minimum_pending_captures': sum(row['minimum_captures'] for row in pending)}
    (ROOT / 'docs/Guide-Asset-Report.json').write_text(json.dumps(report, indent=2) + '\n')
    print('GUIDE:', json.dumps(report))


if __name__ == '__main__': validate()
