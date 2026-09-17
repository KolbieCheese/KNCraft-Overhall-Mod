# KNCraft Compatibility

A gameplay integration mod for **Minecraft 1.20.1, Forge 47.4.0, and Java 17**.
It connects the systems in the KNCraft modpack so farming, climate, camping,
exploration, equipment, and transport support one another.

- **Farming and survival:** Pam's crops and Alex's wildlife supply clothing insulation,
  camping materials, and practical food. Selected meals affect Cold Sweat, and backpack
  feeding avoids counterproductive thermal meals when the player is already hot or cold.
- **Camping and climate:** Nomadic Tents inherit their campsite's ambient temperature.
  Their interiors count as enclosed for Cold Sweat hearths, while native fuel, warm-up,
  range, and air-path rules continue to apply. Seamless entrances retain tent ownership,
  packing, and the dimension synchronization repair. Selected BOP/Terralith climates
  get deliberate defaults, and `/kncraft camp` explains the current campsite conditions.
- **Ingredients and transport:** Aether milk, ordinary berries, and animal eggs work in
  appropriate Pam's recipes; selected Alex's meats join general cooking ingredients.
  Cooking oil can supply Better Minecarts diesel, and the Aether Freezer chills prepared juices.
- **Exploration and equipment:** Aether accessories offer bounded climate protection.
  The Valkyrie Lance retains its special reach in Better Combat, flight enchantments
  recognize Elytra Slot, and companion ownership protects against accidental cleave.
  Selected structure chests and village trades offer modest, relevant supplies.
  Selected expedition armor can be maintained at Aether altars without losing its
  sewn insulation. Enchantment attributes cooperate with other equipment bonuses,
  and backpack magnets give nearby wildlife time to collect deliberate offerings.
- **A guide to the whole pack:** The existing Patchouli Field Guide explains these
  connections within its original chapters, with thermal effects on individual Cook Book
  pages. The FTB accomplishment journal records bosses, landmarks, and mod advancements
  without recipe locks, required quest chains, item turn-ins, or rewards.
  Each chapter includes relevant controls that display the player's current bindings.

The mod also retains native Aether/Depth portal lighting, bounded Depth arrivals,
natural Waystone policy data, one-time encounter scaling, and the original targeted
performance fixes. It also repairs verified tag/recipe gaps and keeps linked campsite
structures on their native packing paths. New building blocks and Skyroot/BOP building variants are outside
this update's scope.

## Install

Download **`KNCraftCompatibilityMod-<version>.jar`** from
[GitHub Releases](https://github.com/KolbieCheese/KNCraft-Overhall-Mod/releases).
Install the same version on the **server and every client**, alongside the pack's
upstream mods. Patchouli supplies the guide UI; FTB Quests supplies the journal UI.
Third-party mods are not bundled in this JAR. The repository is currently private, so
GitHub downloads require repository access.

Read [migration and rollback](docs/Migration.md) before replacing earlier helpers or
datapacks. Existing guide books keep the ID `patchouli:kncraft_guide`; a legacy external
guide needs the included migration tool to receive the bundled pages. Quest installation
preserves administrator-edited files and never resets player or team progress.

The optional ZIP includes the same JAR plus installation documentation and migration tools.
Never install an `ISOLATED-TESTS` JAR in a play world.

## Configure

Fresh installations enable the cohesive pack defaults. Existing administrator settings
are retained, including earlier opt-outs in `kncraft-common.toml`. This means an upgrade
from 0.2 may still have cotton, wildlife insulation, thermal meals, and fiber recipes disabled.

`config/kncraft-common.toml` controls the original integrations and tunable food/material
lists. `config/kncraft-integrations.toml` controls the expanded connections separately.
`config/kncraft-polish.toml` controls regional climate, wildlife offerings, altar repairs
and cooperative enchantment attributes. See [migration details](docs/Migration.md)
for legacy attribute handling and its backup.
Restart after changing switches. Existing Cold Sweat definitions take precedence over
the supplied defaults; the mod does not rewrite upstream configuration.

Use `/kncraft status` or `config/kncraft-status.txt` to inspect enabled integrations.
See [features and behavior](docs/Features.md) and [1.0.1 validation](docs/Polish-Validation.md) for
details and the limits of the completed checks.

The client command `/kncraft controls` reports possible key conflicts. Players may apply
`/kncraft controls preset` for an optional pack layout or use `/kncraft controls restore`
to undo its unchanged assignments. Installing the mod never applies the preset automatically.

## Build and automatic releases

Set `JAVA_HOME` to a Java 17 JDK and use Python 3.11 or newer:

```powershell
python tools/bootstrap_dependencies.py --download
.\gradlew.bat build releaseBundle --console=plain
python tools/validate_artifact.py --jar build/libs/KNCraftCompatibilityMod-1.0.1.jar
```

Alternatively, bootstrap from the installed pack with `--instance "C:\path\to\KNCraft"`.
Both modes verify the exact dependency hashes. On Linux/macOS use `bash gradlew`.

Every push to the repository's default branch builds, tests, and publishes a GitHub
Release containing the downloadable JAR, installation ZIP, and checksums. Version numbers
start at `1.0.0` and increment the patch number from existing release tags. Rerunning a
released commit reuses its version and preserves published assets. Pull requests and
other branches produce downloadable development artifacts without publishing releases.

The workflow uses GitHub-hosted runners and the built-in repository token; it does not
need a private runner or an installed Minecraft profile. See [build details](docs/Build.md),
[release behavior](docs/Releases.md), [changelog](CHANGELOG.md), and [attribution](NOTICE.md).
