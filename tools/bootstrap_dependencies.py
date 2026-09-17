"""Prepare checksum-pinned build inputs from a local pack or official download URLs.

Only libs/ is written. Third-party JARs are never bundled in our release.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument('--instance', type=Path)
    mode.add_argument('--download', action='store_true')
    parser.add_argument('--refresh', action='store_true', help='Verify a fresh download even when the cached input matches')
    args = parser.parse_args()
    destination = ROOT / 'libs'
    destination.mkdir(exist_ok=True)
    report = []
    for spec in json.loads((ROOT / 'docs/Build-Inputs.json').read_text())['inputs']:
        target = destination / spec['target']
        if args.instance:
            data = (args.instance / 'mods' / spec['source']).read_bytes()
        elif not args.refresh and target.is_file() and hashlib.sha256(target.read_bytes()).hexdigest() == spec['sha256']:
            data = target.read_bytes()
        else:
            request = urllib.request.Request(spec['url'], headers={'User-Agent':'KNCraftCompatibility-build/1.0'})
            with urllib.request.urlopen(request, timeout=120) as response:
                data = response.read()
        digest = hashlib.sha256(data).hexdigest()
        if digest != spec['sha256']:
            raise SystemExit('Hash mismatch for ' + spec['source'] + '; refusing an unreviewed dependency')
        print('Verified', spec['source'], flush=True)
        target.write_bytes(data)
        report.append({'source':spec['source'],'file':spec['target'],'sha256':digest})
        if spec['source'] == 'Nomadic-Tents-20.1.1.jar':
            with zipfile.ZipFile(io.BytesIO(data)) as jar:
                meta = json.loads(jar.read('META-INF/jarjar/metadata.json'))
                entries = [j for j in meta['jars'] if 'infiniverse' in j['path'].lower()]
                if len(entries) != 1: raise SystemExit('Expected exactly one nested Infiniverse')
                nested = jar.read(entries[0]['path'])
                (destination / 'infiniverse-1.0.0.5.jar').write_bytes(nested)
                report.append({'source':spec['source']+'!/'+entries[0]['path'], 'file':'infiniverse-1.0.0.5.jar',
                               'sha256':hashlib.sha256(nested).hexdigest()})
    (destination / 'verified-inputs.json').write_text(json.dumps(report,indent=2)+'\n')
    print('Prepared',len(report),'verified build inputs')

if __name__ == '__main__': main()
