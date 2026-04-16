# Mitwirken an Flowscale

## Pre-Commit-Hooks

Das Repo nutzt [pre-commit](https://pre-commit.com/) mit [gitleaks](https://github.com/gitleaks/gitleaks) und einer kleinen Auswahl aus [pre-commit-hooks](https://github.com/pre-commit/pre-commit-hooks), um versehentlich committete Secrets und grosse Binärblobs abzufangen.

Einmalige Einrichtung auf deiner Maschine:

```sh
pipx install pre-commit
pre-commit install
```

Ab dann laufen die Hooks automatisch bei jedem `git commit`. Manuell über alle Dateien laufen lassen:

```sh
pre-commit run --all-files
```

Die aktiven Hooks sind in [.pre-commit-config.yaml](.pre-commit-config.yaml) gepinnt (keine `main`-Referenzen).

## CI-Checks

[.github/workflows/ci.yml](.github/workflows/ci.yml) läuft auf `push` nach `main` und auf jedem `pull_request` gegen `main`. Gebrochen wird der Build bei:

| Check | Gradle-Task | Bricht bei |
|---|---|---|
| Lizenz-Allowlist | `:app:licenseeRelease` | Dependency mit nicht-erlaubter oder fehlender SPDX-Lizenz |
| Lint | `:app:lintDebug` | Lint-Error-Level-Befund |
| Unit-Tests | `:app:testDebugUnitTest` | Test-Failure |
| Debug-APK | `:app:assembleDebug` | Compile-/Packaging-Fehler |

Die erlaubten SPDX-Identifier sind im `licensee { }`-Block in [app/build.gradle.kts](app/build.gradle.kts) aufgeführt: `Apache-2.0`, `MIT`, `BSD-2-Clause`, `BSD-3-Clause`, `ISC`, `CC0-1.0`, `EPL-2.0`.

## Versionierungs-Policy

Die Version wird nicht von Hand in `build.gradle.kts` eingetragen, sondern pro Build aus [gradle.properties](gradle.properties) (`versionMajor`, `versionMinor`) und dem Git-Commit-Count (`versionPatch = git rev-list --count HEAD`) abgeleitet.

- **PATCH** steigt automatisch mit jedem neuen Commit auf `main` — kein Bot-Commit, kein Tag-Push auf Commit-Ebene.
- **MINOR / MAJOR** werden ausschliesslich über den Release-Workflow gebumpt:

  ```sh
  gh workflow run release.yml -f bump=minor   # 0.1.x → 0.2.0
  gh workflow run release.yml -f bump=major   # 0.x.x → 1.0.0
  ```

  Der Workflow editiert `gradle.properties`, committet, tagged `vX.Y.0`, baut `app-release-unsigned.apk` und generiert das SPDX-SBOM, und hängt beide ans GitHub-Release. Nie direkt auf `main` pushen, um `versionMajor`/`versionMinor` zu ändern.

`versionCode` = `versionMajor * 10000 + versionMinor * 100 + versionPatch` (monoton steigend, solange die Commit-Historie linear bleibt).

## Dependency-Policy

- Neue Dependencies mit einer Lizenz **ausserhalb** der Allowlist: **nicht** per `allowDependency` still durchwinken. Stattdessen: Issue öffnen, Diskussion ob Ersatz möglich ist.
- Neue Dependencies **ohne** erkennbare SPDX-Lizenz: dito — Issue öffnen, nicht still hinzufügen.
- Die Allowlist wird bewusst nicht mit `GPL`/`LGPL`/`AGPL`/`SSPL` erweitert; die App liegt unter MIT und soll das bleiben können.

## SBOM

Das SPDX-2.3-SBOM wird über `./gradlew :app:spdxSbomForRelease` erzeugt und liegt unter [build/reports/spdx/flowscale.spdx.json](build/reports/spdx/flowscale.spdx.json). Der Task ist bewusst nicht an `build` gehängt — er läuft nur manuell oder im Release-Workflow.
