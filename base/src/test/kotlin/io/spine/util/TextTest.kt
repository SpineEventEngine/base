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
package io.spine.util

import com.google.common.testing.NullPointerTester
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TextTest {

    private val nl = System.lineSeparator()

    @Test
    fun `handle nulls passed to static methods`() {
        NullPointerTester().testAllPublicStaticMethods(Text::class.java)
    }

    @Test
    fun `split text into lines`() {
        val str = "uno${nl}dos${nl}tres"
        val text = Text(str)
        assertThat(text.lines()).containsExactly("uno", "dos", "tres")
    }

    @Test
    fun `join 'Iterable'`() {
        val iterable = listOf("bir", "iki", "üç")
        val text = Text(iterable)
        assertThat(text.toString()).isEqualTo("bir${nl}iki${nl}üç")
    }

    @Test
    fun `join an array`() {
        val array = arrayOf("one", "two", "three")
        val text = Text(array)
        assertThat(text.toString()).isEqualTo("one${nl}two${nl}three")
    }

    @Test
    fun `find substring`() {
        val text = Text.of("abra", "ka", "dabra")

        assertThrows<IllegalArgumentException> { text.contains("abra${nl}ka") }

        assertThat(text.contains("abra")).isTrue()
        assertThat(text.contains("kada")).isFalse()
    }

    @Test
    fun `must not accept lines with separators`() {
        assertThrows<IllegalArgumentException> {  Text.of("un", "${nl}o") }
        assertThrows<IllegalArgumentException> {  Text(listOf("dos", "tres${nl}")) }
    }

    @Test
    fun `always return the same value`() {
        val text = Text.of("donna", "be", "la", "mare")

        val value = text.value()
        assertThat(value).isSameInstanceAs(text.value())
    }
}
