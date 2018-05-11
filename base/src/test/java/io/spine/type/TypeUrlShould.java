/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import com.google.common.testing.SerializableTester;
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
import io.spine.test.Tests;
import org.junit.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.type.TypeUrl.composeTypeUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TypeUrlShould {

    private static final String STRING_VALUE_TYPE_NAME = StringValue.getDescriptor()
                                                                    .getFullName();

    private static final String STRING_VALUE_TYPE_URL_STR = composeTypeUrl(
            TypeUrl.Prefix.GOOGLE_APIS.value(),
            STRING_VALUE_TYPE_NAME);

    private final TypeUrl stringValueTypeUrl = TypeUrl.from(StringValue.getDescriptor());

    @Test(expected = NullPointerException.class)
    public void not_accept_null_value() {
        TypeUrl.parse(Tests.nullRef());
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_accept_empty_string() {
        TypeUrl.parse("");
    }

    @Test
    public void create_from_message() {
        Message msg = toMessage(newUuid());
        TypeUrl typeUrl = TypeUrl.of(msg);

        assertIsStringValueUrl(typeUrl);
    }

    @Test
    public void create_from_type_name() {
        TypeUrl typeUrl = TypeName.of(STRING_VALUE_TYPE_NAME)
                                        .toUrl();

        assertIsStringValueUrl(typeUrl);
    }

    @Test
    public void create_from_type_url_string() {
        TypeUrl typeUrl = TypeUrl.parse(STRING_VALUE_TYPE_URL_STR);

        assertIsStringValueUrl(typeUrl);
    }

    @Test
    public void do_not_accept_Any_with_malformed_type_url() {
        Any any = Any.newBuilder().setTypeUrl("invalid_type_url").build();
        try {
            TypeUrl.ofEnclosed(any);
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof InvalidProtocolBufferException);
        }
    }

    @Test
    public void return_type_url_prefix() {
        assertEquals(TypeUrl.Prefix.GOOGLE_APIS.value(), stringValueTypeUrl.getPrefix());
    }

    @Test
    public void return_type_name() {
        assertEquals(STRING_VALUE_TYPE_NAME, stringValueTypeUrl.getTypeName());
    }

    @Test
    public void convert_to_TypeName() {
        assertEquals(TypeName.of(STRING_VALUE_TYPE_NAME), stringValueTypeUrl.toName());
    }

    @Test
    public void create_by_descriptor_of_google_msg() {
        TypeUrl typeUrl = TypeUrl.from(StringValue.getDescriptor());

        assertIsStringValueUrl(typeUrl);
    }

    @Test
    public void create_by_descriptor_of_spine_msg() {
        Descriptors.Descriptor descriptor = EntityOption.getDescriptor();
        String expectedUrl = composeTypeUrl(TypeUrl.Prefix.SPINE.value(),
                                                  descriptor.getFullName());

        TypeUrl typeUrl = TypeUrl.from(descriptor);

        assertEquals(expectedUrl, typeUrl.value());
    }

    @Test
    public void create_by_enum_descriptor_of_google_msg() {
        assertCreatesTypeUrlFromEnum(TypeUrl.Prefix.GOOGLE_APIS.value(),
                                     Field.Kind.getDescriptor());
    }

    @Test
    public void create_by_enum_descriptor_of_spine_msg() {
        assertCreatesTypeUrlFromEnum(TypeUrl.Prefix.SPINE.value(),
                                     EntityOption.Kind.getDescriptor());
    }

    private static void assertCreatesTypeUrlFromEnum(String typeUrlPrefix,
                                                     EnumDescriptor enumDescriptor) {
        String expected = composeTypeUrl(typeUrlPrefix, enumDescriptor.getFullName());

        TypeUrl typeUrl = TypeUrl.from(enumDescriptor);

        assertEquals(expected, typeUrl.value());
    }

    @Test
    public void create_instance_by_class() {
        TypeUrl typeUrl = TypeUrl.of(StringValue.class);

        assertIsStringValueUrl(typeUrl);
    }

    private static void assertIsStringValueUrl(TypeUrl typeUrl) {
        assertEquals(STRING_VALUE_TYPE_URL_STR, typeUrl.value());
        assertEquals(TypeUrl.Prefix.GOOGLE_APIS.value(), typeUrl.getPrefix());
        assertEquals(STRING_VALUE_TYPE_NAME, typeUrl.getTypeName());
        assertEquals(StringValue.class.getSimpleName(), TypeName.from(typeUrl)
                                                                .getSimpleName());
    }

    @Test
    public void use_fields_in_equality() {
        new EqualsTester()
                .addEqualityGroup(TypeUrl.of(StringValue.class), TypeUrl.of(StringValue.class))
                .addEqualityGroup(TypeUrl.of(Timestamp.class))
                .addEqualityGroup(TypeUrl.of(UInt32Value.class))
                .testEquals();
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_value_without_prefix_separator() {
        TypeUrl.parse("prefix:Type");
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_empty_prefix() {
        TypeUrl.parse("/package.Type");
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_empty_name() {
        TypeUrl.parse("type.prefix/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void do_not_accept_malformed_type_URL() {
        TypeUrl.parse("prefix/prefix/type.Name");
    }

    @Test(expected = UnknownTypeException.class)
    public void throw_exception_for_unknown_Java_class() {
        TypeUrl url = TypeUrl.parse("unknown/JavaClass");
        url.getJavaClass();
    }

    @Test
    public void return_value() {
        assertTrue(TypeUrl.of(BoolValue.class)
                          .toString()
                          .contains(BoolValue.class.getSimpleName()));
    }

    @Test
    public void have_prefix_enumeration() {
        TypeUrl.Prefix spinePrefix = TypeUrl.Prefix.SPINE;
        assertTrue(spinePrefix.toString()
                              .contains(spinePrefix.name()
                                                   .toLowerCase()));
    }

    @Test
    public void serialize() {
        SerializableTester.reserializeAndAssert(TypeUrl.of(Timestamp.class));
    }
}
