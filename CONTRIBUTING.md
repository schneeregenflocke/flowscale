# Contributing to Flowscale

## Decisions

### The versioning policy

The version does not get entered by hand in `build.gradle.kts` but derived per build from [gradle.properties](gradle.properties) (`versionMajor`, `versionMinor`) and the git commit count (`versionPatch = git rev-list --count HEAD`).

- **PATCH** rises by itself with every new commit on `main` — no bot commit, no tag push at commit level.
- **MINOR and MAJOR** get bumped through the release workflow alone:

  ```sh
  gh workflow run release.yml -f bump=minor   # 0.1.x → 0.2.0
  gh workflow run release.yml -f bump=major   # 0.x.x → 1.0.0
  ```

  The workflow edits `gradle.properties`, commits, tags `vX.Y.0`, builds `app-release-unsigned.apk`, generates the SPDX SBOM and hangs both onto the GitHub release. Never push to `main` directly to change `versionMajor` or `versionMinor`.

`versionCode` = `versionMajor * 10000 + versionMinor * 100 + versionPatch` (monotonically rising, as long as the commit history stays linear).

### The dependency policy

- A new dependency with a licence **outside** the allowlist: do **not** wave it through with `allowDependency` in silence. Open an issue instead and discuss whether a replacement exists.
- A new dependency **without** a recognisable SPDX licence: the same — open an issue, do not add it quietly.
- The allowlist deliberately does not grow to hold `GPL`, `LGPL`, `AGPL` or `SSPL`; the app sits under MIT and should be able to stay there.

### SBOM

`./gradlew :app:spdxSbomForRelease` produces the SPDX 2.3 SBOM under [build/reports/spdx/flowscale.spdx.json](build/reports/spdx/flowscale.spdx.json). The task deliberately does not hang on `build` — it runs by hand or in the release workflow.

## Operations

### Build

#### Pre-commit hooks

The repo uses [pre-commit](https://pre-commit.com/) with [gitleaks](https://github.com/gitleaks/gitleaks) and a small selection from [pre-commit-hooks](https://github.com/pre-commit/pre-commit-hooks), to catch accidentally committed secrets and large binary blobs.

Set it up once on your machine:

```sh
pipx install pre-commit
pre-commit install
```

From then on the hooks run by themselves on every `git commit`. Over every file by hand:

```sh
pre-commit run --all-files
```

The active hooks are pinned in [.pre-commit-config.yaml](.pre-commit-config.yaml) (no `main` references).

#### CI checks

[.github/workflows/ci.yml](.github/workflows/ci.yml) runs on a `push` to `main` and on every `pull_request` against `main`. The build breaks on:

- The licence allowlist (`:app:licenseeRelease`) — on a dependency with a disallowed or missing SPDX licence
- Lint (`:app:lintDebug`) — on a finding at error level
- Unit tests (`:app:testDebugUnitTest`) — on a test failure
- The debug APK (`:app:assembleDebug`) — on a compile or packaging error

The permitted SPDX identifiers stand in the `licensee { }` block in [app/build.gradle.kts](app/build.gradle.kts): `Apache-2.0`, `MIT`, `BSD-2-Clause`, `BSD-3-Clause`, `ISC`, `CC0-1.0`, `EPL-2.0`.
