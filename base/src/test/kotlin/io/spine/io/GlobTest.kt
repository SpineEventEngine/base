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

package io.spine.io

import com.google.common.truth.Truth.assertThat
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `'Glob' should` {

    @Test
    fun `prohibit empty pattern`() {
        assertThrows<IllegalArgumentException> { Glob("") }
    }

    @Nested
    inner class `create instances by extension which` {

        @Test
        fun `is empty`() {
            assertExtensionMatches("", "some/where/file.")
        }

        @Test
        fun `has leading dot`() {
            assertExtensionMatches(".foo", "1/2/3/file.foo")
        }

        @Test
        fun `is just text`() {
            assertExtensionMatches("bar", "4/5/file.bar")
        }

        private fun assertExtensionMatches(extension: String, path: String) {
            val g = Glob.extension(extension)
            val p = Paths.get(path)
            val matches = g.matches(p)
            assertThat(matches).isTrue()
        }
    }

    @Test
    fun `allow both lowercase and uppercase values`() {
        val g = Glob.extensionLowerAndUpper("hey", "jude")
        val lower = Paths.get("file.hey")
        val upper = Paths.get("another.HEY")
        val more = Paths.get("more.JUDE")
        val another = Paths.get("yet.jude")
        val mixed = Paths.get("mix.Hey")

        fun assertMatches(p: Path) {
            assertThat(g.matches(p)).isTrue()
        }
        
        fun assertDoesNotMatch(p: Path) {
            assertThat(g.matches(p)).isFalse()
        }

        assertMatches(lower)
        assertMatches(upper)
        assertMatches(more)
        assertMatches(another)

        assertDoesNotMatch(mixed)
    }
}
