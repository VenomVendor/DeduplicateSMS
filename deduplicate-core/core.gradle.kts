import Constants.DEBUG
import Constants.FROYO
import Constants.RELEASE

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(Config.MAX_SDK_VERSION)

    defaultConfig {
        targetSdkVersion(Config.MAX_SDK_VERSION)
        versionCode = 1
        versionName = "0.0.0"
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }

        getByName("test") {
            java.srcDir("src/test/kotlin")

            resources.srcDirs("src/test/resources")
        }
    }

    flavorDimensions("buildType")
    productFlavors {
        create(FROYO) {
            minSdkVersion(21)
        }
        //
        // create(DONUT) {
        //     minSdkVersion(4)
        //     maxSdkVersion(7)
        //     targetSdkVersion(7)
        // }
    }

    buildTypes {
        getByName(RELEASE) {
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            isPseudoLocalesEnabled = false
            isMinifyEnabled = true
            versionNameSuffix = Config.RELEASE_SUFFIX
            proguardFiles(
                rootProject.file("./pro-guard/proguard-optimize.pro"),
                rootProject.file("./pro-guard/proguard-rules-aggressive.pro"),
                rootProject.file("./pro-guard/proguard-rules-app.pro"),
                rootProject.file("./pro-guard/proguard-rules-log.pro")
            )
        }

        getByName(DEBUG) {
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
            isPseudoLocalesEnabled = true
            isMinifyEnabled = false
            versionNameSuffix = Config.DEBUG_SUFFIX
        }

        packagingOptions {
            Config.EXCLUDE_PACKING.forEach(this::exclude)
        }

        lintOptions {
            isCheckReleaseBuilds = Config.CHECK_RELEASE_BUILDS
            isAbortOnError = Config.ABORT_ON_ERROR
            Config.DISABLE_LINTS.forEach(this::disable)
        }

        compileOptions {
            sourceCompatibility = Config.JDK
            targetCompatibility = Config.JDK
        }

        libraryVariants.all {
            generateBuildConfigProvider?.configure {
                enabled = false
            }
        }
    }
}

// Required dependencies
dependencies {
    api(embeddedKotlin(Constants.KOTLIN_JDK))

    api(Dependencies.App.COROUTINES)
    api(Dependencies.App.KOIN)
}

// Test Dependencies
dependencies {
    testImplementation(Dependencies.Test.J_UNIT)
}

apply(from = rootProject.file("./gradle/formatter.gradle.kts"))
