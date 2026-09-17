# External settings

The runtime JAR includes the portal definitions, Waystone recipes/loot/protection tags,
optional fiber recipes, and the complete Field Guide assets. Do not reinstall the old datapacks.

Upstream configuration stays externally owned. Preserve the installed `config/` and each
world's `serverconfig/`. For a fresh server, review the captured gameplay configuration in
`reference/handoff/live-reference`; merge intended values, never overwrite an existing profile.

`waystones-policy.toml.fragment` records the required natural-network restrictions. Merge its
four keys into the existing restrictions table. The bundled data alone cannot prevent a player
from breaking a generated stone if the upstream protection config is disabled.

Patchouli 85 requires the small external `patchouli_books/kncraft_guide/book.json` declaration
to retain this pack's old book ID. Architecture creates it if absent. For an existing guide,
run `tools/migrate_guide.py --instance PATH` to preview, then repeat with `--apply` while the
game/server is stopped. It backs up the declaration and refuses changed content. It does not
install the mod. Existing artwork packs can remain; the same artwork is now also bundled.

Resetting a world does not delete the global guide declaration, mod configuration or mod JAR.
New worlds receive bundled gameplay data automatically. They still need the intended upstream
server settings in `defaultconfigs/` or their new `serverconfig/`; the mod does not copy old
world-specific settings or regenerate tent dimensions.
