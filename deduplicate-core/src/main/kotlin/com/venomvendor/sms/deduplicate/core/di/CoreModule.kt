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

package com.venomvendor.sms.deduplicate.core.di

import com.venomvendor.sms.deduplicate.core.factory.Deleter
import com.venomvendor.sms.deduplicate.core.internal.MmsDeleter
import com.venomvendor.sms.deduplicate.core.internal.SmsDeleter
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Types of offline messages
 */
enum class MessagingType(val uri: String) {
    SMS("sms"),
    MMS("mms")
}

/**
 * DI module for core
 */
val coreModule = module {

    /**
     * For SMS Deletion
     */
    factory<Deleter>(named(MessagingType.SMS)) {
        SmsDeleter()
    }

    /**
     * For MMS Deletion
     */
    factory<Deleter>(named(MessagingType.MMS)) {
        MmsDeleter()
    }
}
