import Config.VERSION_NAME
import Constants.DEBUG
import Constants.FROYO
import Constants.RELEASE
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
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
        minSdkVersion(21)
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

    // flavorDimensions("buildType")
    // productFlavors {
    //     create(FROYO) {
    //         minSdkVersion(21)
    //     }
    //     //
    //     // create(DONUT) {
    //     //     minSdkVersion(4)
    //     //     maxSdkVersion(7)
    //     //     targetSdkVersion(7)
    //     // }
    // }

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

    applicationVariants.all {
        if (buildType.getName() == RELEASE) {
            productFlavors.forEach { flavor ->
                val name = flavor.getName()
                outputs.forEach { output ->
                    (output as ApkVariantOutputImpl).versionNameOverride = VERSION_NAME
                    output.versionCodeOverride =
                        if (name == FROYO) Config.FROYO else Config.DONUT
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(Dependencies.App.PHONE_NUMBER_PARSER)
}

apply(from = rootProject.file("./gradle/root.gradle.kts"))
