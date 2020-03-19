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

package com.venomvendor.sms.deduplicate.core

import androidx.annotation.CallSuper
import com.venomvendor.sms.deduplicate.core.di.coreModule
import com.venomvendor.sms.deduplicate.core.di.testModule
import com.venomvendor.sms.deduplicate.core.ktx.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get

@ExperimentalCoroutinesApi
abstract class BaseTest : KoinTest, KoinComponent {

    val testDispatcher: TestCoroutineDispatcher by lazy {
        get<DispatcherProvider>().default() as TestCoroutineDispatcher
    }

    @BeforeEach
    @CallSuper
    open fun setUp() {
        startKoin {
            modules(coreModule, testModule)
        }

        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    @CallSuper
    open fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()

        stopKoin()
    }
}
