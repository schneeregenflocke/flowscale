# FlowScale

Android-App (APK): kontinuierliche, visuelle Numerische Rating-Skala (NRS) als Patient-Reported Outcome Measure (PROM).

## Architektur

- Native Android (Kotlin, Jetpack Compose)
- AGP 9.1 mit Built-in Kotlin (kein separates `kotlin-android` Plugin)
- Compose Compiler Plugin (`kotlin-compose`) wird separat angewendet
- iOS-Portierung soll langfristig möglich bleiben (KMP als Option)
- Min SDK 26, Target/Compile SDK 36

## Umgebungsvariablen

| Variable       | Wert                      | Zweck                                                                                                            |
| -------------- | ------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `ANDROID_HOME` | `~/Android/Sdk`           | Pfad zum Android SDK (Build-Tools, Plattformen, Emulator). Gradle und `adb` finden darüber alle SDK-Komponenten. |
| `JAVA_HOME`    | `/opt/android-studio/jbr` | JDK 21, das mit Android Studio gebundelt wird. Gradle nutzt es zum Kompilieren.                                  |

Am besten in `~/.zshenv` oder `~/.zprofile` dauerhaft setzen.

## Build

```sh
export ANDROID_HOME=~/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
./gradlew assembleDebug
```

## Emulator starten

```sh
QT_QPA_PLATFORM=xcb $ANDROID_HOME/emulator/emulator -avd FlowScale -gpu auto &
```

`QT_QPA_PLATFORM=xcb` ist nötig, weil der Android-Emulator kein Wayland-Qt-Plugin mitbringt und über XWayland laufen muss.

Warten bis gebootet, dann App installieren und starten:

```sh
$ANDROID_HOME/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.flowscale.app/.MainActivity
```

## Physisches Gerät (Alternative zum Emulator)

1. USB-Debugging auf dem Android-Gerät aktivieren (Einstellungen → Über das Telefon → 7× auf Build-Nummer tippen → Entwickleroptionen → USB-Debugging)
2. Gerät per USB verbinden
3. `adb devices` prüfen, ob das Gerät erkannt wird
4. `adb install app/build/outputs/apk/debug/app-debug.apk`

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

1. **Persistenz** — Startwert und aktuellen Wert speichern (DataStore / Room), damit sie App-Neustarts überleben
2. **Verlauf / Zeitstempel** — Jede Änderung mit Zeitstempel loggen (Kern des PROM-Konzepts)
3. **Visuelle Skala** — Slider oder Balken-Visualisierung des aktuellen Werts zusätzlich zur Zahl
4. **Export** — Daten als CSV/JSON exportieren
5. **Einstellungen** — Schrittweite, Wertebereich, Sprache konfigurierbar machen

## Offene TODOs

- R8/ProGuard für Release aktivieren (`isMinifyEnabled = true` + `proguard-rules.pro`), bevor die App veröffentlicht wird
- Launcher-Icon (`android:icon` / `android:roundIcon`) im Manifest und als Ressource anlegen

## Konventionen

- Sprache im Code und in Commits: Englisch
- UI-Texte: Deutsch (Lokalisierung später)
- Abhängigkeiten und SDK-Versionen: immer die aktuellste stabile Version verwenden; Versionen nie ohne Grund pinnen, Aktualisierbarkeit hat Vorrang
