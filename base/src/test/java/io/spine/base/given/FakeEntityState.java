/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.base.given;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import io.spine.base.EntityState;
import io.spine.base.ValidatingBuilder;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.NonValidated;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"ReturnOfNull", "Immutable"}) // OK for a fake.
public final class FakeEntityState extends AbstractMessage
        implements EntityState<Any, FakeEntityState.Builder, FakeEntityState> {

    private static final long serialVersionUID = 0;

    @Override
    public Parser<? extends Message> getParserForType() {
        return null;
    }

    @Override
    public Message.Builder newBuilderForType() {
        return null;
    }

    @Override
    public Message.Builder toBuilder() {
        return null;
    }

    @Override
    public Message getDefaultInstanceForType() {
        return null;
    }

    @Override
    public Descriptor getDescriptorForType() {
        return null;
    }

    @Override
    public Map<FieldDescriptor, Object> getAllFields() {
        return null;
    }

    @Override
    public boolean hasField(FieldDescriptor field) {
        return false;
    }

    @Override
    public Object getField(FieldDescriptor field) {
        return null;
    }

    @Override
    public int getRepeatedFieldCount(FieldDescriptor field) {
        return 0;
    }

    @Override
    public Object getRepeatedField(FieldDescriptor field, int index) {
        return null;
    }

    @Override
    public UnknownFieldSet getUnknownFields() {
        return null;
    }

    @Override
    public ImmutableList<ConstraintViolation> validate() {
        return ImmutableList.of();
    }

    public static class Builder implements ValidatingBuilder<FakeEntityState> {

        @Override
        public Message.Builder clear() {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(Message other) {
            return null;
        }

        @Override
        public @NonValidated FakeEntityState build() {
            return null;
        }

        @Override
        public @NonValidated FakeEntityState buildPartial() {
            return null;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public Message.Builder clone() {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(CodedInputStream input) throws IOException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(CodedInputStream input,
                                         ExtensionRegistryLite extensionRegistry) throws
                                                                                  IOException {
            return null;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return null;
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
        public Descriptor getDescriptorForType() {
            return null;
        }

        @Override
        public Map<FieldDescriptor, Object> getAllFields() {
            return null;
        }

        @Override
        public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
            return false;
        }

        @Override
        public FieldDescriptor getOneofFieldDescriptor(
                Descriptors.OneofDescriptor oneof) {
            return null;
        }

        @Override
        public boolean hasField(FieldDescriptor field) {
            return false;
        }

        @Override
        public Object getField(FieldDescriptor field) {
            return null;
        }

        @Override
        public int getRepeatedFieldCount(FieldDescriptor field) {
            return 0;
        }

        @Override
        public Object getRepeatedField(FieldDescriptor field, int index) {
            return null;
        }

        @Override
        public UnknownFieldSet getUnknownFields() {
            return null;
        }

        @Override
        public Message.Builder newBuilderForField(FieldDescriptor field) {
            return null;
        }

        @Override
        public Message.Builder getFieldBuilder(FieldDescriptor field) {
            return null;
        }

        @Override
        public Message.Builder getRepeatedFieldBuilder(FieldDescriptor field,
                                                       int index) {
            return null;
        }

        @Override
        public Message.Builder setField(FieldDescriptor field, Object value) {
            return null;
        }

        @Override
        public Message.Builder clearField(FieldDescriptor field) {
            return null;
        }

        @Override
        public Message.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
            return null;
        }

        @Override
        public Message.Builder setRepeatedField(FieldDescriptor field, int index,
                                                Object value) {
            return null;
        }

        @Override
        public Message.Builder addRepeatedField(FieldDescriptor field, Object value) {
            return null;
        }

        @Override
        public Message.Builder setUnknownFields(UnknownFieldSet unknownFields) {
            return null;
        }

        @Override
        public Message.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(ByteString data) throws InvalidProtocolBufferException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(ByteString data,
                                         ExtensionRegistryLite extensionRegistry) throws
                                                                                  InvalidProtocolBufferException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(byte[] data) throws InvalidProtocolBufferException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(byte[] data, int off, int len) throws
                                                                        InvalidProtocolBufferException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(byte[] data,
                                         ExtensionRegistryLite extensionRegistry) throws
                                                                                  InvalidProtocolBufferException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(byte[] data, int off, int len,
                                         ExtensionRegistryLite extensionRegistry) throws
                                                                                  InvalidProtocolBufferException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(InputStream input) throws IOException {
            return null;
        }

        @Override
        public Message.Builder mergeFrom(InputStream input,
                                         ExtensionRegistryLite extensionRegistry) throws
                                                                                  IOException {
            return null;
        }

        @Override
        public MessageLite.Builder mergeFrom(MessageLite other) {
            return null;
        }

        @Override
        public boolean mergeDelimitedFrom(InputStream input) {
            return false;
        }

        @Override
        public boolean mergeDelimitedFrom(InputStream input,
                                          ExtensionRegistryLite extensionRegistry) {
            return false;
        }
    }
}
