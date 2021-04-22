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

package io.spine.tools.code.proto;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING;
import static io.spine.tools.code.proto.FieldTypes.keyDescriptor;
import static io.spine.tools.code.proto.FieldTypes.valueDescriptor;
import static io.spine.tools.code.proto.given.Given.enumField;
import static io.spine.tools.code.proto.given.Given.mapField;
import static io.spine.tools.code.proto.given.Given.messageField;
import static io.spine.tools.code.proto.given.Given.primitiveField;
import static io.spine.tools.code.proto.given.Given.repeatedField;
import static io.spine.tools.code.proto.given.Given.singularField;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`FieldTypes` utility should")
class FieldTypesTest extends UtilityClassTest<FieldTypes> {

    FieldTypesTest() {
        super(FieldTypes.class);
    }

    @Nested
    @DisplayName("check if a field is")
    class CheckIfField {

        @Test
        @DisplayName("a `Message`")
        void isMessage() {
            assertTrue(FieldTypes.isMessage(messageField()));
            assertFalse(FieldTypes.isMessage(primitiveField()));
            assertFalse(FieldTypes.isMessage(enumField()));
        }

        @Test
        @DisplayName("`repeated`")
        void isRepeated() {
            assertTrue(FieldTypes.isRepeated(repeatedField()));
            assertFalse(FieldTypes.isRepeated(singularField()));
        }

        @Test
        @DisplayName("a `Map`")
        void isMap() {
            assertTrue(FieldTypes.isMap(mapField()));
            assertFalse(FieldTypes.isMap(singularField()));
        }
    }

    @Test
    @DisplayName("not mark map field as `repeated`")
    void notMarkMapAsRepeated() {
        assertFalse(FieldTypes.isRepeated(mapField()));
    }

    @Test
    @DisplayName("get key descriptor for a `Map` field")
    void getKeyDescriptor() {
        FieldDescriptor key = keyDescriptor(mapField());
        assertEquals(INT64, key.getType());
    }

    @Test
    @DisplayName("get value descriptor for a `Map` field")
    void getValueDescriptor() {
        FieldDescriptor value = valueDescriptor(mapField());
        assertEquals(STRING, value.getType());
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` if")
    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
            // Calling methods to throw exception.
    class Prohibit {

        @Test
        @DisplayName("getting key descriptor from non-map field")
        void getKeyForNonMap() {
            assertIllegalArgument(() -> keyDescriptor(repeatedField()));
        }

        @Test
        @DisplayName("getting value descriptor from non-map field")
        void getValueForNonMap() {
            assertIllegalArgument(() -> valueDescriptor(repeatedField()));
        }
    }
}
