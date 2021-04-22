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

package io.spine.code.proto;

import io.spine.code.proto.FieldName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FieldName should")
class FieldNameTest {

    private static final String PROTO_FIELD_NAME = "correct_java_name";

    private final FieldName fieldName = FieldName.of(PROTO_FIELD_NAME);
    private final FieldName fieldNameWithNumbers = FieldName.of("hand22hand");

    @Nested
    @DisplayName("obtain CamelCase")
    class CamelCase {

        @Test
        @DisplayName("of lower-cased letters")
        void lowerCasedLetters() {
            assertCamelCase("CorrectJavaName", fieldName);
        }

        @Test
        @DisplayName("of lower-cased letters with a number")
        void lowerCasedLettersAndNumbers() {
            assertCamelCase("Hand22Hand", fieldNameWithNumbers);
        }

        @Test
        @DisplayName("of capitalized name")
        void capitalizedName() {
            assertCamelCase("TypeURLString", FieldName.of("type_URL_string"));
        }

        private void assertCamelCase(String expectedCamelCase, FieldName fieldName) {
            assertEquals(expectedCamelCase, fieldName.toCamelCase());
        }
    }

    @Nested
    @DisplayName("obtain javaCase")
    class JavaCase {

        @Test
        @DisplayName("of lower-cased letters")
        void lowerCasedLetters() {
            assertJavaCase("correctJavaName", fieldName);
        }

        @Test
        @DisplayName("of lower-cased letters with a number")
        void lowerCasedLettersAndNumbers() {
            assertJavaCase("hand22Hand", fieldNameWithNumbers);
        }

        private void assertJavaCase(String expectedJavaCase, FieldName fieldName) {
            assertEquals(expectedJavaCase, fieldName.javaCase());
        }
    }
}
