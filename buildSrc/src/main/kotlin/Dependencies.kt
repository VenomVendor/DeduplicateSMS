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

object Versions {
    // Build
    const val GRADLE_BUILD_TOOL = "4.1.0-alpha01"
    const val KOTLIN = "1.3.70"

    // Semantic Version
    const val MAJOR = 3
    const val MINOR = 0
    const val PATCH = 0

    // Phone number parser
    const val PHONE_NUMBER_PARSER = "8.10.10"
    const val COROUTINES = "1.3.4"
    const val KOIN = "2.1.2"
    const val KT_LINT = "0.36.0"
    const val J_UNIT = "4.12"
}

object Dependencies {

    fun repoHandler(repositories: RepositoryHandler) {
        repositories.jcenter()
        repositories.google()
    }

    object App {
        // @formatter:off
        const val PHONE_NUMBER_PARSER = "com.googlecode.libphonenumber:libphonenumber:${Versions.PHONE_NUMBER_PARSER}"
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"
        // @formatter:on

        const val KOIN = "org.koin:koin-android:${Versions.KOIN}"
    }

    object Build {
        const val GRADLE_BUILD_TOOL = "com.android.tools.build:gradle:${Versions.GRADLE_BUILD_TOOL}"
        const val KT_LINT = "com.pinterest:ktlint:${Versions.KT_LINT}"
    }

    object Test {
        const val J_UNIT = "junit:junit:${Versions.J_UNIT}"
    }
}
