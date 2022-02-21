/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.code.version

import com.google.common.testing.EqualsTester
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `'Version' should` {

    @Nested
    inner class `be created by` {

        @Test
        fun `'int' components`() {
            assertValueOf(Version(1, 0), "1.0")
            assertValueOf(Version(1, 0, 0), "1.0.0")
            assertValueOf(Version(1, 0, 100), "1.0.100")
            assertValueOf(Version(2, 0, null), "2.0")
            assertValueOf(Version(2, 0, null, null), "2.0")
        }
    }

    @Test
    fun `assume zero patch if null passed and snapshot is not null`() {
        assertValueOf(Version(2, 0, patch = null, snapshot = 0), "2.0.0-SNAPSHOT.0")
    }

    private fun assertValueOf(v: Version, s: String) = assertThat(v.toString()).isEqualTo(s)

    @Nested
    inner class prohibit {

        @Test
        fun `negative values`() {
            assertIllegal { Version(-1, 0) }
            assertIllegal { Version(0, -1) }
            assertIllegal { Version(0, 0, -1) }
            assertIllegal { Version(0, 0, 0, -1) }
            assertIllegal { Version(0, 0, null, -1) }
        }

        private fun assertIllegal(v: () -> Version) =
            assertThrows<IllegalArgumentException> { v.invoke() }
    }

    @Test
    fun `implement equality`() {
        EqualsTester()
            .addEqualityGroup(Version(1, 0), Version(1, 0))
            .addEqualityGroup(Version(2, 0, 0))
            .addEqualityGroup(Version(3, 0, 0, 100))
            .testEquals()
    }

    @Nested
    inner class `provide comparison having` {

        @Test
        fun `version with patch greater than without`() {
            assertThat(Version(1, 0, 0)).isGreaterThan(Version(1, 0))
        }

        @Test
        fun `compared each component`() {
            assertThat(Version(0, 0, 0)).isLessThan(Version(0, 0, 1))
            assertThat(Version(0, 0, 0)).isLessThan(Version(0, 1, 0))
            assertThat(Version(0, 0, 0)).isLessThan(Version(1, 0, 0))
            assertThat(Version(1, 0, 0)).isLessThan(Version(1, 0, 1))
            assertThat(Version(1, 0, 0)).isLessThan(Version(2, 0, 0))
            assertThat(Version(1, 0, 0)).isLessThan(Version(1, 1, 0))
            assertThat(Version(1, 0, 0)).isLessThan(Version(1, 0, 1))
            assertThat(Version(1, 0, 0, 0)).isLessThan(Version(1, 0, 0, 1))

            assertThat(Version(2, 0)).isGreaterThan(Version(1, 100, 1000, 1000000))
            
            // A version with the patch index is generally greater than without.
            assertThat(Version(1, 0, 0)).isGreaterThan(Version(1, 0)) // The edge case.
            assertThat(Version(1, 0, 1)).isGreaterThan(Version(1, 0))
        }

        @Test
        fun `release version greater than snapshot`() {
            assertThat(Version(0, 0, 0)).isGreaterThan(Version(0, 0, 0, 0))
            assertThat(Version(1, 0, 0, 0)).isLessThan(Version(1, 0, 0))
        }
    }
}
