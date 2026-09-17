# KNCraft Architecture

A unified gameplay integration mod for **Minecraft 1.20.1, Forge 47.4.0 and Java 17**.
The principal artifact is `build/libs/KNCraft-Architecture-0.2.0.jar`.
This is a tested development candidate; physical-client acceptance remains outstanding.

## Included

- Native Aether/Depth lighting, bounded Depth arrivals (Y=5..122), tent dimension
  acknowledgement and seamless entrances, and the three original performance optimizations.
- Natural Waystone recipe/loot/protection data and idempotent encounter scaling.
- Opt-in cotton/wildlife insulation, nine curated thermal foods with one shared active effect,
  and two plant-fiber recipes with the same material costs as the existing conversion chains.
- The existing KNCraft Field Guide, cookbook, templates and artwork, plus a new gameplay
  chapter and cross-links. Its ID remains `patchouli:kncraft_guide`.
- A unified config, one-time legacy config import, duplicate-install protection and
  `/kncraft status` (also written to `config/kncraft-status.txt`).

Install the matching Architecture JAR on **server and clients** and retain the normal upstream
mods, including Patchouli for the guide. The mod is not advertised as client-optional.
Read [migration and rollback](docs/Migration.md) before replacing any existing helper or datapack.
The installed CurseForge profile and all play worlds were left unchanged during development.

## Build

Set `JAVA_HOME` to a Java 17 JDK. Supply exact, legally installed dependency JARs:

```powershell
python tools/bootstrap_dependencies.py --instance "C:\path\to\KNCraft"
.\gradlew.bat build harnessJars --console=plain
python tools/validate_artifact.py --jar build/libs/KNCraft-Architecture-0.2.0.jar
```

On other platforms use `./gradlew`. The bootstrap checks the handoff SHA-256 inventory,
extracts nested Infiniverse, and never edits the profile. `libs/`, caches, test worlds and
build products are ignored by Git. No third-party dependencies are bundled in the release.
Gradle 8.8 and ForgeGradle 6.0.36 are pinned; the wrapper distribution is checksum-verified.

Only install `KNCraft-Architecture-*.jar`. The separately named `ISOLATED-TESTS` JARs are
destructive test tools and must never be put on a play server.

## Configure and review

`config/kncraft-common.toml` starts with parity modules enabled and the four cohesion options
disabled. Enable the desired options under `[cohesion]`, then restart server and client:

```toml
[cohesion]
cottonInsulation = true
wildlifeInsulation = true
thermalMeals = true
fiberRecipes = true
```

The generated `insulators` and `foods` lists tune material values and the curated food list.
Invalid/missing IDs are diagnosed; existing Cold Sweat definitions take precedence. No upstream
TOML is rewritten. Existing sewn equipment remains Cold Sweat's responsibility.

See [features](docs/Features.md), [validation](docs/Validation.md),
[architecture](docs/Architecture.md), [build details](docs/Build.md),
[changelog](CHANGELOG.md), [attribution](NOTICE.md) and the exact
[replacement manifest](docs/Migration-Manifest.json).
