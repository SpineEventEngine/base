/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.base

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Extensions for `FieldPath` should")
internal class FieldPathExtsSpec {

    @Nested inner class
    prohibit {

        @Test
        fun `empty paths`() {
            assertIllegal("")
        }

        @Test
        fun `paths not matching the pattern`() {
            assertIllegal("foo/bar")
            assertIllegal(" f o o")
            assertIllegal("0chance")
            assertIllegal(".pasaran")
        }

        private fun assertIllegal(path: String) {
            assertThrows<IllegalArgumentException> {
                FieldPath(path)
            }
        }
    }

    @Test
    fun `tell if the path is nested or not`() {
        FieldPath("fiz.b_z").isNotNested shouldBe false
        FieldPath("top").isNotNested shouldBe true
    }

    @Test
    fun `join into string`() {
        val path = "big.bada.boom"
        FieldPath(path).joined shouldBe path

        val notNested = "plain"
        FieldPath(notNested).joined shouldBe notNested
    }

    @Test
    fun `obtain the top path component`() {
        FieldPath("going.down").root shouldBe "going"
    }

    @Test
    fun `obtain a nested path`() {
        FieldPath("from.top_to.bottom").stepInto() shouldBe FieldPath("top_to.bottom")
        assertThrows<IllegalStateException> {
            FieldPath("top").stepInto()
        }
    }
}
