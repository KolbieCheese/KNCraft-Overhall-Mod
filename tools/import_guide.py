"""One-time source import of the existing Field Guide; never edits the pack.
Refuses to replace source already imported. Subsequent edits belong in src/main/resources.
"""
import argparse
import hashlib
import json
from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parents[1]

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--instance", type=Path, required=True)
    args = p.parse_args()
    guide = args.instance / "patchouli_books/kncraft_guide"
    resources = ROOT / "src/main/resources"
    destination = resources / "assets/patchouli/patchouli_books/kncraft_guide"
    if destination.exists():
        raise SystemExit("Guide already imported; edit the source, do not overwrite it")
    book = json.loads((guide / "book.json").read_text(encoding="utf-8-sig"))
    book["use_resource_pack"] = True
    book["version"] = 9
    book["subtitle"] = "KNCraft | Architecture 0.2"
    declaration = resources / "data/patchouli/patchouli_books/kncraft_guide/book.json"
    declaration.parent.mkdir(parents=True, exist_ok=True)
    declaration.write_text(json.dumps(book, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    shutil.copytree(guide / "en_us", destination / "en_us")
    artwork = args.instance / "resourcepacks/KNCraft-Guide-Resources/assets"
    shutil.copytree(artwork, resources / "assets", dirs_exist_ok=True)
    report = {"book_id": "patchouli:kncraft_guide", "source_version": 8, "source_files": {}}
    for prefix, directory in [("book", guide), ("artwork", artwork)]:
        for file in sorted(directory.rglob("*")):
            if file.is_file():
                report["source_files"][prefix + "/" + file.relative_to(directory).as_posix()] = hashlib.sha256(file.read_bytes()).hexdigest()
    report["preserved_chapter_pages"] = {}
    for name in ["intro", "cold", "food", "animals", "travel", "aether", "depths"]:
        chapter = json.loads((guide / f"en_us/entries/chapters/{name}.json").read_text(encoding="utf-8-sig"))
        report["preserved_chapter_pages"][name] = [hashlib.sha256(json.dumps(page, sort_keys=True, separators=(",", ":")).encode()).hexdigest() for page in chapter["pages"]]
    (ROOT / "docs/Guide-Provenance.json").write_text(json.dumps(report, indent=2) + "\n")
    print("Imported", len(report["source_files"]), "guide/artwork files; original pack untouched")

if __name__ == "__main__":
    main()
