# FlowScale

Android-App (APK): kontinuierliche, visuelle Numerische Rating-Skala (NRS) als Patient-Reported Outcome Measure (PROM).

## Architektur

- Native Android (Kotlin, Jetpack Compose)
- AGP 9.1 mit Built-in Kotlin (kein separates `kotlin-android` Plugin)
- Compose Compiler Plugin (`kotlin-compose`) wird separat angewendet
- iOS-Portierung soll langfristig möglich bleiben (KMP als Option)
- Min SDK 26, Target/Compile SDK 36

## Build

```sh
export ANDROID_HOME=~/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
./gradlew assembleDebug
```

## Konventionen

- Sprache im Code und in Commits: Englisch
- UI-Texte: Deutsch (Lokalisierung später)
