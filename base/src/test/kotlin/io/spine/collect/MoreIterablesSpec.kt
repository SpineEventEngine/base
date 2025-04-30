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

package io.spine.collect

import io.kotest.matchers.shouldBe
import java.util.stream.Stream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("Extensions for `Iterable` should")
class MoreIterablesSpec {

    @Test
    fun `obtain the only element of a collection`() {
        val list = listOf(42)
        list.theOnly() shouldBe 42
    }

    @Test
    fun `fail to obtain the only element if collection is empty`() {
        val set = setOf<Any>()
        assertThrows<NoSuchElementException> { set.theOnly() }
    }

    @Test
    fun `fail to obtain the only element if collection has many elements`() {
        val set = setOf<Any>("foo", "bar")
        assertThrows<IllegalArgumentException> { set.theOnly() }
    }

    @ParameterizedTest
    @MethodSource("interlaceCollections")
    fun `interlace a collection`(elements: List<Any>, separator: Any, expected: List<Any>) {
        elements.interlaced(separator).toList() shouldBe expected
    }

    companion object {

        @Suppress("unused") // Used by JUnit.
        @JvmStatic
        fun interlaceCollections(): Stream<Arguments> = Stream.of(
            Arguments.arguments(listOf(0, 1, 2), 42, listOf(0, 42, 1, 42, 2)),
            Arguments.arguments(
                listOf("sea", "Moon", "Earth", "Sun"),
                "of",
                listOf("sea", "of", "Moon", "of", "Earth", "of", "Sun")
            ),
            Arguments.arguments(listOf<String>(), "doesn't matter", listOf<String>()),
        )
    }
}
