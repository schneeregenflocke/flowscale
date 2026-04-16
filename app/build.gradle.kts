plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.licensee)
    alias(libs.plugins.spdx.sbom)
}

val versionMajorInt = providers.gradleProperty("versionMajor").orElse("0").get().toInt()
val versionMinorInt = providers.gradleProperty("versionMinor").orElse("0").get().toInt()

val gitCommitCountProvider = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText

val versionPatchInt: Int = runCatching {
    gitCommitCountProvider.get().trim().toIntOrNull() ?: 0
}.getOrDefault(0)

android {
    namespace = "com.flowscale.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.flowscale.app"
        minSdk = 26
        targetSdk = 36
        versionCode = versionMajorInt * 10000 + versionMinorInt * 100 + versionPatchInt
        versionName = "$versionMajorInt.$versionMinorInt.$versionPatchInt"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

aboutLibraries {
    export {
        prettyPrint = true
    }
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    allow("ISC")
    allow("CC0-1.0")
    allow("EPL-2.0")
}

spdxSbom {
    targets {
        create("release") {
            configurations.set(listOf("releaseRuntimeClasspath"))
            outputFile.set(rootProject.layout.buildDirectory.file("reports/spdx/flowscale.spdx.json"))
            document {
                name.set("flowscale")
                namespace.set("https://github.com/schneeregenflocke/flowscale/spdx/$versionMajorInt.$versionMinorInt.$versionPatchInt")
                creator.set("Person: Marco Peyer")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.aboutlibraries.compose.m3)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
