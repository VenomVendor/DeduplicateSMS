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

package com.venomvendor.sms.deduplicate.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CountryTest {

    @Test
    internal fun `constructor test`() {
        val country = Country("IN", "SATURN")
        assertNotNull(country)
    }

    @Test
    internal fun `when id is same, Country should be equal`() {
        val country1 = Country("IN", "SATURN")
        val country2 = Country("IN", "VENUS")

        assertEquals(country1, country2)
    }

    @Test
    internal fun `when id is same, Country should be equal in equality`() {
        val country1 = Country("IN", "SATURN")
        val country2 = Country("IN", "VENUS")

        assertTrue(country1 == country2)
    }

    @Test
    internal fun `when items are same, Country should be same`() {
        val country = Country("IN", "SATURN")

        assertSame(country, country)
    }

    @Test
    internal fun `when items are same, Country should be same in equality`() {
        val country = Country("IN", "SATURN")

        assertEquals(country, country)
    }

    @Test
    internal fun `when type is different, items are different`() {
        val country = Country("IN", "SATURN")

        assertNotEquals(country, Any())
    }

    @Test
    internal fun `when type is null, items are different`() {
        val country = Country("IN", "SATURN")

        assertNotEquals(country, null)
    }

    @Test
    internal fun `items should be arranged by display name`() {

        val countries = mutableListOf(
            Country("2", "B"),
            Country("4", "a"),
            Country("3", "Z"),
            Country("1", "A"),
            Country("5", "b")
        )

        countries.sort()

        assertEquals(5, countries.count())

        countries.forEachIndexed { index, country ->
            assertEquals(index.plus(1).toString(), country.countryCode)
        }
    }

    @Test
    internal fun `evaluating toString`() {
        val country = Country("IN", "SATURN")

        assertEquals("SATURN | IN", country.toString())
    }

    @Test
    internal fun `evaluating hashCode`() {
        val country = Country("IN", "SATURN")

        assertNotEquals(-1, country.hashCode())
    }
}
