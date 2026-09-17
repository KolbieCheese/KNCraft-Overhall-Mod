"""Copy hash-verified build inputs from a legally installed KNCraft profile.

Usage: python tools/bootstrap_dependencies.py --instance PATH
Never downloads, redistributes or edits the supplied profile.
"""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
INPUTS = {
    "immersive-portals-3.0.7-all.jar": "immersiveportals-3.0.7.jar",
    "Nomadic-Tents-20.1.1.jar": "nomadictents-20.1.1.jar",
    "weather2-1.20.1-2.8.3.jar": "weather2-2.8.3.jar",
    "ColdSweat-2.4.3.jar": "coldsweat-2.4.3.jar",
}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--instance", required=True, type=Path)
    args = parser.parse_args()
    inventory = json.loads((ROOT / "reference/handoff/live-reference/gameplay-mod-inventory.json").read_text())
    hashes = {m["filename"]: m["sha256"] for m in inventory["mods"]}
    destination = ROOT / "libs"
    destination.mkdir(exist_ok=True)
    report = []
    for name, target in INPUTS.items():
        data = (args.instance / "mods" / name).read_bytes()
        digest = hashlib.sha256(data).hexdigest()
        if digest != hashes[name]:
            raise SystemExit("Hash mismatch for " + name + "; do not upgrade pinned inputs silently")
        (destination / target).write_bytes(data)
        report.append({"source": name, "file": target, "sha256": digest})
        if name == "Nomadic-Tents-20.1.1.jar":
            with zipfile.ZipFile(args.instance / "mods" / name) as jar:
                meta = json.loads(jar.read("META-INF/jarjar/metadata.json"))
                entries = [j for j in meta["jars"] if "infiniverse" in j["path"].lower()]
                if len(entries) != 1:
                    raise SystemExit("Expected exactly one nested Infiniverse")
                nested = jar.read(entries[0]["path"])
                (destination / "infiniverse-1.0.0.5.jar").write_bytes(nested)
                report.append({"source": name + "!/" + entries[0]["path"], "file": "infiniverse-1.0.0.5.jar", "sha256": hashlib.sha256(nested).hexdigest()})
    (destination / "verified-inputs.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    main()
