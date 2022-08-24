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

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import io.spine.annotation.Internal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`UnpublishedLanguageException` should")
class UnpublishedLanguageExceptionTest {

    @Test
    @DisplayName("contain the name of the type in its message")
    void containTypeName() {
        var msg = new SecretMessage();

        assertThat(SecretMessage.class.isAnnotationPresent(Internal.class))
                .isTrue();

        var typeName = TypeName.of(msg);
        var exception = new UnpublishedLanguageException(msg);

        assertThat(exception.getMessage()).contains(typeName.toString());
    }

    /**
     * A stub implementation of the {@code Message} interface, which is
     * annotated as internal, and tries to pretend being {@code Any}
     * so that its type name can be obtained.
     */
    @Internal
    @SuppressWarnings("ReturnOfNull")
    private static class SecretMessage implements Message {

        private SecretMessage() {
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
        }

        @Override
        public int getSerializedSize() {
            return 0;
        }

        @Override
        public Parser<? extends Message> getParserForType() {
            return null;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }

        @SuppressWarnings("ZeroLengthArrayAllocation")
        @Override
        public byte[] toByteArray() {
            return new byte[0];
        }

        @Override
        public void writeTo(OutputStream output) throws IOException {
        }

        @Override
        public void writeDelimitedTo(OutputStream output) {
        }

        @Override
        public Builder newBuilderForType() {
            return null;
        }

        @Override
        public Builder toBuilder() {
            return null;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return Any.getDefaultInstance();
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        public List<String> findInitializationErrors() {
            return null;
        }

        @Override
        public String getInitializationErrorString() {
            return null;
        }

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return Any.getDescriptor();
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            return null;
        }

        @Override
        public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
            return false;
        }

        @Override
        public Descriptors.FieldDescriptor getOneofFieldDescriptor(
                Descriptors.OneofDescriptor oneof) {
            return null;
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            return false;
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            return null;
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            return 0;
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            return null;
        }

        @Override
        public UnknownFieldSet getUnknownFields() {
            return null;
        }
    }
}
