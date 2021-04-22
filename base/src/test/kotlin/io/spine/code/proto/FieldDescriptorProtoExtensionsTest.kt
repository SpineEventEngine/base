/*
 * Copyright 2021, TeamDev. All rights reserved.
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
package io.spine.code.proto

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64
import com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING
import io.spine.code.proto.given.Given
import io.spine.testing.Assertions.assertIllegalState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class `'FieldDescriptorProtoExtensions' should` {

    @Nested
    internal inner class `check if field` {

        @Test
        fun `is message`() {
            assertTrue(Given.messageField().isMessage())
            assertFalse(Given.primitiveField().isMessage())
            assertFalse(Given.enumField().isMessage())
            assertFalse(Given.repeatedField().isMessage())
        }

        @Test
        fun `is repeated`() {
            assertTrue(Given.repeatedField().isRepeated())
            assertFalse(Given.mapField().isRepeatedIndeed())
            assertFalse(Given.singularField().isRepeated())
        }

        @Test
        fun `is map`() {
            assertTrue(Given.mapField().isMap())
            assertFalse(Given.singularField().isMap())
        }
    }

    @Test
    fun `obtain a map entry name`() {
        Assertions.assertEquals("MapFieldEntry", Given.mapField().entryName())
    }

    @Test
    fun `get key descriptor for a map field`() {
        val key = Given.mapField().keyDescriptor()
        assertThat(key.type).isEqualTo(INT64)
    }

    @Test
    fun `get value descriptor for a map field`() {
        val value = Given.mapField().valueDescriptor()
        assertThat(value.type).isEqualTo(STRING)
    }

    @Nested
    internal inner class `throw 'IllegalArgumentException' if` {
        @Test
        fun `getting key descriptor from non-map field`() {
            assertIllegalState {
                Given.repeatedField().keyDescriptor()
            }
        }

        @Test
        fun `getting value descriptor from non-map field`() {
            assertIllegalState {
                Given.repeatedField().valueDescriptor()
            }
        }
    }
}
