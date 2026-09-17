"""Read-only version allocation. Run after fetching tags in the serialized release workflow."""
import argparse
import os
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]

def select_version(base, tags, matching=()):
    major, minor, minimum = map(int, base.split('.'))
    pattern = re.compile(rf'v({major}\.{minor}\.(\d+))$')
    existing = [m for tag in matching if (m := pattern.fullmatch(tag))]
    if existing: return max(existing, key=lambda m:int(m[2]))[1]
    patches = [int(m[2]) for tag in tags if (m := pattern.fullmatch(tag))]
    return f'{major}.{minor}.{max([minimum - 1] + patches) + 1}'

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--snapshot', action='store_true')
    args=parser.parse_args()
    base=re.search(r'^mod_version=(\d+\.\d+\.\d+)$',(ROOT/'gradle.properties').read_text(),re.M)[1]
    def git(*args): return subprocess.check_output(['git',*args],cwd=ROOT,text=True).splitlines()
    if args.snapshot:
        run=os.environ.get('GITHUB_RUN_NUMBER','0')
        if not run.isdigit(): raise SystemExit('Invalid workflow run number')
        version=base+'-dev.'+run
    else:
        version=select_version(base,git('tag','--list'),git('tag','--points-at','HEAD'))
    print(version)
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'],'a',encoding='utf8') as output:
            output.write(f'version={version}\ntag=v{version}\n')

if __name__=='__main__':main()
