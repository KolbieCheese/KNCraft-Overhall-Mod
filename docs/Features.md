# Feature state and tuning

| Connection | Behavior and defaults |
|---|---|
| Nomadic Tents + Cold Sweat | Campsite ambient climate, bounded enclosed interiors, native hearth fuel/range/warm-up; enabled |
| Pam's + wildlife + Aether | Selected real eggs, milk, ordinary berries, general meat/fish ingredients; enabled |
| Food + climate + backpacks | Nine thermal foods; latest effect replaces previous; feeding avoids the wrong sign beyond BODY +/-20 unless hunger is 6 or lower |
| Backpack water supply | Native sneak-use on a placed water tank fills an empty waterskin for 250 mB; documented and tested without a duplicate adapter |
| Aether + climate | Ice accessories give 2 heat insulation; ordinary colored capes 0.5 cold/0.5 heat; combined managed heat cap 4; Icestone 100 cooling fuel |
| Aether Freezer + Pam's | Prepared apple/melon/sweetberry juice becomes one smoothie in 200 ticks with native fuel |
| Farming + rail | Two cooking oil and one bottle make native bio-diesel; existing locomotive accounting and glass-bottle return |
| Flight equipment | More Enchantments reads usable chest elytra, otherwise Elytra Slot; normal armor checks remain on armor |
| Better Combat | Valkyrie Lance reach 6.5; owned companions protected from cleave even when their owner is unloaded |
| Exploration | Modest additive supplies in 22 named chest tables; four finite-stock farmer/butcher sales |
| Ecology | Fresh Pam's berries feed crows/raccoons without becoming taming ingredients; missing savanna garden biome tag bridged |
| Carry On | Tent doors and hearth halves use normal packing/dismantling; native tag cache refreshed after data loads; normal storage stays movable |
| Resource repairs | Three invalid Food Core recipes disabled; three missing seed modifiers remain no-ops; seven broken tag contributors made optional |
| FTB journal | 17 chapters, 344 independent records including BOP and RopeBridge; bosses, structures and native advancements; no rewards, locks or item turn-ins |
| Everyday controls | Opt-in client preset, backup/restore and conflict report; all 11 guide chapters contain live key references |
| Enchantment attributes | Native strengths become transient additive modifiers; unchanged contributions are not recreated; bounded legacy migration |
| Wildlife offerings | Player-thrown items accepted by a visible Alex's animal within 8 blocks get 200 ticks of magnet grace; ordinary loot unchanged |
| Regional climate | 20 selected BOP/Terralith defaults only where no explicit absolute temperature exists; native climate/weather modifiers retained |
| Altar maintenance | 8 Cold Sweat armor pieces and Frontier Cap; native fuel, 700/1000 ticks, complete input data retained |
| Campsite inspection | `/kncraft camp` shows source, live/saved climate, bounded enclosure and up to four loaded climate appliances within 48 blocks |
| Field Guide | Original chapters and individual Cook Book entries explain the changes; historical book ID and artwork retained |
| Original integrations | Native portal lighting, bounded Depth arrivals, tent synchronization, performance fixes, Waystones and encounter scaling retained |

`kncraft-integrations.toml` supplies independent switches for the 1.0 connections;
`kncraft-polish.toml` controls the 1.0.1 additions and regional temperature list.
Fresh installations enable the original cotton/wildlife/meal/fiber features too.
Existing `kncraft-common.toml` values remain authoritative, including previous opt-outs.
Restart after changing module switches. See [1.0.1 validation](Polish-Validation.md) for actual test scope.

## Starting values

| Material | Cold | Heat | Use |
|---|---:|---:|---|
| Pam's cotton | 1 | 0.5 | Sewing ingredient |
| Bear fur | 1.5 | 0 | Sewing ingredient |
| Bison fur | 1.75 | 0 | Sewing ingredient |
| Kangaroo hide | 0.75 | 0.75 | Sewing ingredient |
| Frontier Cap | 3 | 1 | Built-in worn insulation; consumes available capacity |

Values are tuning defaults, not a physical simulation. Cold Sweat controls splitting/fill slots,
removal and persistence. The Frontier Cap's native behavior is not replaced. No global wool tags
are changed and no upgrade to Cold Sweat is performed.

Tea, coffee and nettle tea: +0.2 for 1,200 ticks. Carrot soup, potato soup and stew: +0.2 for
1,800 ticks. Melon smoothie and ice cream: -0.2 for 1,200 ticks. Apple juice: -0.1 for 600 ticks.
Duration-based food effects use Cold Sweat's BASE trait; they are not instant CORE changes of
20 temperature points. Only one managed KNCraft meal remains active, regardless of food or sign.
The newest replaces the previous. Existing external effects and waterskins are not removed.
Cold Sweat's native tooltips receive the definitions. Native hunger and container rules remain;
the runtime test confirmed Pam's hot tea cannot be used at full hunger.

Canvas: eight cotton around one wool -> one tent canvas. Since two cotton -> one string and
four string -> one wool, total cost equals the original two wool (16 cotton). Cotton can supply
the wool too; sheep are not mandatory. Rope: six cotton plus three vines -> eight rope, equivalent
to the original three string plus three vines. No reverse recipe or new intermediate item is added.

## Climate and administrator ownership

Tent samples include Cold Sweat's native ambient biome/dimension, time, cave/elevation,
shade and supported weather components, once each. Local appliances and other personal
modifiers continue inside. A loaded exterior sample is refreshed every 40 ticks by default.
If its surrounding chunks are unloaded, the saved sample is retained without force-loading
them. A tent without any valid saved/loaded sample temporarily uses native interior climate.
Moving or upgrading through the native door refreshes its source and bounds. The snapshot
is stored on the interior door, not in a new world registry.

Hearth enclosure covers all five styles, six sizes and expandable floor layers. Enclosure
does not extend a hearth's range, supply fuel, remove its redstone requirements or skip
warm-up. Only the template interior is enclosed; air paths cannot escape its bounds.

Explicit Cold Sweat JSON/TOML definitions take precedence. The accessory cap subtracts
only insulation added by KNCraft; administrator-defined accessories and other armor retain
their own values. The cap is heat insulation, not temperature immunity. The Aether's
existing climate and Weather2 support remain unchanged.

## Connected Field Guide (1.0.2)

Players receive one guide, with a carried-copy check and retry after a full inventory.
The receipt survives respawn and save/load. Every player can recover a lost guide with
`/guide` or `/kncraft guide`, without operator permission. Recovery puts the book in an
empty inventory slot, recognizes carried/offhand copies and asks for space when full.
It works even when the automatic first-join gift is disabled. The book also remains craftable. This is
the only starter gift: there are no expedition loadouts or equipment profiles.

Golden Apple Stew and enchanted golden apples are reserved from native backpack auto-feeding
by default, even at low hunger. The server can edit/disable the list and each player can
opt out with `/kncraft feeding reserved false`. Manual eating and normal eligible food
remain available; upstream backpack filters still apply.

The 344 independent journal records use FTB's native Open in Guide action to open an
existing Field Guide chapter through Patchouli. Only KNCraft's own guide routes are handled.
Quest IDs, rewards (none), dependencies (none) and administrator-file ownership stay intact.

Nine food definitions, five insulation definitions and twelve machine recipe times are
read on the server and sent to the requesting player's open reference pages. The client
refreshes every five seconds and marks its fallback values explicitly. Requests contain
no arbitrary item IDs or commands, are rate-limited, and never alter gameplay settings.
Food/insulation descriptions are for plain items; native conditions and modifiers can
change the actual effect. Machine recipe ingredients/results remain available in JEI.

The original BlueMap JPEG supplies an overview and closer view of three storm markers.
Existing images remain unchanged; short captions are completed on their image pages.
Numbered hearth/kitchen illustrations use native item icons. Pending photography is kept
in an external checklist, with no author capture prompts in the player book.

## Existing systems retained

Insulated minecarts already support Better Minecarts linking. Comforts calls the Forge
sleep event used by Cold Sweat; the installed pack explicitly exempts sleeping bags.
Inventory Totem checks ordinary player inventory, not nested backpacks or Curios.
Easy Anvils and JRFTL provide existing repair/leather routes. FallingTree's 100-log scan
limit and Pam's right-click fruit harvest remain intact. Better Combat already has
fallback profiles for Depth claymores/scythes, and reads attack speed for timing;
Furor retains its configured strength through a cooperative attribute modifier;
Alex's active-use nunchaku retains its native behavior. These connections are
documented where players use them.

No new energy network, world regeneration, forced quest route, new dimension, or
Skyroot/BOP building variant is introduced.
