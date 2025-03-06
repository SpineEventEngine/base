/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.protobuf

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Protobuf string functions should")
internal class ProtobufLiteralSpec {

    @Test
    fun `restore escaped ASCII control characters`() {
        // The test string contains a mix of control characters and English letters.
        val asciiCodes = listOf(7, 8, 101, 102, 9, 10, 11, 72, 73, 12, 13, 111, 34, 39, 92)
        val asciiString = asciiCodes.map { it.toChar() }.joinToString()

        val expected = "\\a, \\b, e, f, \\t, \\n, \\v, H, I, \\f, \\r, o, \\\", \\', \\\\"
        val escaped = restoreProtobufEscapes(asciiString)
        escaped shouldBe expected

        // Uncomment these lines to debug the test.
        // println(asciiString) // Prints whitespaces and English letters.
        // println(escaped) // => `\a, \b, e, f, \t, \n, \v, H, I, \f, \r, o, \", \', \\`.
    }
}
