# Feature state and tuning

| Module | Implemented | Default | Verified here | Still required |
|---|---|---|---|---|
| Native Aether/Depth lighting | Retained adapters and generator IDs | On | Four native lighting/conversion cases, small/large frames | Real player crossings/rendering |
| Bounded Depth placement | Retained full room/frame bounds | On | Both axes, negative/high targets, large frames, bedrock, unsafe sources | More occupied/fluid failure fixtures; real client |
| Tent synchronization | Vanilla ping/pong barrier | On with pinned dependencies | Correct/wrong/stale/missing acknowledgements and disconnected-client cleanup | Connected physical clients, late login |
| Tent entrances | Ownership, safety, packing/move tokens | On | Four facings, packing, saved contents/links after restart, permissions and server-entity round trip | Physical rapid return/prediction |
| Performance | Three independent original optimizations | On | 10,000 selection cases, 135 live portal comparisons, real item goal, 64 tornado comparisons | Full-pack profiling is not claimed |
| Waystone policy | Original recipes, loot and protection tags | On | Byte-for-byte data parity | Player break/carry/explosion checks; retain upstream restrictions |
| Encounters | Persistent modifiers and tags | On | Scope/marker unit tests; no repeated healing in runtime harness | Full boss phase/progression playthrough |
| Cotton/wildlife insulation | Cold Sweat registry API | Off | Real registry values and repeat-load idempotence | Sewing UI, removal, tooltip/slot rendering, old equipped armor |
| Thermal meals | Nine curated entries; one shared effect | Off | Real consumption-finish events, hot/cold replacement, refresh, reload | Complete manual eating/container and backpack-feeding paths |
| Fiber recipes | Cotton canvas and cotton rope | Off | Real recipe matching/output, material conservation | JEI presentation and player crafting |
| Field Guide | Existing complete guide + 17 new pages | Available with Patchouli | Registered historical book ID; asset/JSON checks | Physical client pagination, textures, links, bookmarks |

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

## Roadmap, not active functionality

Aether icestone fuel/accessories, thematic loot, optional quests, campsite-derived tent climate,
and temperature-aware backpack feeding remain proposed. They are not advertised as implemented.
The Aether +0.7 offset and existing Weather2 temperature support remain externally owned and
unchanged. No double storm modifier, progression lock, mandatory hunting route or automatic
world regeneration was introduced.
