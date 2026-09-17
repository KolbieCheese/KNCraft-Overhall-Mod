"""Migrate the existing external guide declaration to the JAR's bundled resources.
Defaults to preview. --apply backs up only book.json and then updates it. No worlds are touched.
"""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import time

ROOT = Path(__file__).resolve().parents[1]

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--instance", type=Path, required=True)
    p.add_argument("--apply", action="store_true")
    args = p.parse_args()
    guide = args.instance.resolve() / "patchouli_books/kncraft_guide"
    book = guide / "book.json"
    if not book.exists():
        print("No legacy guide: KNCraft Architecture creates the declaration when the game starts."); return
    data = json.loads(book.read_text(encoding="utf-8-sig"))
    if data.get("use_resource_pack"):
        print("Guide already uses bundled resources; nothing changed."); return
    source = json.loads((ROOT / "docs/Guide-Provenance.json").read_text())["source_files"]
    modified = []
    for relative in source:
        if relative.startswith("book/") and relative != "book/book.json" and not (guide / relative[len("book/"):]).is_file():
            modified.append("missing: " + relative[len("book/"):])
    for file in guide.rglob("*"):
        if not file.is_file(): continue
        relative = file.relative_to(guide).as_posix()
        if relative == "book.json": continue
        if hashlib.sha256(file.read_bytes()).hexdigest() != source.get("book/" + relative): modified.append(relative)
    if modified:
        raise SystemExit("Guide content differs from the imported baseline. Preserve these edits in a resource pack before migrating:\n" + "\n".join(modified))
    print("Set use_resource_pack=true; keep patchouli:kncraft_guide and all original content on disk.")
    if not args.apply:
        print("Preview only. Close Minecraft/server, then repeat with --apply after installing the matching mod."); return
    backup = args.instance / "kncraft-guide-backups" / time.strftime("%Y%m%d-%H%M%S")
    backup.mkdir(parents=True, exist_ok=False)
    shutil.copy2(book, backup / "book.json")
    data["use_resource_pack"] = True
    data["version"] = max(int(data.get("version", 8)), 9)
    temporary = book.with_suffix(".json.tmp")
    temporary.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    temporary.replace(book)
    print("Updated declaration. Backup:", backup / "book.json")

if __name__ == "__main__": main()
