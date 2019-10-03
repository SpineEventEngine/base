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
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.test.protobuf.AnyHolder;
import io.spine.test.protobuf.GenericHolder;
import io.spine.test.protobuf.GenericHolder.Count;
import io.spine.test.protobuf.StringHolder;
import io.spine.test.protobuf.StringHolderHolder;
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


    @Nested
    @DisplayName("parse the passed path")
    class Parsing {

        @Test
        @DisplayName("with immediate field name")
        void immediate() {
            String expected = "val";
            Field field = Field.parse(expected);
            assertThat(field.path().getFieldNameList())
                    .containsExactly(expected);
        }

        @Test
        @DisplayName("delimited with dots")
        void nested() {
            String path = "highway.to.hell";
            Field field = Field.parse(path);
            assertThat(field.toString())
                    .isEqualTo(path);
            assertThat(field.path().getFieldNameList())
                    .containsExactly("highway", "to", "hell");
        }

        @Test
        @DisplayName("rejecting empty path")
        void rejectingEmpty() {
            assertThrows(IllegalArgumentException.class, () -> Field.parse(""));
        }
    }

    @Test
    @DisplayName("create the instance by the path")
    void byPath() {
        FieldPath expected = Field.doParse("road_to.mandalay");
        assertThat(Field.withPath(expected).path())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("create the field by its name")
    class SingleField {

        @Test
        @DisplayName("accepting non-empty string")
        void byName() {
            String name = "my_way";
            assertThat(Field.named(name).toString())
                    .isEqualTo(name);
        }

        @Test
        @DisplayName("rejecting `.` in the field name")
        void noSeparatorInName() {
            assertThrows(IllegalArgumentException.class, () -> Field.named("with.dot"));
        }
    }

    @Nested
    @DisplayName("obtain the value in a message")
    class GettingValue {

        private final Field field = Field.parse("any.val.type_url");
        private final Field missingField = Field.parse("type_url");
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
        @DisplayName("as `Optional` value")
        void ifFound() {
            Truth8.assertThat(field.findValue(message))
                  .hasValue(expectedValue);
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            Truth8.assertThat(missingField.findValue(message))
                  .isEmpty();
        }

        @Test
        @DisplayName("directly via a nested path")
        void directValue() {
            assertThat(field.valueIn(message))
                    .isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("directly via a simple path")
        void obtainSimple() {
            StringHolder holder = StringHolder
                    .newBuilder()
                    .setVal("foobar")
                    .build();
            Field field = Field.parse("val");
            assertThat(field.valueIn(holder))
                    .isEqualTo(holder.getVal());
        }

        @Test
        @DisplayName("via recursive path")
        void recursivePath() {
            String value = "42";
            StringHolder holder0 = StringHolder
                    .newBuilder()
                    .setVal(value)
                    .build();
            StringHolderHolder holder1 = StringHolderHolder
                    .newBuilder()
                    .setHolder(holder0)
                    .build();
            GenericHolder holder2 = GenericHolder
                    .newBuilder()
                    .setHolderHolder(holder1)
                    .build();
            GenericHolder holder3 = GenericHolder
                    .newBuilder()
                    .setGeneric(holder2)
                    .build();

            Field field = Field.parse("generic.holder_holder.holder.val");
            assertThat(field.valueIn(holder3))
                    .isEqualTo(value);
        }

        @Test
        @DisplayName("if the value is enum")
        void enumValue() {
            GenericHolder holder = GenericHolder
                    .newBuilder()
                    .setCount(Count.TWO)
                    .build();

            Field field = Field.parse("count");
            assertThat(field.valueIn(holder))
                    .isEqualTo(Count.TWO);
        }

        @Test
        @DisplayName("throwing `ISE` if missed and getting directly")
        void directFailure() {
            assertThrows(IllegalStateException.class, () -> missingField.valueIn(message));
        }

        @Test
        @DisplayName("failing if the path reaches over a primitive value")
        void failOnMissingField() {
            String value = "primitive value";
            StringHolder holder = StringHolder
                    .newBuilder()
                    .setVal(value)
                    .build();
            Field wrongPath = Field.parse("val.this_field_is_absent");
            assertThrows(IllegalStateException.class, () -> wrongPath.valueIn(holder));
        }
    }

    @Nested
    @DisplayName("obtain the descriptor of the field")
    class GettingDescriptor {

        @Test
        @DisplayName("if present")
        void ifFound() {
            Truth8.assertThat(Field.named("val")
                                   .findDescriptor(AnyHolder.getDescriptor()))
                  .isPresent();
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            Truth8.assertThat(Field.named("value") // the real name is `val`.
                                   .findDescriptor(StringHolder.getDescriptor()))
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
                                   .findType(GenericHolder.class))
                  .hasValue(StringHolder.class);
        }

        @Test
        @DisplayName("for enums")
        void primitiveType() {
            Truth8.assertThat(Field.named("count").findType(GenericHolder.class))
                  .hasValue(Count.class);
        }

        @Test
        @DisplayName("for primitives")
        void enumType() {
            Truth8.assertThat(Field.named("size").findType(GenericHolder.class))
                  .hasValue(int.class);
        }

        @Test
        @DisplayName("returning empty `Optional` if not found")
        void notFound() {
            Truth8.assertThat(Field.parse("holder")
                                   .findType(GenericHolder.class))
                  .isEmpty();
        }

        @Test
        @DisplayName("when the type is recursive")
        void recursiveType() {
            Field field = Field.parse("generic.generic.generic.generic.generic");

            Truth8.assertThat(field.findType(GenericHolder.class))
                  .hasValue(GenericHolder.class);
        }
    }

    @Nested
    @DisplayName("obtain the name of the field by its number")
    class ByNumber {

        private final Descriptor message = Timestamp.getDescriptor();

        @Test
        @DisplayName("returning the short name of the field, if present")
        void nameValue() {
            String name = Field.nameOf(Timestamp.NANOS_FIELD_NUMBER, message);
            assertThat(name).isEqualTo("nanos");
        }

        @Nested
        @DisplayName("throwing")
        class Throwing {

            @Test
            @DisplayName("`IllegalArgumentException` for non-positive number")
            void zeroOrNegative() {
                assertThrows(IllegalArgumentException.class,
                             () -> Field.nameOf(-1, message));
                assertThrows(IllegalArgumentException.class,
                             () -> Field.nameOf(0, message));
            }

            @Test
            @DisplayName("`IllegalStateException` if there is no field with such number")
            void noField() {
                assertThrows(IllegalStateException.class,
                             () -> Field.nameOf(100, message));
            }
        }
    }

    @Test
    @DisplayName("obtain the instance by number in a message type")
    void byNumber() {
        Field seconds = Field.withNumberIn(1, Timestamp.getDescriptor());
        assertThat((Long) seconds.valueIn(Time.currentTime()))
                .isGreaterThan(0);
    }
}
