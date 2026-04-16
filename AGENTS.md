# Flowscale

Android-App (APK): kontinuierliche, visuelle numerische Rating-Skala (NRS) als Patient-Reported Outcome Measure (PROM).

## Architektur

- Native Android (Kotlin, Jetpack Compose)
- AGP 9.1 mit Built-in Kotlin (kein separates `kotlin-android` Plugin)
- Compose Compiler Plugin (`kotlin-compose`) wird separat angewendet
- `FlowScaleApplication` hält die Room-Datenbank als Singleton; ViewModels greifen über `getApplication<FlowScaleApplication>().database` darauf zu
- iOS-Portierung soll langfristig möglich bleiben (KMP als Option)
- Min SDK 26, Target/Compile SDK 36

## Voraussetzungen (Arch Linux)

Alle Build-Abhängigkeiten lassen sich über `pacman` installieren:

```sh
sudo pacman -S jdk21-openjdk android-tools
```

| Paket           | Zweck                                    |
| --------------- | ---------------------------------------- |
| `jdk21-openjdk` | JDK 21 — Gradle nutzt es zum Kompilieren |
| `android-tools` | `adb`, `fastboot` — Deploy auf Geräte    |

Das Android SDK (Build-Tools, Plattformen) wird separat unter `~/Android/Sdk` verwaltet (Commandline-Tools oder Android Studio).

### JDK-Version

AGP 9.x ist für JDK 17–21 freigegeben. JDK 26 (Arch-Default) scheitert beim `jlink`/`JdkImageTransform`-Schritt. Deshalb:

- Entweder `JAVA_HOME` explizit setzen (s. u.), oder
- `sudo archlinux-java set java-21-openjdk` für systemweiten Default

### Kotlin über pacman

Das `kotlin`-Paket aus pacman ist **nicht nötig** — der Kotlin-Compiler ist im Gradle-Plugin eingebettet.

## Umgebungsvariablen

| Variable       | Wert                           | Zweck                                                                                                  |
| -------------- | ------------------------------ | ------------------------------------------------------------------------------------------------------ |
| `ANDROID_HOME` | `~/Android/Sdk`                | Pfad zum Android SDK (Build-Tools, Plattformen, Emulator). Gradle findet darüber alle SDK-Komponenten. |
| `JAVA_HOME`    | `/usr/lib/jvm/java-21-openjdk` | JDK 21 (via `pacman -S jdk21-openjdk`). Gradle nutzt es zum Kompilieren.                               |

Dauerhaft in `~/.zshenv` setzen:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

`sdk.dir` wird pro Host in `local.properties` gesetzt (Datei ist `.gitignore`d):

```properties
sdk.dir=/home/<user>/Android/Sdk
```

## Build

`./gradlew assembleDebug`

## Tests

# Unit-Tests (kein Gerät nötig)
`./gradlew testDebugUnitTest`

# Instrumentierungstests (Gerät/Emulator muss verbunden sein)
`./gradlew connectedDebugAndroidTest`

## Emulator starten

`QT_QPA_PLATFORM=xcb $ANDROID_HOME/emulator/emulator -avd FlowScale -gpu auto &`

`QT_QPA_PLATFORM=xcb` ist nötig, weil der Android-Emulator kein Wayland-Qt-Plugin mitbringt und über XWayland laufen muss.

Warten bis gebootet, dann App installieren und starten:

`adb install app/build/outputs/apk/debug/app-debug.apk`
`adb shell am start -n com.flowscale.app/.MainActivity`

## Physisches Gerät

1. USB-Debugging auf dem Android-Gerät aktivieren (Einstellungen → Über das Telefon → 7× auf Build-Nummer tippen → Entwickleroptionen → USB-Debugging)
2. Gerät per USB verbinden
3. `adb devices` prüfen, ob das Gerät erkannt wird
4. `adb install app/build/outputs/apk/debug/app-debug.apk`

Bei `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (anderer Signing-Key): erst `adb uninstall com.flowscale.app`, dann erneut installieren.

## Datenbank inspizieren

Die App speichert Datenpunkte in einer Room/SQLite-Datenbank (`flowscale.db`) auf dem Gerät. Dateien auflisten:

```sh
adb shell "run-as com.flowscale.app ls -la databases/"
```

DB auf den Host kopieren und lokal abfragen (sqlite3 ist auf dem Gerät i.d.R. nicht verfügbar):

```sh
adb shell "run-as com.flowscale.app cat databases/flowscale.db" > /tmp/flowscale.db
sqlite3 /tmp/flowscale.db "SELECT COUNT(*) FROM intensity_records;"
sqlite3 /tmp/flowscale.db ".schema intensity_records"
sqlite3 /tmp/flowscale.db "SELECT * FROM intensity_records ORDER BY recordedAt ASC LIMIT 10;"
```

**Hinweis:** `adb install` bewahrt App-Daten (gleicher Signing-Key). Daten gehen nur bei `adb uninstall`, inkompatiblen Signaturen oder explizitem „Clear Data" verloren.

## Screenshots per CLI (Hyprland + grim)

Voraussetzungen: `grim` und `hyprctl` (Hyprland Compositor).

Emulator-Fenster finden (JSON-Ausgabe, Klasse `Emulator`):

```sh
hyprctl -j clients | python3 -c "
import json, sys
for c in json.load(sys.stdin):
    if c.get('class') == 'Emulator' and not c.get('floating'):
        print(f\"at={c['at']}  size={c['size']}  title={c['title']}\")
"
```

Screenshot der Region aufnehmen (`x,y WxH`):

```sh
grim -g 'X,Y WxH' screenshot.png
```

Beispiel mit den ermittelten Werten:

```sh
grim -g '3,38 794x859' emulator-screenshot.png
```

Weitere `grim`-Modi:

- `grim screenshot.png` — ganzer Output
- `grim -o DP-1 screenshot.png` — bestimmter Monitor

Screenshots im Projektverzeichnis unter `screenshots/` speichern — das Verzeichnis ist in `.gitignore` ausgenommen, sodass `*.png` im restlichen Projekt später nutzbar bleibt:

```sh
grim -g 'X,Y WxH' screenshots/emulator-screenshot.png
```

## Roadmap

Mögliche nächste Ziele (grobe Reihenfolge):

1. **Export** — Daten als CSV/JSON exportieren
2. **Einstellungen** — Schrittweite, Wertebereich, Sprache konfigurierbar machen

## Offene TODOs

- ~~R8/ProGuard für Release aktivieren (`isMinifyEnabled = true` + `proguard-rules.pro`), bevor die App veröffentlicht wird~~ ✅
- ~~Launcher-Icon (`android:icon` / `android:roundIcon`) im Manifest und als Ressource anlegen~~ ✅

## Konventionen

- Sprache im Code und in Commits: Englisch
- UI-Texte: Deutsch (Lokalisierung später)
- Abhängigkeiten und SDK-Versionen: immer die aktuellste stabile Version verwenden; Versionen nie ohne Grund pinnen, Aktualisierbarkeit hat Vorrang
