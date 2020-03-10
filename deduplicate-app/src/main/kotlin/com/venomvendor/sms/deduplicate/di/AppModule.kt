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

package com.venomvendor.sms.deduplicate.di

import android.app.Application
import android.net.Uri
import com.venomvendor.sms.deduplicate.core.di.MessagingType
import com.venomvendor.sms.deduplicate.core.factory.DeletionManager
import com.venomvendor.sms.deduplicate.manager.DeletionHandler
import org.koin.dsl.module

/**
 * DI module for core
 */
val appModule = module {

    factory {
        get<Application>().contentResolver
    }

    /**
     * For SMS Deletion
     */
    factory<DeletionManager> { (wrapper: MessagingType) ->
        /* Uri should start with `content://` */
        DeletionHandler(Uri.parse("content://${wrapper.uri}"))
    }
}
