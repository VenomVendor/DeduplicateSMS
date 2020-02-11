import Config.VERSION_NAME
import Constants.DEBUG
import Constants.DONUT
import Constants.FROYO
import Constants.RELEASE
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
}

val credProps = File("${rootProject.rootDir}/${Config.CREDENTIALS}").loadProperties()

android {
    compileSdkVersion(Config.MAX_SDK_VERSION)

    defaultConfig {
        applicationId = Config.APP_ID
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
            minSdkVersion(8)
        }

        create(DONUT) {
            minSdkVersion(4)
            maxSdkVersion(7)
            targetSdkVersion(7)
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

        applicationVariants.all {
            if (buildType.name == RELEASE) {
                productFlavors.forEach { flavor ->
                    val name = flavor.name
                    outputs.forEach { output ->
                        (output as ApkVariantOutputImpl).versionNameOverride = VERSION_NAME
                        output.versionCodeOverride =
                            if (name == FROYO) Config.FROYO else Config.DONUT
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core"))

    kotlin(Constants.KOTLIN_JDK, version = Versions.KOTLIN)

    implementation(Dependencies.App.PHONE_NUMBER_PARSER)
}

apply(from = rootProject.file("./gradle/formatter.gradle.kts"))
