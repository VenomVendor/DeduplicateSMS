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

package com.venomvendor.sms.deduplicate.data;

public final class Country implements Comparable<Country> {

    private final String countryCode;
    private final String displayName;

    public Country(String countryCode, String displayName) {
        this.countryCode = countryCode;
        this.displayName = displayName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public int compareTo(Country another) {
        return displayName.compareTo(another.displayName);
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        if (!(another instanceof Country)) {
            return false;
        }

        Country country = (Country) another;

        return countryCode != null ?
                countryCode.equals(country.countryCode) :
                country.countryCode == null;
    }

    @Override
    public int hashCode() {
        return countryCode != null ? countryCode.hashCode() : 0;
    }

    @Override
    public String toString() {
        return displayName + " | " + countryCode;
    }
}
