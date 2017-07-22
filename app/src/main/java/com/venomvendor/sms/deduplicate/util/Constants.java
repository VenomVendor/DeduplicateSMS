/*
 *   Copyright (C) 2015 VenomVendor <info@VenomVendor.com>
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

package com.venomvendor.sms.deduplicate.util;

import android.net.Uri;

/*
 * Created by VenomVendor on 11/8/15.
 */
public final class Constants {
    public static final String CURRENT_SMS_APP = "currentSmsApp";
    public static final String SHOW_EULA = "eula";
    public static final Uri CONTENT_URI = Uri.parse("content://sms");
    public static final String _ID = "_id";
    public static final String ADDRESS = "address";
    public static final String DATE = "date";
    public static final String DATE_SENT = "date_sent";
    public static final String TYPE = "type";

    public static final String BODY = "body";

    private Constants() {
    }
}
