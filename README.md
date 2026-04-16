# FlowScale

Kontinuierliche visuelle NRS/PROM-Skala für Android — eine minimale App, mit der Patient:innen selbstberichtete Intensitätswerte (Numeric Rating Scale / Patient-Reported Outcome Measure) über die Zeit erfassen und exportieren können.

## Screenshots

<!-- TODO: Screenshots einfügen, z. B.:
<p>
  <img src="screenshots/main.png" width="240" alt="Hauptansicht" />
  <img src="screenshots/chart.png" width="240" alt="Intensitätsverlauf" />
</p>
-->

## Funktionen

- Intensität per Plus/Minus-Tasten oder Lautstärkewippe erfassen
- Verlaufs-Chart mit konfigurierbarem Zeitfenster (1–120 min)
- CSV-Export der gesammelten Datenpunkte
- Option „Bildschirm immer an" für längere Messsitzungen
- Lokale Speicherung in Room/SQLite, kein Netzwerkzugriff

## Build

### Voraussetzungen

| Tool           | Version        | Hinweis                                   |
| -------------- | -------------- | ----------------------------------------- |
| JDK            | 17–21          | JDK 26 (Arch-Default) wird nicht unterstützt |
| Android SDK    | Platform 36    | Build-Tools passend zum SDK               |
| Gradle-Wrapper | mitgeliefert   | `./gradlew`                               |

Android Gradle Plugin 9.1 ist für JDK 17–21 freigegeben. Setze `JAVA_HOME` entsprechend:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

Das Android-SDK wird über `ANDROID_HOME` (z. B. `~/Android/Sdk`) und pro Host in `local.properties` (`sdk.dir=…`, ge-`.gitignore`d) konfiguriert.

### Debug-APK bauen

```sh
./gradlew assembleDebug
```

Das APK liegt anschliessend unter `app/build/outputs/apk/debug/app-debug.apk`.

### Tests

```sh
./gradlew testDebugUnitTest          # Unit-Tests
./gradlew connectedDebugAndroidTest  # Instrumentierungstests (Gerät/Emulator nötig)
```

## Dependency-Lizenzen

Die vollständige Liste aller gebundelten Third-Party-Lizenzen wird vom [AboutLibraries](https://github.com/mikepenz/AboutLibraries)-Plugin generiert und ist in der App sichtbar unter:

> Info-Button (unten rechts) → **Open-Source-Lizenzen**

Manueller Export der Definitionen:

```sh
./gradlew :app:exportLibraryDefinitions
```

## Lizenz

FlowScale steht unter der [MIT-Lizenz](LICENSE) — Copyright © 2026 Marco Peyer.

Siehe auch [NOTICE](NOTICE) für Hinweise zu Drittanbieter-Lizenzen.
