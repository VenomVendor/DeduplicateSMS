/*
 *   Copyright (C) 2020 VenomVendor <info@VenomVendor.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

object Versions {
    // Build
    const val GRADLE_BUILD_TOOL = "4.1.0-alpha09"
    const val KOTLIN = "1.3.72"

    // Semantic Version
    const val MAJOR = 3
    const val MINOR = 0
    const val PATCH = 0

    // Phone number parser
    const val PHONE_NUMBER_PARSER = "8.10.10"
    const val COROUTINES = "1.3.4"
    const val KOIN = "2.1.2"

    const val ANNOTATION = "1.1.0"
    const val APPCOMPAT = "1.1.0"
    const val KTX_ACTIVITY = "1.1.0"
    const val KTX_CORE = "1.2.0"

    const val COMPOSE = "0.1.0-dev11"

    const val KT_LINT = "0.36.0"
    const val J_UNIT = "5.6.0"
    const val MOCKK = "1.9"
}

object Dependencies {

    fun repoHandler(repositories: RepositoryHandler) {
        repositories.jcenter()
        repositories.google()
        repositories.maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }

    object App {
        // @formatter:off
        const val PHONE_NUMBER_PARSER = "com.googlecode.libphonenumber:libphonenumber:${Versions.PHONE_NUMBER_PARSER}"
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"
        // @formatter:on

        const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.APPCOMPAT}"
        const val KTX_ACTIVITY = "androidx.activity:activity-ktx:${Versions.KTX_ACTIVITY}"
        const val KTX_CORE = "androidx.core:core-ktx:${Versions.KTX_CORE}"

        const val KOIN = "org.koin:koin-android:${Versions.KOIN}"
        const val ANNOTATION = "androidx.annotation:annotation:${Versions.ANNOTATION}"

        object Compose {
            const val COMPILER = "androidx.compose:compose-compiler:${Versions.COMPOSE}"
            const val RUNTIME = "androidx.compose:compose-runtime:${Versions.COMPOSE}"
            const val CORE = "androidx.ui:ui-core:${Versions.COMPOSE}"
            const val TEXT_ANDROID = "androidx.ui:ui-text-android:${Versions.COMPOSE}"
            const val TEXT_CORE = "androidx.ui:ui-text-core:${Versions.COMPOSE}"
            const val TEXT = "androidx.ui:ui-text:${Versions.COMPOSE}"
            const val ANIMATION_CORE = "androidx.ui:ui-animation-core:${Versions.COMPOSE}"
            const val ANIMATION = "androidx.ui:ui-animation:${Versions.COMPOSE}"
            const val FOUNDATION = "androidx.ui:ui-foundation:${Versions.COMPOSE}"
            const val GEOMETRY = "androidx.ui:ui-geometry:${Versions.COMPOSE}"
            const val GRAPHICS = "androidx.ui:ui-graphics:${Versions.COMPOSE}"
            const val LAYOUT = "androidx.ui:ui-layout:${Versions.COMPOSE}"
            const val TOOLING = "androidx.ui:ui-tooling:${Versions.COMPOSE}"
            const val UTIL = "androidx.ui:ui-util:${Versions.COMPOSE}"
            const val VECTOR = "androidx.ui:ui-vector:${Versions.COMPOSE}"
            const val MATERIAL = "androidx.ui:ui-material:${Versions.COMPOSE}"
            const val ICONS_CORE = "androidx.ui:ui-material-icons-core:${Versions.COMPOSE}"
            const val EXTENDED = "androidx.ui:ui-material-icons-extended:${Versions.COMPOSE}"
        }
    }

    object Build {
        const val GRADLE_BUILD_TOOL = "com.android.tools.build:gradle:${Versions.GRADLE_BUILD_TOOL}"
        const val KT_LINT = "com.pinterest:ktlint:${Versions.KT_LINT}"
    }

    object Test {
        const val J_UNIT = "org.junit.jupiter:junit-jupiter:${Versions.J_UNIT}"
        const val KOIN = "org.koin:koin-test:${Versions.KOIN}"
        const val MOCKK = "io.mockk:mockk:${Versions.MOCKK}"
        const val COMPOSE_UI = "androidx.ui:ui-test:${Versions.COMPOSE}"

        // @formatter:off
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}"
        // @formatter:on
    }

    val COMPOSE = App.Compose
}
