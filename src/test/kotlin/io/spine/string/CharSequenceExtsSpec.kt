/*
 * Copyright 2023, TeamDev. All rights reserved.
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

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.util.stream.Stream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("`CharSequence` extensions should")
class CharSequenceExtsSpec {

    @Test
    fun `find line separators of sorts`() {
        val text = "1\r2\n3\r\n"

        val separators = text.findLineSeparators()

        separators.keys shouldBe setOf(IntRange(1, 1), IntRange(3, 3), IntRange(5, 6))
        separators.values shouldContainInOrder listOf(Separator.CR, Separator.LF, Separator.CRLF)
    }

    @ParameterizedTest
    @MethodSource("separatorsAndExpectations")
    fun `tell if a sequence contains a separator`(input: String, expected: Boolean) {
        input.containsLineSeparators() shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("separatorsAndNonSysExpectations")
    fun `tell if a sequence contains non-system line separators`(input: String, expected: Boolean) {
        input.containsNonSystemLineSeparator() shouldBe expected
    }

    @Test
    fun `tell if a sequence contains a 'Separator'`() {
        Separator.values().forEach {
            " ${it.value} ".contains(it) shouldBe true
            // `Separator` does not override `toString()` to return its `value`, so...
            " $it ".contains(it) shouldBe false
        }
    }

    @ParameterizedTest
    @MethodSource("escapingSeparators")
    fun `escape line separators in a char sequence`(input: CharSequence, expected: String) {
        input.escapeLineSeparators() shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("revealingSeparators")
    fun `reveal line separators in a char sequence`(input: CharSequence, expected: String) {
        input.revealLineSeparators() shouldBe expected
    }

    @Test
    fun `return same instance if there are no separators`() {
        val noSeparators = "no separators"
        noSeparators.escapeLineSeparators() shouldBeSameInstanceAs noSeparators
        noSeparators.revealLineSeparators() shouldBeSameInstanceAs noSeparators
    }

    @Test
    fun `tell if the sequence contains any of characters`() {
        val binaryName = "org.example.MyClass\$Nested"
        binaryName.containsAny('.', '$') shouldBe true
        binaryName.containsAny('_', 'w', 'z') shouldBe false
    }

    companion object {

        @JvmStatic
        fun separatorsAndExpectations(): Stream<Arguments> {
            return Stream.of(
                arguments("1\r2", true),
                arguments("3\n4\"", true),
                arguments("5\r\n6\"", true),
                arguments("foo", false)
            )
        }

        @JvmStatic
        fun separatorsAndNonSysExpectations(): Stream<Arguments> {
            val builder = Stream.builder<Arguments>()
            Separator.nonSystem().forEach {
                builder.add(arguments(" ${it.value} ", true))
            }
            builder.add(arguments(" ${Separator.system.value} ", false))
            return builder.build()
        }

        @JvmStatic
        fun escapingSeparators(): Stream<Arguments> {
            return Stream.of(
                arguments("\r", "\\r"),
                arguments(" \r ", " \\r "),

                arguments("\r\r", "\\r\\r"),

                arguments("\r\n", "\\r\\n"),
                arguments("\r\n ", "\\r\\n "),

                arguments("\r \n", "\\r \\n"),
                arguments("\r \n ", "\\r \\n "),

                arguments("1\r 2\n 3\r\n", "1\\r 2\\n 3\\r\\n")
            )
        }

        @JvmStatic
        fun revealingSeparators(): Stream<Arguments> {
            val nl = Separator.nl()
            return Stream.of(
                arguments("\r", "\\r$nl"),
                arguments(" \r ", " \\r$nl "),

                arguments("\r\r", "\\r$nl\\r$nl"),

                arguments("\r\n", "\\r\\n$nl"),
                arguments("\r\n ", "\\r\\n$nl "),

                arguments("\r \n", "\\r$nl \\n$nl"),
                arguments("\r \n ", "\\r$nl \\n$nl "),

                arguments("1\r 2\n 3\r\n", "1\\r$nl 2\\n$nl 3\\r\\n$nl")
            )
        }
    }
}
