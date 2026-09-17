"""Stage selected gameplay mods and mandatory dependencies into a marked isolated server.
Requires Python 3.11+ or tomli. Does not copy worlds, accounts, servers.dat or service settings.
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import shutil
import zipfile
try:
    import tomllib
except ImportError:
    import tomli as tomllib

ROOT = Path(__file__).resolve().parents[1]
TARGETS = {"patchouli", "immersive_portals", "nomadictents", "weather2", "witherstormmod", "alexsmobs", "cold_sweat",
           "pamhc2crops", "pamhc2foodcore", "pamhc2foodextended", "pamhc2trees", "aether", "callfromthedepth_",
           "waystones", "carryon", "ropebridge", "sophisticatedbackpacks", "geckolib",
           "elytraslot", "more_enchantments", "bettercombat", "betterminecarts", "ftbquests",
           "dungeons_enhanced", "structory", "t_and_t", "abridged", "comforts", "inventorytotem",
           "fallingtree", "easyanvils", "superbarrels", "ironchest", "biomesoplenty", "terralith", "tectonic"}  # Depth uses GeckoLib without declaring it mandatory.

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--instance", required=True, type=Path)
    p.add_argument("--server", required=True, type=Path)
    args = p.parse_args()
    if not (args.server / "KNCraft-ISOLATED-TEST-WORLD").is_file(): raise SystemExit("Refusing unmarked server")
    providers, files = {}, {}
    def inspect(data, parent):
        with zipfile.ZipFile(io.BytesIO(data)) as jar:
            if "META-INF/mods.toml" in jar.namelist():
                try: meta = tomllib.loads(jar.read("META-INF/mods.toml").decode("utf-8-sig"))
                except Exception: return
                files.setdefault(parent, []).append(meta)
                for mod in meta.get("mods", []): providers[mod["modId"]] = parent
            if "META-INF/jarjar/metadata.json" in jar.namelist():
                for entry in json.loads(jar.read("META-INF/jarjar/metadata.json"))["jars"]:
                    inspect(jar.read(entry["path"]), parent)
    for file in (args.instance / "mods").glob("*.jar"): inspect(file.read_bytes(), file)
    required = list(TARGETS); selected = set()
    while required:
        id = required.pop()
        if id in {"minecraft", "forge"}: continue
        if id not in providers: raise SystemExit("Missing dependency: " + id)
        file = providers[id]
        if file in selected: continue
        selected.add(file)
        for meta in files[file]:
            for deps in meta.get("dependencies", {}).values():
                for dep in deps:
                    if dep.get("mandatory") and dep.get("side", "BOTH") != "CLIENT": required.append(dep["modId"])
    hashes = {m["filename"]: m["sha256"] for m in json.loads((ROOT / "reference/handoff/live-reference/gameplay-mod-inventory.json").read_text())["mods"]}
    destination = args.server / "mods"; destination.mkdir(exist_ok=True)
    report = []
    for file in sorted(selected):
        digest = hashlib.sha256(file.read_bytes()).hexdigest()
        if file.name in hashes and digest != hashes[file.name]: raise SystemExit("Hash mismatch: " + file.name)
        shutil.copy2(file, destination / file.name)
        report.append({"file": file.name, "sha256": digest, "matches_handoff": file.name in hashes})
    (args.server / "test-mods.json").write_text(json.dumps(report, indent=2) + "\n")
    print("Staged", len(report), "gameplay/dependency JARs in", destination)

if __name__ == "__main__": main()
