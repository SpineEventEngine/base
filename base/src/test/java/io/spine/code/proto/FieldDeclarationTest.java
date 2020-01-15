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

package io.spine.code.proto;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.spine.net.Uri;
import io.spine.net.Uri.Protocol;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldDeclaration should")
class FieldDeclarationTest {

    @Test
    @DisplayName("not accept nulls on construction")
    void notAcceptNullsOnCtor() {
        Descriptor descriptor = Any.getDescriptor();
        NullPointerTester tester = new NullPointerTester()
                .setDefault(MessageType.class, new MessageType(descriptor))
                .setDefault(FieldDescriptor.class, descriptor.getFields()
                                                             .get(0));
        tester.testAllPublicConstructors(FieldDeclaration.class);
    }

    @Test
    @DisplayName("not accept nulls")
    void notAcceptNulls() {
        Descriptor descriptor = Any.getDescriptor();
        FieldDescriptor field = descriptor.getFields()
                                          .get(0);
        new NullPointerTester()
                .testAllPublicInstanceMethods(new FieldDeclaration(field));
    }

    @Test
    @DisplayName("have equals() and hashCode()")
    void equalsAndHashCode() {
        Descriptor any = Any.getDescriptor();
        FieldDescriptor typeUrl = any.getFields()
                                     .get(0);
        FieldDescriptor bytes = any.getFields()
                                   .get(1);
        new EqualsTester()
                .addEqualityGroup(new FieldDeclaration(typeUrl),
                                  new FieldDeclaration(typeUrl, new MessageType(any)))
                .addEqualityGroup(new FieldDeclaration(bytes),
                                  new FieldDeclaration(bytes, new MessageType(any)))
                .testEquals();
    }

    @Nested
    @DisplayName("check default values of")
    class Defaults {

        @Test
        @DisplayName("int32")
        void anInt32() {
            FieldDescriptor int32Field = Int32Value.getDescriptor()
                                                   .getFields()
                                                   .get(0);
            FieldDeclaration declaration = new FieldDeclaration(int32Field);
            assertTrue(declaration.isDefault(0));
            assertFalse(declaration.isDefault(""));
            assertFalse(declaration.isDefault(0.0));
        }

        @Test
        @DisplayName("string")
        void aString() {
            FieldDescriptor stringField = StringValue.getDescriptor()
                                                     .getFields()
                                                     .get(0);
            FieldDeclaration declaration = new FieldDeclaration(stringField);
            assertTrue(declaration.isDefault(""));
            assertFalse(declaration.isDefault(0L));
        }

        @Test
        @DisplayName("message")
        void aMessage() {
            FieldDescriptor messageField = Uri.getDescriptor()
                                              .findFieldByName("auth");
            FieldDeclaration declaration = new FieldDeclaration(messageField);
            assertTrue(declaration.isDefault(Uri.Authorization.getDefaultInstance()));
            assertFalse(declaration.isDefault(0L));
            assertFalse(declaration.isDefault(Empty.getDefaultInstance()));
        }
    }

    @Nested
    @DisplayName("obtain Java type name of")
    class TypeName {

        @Test
        @DisplayName("int64")
        void int64() {
            FieldDescriptor int64Field = Int64Value.getDescriptor()
                                                   .getFields()
                                                   .get(0);
            FieldDeclaration declaration = new FieldDeclaration(int64Field);
            String typeName = declaration.javaTypeName();
            assertThat(typeName).isEqualTo(long.class.getName());
        }

        @Test
        @DisplayName("bytes")
        void bytes() {
            FieldDescriptor int64Field = BytesValue.getDescriptor()
                                                   .getFields()
                                                   .get(0);
            FieldDeclaration declaration = new FieldDeclaration(int64Field);
            String typeName = declaration.javaTypeName();
            assertThat(typeName).isEqualTo(ByteString.class.getCanonicalName());
        }

        @Test
        @DisplayName("message")
        void message() {
            FieldDescriptor messageField = Uri.getDescriptor()
                                              .findFieldByName("protocol");
            FieldDeclaration declaration = new FieldDeclaration(messageField);
            String typeName = declaration.javaTypeName();
            assertThat(typeName).isEqualTo(Protocol.class.getCanonicalName());
        }

        @Test
        @DisplayName("enum")
        void anEnum() {
            FieldDescriptor enumField = Uri.Protocol.getDescriptor()
                                                    .findFieldByName("schema");
            FieldDeclaration declaration = new FieldDeclaration(enumField);
            String typeName = declaration.javaTypeName();
            assertThat(typeName).isEqualTo(Uri.Schema.class.getCanonicalName());
        }
    }

    @Test
    @DisplayName("obtain a name of the getter generated for the field by Protobuf Java")
    void obtainJavaGetterName() {
        FieldDescriptor field = Uri.Authorization.getDescriptor()
                                                 .findFieldByName("user_name");
        FieldDeclaration declaration = new FieldDeclaration(field);
        String javaGetterName = declaration.javaGetterName();
        assertThat(javaGetterName).isEqualTo("getUserName");
    }

    @Test
    @DisplayName("obtain the message type of the field")
    void obtainMessageType() {
        FieldDescriptor field = Uri.getDescriptor()
                                   .findFieldByName("auth");
        FieldDeclaration declaration = new FieldDeclaration(field);
        MessageType authorization = new MessageType(Uri.Authorization.getDescriptor());
        assertThat(declaration.messageType()).isEqualTo(authorization);
    }

    @Test
    @DisplayName("throw an `ISE` if the field is of non-message type")
    void throwOnWrongType() {
        FieldDescriptor field = Uri.Authorization.getDescriptor()
                                                 .findFieldByName("user_name");
        FieldDeclaration declaration = new FieldDeclaration(field);
        assertThrows(IllegalStateException.class, declaration::messageType);
    }
}
