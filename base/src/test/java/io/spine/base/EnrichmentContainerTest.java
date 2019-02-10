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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.truth.Truth8.assertThat;

/**
 * This test fixes the contract defined by the {@link EnrichmentContainer} interface.
 *
 * <p>The implementations of this interface are expected in the {@code core-java} sub-project.
 */
@DisplayName("EnrichmentContainer should")
class EnrichmentContainerTest {

    private final EnrichmentContainer container = new EcStub();

    @Test
    @DisplayName("obtain enrichment by its class")
    void positive() {
        assertThat(container.find(EmStub.class))
                .isPresent();
    }

    @Test
    @DisplayName("return empty Optional if no enrichment with such class found")
    void negative() {
        assertThat(container.find(EnrichmentMessage.class))
                .isEmpty();
    }

    /**
     * Stub implementation of {@link EnrichmentMessage}.
     */
    @SuppressWarnings("ReturnOfNull")
    @Immutable
    private static class EmStub implements EnrichmentMessage {

        private static final long serialVersionUID = 0L;

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
        public void writeDelimitedTo(OutputStream output) throws IOException {

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
        public Descriptors.Descriptor getDescriptorForType() {
            return null;
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

    private static class EcStub implements EnrichmentContainer {

        private final EnrichmentMessage enrichmentMessage = new EmStub();
        @Override
        public <E extends EnrichmentMessage> Optional<E> find(Class<E> cls) {
            if (cls.equals(EmStub.class)) {
                E enr = (E) enrichmentMessage;
                return Optional.of(enr);
            }
            return Optional.empty();
        }
    }
}
