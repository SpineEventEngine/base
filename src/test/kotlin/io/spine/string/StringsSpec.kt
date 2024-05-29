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

package io.spine.string

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.spine.testing.TestValues.randomString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("Extensions for `String` should")
class StringsSpec {

    @ParameterizedTest
    @CsvSource("aaa,Aaa", "field_name,Field_name", "TypeName,TypeName", "_uri,_uri")
    fun `produce a title case string`(initial: String, expected: String) {
        initial.titleCase() shouldBe expected
    }

    @ParameterizedTest
    @CsvSource("aaa,Aaa", "field_name,FieldName", "TypeName,TypeName", "___u_ri____,URi")
    fun `produce a camel case string`(initial: String, expected: String) {
        initial.camelCase() shouldBe expected
    }

    @Test
    fun `trim whitespace`() {
        val value = """
            line one   
             line two 
        """
        // Check that we have space char at the end after `trimIndent()`.
        value.trimIndent().lines()[0].last() shouldBe ' '

        val trimmed = value.trimWhitespace()

        trimmed.lines()[0].last() shouldBe 'e' // as in "one".

        trimmed shouldBe "line one" + System.lineSeparator() + " line two"
    }

    @Test
    fun `trim indent preserving system line separators`() {
        val value = """
            line 1
            line 2
        """.ti()

        value shouldContain Separator.nl()
    }

    @Test
    fun `prepend indentation preserving system line separator`() {
        val lines = """
          a
           b
            c
        """.trimIndent() // This splits lines with "\n"
           .pi()         // This applies `Separator.NL` prepending with default indentation.

        lines shouldStartWith "    a"
        lines shouldContain Separator.nl()
    }

    @Test
    fun `join iterable with backticks`() {
        listOf("a", "b", "c").joinBackticked() shouldBe "`a`, `b`, `c`"
    }

    @Test
    fun `provide extension for stringifying an object`() {
        val value = "some value"
        value.stringify() shouldBe value
    }

    @Test
    fun `indent lines`() {
        val source = """
            line 1
            line 2
            line 3
        """.ti().lines()

        source.indent(Indent(size = 2), level = 2) shouldBe """
            |    line 1
            |    line 2
            |    line 3
        """.tm()

        source.indent(Indent(size = 3), level = 0) shouldBe """
            |line 1
            |line 2
            |line 3
        """.tm()
    }

    @Test
    fun `encode and decode using Base64`() {
        val original = randomString()
        val encoded: String = original.toBase64Encoded()
        val decoded: String = encoded.decodeBase64()

        decoded shouldBe original
    }

    @Test
    fun `count non-overlapping subscrings`() {
        "baobab".count("mao") shouldBe 0
        "ababababa".count("aba") shouldBe 2
        "the three truths".count("th") shouldBe 3
    }
}
