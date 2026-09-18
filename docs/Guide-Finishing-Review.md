# Guide finishing review and evening capture checklist

Reviewed 17 September 2026 against released source `89b5301` (1.0.1), the installed
KNCraft Field Guide, installed mod resources, and the existing screenshot directory.
The review below records the proposals that preceded 1.0.2. The user approved all
except expedition loadouts/profiles. Implementation and validation are documented
in Reference-Validation.md. No expedition equipment profiles will be supplied.

## What is actually missing

Updated 18 September 2026: five tent photographs now illustrate both campsites,
the furnished interior and the hearth off/on comparison. Native HUD close-ups make
the cold-versus-conditioned example readable. The Symbiont weak point and Bowels
entrance are complete as written instructions at the user's request; no encounter
photographs are still required. The remaining checklist has **four groups / seven
captures**: packed Tent ID (1), Deflector (1), Depths (2), and kitchen/recipes (3).
The tent photographs also complete the optional tent-climate sequence.

The following paragraph records the earlier 17 September inventory:

The original guide had seven screenshot-request sections. The supplied BlueMap
photo now fills its request, showing **three Wither Storm markers**. Its original JPEG
is preserved with a native overview and closer view. The six remaining groups need
**13 captures at minimum**, counting a final meal recipe and one intermediate separately. A longer meal chain can
need more. All **27 existing `capture_*.png` images** are present, and no referenced
local KNCraft guide image is missing. Player-facing capture prompts have been replaced with useful instructions; the
remaining photography backlog is tracked here and in Guide-Capture-Backlog.json.

The following filenames are suggested names for new originals. Keep full-resolution
PNGs; the names are not pre-existing texture paths.

## Original capture checklist

### Welcome to KNCraft: BlueMap — 1 capture

Guide anchor: `chapters/intro#shot_map`.

- [x] Supplied **Photo 1.jpg**: the three-storm BlueMap example is integrated.
  No retake is required for this checklist. It is an example rather than a live count.

### Wither Storm: Symbiont opening — completed as text

Guide anchor: `chapters/wither#shot_symbiont`.

- Cancelled by user, 18 September: `symbiont_exposed_back.png`. Original request: the weakened posture from behind, with the purple
  weak point large and unobstructed. Prefer a clean scene without chat.
- Cancelled by user, 18 September: `symbiont_encounter_hud.png`. Original request: the same encounter with the boss bar visible.
  Record the nearby player count and, if available, the actual maximum-health value
  separately. A normal percentage bar alone does not establish maximum-health
  scaling, so the eventual caption must not claim that it does.

The finished Wither Storm chapter explains the weakened posture, moving behind the
Symbiont, striking the exposed purple back and resuming evasive play when it recovers.

### Wither Storm: Find the entrance — completed as text

Guide anchor: `chapters/wither#shot_bowels`.

- Cancelled by user, 18 September: `storm_entrance_overview.png`. Original request: show the complete open entrance above the main
  head at a useful distance, with enough of the head to establish its location.
- Cancelled by user, 18 September: `storm_entrance_approach.png`. Original request: show a clear approach/aiming position. Keep the
  opening identifiable, chat hidden and motion blur off. Include the crosshair if
  it helps explain the aim; the caption should describe the demonstrated position
  without promising immunity to the encounter's attacks.

### Weather2: Weather to recognize — 1 capture

Guide anchor: `chapters/weather#shot_weather`.

- [ ] `weather_deflector_setup.png`: A placed Deflector with nearby shelter/ground
  visible, making its placement easy to copy. No temperature HUD is needed.

Storm, tornado, snowstorm and siren examples are already included.

### Call from the Depths: Gateway and first view — 2 captures

Guide anchor: `chapters/depths#shot_depths`.

- [ ] `depths_gateway_inactive.png`: The gateway before activation, with the frame
  clearly visible. The activated gateway is already illustrated.
- [ ] `depths_arrival_landscape.png`: A brighter, readable first-arrival view with
  recognizable footing and a route forward. Keep bosses and major story reveals
  out of frame. No temperature HUD is needed for this page.

### Shelter, storage and travel: One camp, two locations — 1 capture left

Guide anchor: `chapters/travel#shot_tent`.

- [x] `TentLocation1Interior.png`, with `TentLocation1Exterior.png`: A furnished interior with storage, a compact
  kitchen, temperature supplies, and a clear door. Use a layout with recognizable
  landmarks so the second interior picture can be compared easily.
- [ ] `tent_packed_id.png`: Hover the packed tent item with its Tent ID visible.
  Keep the tooltip and relevant inventory area readable.
- [x] `TentLocation2InteriorHearthOff.png` and `TentLocation2InteriorHearthOn.png`,
  with `TentLocation2Exterior.png`. Original request: pitch that same tent elsewhere and photograph
  the same interior from approximately the same viewpoint. Retain the arrangement
  so readers can see what survived packing and moving. Note the new campsite
  separately; an optional exterior picture can establish the new location.

### Pam's HarvestCraft: A compact traveling kitchen — 3 or more captures

Guide anchor: `chapters/food#shot_food`.

- [ ] `travel_kitchen_supplies.png`: Show a practical kitchen and its reusable tools
  and ingredients. The tent used above is an efficient place to stage it. A clear
  inventory/container view is useful if the tools cannot be recognized in the scene.
- [ ] `travel_meal_jei.png`: JEI's recipe for a recommended repeatable meal, with the
  result and ingredient layout legible. A managed warming meal or cooling drink
  would also illustrate the new climate integration.
- [ ] `travel_meal_intermediate_01.png`: The JEI recipe for an ingredient made in
  another step. Add `_02`, `_03`, etc. only for meaningful additional steps.

Cotton and fruit-tree images are already included. A tooltip showing the selected
meal's active temperature effect can also serve the optional list below.

## Optional images for the new integrations

These are new suggestions, not additional old placeholders. Prioritize the tent
climate comparison because it explains the update's most distinctive behavior.

- [x] **Tent climate:** The supplied second-campsite exterior, hearth-off and hearth-on
  photos are integrated. Original request: outside temperature and then the interior temperature before
  conditioning, followed by the powered hearth warming the occupant. Keep the
  temperature HUD visible and conditions close together in time. Record
  `/kncraft camp` output separately for the caption; a clean picture and a diagnostic
  capture can be different files. Readings need not be instantaneously identical.
- [ ] **Cotton sewing:** A close-up of cotton in the Sewing Table and the resulting
  equipment tooltip. This connects the existing cotton photograph to a practical use.
- [ ] **Altar repair:** The named, insulated item before and after an actual fueled
  repair, with matching enchantment/insulation information visible. Use one of the
  nine supported expedition items, rather than implying that every item is supported.
- [ ] **Backpack water:** The placed tank backpack and a before/after tank amount
  while filling an empty waterskin. Keep the UI legible; the caption can explain
  sneak-use and the 250 mB cost.
- [ ] **Thermal meal:** A relevant food tooltip showing its active effect alongside
  a matching Cookbook or JEI page. It can reuse the meal from the kitchen sequence.
- [ ] **Journal:** A readable overview with some records completed and others open,
  illustrating optional tracking without rewards or progression locks.

A short clip would explain wildlife magnet protection better than one still image:
drop accepted food near the animal, let it collect it, and show that ordinary loot
still goes to the magnet. This is optional and not required to finish the book.

## Capturing efficiently

1. Set up the furnished tent/kitchen first. Capture the kitchen, tent move, item
   tooltip, and optional climate/waterskin/sewing sequence during the same session.
2. Capture the Deflector. The BlueMap image is already received.
3. Capture the inactive Depths gateway and arrival landscape.
4. No Wither Storm encounter photographs are needed; those instructions are complete as text.

The installed screenshot binding is **F2**. Originals are saved under
`C:\Users\maste\curseforge\minecraft\Instances\KNCraft\screenshots`.
Use a clean HUD-hidden view for scenery and construction; retain the HUD, tooltip
or GUI when it is the subject. Keep shaders consistent across before/after pairs;
readable lighting matters more than dramatic darkness for instructional images.

The current `kn_image` template displays a 512-by-288 image region at about
116-by-65 book units, inside a 512-by-512 texture. That is a wide 16:9 presentation
area. Keep the subject large and allow room for a wide crop. There is no need to
resize originals or add padding yourself. UI/recipe details deserve separate close
views because a full-screen GUI shrinks too much at that book size.

Existing coverage includes the Wither Storm overview; four weather images; Aether
entrance/return; activated Depths gateway; six portal/climate/hearth images; cotton
and fruit; and eleven exploration/wildlife/distant-terrain images. These should not
be retaken merely because the chapter also has a placeholder.

## Original proposals and decisions

### 1. Reserve curative food from automatic feeding

The food chapter already recommends excluding Golden Apple Stew and other reserved
supplies from backpack feeding. The current `FeedingClimateMixin` enforces only
thermal suitability. A small, configurable reserved-food list could keep
`witherstormmod:golden_apple_stew` for deliberate use while leaving the player free
to opt out. Prefer a native feeding filter where practical; otherwise extend the
existing eligibility hook. Preserve actual manual consumption and test the normal
food/hunger fallback explicitly. This is a proposal, not a reproduced loss report.

### 2. Optional expedition equipment profiles — declined

**Excluded by the user. No profiles, kits or expedition loadouts will be installed.**
The only starter gift is one Field Guide. Original proposal for provenance:

Use installed Inventory Profiles Next for Cold Travel, Hot Travel, Building and
Storm Preparation presets, with a short Travel-chapter explanation of locked slots
and restocking. The installed version contains equipment profiles and per-server
profile support; its [official profile documentation](https://inventory-profiles-next.github.io/en/profiles/)
also explains equipment/hotbar profiles and selection rules. Configuration editing
is involved, so curated examples would remove some setup work.

Keep profiles optional and preserve players' configurations. Validate named,
enchanted and insulated equipment selection; do not assume the mod can manage
Curios slots or distinguish warm and cold waterskins without testing. IPN's slot
locks should not be described as controls for Sophisticated's separate automation.

### 3. Server-aware guide values

Where the server exposes synchronized values, render actual food effects, insulation
and recipe information in the relevant pages. Keep a clearly labeled default when
the effective value is unavailable. Current thermal recipe pages print the pack
defaults, although they tell readers that administrators can change them.
[Patchouli component processors](https://vazkiimods.github.io/Patchouli/docs/patchouli-advanced/component-processors/)
can provide page data from code, and the processor interface is present in the
installed Patchouli 85 JAR. A prototype should prove server/client consistency
before this is described as live information.

### 4. Journal-to-guide navigation

Current generated quest descriptions name a Field Guide chapter as plain text.
An actual link/button opening the relevant guide entry would connect accomplishment
tracking to instruction, especially for RopeBridge, climate equipment and bosses.
FTB's installed UI has an Open-in-Guide action, but its compatibility with this
Patchouli book needs verification; do not assume that field alone implements it.
Keep all records independent and reward-free.

### 5. Finish the book as a player-facing reference

Replace author capture prompts with the finished pictures and captions. Consolidate
short continuation pages, including the one-word "route." page before the Depths
placeholder, after adding the new photographs. Use close-up diagrams/callouts for
working hearths and kitchen steps, with text that remains understandable without
color alone. Generate a missing-image/unfinished-placeholder report during builds.

Prioritize these navigation, clarity and automation improvements. The 1.0.1 release
already covers the previously approved gameplay connections; further balancing
should follow the first real multiplayer expedition and physical-client checks.

## Original review provenance (before the approved finishing pass)

- Original and updated guide: all seven placeholder anchors and request text match.
- Existing guide: 27 `capture_*.png` assets; all referenced local KNCraft image paths
  resolve. This is a resource check, not certification of in-game layout.
- Inspected image examples: current hearth inventory and activated Depths gateway;
  these support the recommendations for close UI crops and readable cave lighting.
- Existing screenshot filenames were inventoried; unnamed historical screenshots
  were not exhaustively classified. The checklist describes the guide's remaining
  requests, not proof that no unused screenshot anywhere could satisfy one.
- Gameplay ideas were compared with 1.0.1 source and relevant installed JAR resources.
  No new gameplay implementation, release, or live-world edit was made for this review.
