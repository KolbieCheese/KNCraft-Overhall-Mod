# Field Guide finishing validation — 1.0.2

Validated 17 September 2026 using Java 17, Forge 47.4.0 and the installed upstream
artifacts. Only marked, loopback test servers inside this checkout were started.
The live instance and play worlds were not changed. The owner previously approved
Minecraft's EULA for these isolated fixtures.

## Implemented scope

All finishing proposals except expedition equipment profiles/loadouts are implemented.
The only starter gift is one Field Guide. Controls presets remain optional key assignments,
not equipment presets. Gameplay explanations remain in the existing guide chapters and
food/equipment pages; no separate integration chapter was added.

The supplied three-storm BlueMap photo is embedded unchanged, with native Patchouli
texture coordinates for its overview and closer view. A SHA-256 provenance record
guards the original bytes. All prior artwork remains intact. Eighteen explicit editorial
replacements remove author prompts and consolidate short continuations while preserving
the original anchors. The native item-icon sequences explain hearth checks and kitchen
steps. No missing photograph has been replaced with a fabricated scene.

## Checks completed

- Java compilation, reobfuscation, release ZIP and 13 JUnit tests passed, including
  reference-cache replacement, expiration, disconnected fallback and server changes.
- Three release-version allocation tests passed. Resource validation checks 1,482 JSON
  resources, 344 independent records, all journal guide destinations, unchanged original
  content outside the editorial manifest, live-reference reachability and release isolation.
- The guide audit finds 42 distinct local texture references, no missing image and no
  player-facing unfinished prompt. All 26 live keys have a reachable page and fallback.
  Re-running the reference-guide generator makes no changes.
- The JPEG decoded to 1,280 × 595 RGBA pixels with LWJGL 3.3.1's STB decoder, the decoder
  used by this Minecraft version's `NativeImage.read`. This confirms image decoding,
  not a physical GPU-rendered book view.
- The full gameplay fixture passed `kncraftreferencetest` before and after resource reload.
  It verifies native Patchouli book identity, one-time delivery, carried offhand copies,
  full-inventory retry, disabled delivery, save/load persistence and respawn receipts.
- The actual Sophisticated feeding eligibility method rejects both reserved foods at
  emergency hunger, still accepts bread, and respects player opt-out, server opt-out and
  an edited reserve list. Both foods retain native edibility for manual consumption.
  Preference persistence is checked through respawn and saved-player reload.
- The effective Cold Sweat registry was changed to tea at +0.35 for 200 ticks and cotton
  at 3 cold / 2 heat. Snapshots reflected these overrides rather than pack defaults.
  Removing a food definition reports no matching effect; a zero-duration definition is
  labeled an instant core-temperature effect. Fixtures restore the original definitions.
- A native Aether repair recipe was replaced with a 250-tick version. The snapshot reported
  12.5 seconds; removing that recipe reported unavailable. NBT packet data round-tripped
  unchanged. The recipe manager is restored after the fixture.
- All 344 native FTB quests loaded their `guide_page` field with the correct route.
  The actual FTB/Architectury custom-click event invoked the registered bridge and selected
  the Food chapter. Foreign and unknown routes do not open a KNCraft chapter.
- The wider regression passed polish, expansion, cohesion, all 30 tent style/size climate
  cases, actual powered hearths, performance comparisons, tent acknowledgement gating
  and Depth height placement. Relevant expansion checks passed again after reload.
- The optional-mods-absent server started, reported all 26 reference keys, reloaded and
  stopped cleanly. No optional client or gameplay class was required at server startup.

Selected runtime output is preserved in [Reference-Validation-Evidence.txt](Reference-Validation-Evidence.txt).
The separate test harnesses remain excluded from the downloadable production JAR.

## Client acceptance still needed

No physical client was launched for this pass. Visually check the two BlueMap pages,
completed captions, item-icon sequences and refreshed reference pages at the intended GUI
scale/resource pack. Click Open in Guide in FTB's actual UI, change pages while a server
snapshot arrives, and reconnect to a differently configured test server. The native event,
route, authoritative data, packet representation and cache policy were exercised separately;
this is not a claim of end-to-end rendered client/network acceptance.

The client requests the allowlisted public snapshot every five seconds while a live-value
page renders; the server limits requests to one per 40 ticks per player. It never accepts
arbitrary recipe/item IDs or executes a command from these packets. The client labels a
fallback after 15 seconds without a valid snapshot and clears the cache on disconnect.

## Photography remaining

BlueMap is received. Six original groups need at least 13 photos: Symbiont (2), storm
entrance (2), Deflector (1), Depths gateway/arrival (2), tent relocation (3), and kitchen/
meal chain (3 or more). Full instructions and optional integration shots are retained in
[Guide-Finishing-Review.md](Guide-Finishing-Review.md) and the structured
[Guide-Capture-Backlog.json](Guide-Capture-Backlog.json). These are tracked editorial
tasks, not unresolved texture references or prompts inside the player book.
