import Constants.DEBUG
import Constants.RELEASE

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(Config.MAX_SDK_VERSION)

    defaultConfig {
        targetSdkVersion(Config.MAX_SDK_VERSION)
        minSdkVersion(Config.MIN_SDK_VERSION)
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

        getByName("androidTest") {
            java.srcDir("src/androidTest/kotlin")

            resources.srcDirs("src/androidTest/resources")
        }
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

    kotlinOptions {
        jvmTarget = Config.JDK.toString()
        freeCompilerArgs =
            freeCompilerArgs + listOf("-Xinline-classes", "-Xskip-metadata-version-check")
    }

    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }
}

// Default
dependencies {
    api(project(":core"))

    implementation(Dependencies.App.PHONE_NUMBER_PARSER)

    implementation(Dependencies.App.KTX_CORE)
}

apply(from = rootProject.file("./gradle/root.gradle.kts"))
