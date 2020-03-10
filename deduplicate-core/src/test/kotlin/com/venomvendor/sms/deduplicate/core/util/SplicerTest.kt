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

package com.venomvendor.sms.deduplicate.core.util

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SplicerTest {

    private lateinit var splicer: Splicer<String>

    @BeforeEach
    internal fun setUp() {
        splicer = Splicer()
    }

    @Test
    internal fun `check if collection is empty`() {
        assertTrue(splicer.isEmpty())
    }

    @Test
    internal fun `check if collection is not empty`() {
        splicer = Splicer(listOf("1", "2", "3"))

        assertTrue(splicer.isNotEmpty())
    }

    @Test
    internal fun `splice should return two items`() {
        splicer = Splicer(listOf("1", "2", "3"))
        val spliced = splicer.splice(0, 2)
        assertEquals(2, spliced.count())
    }

    @Test
    internal fun `splice should retain one item`() {
        splicer = Splicer(listOf("1", "2", "3"))
        splicer.splice(0, 2)
        assertEquals(1, splicer.count())
    }

    @Test
    internal fun `splice should retain one item at last`() {
        splicer = Splicer(listOf("1", "2", "3"))
        splicer.splice(0, 2)

        assertSame(splicer.last(), splicer.first())
        assertEquals("3", splicer.first())
    }

    @Test
    internal fun `should splice empty list without exception`() {
        assertDoesNotThrow {
            splicer.splice(0, 0)
        }
    }

    @Test
    internal fun `should throw exception in empty list`() {
        assertThrows<IndexOutOfBoundsException> {
            splicer.splice(1, 2)
        }
    }

    @Test
    internal fun `splice should throw exception on negative`() {
        splicer = Splicer(listOf("1", "2", "3"))

        assertThrows<IndexOutOfBoundsException> {
            splicer.splice(-1, 0)
        }
    }

    @Test
    internal fun `splice should throw exception on exceeding positive`() {
        splicer = Splicer(listOf("1", "2", "3"))

        assertThrows<IndexOutOfBoundsException> {
            splicer.splice(splicer.count(), splicer.count() + 1)
        }
    }

    @Test
    internal fun `can add item post initialize`() {
        splicer = Splicer()
        splicer.add("1")
        splicer.add("2")

        assertEquals(2, splicer.count())
    }

    @Test
    internal fun `can add items post initialize`() {
        splicer = Splicer()
        splicer.addAll(listOf("1", "2", "3"))

        assertEquals(3, splicer.count())
    }
}
