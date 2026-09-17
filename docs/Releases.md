# Automatic GitHub releases

Repository: <https://github.com/KolbieCheese/KNCraft-Overhall-Mod>.
The repository is currently private; releases inherit its access restrictions.
The workflow in `.github/workflows/build.yml` runs on pushes, pull requests, and manual dispatch.

Default-branch pushes and manual runs on that branch publish. Other branches and pull
requests build `1.0.0-dev.<run number>` artifacts. Publishing receives `contents: write`
only in the release job; build and PR jobs have read-only repository permissions.

## Version allocation

`gradle.properties` sets the minimum release and major/minor series. `mod_version=1.0.0`
starts a fresh repository at 1.0.0. After fetching tags, `tools/release_version.py` selects
one above the largest patch tag in that series. Set `mod_version=1.1.0` to start the 1.1
series. Source files do not need a version-bump commit for every release.

The workflow uses [GitHub concurrency queues](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/control-workflow-concurrency) per branch through publication to serialize version allocation.
A matching tag on the same commit is reused for retries. A completed release keeps its
assets; an interrupted draft can have its assets uploaded again and then be published.
Failed builds publish nothing. Manually created `vX.Y.Z` tags also reserve those numbers.

## Build inputs and assets

`docs/Build-Inputs.json` records exact official download URLs and SHA-256 values for
compile dependencies. The bootstrap validates cached files before reuse, downloads missing
files, and extracts Infiniverse only from the verified Nomadic Tents archive. Dependencies
remain outside Git and outside the distributed mod.

The public release contains:

- `KNCraftCompatibilityMod-<version>.jar`
- `KNCraftCompatibilityMod-<version>.zip` (JAR, docs, migration tools, policy fragments)
- `SHA256SUMS.txt`

Compilation, JUnit, version-allocation tests, resource validation, and packaged-artifact
validation must pass first. Runtime test harnesses are deliberately excluded. Dedicated
gameplay and physical-client acceptance are tracked separately in `Validation.md`.

The repository must allow GitHub Actions and release creation by its built-in token.
No personal access token, CurseForge account, or private runner is required. If an upstream
download becomes unavailable or its bytes change, verification fails rather than substituting
an unreviewed mod version.
