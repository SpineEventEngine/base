/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.util.Timestamps;
import io.spine.testing.Tests;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static io.spine.testing.TestValues.newUuidValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Tymchenko
 * @author Alexander Yevsyukov
 */
public class ThrowableMessageShould {

    private GeneratedMessageV3 message;
    private ThrowableMessage throwableMessage;
    private Any producer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        message = newUuidValue();
        throwableMessage = new TestThrowableMessage(message);
        producer = Identifier.pack(getClass().getName());
    }

    @Test
    public void return_message_thrown() {
        assertEquals(message, throwableMessage.getMessageThrown());
    }

    @Test
    public void have_timestamp() {
        assertTrue(Timestamps.isValid(throwableMessage.getTimestamp()));
    }

    @Test
    public void init_producer() {
        assertFalse(throwableMessage.producerId()
                                    .isPresent());

        ThrowableMessage retVal = throwableMessage.initProducer(producer);

        assertSame(throwableMessage, retVal);
        Optional<Any> optional = throwableMessage.producerId();
        assertTrue(optional.isPresent());
        assertEquals(producer, optional.get());
    }

    @Test
    public void not_allow_repeated_producer_initialization() {
        thrown.expect(IllegalStateException.class);
        throwableMessage.initProducer(producer)
                        .initProducer(producer);
    }

    @Test
    public void not_allow_null_producer() {
        thrown.expect(NullPointerException.class);
        throwableMessage.initProducer(Tests.nullRef());
    }

    @SuppressWarnings("ThrowableNotThrown")
    @Test
    public void prohibit_null_message() {
        thrown.expect(NullPointerException.class);
        new TestThrowableMessage(Tests.<GeneratedMessageV3>nullRef());
    }

    /**
     * Sample {@code ThrowableMessage} class used for test purposes only.
     */
    private static class TestThrowableMessage extends ThrowableMessage {

        private static final long serialVersionUID = 0L;

        private TestThrowableMessage(GeneratedMessageV3 rejection) {
            super(rejection);
        }
    }
}
