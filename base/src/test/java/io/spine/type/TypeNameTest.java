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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt64Value;
import io.spine.option.EntityOption;
import io.spine.option.IfMissingOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Provides only class-level tests.
 *
 * <p>Other methods of {@link TypeName} are just over {@link TypeUrl} which are tested by
 * its own set of tests.
 */
@DisplayName("TypeName should")
class TypeNameTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void pass_the_null_tolerance_check() {
        new NullPointerTester()
                .setDefault(Descriptor.class, EntityOption.getDefaultInstance()
                                                          .getDescriptorForType())
                .testAllPublicStaticMethods(TypeName.class);
    }

    @Test
    @DisplayName("reject empty name")
    void reject_empty_name() {
        assertThrows(IllegalArgumentException.class,
                     () -> TypeName.of(""));
    }

    @Test
    @DisplayName("return simple type name")
    void return_simple_type_name() {
        assertEquals(StringValue.class.getSimpleName(), TypeName.of(StringValue.class)
                                                                .simpleName());
    }

    @Test
    @DisplayName("return simple name if no package")
    void return_simple_name_if_no_package() {
        // A msg type without Protobuf package
        String name = IfMissingOption.class.getSimpleName();
        TypeUrl typeUrl = TypeName.of(name)
                                  .toUrl();

        String actual = TypeName.from(typeUrl)
                                .simpleName();

        assertEquals(name, actual);
    }

    @Test
    @DisplayName("obtain instance for message")
    void obtain_instance_for_message() {
        TypeName typeName = TypeName.of(StringValue.getDefaultInstance());
        assertNotNull(typeName);
        assertEquals(StringValue.class.getSimpleName(), typeName.simpleName());
    }

    @Test
    @DisplayName("obtain instance for Java class")
    void obtain_instance_for_Java_class() {
        TypeName typeName = TypeName.of(StringValue.class);
        assertNotNull(typeName);
        assertEquals(StringValue.class.getSimpleName(), typeName.simpleName());
    }

    @Test
    @DisplayName("obtain instance by descriptor")
    void obtain_instance_by_descriptor() {
        TypeName typeName = TypeName.from(UInt64Value.getDescriptor());
        assertNotNull(typeName);
        assertEquals(UInt64Value.class.getSimpleName(), typeName.simpleName());
    }

    @Test
    @DisplayName("provide proto descriptor")
    void provide_proto_descriptor_by_type_name() {
        TypeName typeName = TypeName.of("spine.test.types.KnownTask");
        Descriptor typeDescriptor = typeName.messageDescriptor();
        assertNotNull(typeDescriptor);
        assertEquals(typeName.value(), typeDescriptor.getFullName());
    }

    @Test
    @DisplayName("fail to find invalid type descriptor")
    void fail_to_find_invalid_type_descriptor() {
        TypeName invalidTypeName = TypeName.of("no.such.package.InvalidType");
        assertThrows(UnknownTypeException.class,
                     invalidTypeName::genericDescriptor);
    }
}
