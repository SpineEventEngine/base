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

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertWithMessage
import io.kotest.matchers.shouldBe
import io.spine.io.Glob.Companion.extension
import io.spine.io.Glob.Companion.extensionLowerAndUpper
import java.nio.file.Paths
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Glob` Java API should expose")
internal class GlobSpec {
    
    /** The test subject.  */
    private var glob: Glob? = null

    @Test
    fun `'any' pattern`() {
        Glob.any.matches(Paths.get(".")) shouldBe true
    }

    @Nested
    @DisplayName("`extensions()` method with")
    internal inner class ExtensionsMethod {

        @Test
        fun `'vararg' parameter`() {
            glob = extension(".bar", ".b")
            assertMatches("f.bar")
            assertMatches("baz.b")
        }

        @Test
        fun `'Iterable' parameter`() {
            glob = extension(ImmutableList.of("cc", "h", "hpp", "cpp"))
            assertMatches("format.cc")
            assertMatches("sprintf.h")
        }
    }

    @Nested
    @DisplayName("`extensionLowerAndUpper()` method with")
    internal inner class ExtensionLowerAndUpperMethod {

        @Test
        fun `'vararg' parameter`() {
            glob = extensionLowerAndUpper("high", "LOW")
            assertMatches("1.high")
            assertMatches("2.HIGH")
            assertMatches("3.low")
            assertMatches("4.LOW")
        }

        @Test
        fun `'Iterable' parameter`() {
            glob = extensionLowerAndUpper(".snake", "CASE")
            assertMatches("1.snake")
            assertMatches("2.SNAKE")
            assertMatches("3.case")
            assertMatches("4.CASE")
        }
    }

    private fun assertMatches(fileName: String) {
        val p = Paths.get(fileName)
        val matches = glob!!.matches(p)
        assertWithMessage(
            "The file `%s` should match the pattern `%s`.", fileName, glob!!.pattern
        ).that(matches).isTrue()
    }
}
