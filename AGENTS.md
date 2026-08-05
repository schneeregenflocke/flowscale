# Flowscale

An Android app (APK): a continuous, visual numeric rating scale (NRS) as a patient-reported outcome measure (PROM).

## Architecture

### Stack

- Native Android (Kotlin, Jetpack Compose)
- AGP 9.1 with built-in Kotlin (no separate `kotlin-android` plugin)
- The Compose compiler plugin (`kotlin-compose`) gets applied separately
- `FlowscaleApplication` holds the Room database as a singleton; ViewModels reach it through `getApplication<FlowscaleApplication>().database`
- An iOS port should stay possible in the long run (KMP as an option)
- Min SDK 26, target and compile SDK 36

## Decisions

- UI texts: German (localisation later). The CSV export header follows them — it is content for the user, not for us.
- Dependencies and SDK versions: always the newest stable one; never pin a version without a reason, being updatable comes first.

## Operations

### Build

#### Prerequisites (Arch Linux)

`pacman` installs every build dependency:

```sh
sudo pacman -S jdk21-openjdk android-tools
```

- `jdk21-openjdk` — JDK 21, Gradle compiles with it
- `android-tools` — `adb` and `fastboot` for the deploy onto a device

The Android SDK (build tools, platforms) gets managed separately under `~/Android/Sdk` (commandline tools or Android Studio). The `kotlin` package from pacman is **not needed** — the Kotlin compiler sits embedded in the Gradle plugin.

#### The JDK version

AGP 9.x is released for JDK 17 to 21. JDK 26 (the Arch default) fails on the `jlink` and `JdkImageTransform` step. Hence either set `JAVA_HOME` explicitly, or `sudo archlinux-java set java-21-openjdk` for a system-wide default.

- `ANDROID_HOME` — `~/Android/Sdk`: the path to the Android SDK (build tools, platforms, emulator). Gradle finds every SDK component through it.
- `JAVA_HOME` — `/usr/lib/jvm/java-21-openjdk`: JDK 21. Set it permanently in `~/.zshenv`:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

`sdk.dir` gets set per host in `local.properties` (the file is gitignored):

```properties
sdk.dir=/home/<user>/Android/Sdk
```

#### Building and testing

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest        # unit tests, no device needed
./gradlew connectedDebugAndroidTest # instrumentation tests, a device must be connected
```

### Starting the emulator

```sh
QT_QPA_PLATFORM=xcb $ANDROID_HOME/emulator/emulator -avd Flowscale -gpu auto &
```

`QT_QPA_PLATFORM=xcb` is needed because the Android emulator brings no Wayland Qt plugin and must run over XWayland.

Wait until it has booted, then install and start the app:

```sh
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.flowscale.app/.MainActivity
```

### A physical device

1. Switch on USB debugging on the Android device (settings → about phone → tap the build number 7 times → developer options → USB debugging)
2. Connect the device by USB
3. Check with `adb devices` that it gets recognised
4. `adb install app/build/outputs/apk/debug/app-debug.apk`

On `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (a different signing key): `adb uninstall com.flowscale.app` first, then install again.

### Diagnosis

#### Inspecting the database

The app stores its data points in a Room and SQLite database (`flowscale.db`) on the device. List the files:

```sh
adb shell "run-as com.flowscale.app ls -la databases/"
```

Copy the database to the host and query it locally (sqlite3 is usually not available on the device):

```sh
adb shell "run-as com.flowscale.app cat databases/flowscale.db" > /tmp/flowscale.db
sqlite3 /tmp/flowscale.db "SELECT COUNT(*) FROM intensity_records;"
sqlite3 /tmp/flowscale.db ".schema intensity_records"
sqlite3 /tmp/flowscale.db "SELECT * FROM intensity_records ORDER BY recordedAt ASC LIMIT 10;"
```

`adb install` keeps the app data (same signing key). Data goes only on `adb uninstall`, an incompatible signature or an explicit "clear data".

#### Screenshots from the CLI (Hyprland plus grim)

Prerequisites: `grim` and `hyprctl` (the Hyprland compositor).

Find the emulator window (JSON output, class `Emulator`):

```sh
hyprctl -j clients | python3 -c "
import json, sys
for c in json.load(sys.stdin):
    if c.get('class') == 'Emulator' and not c.get('floating'):
        print(f\"at={c['at']}  size={c['size']}  title={c['title']}\")
"
```

Capture that region (`x,y WxH`), and put the file under `screenshots/` — that directory is exempted in `.gitignore`, so `*.png` elsewhere in the project stays usable:

```sh
grim -g '3,38 794x859' screenshots/emulator-screenshot.png
```

Further `grim` modes: `grim screenshot.png` for the whole output, `grim -o DP-1 screenshot.png` for one monitor.
