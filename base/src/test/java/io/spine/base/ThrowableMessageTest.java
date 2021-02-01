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
package io.spine.base;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.Assertions.assertNpe;
import static io.spine.testing.TestValues.nullRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`ThrowableMessage` should")
class ThrowableMessageTest {

    private RejectionMessage<?, ?> message;
    private ThrowableMessage throwableMessage;
    private Any producer;

    @BeforeEach
    void setUp() {
        message = new FakeRejectionMessage();
        throwableMessage = new TestThrowableMessage(message);
        producer = Identifier.pack(getClass().getName());
    }

    @Test
    @DisplayName("return the thrown message")
    void messageThrown() {
        assertEquals(message, throwableMessage.messageThrown());
    }

    @Test
    @DisplayName("have timestamp")
    void haveTimestamp() {
        assertTrue(Timestamps.isValid(throwableMessage.timestamp()));
    }

    @Test
    @DisplayName("initialize the producer")
    void producer() {
        assertFalse(throwableMessage.producerId()
                                    .isPresent());

        ThrowableMessage retVal = throwableMessage.initProducer(producer);

        assertSame(throwableMessage, retVal);
        Optional<Any> optional = throwableMessage.producerId();
        assertTrue(optional.isPresent());
        assertEquals(producer, optional.get());
    }

    @Test
    @DisplayName("not allow repeated producer initialization")
    void initOnce() {
        throwableMessage.initProducer(producer);
        assertIllegalState(() -> throwableMessage.initProducer(producer));
    }

    @Test
    @DisplayName("not allow null producer")
    void prohibitNullProducer() {
        assertNpe(() -> throwableMessage.initProducer(nullRef()));
    }

    @Test
    @DisplayName("prohibit null message")
    void prohibitNullMessage() {
        assertNpe(() -> new TestThrowableMessage(nullRef()));
    }

    /**
     * Sample {@code ThrowableMessage} class used for test purposes only.
     */
    private static class TestThrowableMessage extends ThrowableMessage {

        private static final long serialVersionUID = 0L;

        private TestThrowableMessage(@SuppressWarnings("rawtypes") RejectionMessage rejection) {
            super(rejection);
        }
    }

    /**
     * Test implementation of {@link RejectionMessage}.
     *
     * <p>The message should never evaluated or queried for any fields. The implementation is
     * completely non-operational.
     *
     * <p>In a real world scenario, the message would be generated by the Protobuf Compiler with
     * the Spine Protobuf Compiler plugin. However, in this module we're unable to use the plugin,
     * so this fake implementation is declared.
     */
    @SuppressWarnings({"ReturnOfNull", "Immutable", "rawtypes"}) // OK for a fake.
    private static class FakeRejectionMessage extends AbstractMessage implements RejectionMessage {

        private static final long serialVersionUID = 0L;

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
        public Descriptors.Descriptor getDescriptorForType() {
            return null;
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
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
