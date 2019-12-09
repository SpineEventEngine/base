/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Syntax;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.STRING;
import static com.google.protobuf.Syntax.SYNTAX_PROTO3;
import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.given.GivenField.mapContext;
import static io.spine.validate.given.GivenField.repeatedContext;
import static io.spine.validate.given.GivenField.scalarContext;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FieldValue should")
class FieldValueTest {

    @Nested
    @DisplayName("convert")
    class Convert {

        @Test
        @DisplayName("a map to values")
        void map() {
            Map<String, String> map = ImmutableMap.of(newUuid(), newUuid(), newUuid(), newUuid());
            FieldValue> fieldValue = FieldValue.of(map, mapContext());
            assertConversion(map.values(), fieldValue);
        }

        @Test
        @DisplayName("a repeated field")
        void repeated() {
            List<String> repeated = ImmutableList.of(newUuid(), newUuid());
            FieldValue> fieldValue =
                    FieldValue.of(repeated, repeatedContext());
            assertConversion(repeated, fieldValue);
        }

        @Test
        @DisplayName("a scalar field")
        void scalar() {
            String scalar = newUuid();
            FieldValue fieldValue = FieldValue.of(scalar, scalarContext());
            assertConversion(singletonList(scalar), fieldValue);
        }
    }

    @Nested
    @DisplayName("determine JavaType for")
    class DetermineJavaType {

        @Test
        @DisplayName("a map")
        void map() {
            FieldValue> mapValue =
                    FieldValue.of(ImmutableMap.<String, String>of(), mapContext());
            assertEquals(STRING, mapValue.javaType());
        }

        @Test
        @DisplayName("a repeated")
        void repeated() {
            FieldValue> repeatedValue =
                    FieldValue.of(ImmutableList.<String>of(), repeatedContext());
            assertEquals(STRING, repeatedValue.javaType());
        }
    }

    @Test
    @DisplayName("handle Enum value")
    void enumValue() {
        Syntax rawValue = SYNTAX_PROTO3;
        FieldValue enumValue = FieldValue.of(rawValue, scalarContext());
        List<EnumValueDescriptor> expectedValues = singletonList(rawValue.getValueDescriptor());
        assertConversion(expectedValues, enumValue);
    }

    @Nested
    @DisplayName("check if the value is default for a")
    @SuppressWarnings("Immutable")
    class Default {

        @Test
        @DisplayName("repeated fields")
        void repeatedField() {
            assertDefault(FieldValue.of(ImmutableList.of("", "", ""), repeatedContext()));
            assertNotDefault(FieldValue.of(ImmutableList.of("", "abc", ""), repeatedContext()));
        }

        @Test
        @DisplayName("map fields")
        void mapField() {
            assertDefault(FieldValue.of(ImmutableMap.of("aaaa", ""), mapContext()));
            assertNotDefault(FieldValue.of(ImmutableMap.of("", "",
                                                           "aaaa", "aaa",
                                                           " ", ""),
                                           mapContext()));
        }

        @Test
        @DisplayName("string fields")
        void stringField() {
            assertDefault(FieldValue.of("", scalarContext()));
            assertNotDefault(FieldValue.of(" ", scalarContext()));
        }

        private void assertDefault(FieldValue value) {
            assertThat(value.isDefault()).isTrue();
        }

        private void assertNotDefault(FieldValue value) {
            assertThat(value.isDefault()).isFalse();
        }
    }

    private static <T> void assertConversion(Collection<T> expectedValues, FieldValue fieldValue) {
        assertEquals(expectedValues, fieldValue.asList());
    }
}
