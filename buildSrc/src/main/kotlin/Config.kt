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

import org.gradle.api.JavaVersion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Config {
    val JDK: JavaVersion = JavaVersion.VERSION_1_8

    const val APP_ID = "com.venomvendor.sms.deduplicate"
    const val MAX_SDK_VERSION = 29

    const val KEY_FILE = "key.keystore"
    const val KEY_ALIAS = "KEY_ALIAS"
    const val KEY_PASSWORD = "KEY_PASSWORD"
    const val KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD"

    const val CREDENTIALS = "local.properties"

    // "alpha/beta/debug" TODO-Always check during release.
    const val RELEASE_SUFFIX = "-beta"
    const val DEBUG_SUFFIX = "$RELEASE_SUFFIX-debug"

    // For Users.
    const val VERSION_NAME = "${Versions.MAJOR}.${Versions.MINOR}.${Versions.PATCH}"

    // For PlayStore.
    private val VERSION_CODE = computeVersionCode()
    val DONUT = VERSION_CODE
    val FROYO = VERSION_CODE.plus(1)
    const val ABORT_ON_ERROR = false
    const val CHECK_RELEASE_BUILDS = true

    val DISABLE_LINTS = listOf("IconMissingDensityFolder")

    val EXCLUDE_PACKING = listOf(
        "META-INF/ASL2.0",
        "META-INF/LICENSE",
        "META-INF/NOTICE",
        "META-INF/LICENSE.txt",
        "META-INF/NOTICE.txt",
        ".readme"
    )
}

fun computeVersionCode(): Int {
    val format = "YYMMddHHmm"
    val formattedDate = SimpleDateFormat(format, Locale.ENGLISH).format(Date())
    return Integer.parseInt(formattedDate)
}
