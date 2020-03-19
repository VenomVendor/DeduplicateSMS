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

package com.venomvendor.sms.deduplicate.core.ktx

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Create custom Dispatch provider
 * Ref: https://github.com/CDRussell/testing-coroutines/blob/e6cfd71f37/app/src/main/java/com/cdrussell/coroutines/testing/DispatcherProvider.kt
 */
interface DispatcherProvider {

    /**
     * Refer [Dispatchers.Main]
     */
    fun main(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Refer [Dispatchers.Default]
     */
    fun default(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Refer [Dispatchers.IO]
     */
    fun io(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Refer [Dispatchers.Unconfined]
     */
    fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}

class DefaultDispatcherProvider : DispatcherProvider
