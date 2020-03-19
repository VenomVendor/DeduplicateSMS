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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

abstract class BaseTest : KoinTest, KoinComponent {

    @BeforeEach
    @CallSuper
    open fun setUp() {
        startKoin {
            modules(coreModule, testModule)
        }
    }

    @AfterEach
    @CallSuper
    open fun tearDown() {
        stopKoin()
    }
}
