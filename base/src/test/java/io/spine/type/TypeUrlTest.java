/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.type;

import com.google.common.testing.EqualsTester;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Field;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import io.spine.option.EntityOption;
import io.spine.testing.Tests;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.type.TypeUrl.composeTypeUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TypeUrl should")
class TypeUrlTest {

    private static final String STRING_VALUE_TYPE_NAME = StringValue.getDescriptor()
                                                                    .getFullName();
    private static final String STRING_VALUE_TYPE_URL_STR =
            composeTypeUrl(TypeUrl.Prefix.GOOGLE_APIS.value(), STRING_VALUE_TYPE_NAME);

    private final TypeUrl stringValueTypeUrl = TypeUrl.from(StringValue.getDescriptor());

    @Nested
    @DisplayName("Create instance by")
    class CreateBy {

        @Test
        @DisplayName("Message")
        void message() {
            Message msg = toMessage(newUuid());
            TypeUrl typeUrl = TypeUrl.of(msg);

            assertIsStringValueUrl(typeUrl);
        }

        @Test
        @DisplayName("fully-qualified type name")
        void typeName() {
            TypeUrl typeUrl = TypeName.of(STRING_VALUE_TYPE_NAME)
                                      .toUrl();

            assertIsStringValueUrl(typeUrl);
        }

        @Test
        @DisplayName("type URL")
        void typeUrl() {
            TypeUrl typeUrl = TypeUrl.parse(STRING_VALUE_TYPE_URL_STR);

            assertIsStringValueUrl(typeUrl);
        }

        @Test
        @DisplayName("a descriptor of standard Protobuf type")
        void standardDescriptor() {
            TypeUrl typeUrl = TypeUrl.from(StringValue.getDescriptor());

            assertIsStringValueUrl(typeUrl);
        }

        @Test
        @DisplayName("a descriptor of Spine type")
        void spineDescriptor() {
            Descriptors.Descriptor descriptor = EntityOption.getDescriptor();
            String expectedUrl = composeTypeUrl(TypeUrl.Prefix.SPINE.value(),
                                                descriptor.getFullName());

            TypeUrl typeUrl = TypeUrl.from(descriptor);

            assertEquals(expectedUrl, typeUrl.value());
        }

        @Test
        @DisplayName("enum descriptor of Protobuf type")
        void standardEnumDescriptor() {
            assertCreatesTypeUrlFromEnum(TypeUrl.Prefix.GOOGLE_APIS.value(),
                                         Field.Kind.getDescriptor());
        }

        @Test
        @DisplayName("enum descriptor of Spine type")
        void spineEnumDescriptor() {
            assertCreatesTypeUrlFromEnum(TypeUrl.Prefix.SPINE.value(),
                                         EntityOption.Kind.getDescriptor());
        }

        @Test
        public void create_instance_by_class() {
            TypeUrl typeUrl = TypeUrl.of(StringValue.class);

            assertIsStringValueUrl(typeUrl);
        }
    }

    @Nested
    @DisplayName("Reject")
    class Reject {

        @Test
        @DisplayName("null URL")
        void nullUrl() {
            assertThrows(
                    NullPointerException.class,
                    () -> TypeUrl.parse(Tests.nullRef())
            );
        }

        @Test
        @DisplayName("empty URL")
        void emptyUrl() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> TypeUrl.parse("")
            );
        }

        @Test
        @DisplayName("invalid URL of a packed message")
        void anyWithInvalidUrl() {
            Any any = Any.newBuilder()
                         .setTypeUrl("invalid_type_url")
                         .build();
            RuntimeException exception = assertThrows(RuntimeException.class,
                                                      () -> TypeUrl.ofEnclosed(any));
            assertThat(exception.getCause(), instanceOf(InvalidProtocolBufferException.class));
        }

        @Test
        @DisplayName("value without prefix separator")
        void noPrefixSeparator() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> TypeUrl.parse("prefix:Type")
            );
        }

        @Test
        @Disabled("Until we decide if there may be URLs without type prefix")
        @DisplayName("empty prefix")
        void emptyPrefix() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> TypeUrl.parse("/package.Type")
            );
        }

        @Test
        @DisplayName("empty type name")
        void emptyTypeName() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> TypeUrl.parse("type.prefix/")
            );
        }

        @Test
        @DisplayName("malformed type URL")
        void malformedTypeUrl() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> TypeUrl.parse("prefix/prefix/type.Name")
            );
        }
    }

    @Test
    @DisplayName("obtain prefix")
    void getPrefix() {
        assertEquals(TypeUrl.Prefix.GOOGLE_APIS.value(), stringValueTypeUrl.prefix());
    }

    @Test
    @DisplayName("obtain type name")
    void getTypeName() {
        assertEquals(STRING_VALUE_TYPE_NAME, stringValueTypeUrl.toTypeName()
                                                               .value());
    }

    @Test
    @DisplayName("convert to TypeName")
    void toTypeName() {
        assertEquals(TypeName.of(STRING_VALUE_TYPE_NAME), stringValueTypeUrl.toTypeName());
    }

    private static void assertCreatesTypeUrlFromEnum(String typeUrlPrefix,
                                                     EnumDescriptor enumDescriptor) {
        String expected = composeTypeUrl(typeUrlPrefix, enumDescriptor.getFullName());

        TypeUrl typeUrl = TypeUrl.from(enumDescriptor);

        assertEquals(expected, typeUrl.value());
    }

    private static void assertIsStringValueUrl(TypeUrl typeUrl) {
        assertEquals(STRING_VALUE_TYPE_URL_STR, typeUrl.value());
        assertEquals(TypeUrl.Prefix.GOOGLE_APIS.value(), typeUrl.prefix());
        assertEquals(STRING_VALUE_TYPE_NAME, typeUrl.toTypeName()
                                                    .value());
        assertEquals(StringValue.class.getSimpleName(), TypeName.from(typeUrl)
                                                                .simpleName());
    }

    @Test
    @DisplayName("throw UnknownTypeException when trying to obtain unknown Java class")
    void unknownJavaClass() {
        TypeUrl url = TypeUrl.parse("unknown/JavaClass");
        assertThrows(
                UnknownTypeException.class,
                url::toJavaClass
        );
    }

    @Test
    @DisplayName("provide equality")
    void equals() {
        new EqualsTester()
                .addEqualityGroup(TypeUrl.of(StringValue.class), TypeUrl.of(StringValue.class))
                .addEqualityGroup(TypeUrl.of(Timestamp.class))
                .addEqualityGroup(TypeUrl.of(UInt32Value.class))
                .testEquals();
    }

    @Test
    @DisplayName("provide string representation")
    void stringForm() {
        assertTrue(TypeUrl.of(BoolValue.class)
                          .toString()
                          .contains(BoolValue.class.getSimpleName()));
    }

    @Test
    @DisplayName("provide popular type prefixes")
    void prefixEnumeration() {
        TypeUrl.Prefix spinePrefix = TypeUrl.Prefix.SPINE;
        assertTrue(spinePrefix.toString()
                              .contains(spinePrefix.name()
                                                   .toLowerCase()));
    }

    @Test
    @DisplayName("serialize")
    void serialize() {
        reserializeAndAssert(TypeUrl.of(Timestamp.class));
    }
}
