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

package com.venomvendor.sms.deduplicate.core.internal

import android.net.Uri
import android.provider.Telephony
import com.venomvendor.sms.deduplicate.core.ktx.DefaultDispatcherProvider
import com.venomvendor.sms.deduplicate.core.ktx.DispatcherProvider
import org.koin.core.KoinComponent

class MmsDeleter(dispatcher: DispatcherProvider = DefaultDispatcherProvider()) :
    SmsDeleter(dispatcher), KoinComponent {

    override val primaryKey = Telephony.Mms._ID

    override val messagingType: Uri = Telephony.Mms.CONTENT_URI
}
