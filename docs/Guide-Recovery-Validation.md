# Guide recovery validation — 1.0.3

Validated 17 September 2026 with Java 17 and Forge 47.4.0 in the existing marked,
loopback-only server fixture. The live modpack and play worlds were not changed.

`/guide` and `/kncraft guide` use the same delivery path. Both are available at
permission level zero. They bypass the automatic-gift switch and prior receipt,
recognize a carried Field Guide (including offhand), and place one book in an empty
main-inventory/hotbar slot. Full inventory and missing Patchouli produce explanatory
messages; console use requires a player. Delivery synchronizes inventory changes and
marks the ordinary receipt to avoid an extra automatic gift.

The actual registered Brigadier commands passed before and after `/reload`:

- A non-operator recovered a book after already receiving the first-join gift.
- Placement used the first empty slot and preserved the occupied diamond stack.
- Repeated use and the alias did not duplicate an inventory/offhand copy.
- A full inventory remained unchanged; freeing slot 17 allowed recovery into that slot.
- Recovery still worked with automatic first-join gifts disabled.
- A different Patchouli book was preserved and did not block the KNCraft guide.
- Console invocation returned the normal player-required command error.

The existing first-join, full-inventory retry, persisted receipt, reference snapshot,
reserved-feeding and native journal-link checks also passed on both runs. The new
missing-Patchouli message is guarded by the registry check; a player command on a
Patchouli-free server was not separately exercised. No physical client was launched.

Java compilation, reobfuscation, release packaging, 13 JUnit tests and three version
allocation tests passed. The resource audit checked 1,482 JSON resources and found no
missing guide image, broken guide link or unfinished player-facing prompt. Book version
13 includes recovery instructions in the existing Welcome chapter.

Selected runtime output:

```text
[13:26:52] GUIDE COMMAND PASSED: permission zero, received/lost guide, empty-slot placement, repeated and offhand copies, full inventory, retry, alias, disabled automatic gift, other book and console rejection
[13:26:52] REFERENCE CHECKS PASSED
[13:26:58] GUIDE COMMAND PASSED: permission zero, received/lost guide, empty-slot placement, repeated and offhand copies, full inventory, retry, alias, disabled automatic gift, other book and console rejection
[13:26:58] REFERENCE CHECKS PASSED
```
