# Feature state and tuning

| Connection | Behavior and defaults |
|---|---|
| Nomadic Tents + Cold Sweat | Campsite ambient climate, bounded enclosed interiors, native hearth fuel/range/warm-up; enabled |
| Pam's + wildlife + Aether | Selected real eggs, milk, ordinary berries, general meat/fish ingredients; enabled |
| Food + climate + backpacks | Nine thermal foods; latest effect replaces previous; feeding avoids the wrong sign beyond BODY +/-20 unless hunger is 6 or lower |
| Aether + climate | Ice accessories give 2 heat insulation; ordinary colored capes 0.5 cold/0.5 heat; combined managed heat cap 4; Icestone 100 cooling fuel |
| Aether Freezer + Pam's | Prepared apple/melon/sweetberry juice becomes one smoothie in 200 ticks with native fuel |
| Farming + rail | Two cooking oil and one bottle make native bio-diesel; existing locomotive accounting and glass-bottle return |
| Flight equipment | More Enchantments reads usable chest elytra, otherwise Elytra Slot; normal armor checks remain on armor |
| Better Combat | Valkyrie Lance reach 6.5; owned companions protected from cleave even when their owner is unloaded |
| Exploration | Modest additive supplies in 22 named chest tables; four finite-stock farmer/butcher sales |
| Ecology | Fresh Pam's berries feed crows/raccoons without becoming taming ingredients; missing savanna garden biome tag bridged |
| Carry On | Tent doors and hearth halves use normal packing/dismantling; native tag cache refreshed after data loads; normal storage stays movable |
| Resource repairs | Three invalid Food Core recipes disabled; three missing seed modifiers remain no-ops; seven broken tag contributors made optional |
| FTB journal | 15 chapters, 339 independent records; bosses, structures and native advancements; no rewards, locks or item turn-ins |
| Field Guide | Original chapters and individual Cook Book entries explain the changes; historical book ID and artwork retained |
| Original integrations | Native portal lighting, bounded Depth arrivals, tent synchronization, performance fixes, Waystones and encounter scaling retained |

`kncraft-integrations.toml` supplies independent switches for the new connections.
Fresh installations enable the original cotton/wildlife/meal/fiber features too.
Existing `kncraft-common.toml` values remain authoritative, including previous opt-outs.
Restart after changing module switches. See [validation](Validation.md) for actual test scope.

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

## Existing systems retained

Insulated minecarts already support Better Minecarts linking. Comforts calls the Forge
sleep event used by Cold Sweat; the installed pack explicitly exempts sleeping bags.
Inventory Totem checks ordinary player inventory, not nested backpacks or Curios.
Easy Anvils and JRFTL provide existing repair/leather routes. FallingTree's 100-log scan
limit and Pam's right-click fruit harvest remain intact. Better Combat already has
fallback profiles for Depth claymores/scythes, and reads attack speed for timing;
Furor's native attribute behavior and Alex's active-use nunchaku are not replaced by
blanket weapon profiles. These connections are documented where players use them.

No new energy network, world regeneration, forced quest route, new dimension, or
Skyroot/BOP building variant is introduced.
