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

package io.spine.base;

import com.google.common.testing.NullPointerTester;
import com.google.common.truth.Truth8;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.test.protobuf.AnyHolder;
import io.spine.test.protobuf.GenericHolder;
import io.spine.test.protobuf.StringHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.protobuf.AnyPacker.pack;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Field}.
 *
 * <p>See {@code field_paths_test.proto} for definition of proto types used in these tests.
 */
@DisplayName("`Field` should")
class FieldTest {

    @Test
    @DisplayName("pass null tolerance check")
    void nullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(Field.class);
    }

    @Test
    @DisplayName("parse the passed path")
    void parsing() {
        String path = "highway.to.hell";
        assertThat(Field.parse(path).toString())
                .isEqualTo(path);
    }

    @Test
    @DisplayName("create the field by its name")
    void byName() {
        String name = "my_way";
        assertThat(Field.named(name).toString())
                .isEqualTo(name);
    }

    @Test
    @DisplayName("create the instance by the path")
    void byPath() {
        FieldPath expected = FieldPaths.doParse("road_to.mandalay");
        assertThat(Field.withPath(expected).path())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("do not allow `.` in the field name")
    void noSeparatorInName() {
        assertThrows(IllegalArgumentException.class, () -> Field.named("with.dot"));
    }

    @Test
    @DisplayName("obtain the path")
    void path() {
        String path = "something.borrowed";
        assertThat(Field.parse(path)
                        .path()
                        .getFieldNameList())
                .containsExactly("something", "borrowed");
    }

    @Nested
    @DisplayName("obtain the value in a message")
    class GettingValue {

        private Message message;
        private Object expectedValue;

        @BeforeEach
        void createMessage() {
            Any value = pack(Time.currentTime());
            expectedValue = value.getTypeUrl();
            AnyHolder anyHolder = AnyHolder
                    .newBuilder()
                    .setVal(value)
                    .build();
            message = GenericHolder
                    .newBuilder()
                    .setAny(anyHolder)
                    .build();
        }

        @Test
        @DisplayName("if present")
        void ifFound() {
            Field field = Field.parse("any.val.type_url");
            Truth8.assertThat(field.valueIn(message))
                  .hasValue(expectedValue);
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            Truth8.assertThat(Field.parse("type_url")
                                   .valueIn(message))
                  .isEmpty();
        }
    }

    @Nested
    @DisplayName("obtain the descriptor of the field")
    class GettingDescriptor {

        @Test
        @DisplayName("if present")
        void ifFound() {
            Truth8.assertThat(Field.named("val")
                                   .descriptorIn(AnyHolder.getDescriptor()))
                  .isPresent();
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            Truth8.assertThat(Field.named("value") // the real name is `val`.
                                   .descriptorIn(StringHolder.getDescriptor()))
                  .isEmpty();
        }
    }

    @Nested
    @DisplayName("obtain the type of the field in a message type")
    class GettingType {

        @Test
        @DisplayName("for messages")
        void messageType() {
            Truth8.assertThat(Field.parse("holder_holder.holder")
                                   .typeIn(GenericHolder.class))
                  .hasValue(StringHolder.class);
        }

        @Test
        @DisplayName("for primitives")
        void primitiveType() {
            Truth8.assertThat(Field.named("count").typeIn(GenericHolder.class))
                  .hasValue(GenericHolder.Count.class);
        }

        @Test
        @DisplayName("for enums")
        void enumType() {
            Truth8.assertThat(Field.named("size").typeIn(GenericHolder.class))
                  .hasValue(int.class);
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            Truth8.assertThat(Field.parse("holder")
                                   .typeIn(GenericHolder.class))
                  .isEmpty();
        }
    }
}
