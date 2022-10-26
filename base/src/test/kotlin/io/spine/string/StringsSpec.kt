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

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("Extensions for `String` should")
class StringsSpec {

    @ParameterizedTest
    @CsvSource("aaa,Aaa", "field_name,Field_name", "TypeName,TypeName", "_uri,_uri")
    fun `produce a title case string`(initial: String, expected: String) {
        assertThat(initial.titleCase())
            .isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource("aaa,Aaa", "field_name,FieldName", "TypeName,TypeName", "___u_ri____,URi")
    fun `produce a camel case string`(initial: String, expected: String) {
        assertThat(initial.camelCase())
            .isEqualTo(expected)
    }

    @Test
    fun `trim whitespace`() {
        val value = """
            line one   
             line two 
        """
        assertThat(value.trimIndent().lines()[0].last())
            .isEqualTo(' ')
        val trimmed = value.trimWhitespace()
        assertThat(trimmed.lines()[0].last())
            .isEqualTo('e')
        assertThat(trimmed).isEqualTo(
            "line one" + System.lineSeparator() + " line two"
        )
    }
}
