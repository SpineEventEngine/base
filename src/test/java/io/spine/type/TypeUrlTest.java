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
import io.spine.test.type.TypeWithoutPrefix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertNpe;
import static io.spine.testing.TestValues.nullRef;
import static io.spine.type.Assertions.assertUnknownType;
import static io.spine.type.TypeUrl.composeTypeUrl;
import static io.spine.type.TypeUrl.parse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`TypeUrl` should")
class TypeUrlTest {

    /** The descriptor of the type used for the tests. */
    private static final Descriptors.Descriptor TYPE_DESCRIPTOR = StringValue.getDescriptor();

    /** Full proto type name for the type used under the tests. */
    private static final String TYPE_NAME = TYPE_DESCRIPTOR.getFullName();

    /** The value of the Type URL. */
    private static final String TYPE_URL_VALUE =
            composeTypeUrl(TypeUrl.Prefix.GOOGLE_APIS.value(), TYPE_NAME);

    private final TypeUrl stringValueTypeUrl = TypeUrl.from(TYPE_DESCRIPTOR);

    private static void assertValue(String expected, TypeUrl typeUrl) {
        assertThat(typeUrl.value())
                .isEqualTo(expected);
    }

    private static void assertValue(String expected, TypeName typeName) {
        assertThat(typeName.value())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("create an instance by")
    class CreateBy {

        @Test
        @DisplayName("`Message`")
        void message() {
            var msg = toMessage(newUuid());
            var typeUrl = TypeUrl.of(msg);

            assertTypeUrl(typeUrl);
        }

        @Test
        @DisplayName("fully-qualified type name")
        void typeName() {
            var typeUrl = TypeName.of(TYPE_NAME).toUrl();

            assertTypeUrl(typeUrl);
        }

        @Test
        @DisplayName("type URL")
        void typeUrl() {
            var typeUrl = TypeUrl.parse(TYPE_URL_VALUE);

            assertTypeUrl(typeUrl);
        }

        @Test
        @DisplayName("a descriptor of standard Protobuf type")
        void standardDescriptor() {
            var typeUrl = TypeUrl.from(TYPE_DESCRIPTOR);

            assertTypeUrl(typeUrl);
        }

        @Test
        @DisplayName("a descriptor of Spine type")
        void spineDescriptor() {
            var descriptor = EntityOption.getDescriptor();
            var expectedUrl = composeTypeUrl(TypeUrl.Prefix.SPINE.value(),
                                             descriptor.getFullName());

            var typeUrl = TypeUrl.from(descriptor);

            assertValue(expectedUrl, typeUrl);
        }

        @Test
        @DisplayName("enum descriptor of Protobuf type")
        void standardEnumDescriptor() {
            assertCreatedTypeUrl(TypeUrl.Prefix.GOOGLE_APIS.value(),
                                 Field.Kind.getDescriptor());
        }

        @Test
        @DisplayName("enum descriptor of Spine type")
        void spineEnumDescriptor() {
            assertCreatedTypeUrl(TypeUrl.Prefix.SPINE.value(),
                                 EntityOption.Kind.getDescriptor());
        }

        @Test
        @DisplayName("by a class")
        void byClass() {
            var typeUrl = TypeUrl.of(StringValue.class);

            assertTypeUrl(typeUrl);
        }

        private void assertCreatedTypeUrl(String expectedPrefix, EnumDescriptor descriptor) {
            var expected = composeTypeUrl(expectedPrefix, descriptor.getFullName());

            var typeUrl = TypeUrl.from(descriptor);
            assertValue(expected, typeUrl);
        }

        /**
         * Asserts properties that the passed type URL.
         *
         * <p>Assertions are based on the assumption that the passed instance was created by
         * one of the inputs specified as test environment constants declared in this test suite.
         *
         * @see #TYPE_URL_VALUE
         * @see #TYPE_NAME
         * @see #TYPE_URL_VALUE
         */
        private void assertTypeUrl(TypeUrl typeUrl) {
            assertValue(TYPE_URL_VALUE, typeUrl);
            assertThat(typeUrl.prefix())
                    .isEqualTo(TypeUrl.Prefix.GOOGLE_APIS.value());
            assertValue(TYPE_NAME, typeUrl.toTypeName());
            assertThat(TypeName.from(typeUrl)
                               .simpleName())
                    .isEqualTo(StringValue.class.getSimpleName());
        }
    }

    /**
     * This class tests cases when a type URL is defined without a prefix.
     *
     * <p>At the time of writing Protobuf does not specify how a type URL prefix is supposed to
     * be used. The only place it is used when {@linkplain Any#pack(Message, String)
     * packing a message} into {@code Any}.
     *
     * <p>Even though Spine provides convenience features (i.e. in the form of custom Protobuf
     * file option {@code (type_url_prefix)} for working with type URLs, the framework does not
     * provide features for exposing type information via a site.
     *
     * <p>Therefore, it is unreasonable to require using type URL prefixes until such a support
     * is provided by Protobuf, or by the Spine framework.
     */
    @Nested
    @DisplayName("allow empty prefix when")
    class EmptyPrefix {

        private TypeUrl noPrefixType;

        @Test
        @DisplayName("parsing type name")
        void inTypeName() {
            noPrefixType = parse("/package.Type");
            assertEmptyPrefix();
        }

        @Test
        @DisplayName("created for a type declared in a file with empty `(type_url_prefix)`")
        void inFile() {
            noPrefixType = TypeUrl.from(TypeWithoutPrefix.getDescriptor());
            assertEmptyPrefix();
        }

        private void assertEmptyPrefix() {
            assertThat(noPrefixType.prefix())
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("`null` URL")
        void nullUrl() {
            assertNpe(() -> parse(nullRef()));
        }

        @Test
        @DisplayName("empty URL")
        void emptyUrl() {
            assertIllegalArgument(() -> parse(""));
        }

        @Test
        @DisplayName("invalid URL of a packed message")
        void anyWithInvalidUrl() {
            var any = Any.newBuilder()
                    .setTypeUrl("invalid_type_url")
                    .build();
            var exception = assertThrows(RuntimeException.class, () -> TypeUrl.ofEnclosed(any));
            assertThat(exception.getCause())
                 .isInstanceOf(InvalidProtocolBufferException.class);
        }

        @Test
        @DisplayName("value without prefix separator")
        void noPrefixSeparator() {
            assertIllegalArgument(() -> TypeUrl.parse("prefix:Type"));
        }

        @Test
        @DisplayName("empty type name")
        void emptyTypeName() {
            assertIllegalArgument(() -> TypeUrl.parse("type.prefix/"));
        }

        @Test
        @DisplayName("malformed type URL")
        void malformedTypeUrl() {
            assertIllegalArgument(() -> TypeUrl.parse("prefix/prefix/type.Name"));
        }
    }

    @Test
    @DisplayName("obtain prefix")
    void prefix() {
        assertThat(stringValueTypeUrl.prefix())
                .isEqualTo(TypeUrl.Prefix.GOOGLE_APIS.value());
    }

    @Test
    @DisplayName("obtain type name")
    void typeName() {
        assertValue(TYPE_NAME, stringValueTypeUrl.toTypeName());
    }

    @Test
    @DisplayName("convert to `TypeName`")
    void toTypeName() {
        assertThat(stringValueTypeUrl.toTypeName())
                .isEqualTo(TypeName.of(TYPE_NAME));
    }

    @Test
    @DisplayName("throw `UnknownTypeException` when trying to obtain unknown Java class")
    void unknownJavaClass() {
        var url = TypeUrl.parse("unknown/JavaClass");
        assertUnknownType(url::toJavaClass);
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
    @DisplayName("have string representation")
    void stringForm() {
        assertThat(TypeUrl.of(BoolValue.class)
                          .toString())
                .contains(BoolValue.class.getSimpleName());
    }

    /**
     * This method tests only {@link TypeUrl.Prefix.SPINE} because the other prefix
     * {@link TypeUrl.Prefix.GOOGLE_APIS} is automatically tested by tests that invoke
     * {@link CreateBy#assertTypeUrl(TypeUrl)}.
     */
    @Test
    @DisplayName("have popular type prefixes")
    void prefixEnumeration() {
        var spinePrefix = TypeUrl.Prefix.SPINE;
        assertThat(spinePrefix.toString())
             .ignoringCase()
             .contains(spinePrefix.name());
    }

    @Test
    @DisplayName("serialize")
    void serialize() {
        reserializeAndAssert(TypeUrl.of(Timestamp.class));
    }
}
