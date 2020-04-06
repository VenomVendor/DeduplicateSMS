import Constants.DEBUG
import Constants.RELEASE
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.konan.properties.saveProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val credProps = File("${rootProject.rootDir}/${Config.CREDENTIALS}").let {
    return@let if (it.exists) {
        it.loadProperties()
    } else {
        val props = Properties().apply {
            setProperty(Config.KEY_ALIAS, Config.KEY_ALIAS)
            setProperty(Config.KEY_PASSWORD, Config.KEY_PASSWORD)
            setProperty(Config.KEYSTORE_PASSWORD, Config.KEYSTORE_PASSWORD)
        }
        it.saveProperties(props)
        props
    }
}

android {
    compileSdkVersion(Config.MAX_SDK_VERSION)

    defaultConfig {
        applicationId = Config.APP_ID
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
    }

    signingConfigs {
        register(RELEASE) {
            try {
                storeFile = rootProject.file(Config.KEY_FILE)
                keyAlias = credProps[Config.KEY_ALIAS] as String
                keyPassword = credProps[Config.KEY_PASSWORD] as String
                storePassword = credProps[Config.KEYSTORE_PASSWORD] as String
            } catch (ex: Exception) {
                throw InvalidUserDataException(
                    "You should define KEY_ALIAS, KEY_PASSWORD & KEYSTORE_PASSWORD in " +
                        "local.properties. \n ${ex.localizedMessage}"
                )
            }
        }
    }

    buildTypes {
        getByName(RELEASE) {
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            isPseudoLocalesEnabled = false
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName(RELEASE)
            versionNameSuffix = Config.RELEASE_SUFFIX
            proguardFiles(
                project.file("./pro-guard/proguard-optimize.pro"),
                project.file("./pro-guard/proguard-rules-aggressive.pro"),
                project.file("./pro-guard/proguard-rules-app.pro"),
                project.file("./pro-guard/proguard-rules-log.pro")
            )
        }

        getByName(DEBUG) {
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
            isPseudoLocalesEnabled = true
            isShrinkResources = false
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
        freeCompilerArgs = freeCompilerArgs + listOf("-Xinline-classes")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // kotlinCompilerVersion = "1.3.61-dev-withExperimentalGoogleExtensions-20200129"
        kotlinCompilerExtensionVersion = Versions.COMPOSE
    }
}

// Default
dependencies {
    implementation(project(":core"))
    implementation(Dependencies.App.PHONE_NUMBER_PARSER)
    implementation(Dependencies.App.APPCOMPAT)
    implementation(Dependencies.App.KTX_ACTIVITY)
    implementation(Dependencies.App.KTX_CORE)
}

// Compose
dependencies {
    implementation(Dependencies.App.Compose.COMPILER)
    implementation(Dependencies.App.Compose.RUNTIME)
    implementation(Dependencies.App.Compose.CORE)
    implementation(Dependencies.App.Compose.ANDROID_TEXT)
    implementation(Dependencies.App.Compose.ANIMATION_CORE)
    implementation(Dependencies.App.Compose.ANIMATION)
    implementation(Dependencies.App.Compose.FOUNDATION)
    implementation(Dependencies.App.Compose.FRAMEWORK)
    implementation(Dependencies.App.Compose.GEOMETRY)
    implementation(Dependencies.App.Compose.GRAPHICS)
    implementation(Dependencies.App.Compose.LAYOUT)
    implementation(Dependencies.App.Compose.PLATFORM)
    implementation(Dependencies.App.Compose.TEXT)
    implementation(Dependencies.App.Compose.TOOLING)
    implementation(Dependencies.App.Compose.UTIL)
    implementation(Dependencies.App.Compose.VECTOR)
    implementation(Dependencies.App.Compose.MATERIAL)
    implementation(Dependencies.App.Compose.ICONS_CORE)
    implementation(Dependencies.App.Compose.EXTENDED)

    testImplementation(Dependencies.Test.COMPOSE_UI)
}

apply(from = rootProject.file("./gradle/root.gradle.kts"))
