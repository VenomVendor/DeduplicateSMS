/*
 *   Copyright (C) 2018 VenomVendor <info@VenomVendor.com>
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
package com.venomvendor.sms.deduplicate.data

class Country(val countryCode: String, private val displayName: String) : Comparable<Country> {

    override fun compareTo(other: Country): Int {
        return displayName.compareTo(other.displayName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Country) {
            return false
        }
        return countryCode == other.countryCode
    }

    override fun hashCode() = countryCode.hashCode()

    override fun toString() = "$displayName | $countryCode"
}
