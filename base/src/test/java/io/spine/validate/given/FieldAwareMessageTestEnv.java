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

package io.spine.validate.given;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import io.spine.test.validate.AggregateState;
import io.spine.test.validate.AggregateStateOrBuilder;
import io.spine.validate.FieldAwareMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static io.spine.util.Exceptions.newIllegalStateException;

public final class FieldAwareMessageTestEnv {

    /**
     * Prevents instantiation.
     */
    private FieldAwareMessageTestEnv() {
    }

    public static AggregateState msg() {
        return AggregateState.newBuilder()
                             .setEntityId("entity ID")
                             .setAnotherId("another ID")
                             .build();
    }

    /**
     * A wrapper of {@link AggregateState} message, which acts close to a real message by delegating
     * the calls to the wrapped instance, and allows to extend itself.
     *
     * <p>It's impossible to generate the code in {@code base} module, so this class is extended by
     * {@link io.spine.validate.FieldAwareMessage FieldAwareMessage} mixin interface in test
     * scenarios.
     */
    public static class AggregateStateDelegate implements AggregateStateOrBuilder, Message {

        private final AggregateState delegate;

        AggregateStateDelegate(AggregateState delegate) {
            this.delegate = delegate;
        }

        public static Descriptors.Descriptor getDescriptor() {
            return AggregateState.getDescriptor();
        }

        @Override
        public String getEntityId() {
            return delegate.getEntityId();
        }

        @Override
        public ByteString getEntityIdBytes() {
            return delegate.getEntityIdBytes();
        }

        @Override
        public String getAnotherId() {
            return delegate.getAnotherId();
        }

        @Override
        public ByteString getAnotherIdBytes() {
            return delegate.getAnotherIdBytes();
        }

        public static AggregateState parseFrom(ByteBuffer data) throws
                                                                InvalidProtocolBufferException {
            return AggregateState.parseFrom(data);
        }

        public static AggregateState parseFrom(ByteBuffer data,
                                               ExtensionRegistryLite reg)
                throws
                InvalidProtocolBufferException {
            return AggregateState.parseFrom(data, reg);
        }

        public static AggregateState parseFrom(ByteString data) throws
                                                                InvalidProtocolBufferException {
            return AggregateState.parseFrom(data);
        }

        public static AggregateState parseFrom(ByteString data,
                                               ExtensionRegistryLite reg)
                throws
                InvalidProtocolBufferException {
            return AggregateState.parseFrom(data, reg);
        }

        public static AggregateState parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return AggregateState.parseFrom(data);
        }

        public static AggregateState parseFrom(byte[] data,
                                               ExtensionRegistryLite reg)
                throws InvalidProtocolBufferException {
            return AggregateState.parseFrom(data, reg);
        }

        public static AggregateState parseFrom(InputStream input) throws IOException {
            return AggregateState.parseFrom(input);
        }

        public static AggregateState parseFrom(InputStream input,
                                               ExtensionRegistryLite reg) throws IOException {
            return AggregateState.parseFrom(input, reg);
        }

        public static AggregateState parseDelimitedFrom(InputStream input) throws IOException {
            return AggregateState.parseDelimitedFrom(input);
        }

        public static AggregateState parseDelimitedFrom(InputStream input,
                                                        ExtensionRegistryLite reg)
                throws IOException {
            return AggregateState.parseDelimitedFrom(input, reg);
        }

        public static AggregateState parseFrom(CodedInputStream input) throws IOException {
            return AggregateState.parseFrom(input);
        }

        public static AggregateState parseFrom(CodedInputStream input,
                                               ExtensionRegistryLite reg) throws IOException {
            return AggregateState.parseFrom(input, reg);
        }

        public static AggregateState.Builder newBuilder() {
            return AggregateState.newBuilder();
        }

        public static AggregateState.Builder newBuilder(AggregateState prototype) {
            return AggregateState.newBuilder(prototype);
        }

        public static AggregateState getDefaultInstance() {
            return AggregateState.getDefaultInstance();
        }

        public static Parser<AggregateState> parser() {
            return AggregateState.parser();
        }

        @Override
        public AggregateState getDefaultInstanceForType() {
            return delegate.getDefaultInstanceForType();
        }

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return delegate.getDescriptorForType();
        }

        @Override
        public boolean isInitialized() {
            return delegate.isInitialized();
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            return delegate.getAllFields();
        }

        @Override
        public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
            return delegate.hasOneof(oneof);
        }

        @Override
        public Descriptors.FieldDescriptor getOneofFieldDescriptor(
                Descriptors.OneofDescriptor oneof) {
            return delegate.getOneofFieldDescriptor(oneof);
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            return delegate.hasField(field);
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            return delegate.getField(field);
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            return delegate.getRepeatedFieldCount(field);
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            return delegate.getRepeatedField(field, index);
        }

        @Override
        public UnknownFieldSet getUnknownFields() {
            return delegate.getUnknownFields();
        }

        @Override
        public List<String> findInitializationErrors() {
            return delegate.findInitializationErrors();
        }

        @Override
        public String getInitializationErrorString() {
            return delegate.getInitializationErrorString();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            // no-op.
        }

        @Override
        public int getSerializedSize() {
            return 0;
        }

        @SuppressWarnings("ReturnOfNull")
        @Override
        public Parser<? extends Message> getParserForType() {
            return null;
        }

        @SuppressWarnings("ReturnOfNull")
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
            // no-op.
        }

        @Override
        public void writeDelimitedTo(OutputStream output) {
            // no-op.
        }

        @SuppressWarnings("ReturnOfNull")
        @Override
        public Builder newBuilderForType() {
            return null;
        }

        @SuppressWarnings("ReturnOfNull")
        @Override
        public Builder toBuilder() {
            return null;
        }
    }

    /**
     * A wrapper that properly implements {@link #readValue(Descriptors.FieldDescriptor)}.
     */
    public static class FieldAwareMsg extends AggregateStateDelegate implements FieldAwareMessage {

        public FieldAwareMsg(AggregateState delegate) {
            super(delegate);
        }

        @Override
        public Object readValue(Descriptors.FieldDescriptor field) {
            int index = field.getIndex();
            switch (index) {
                case 0:
                    return getEntityId();
                case 1:
                    return getAnotherId();
                default:
                    throw newIllegalStateException(
                            "Wrong field index `%s` passed when reading values " +
                                    "of `AggregateState`.", index);
            }
        }
    }

    /**
     * A wrapper that has errors implementing {@link #readValue(Descriptors.FieldDescriptor)}.
     */
    public static class BrokenFieldAware
            extends AggregateStateDelegate implements FieldAwareMessage {

        public BrokenFieldAware(AggregateState delegate) {
            super(delegate);
        }

        @Override
        public Object readValue(Descriptors.FieldDescriptor field) {
            // Error here. The `field.getIndex()` value isn't taken into account at all.
            return getEntityId();
        }
    }
}
