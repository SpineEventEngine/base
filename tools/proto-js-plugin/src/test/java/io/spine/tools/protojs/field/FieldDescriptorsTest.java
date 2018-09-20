/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.field;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.js.FieldDescriptors;
import io.spine.testing.UtilityClassTest;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import spine.test.protojs.Task.TaskId;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static io.spine.code.js.FieldDescriptors.camelCaseName;
import static io.spine.code.js.FieldDescriptors.keyDescriptor;
import static io.spine.code.js.FieldDescriptors.valueDescriptor;
import static io.spine.tools.protojs.given.Given.mapField;
import static io.spine.tools.protojs.given.Given.messageField;
import static io.spine.tools.protojs.given.Given.primitiveField;
import static io.spine.tools.protojs.given.Given.repeatedField;
import static io.spine.tools.protojs.given.Given.timestampField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("InnerClassMayBeStatic") // JUnit nested classes cannot be static.
@DisplayName("Fields utility should")
class FieldDescriptorsTest extends UtilityClassTest<FieldDescriptors> {

    FieldDescriptorsTest() {
        super(FieldDescriptors.class);
    }

    @Nested
    @DisplayName("check if field")
    class CheckIfField {

        @Test
        @DisplayName("is message")
        void isMessage() {
            assertTrue(FieldDescriptors.isMessage(messageField()));
            assertFalse(FieldDescriptors.isMessage(primitiveField()));
            assertTrue(FieldDescriptors.isMessage(repeatedField()));
        }

        @Test
        @DisplayName("is standard type with known parser")
        void isWellKnownType() {
            assertTrue(FieldDescriptors.isWellKnownType(timestampField()));
            assertFalse(FieldDescriptors.isWellKnownType(messageField()));
            assertFalse(FieldDescriptors.isWellKnownType(primitiveField()));
        }

        @Test
        @DisplayName("is repeated")
        void isRepeated() {
            assertTrue(FieldDescriptors.isRepeated(repeatedField()));
        }

        @Test
        @DisplayName("is map")
        void isMap() {
            assertTrue(FieldDescriptors.isMap(mapField()));
        }
    }

    @Test
    @DisplayName("not mark map field as repeated")
    void notMarkMapAsRepeated() {
        assertFalse(FieldDescriptors.isRepeated(mapField()));
    }

    @Test
    @DisplayName("get key descriptor for map field")
    void getKeyDescriptor() {
        FieldDescriptor key = keyDescriptor(mapField());
        assertEquals(INT64, key.getType());
    }

    @Test
    @DisplayName("get value descriptor for map field")
    void getValueDescriptor() {
        FieldDescriptor value = valueDescriptor(mapField());
        assertEquals(MESSAGE, value.getType());
        Descriptor messageType = value.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(messageType);
        TypeUrl expected = TypeUrl.from(TaskId.getDescriptor());
        assertEquals(expected, typeUrl);
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Calling methods to throw exception.
    @Nested
    @DisplayName("throw IAE if")
    class ThrowIae {

        @Test
        @DisplayName("getting key descriptor from non-map field")
        void getKeyForNonMap() {
            assertThrows(IllegalArgumentException.class, () -> keyDescriptor(repeatedField()));
        }

        @Test
        @DisplayName("getting value descriptor from non-map field")
        void getValueForNonMap() {
            assertThrows(IllegalArgumentException.class, () -> valueDescriptor(repeatedField()));
        }
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    @Test
    @DisplayName("return CamelCase field name")
    void getCapitalizedName() {
        String camelCaseName = camelCaseName(messageField());
        String expected = "MessageField";
        assertEquals(expected, camelCaseName);
    }
}
