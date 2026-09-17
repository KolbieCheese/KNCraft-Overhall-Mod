# Cohesion update implementation checklist

Accepted scope: the integration audit, including tent climate and temperature-aware
feeding; exclude new Skyroot/BOP building variants. Quests record accomplishments
without progression locks. Integrate documentation into the existing chapters and
cookbook. Preserve world IDs, existing items, tent ownership and administrator data.

- [x] Tent exterior climate, enclosure/hearth compatibility, all shapes/sizes, persistence and moves.
- [x] Ingredient bridges, targeted broken-recipe cleanup and cooking-oil rail fuel.
- [x] Elytra Slot flight enchantments, special weapon profiles, companion protection.
- [x] Aether cooling fuels/accessories/freezer recipes, capped effects and temperature-aware feeding.
- [x] Themed loot, restrained trades, wildlife food and demonstrated ecology gaps.
- [x] Optional FTB accomplishment journal: bosses, structures and mod advancements; stable IDs.
- [x] Existing guide chapter integration and per-food cookbook effects; remove separate implementations chapter.
- [x] Automatic GitHub build/version/release on default-branch pushes; PR builds do not publish.
- [x] README overview, migration/config documentation, artifact and isolated runtime verification.

The installed pack and play worlds remain source inputs. Test only in isolated
directories. Minecraft EULA approval for the isolated server was already provided.
GitHub destination: https://github.com/KolbieCheese/KNCraft-Overhall-Mod.
The default-branch workflow builds and publishes incrementing versions. No new energy network or building models are in scope.

Physical-client rendering, movement and UI acceptance remain separate manual checks;
see Validation.md. The completed implementation checklist is not a claim of full-pack
multiplayer or graphics certification.
