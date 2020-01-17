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

package io.spine.protobuf;

import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.test.protobuf.MessageToPack;
import io.spine.testing.Tests;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.AnyPacker.unpackFunc;
import static io.spine.testing.DisplayNames.HAVE_PARAMETERLESS_CTOR;
import static io.spine.testing.TestValues.newUuidValue;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AnyPacker utility class should")
class AnyPackerTest {

    /** A message with type URL standard to Google Protobuf. */
    private final StringValue googleMsg = StringValue.of(newUuid());

    /** A message with different type URL. */
    private final MessageToPack spineMsg = MessageToPack.newBuilder()
                                                        .setValue(newUuidValue())
                                                        .build();

    @Test
    @DisplayName(HAVE_PARAMETERLESS_CTOR)
    void have_private_constructor() {
        assertHasPrivateParameterlessCtor(AnyPacker.class);
    }

    @Test
    @DisplayName("pack Spine message to Any")
    void pack_spine_message_to_Any() {
        Any actual = pack(spineMsg);
        TypeUrl typeUrl = TypeUrl.of(spineMsg);

        assertEquals(Any.pack(spineMsg)
                        .getValue(), actual.getValue());
        assertEquals(typeUrl.value(), actual.getTypeUrl());
    }

    @Test
    @DisplayName("unpack Spine message from Any")
    void unpack_spine_message_from_Any() {
        Any any = pack(spineMsg);

        MessageToPack actual = (MessageToPack) unpack(any);

        assertEquals(spineMsg, actual);
    }

    @Test
    @DisplayName("pack Google message to Any")
    void pack_google_message_to_Any() {
        Any expected = Any.pack(googleMsg);

        Any actual = pack(googleMsg);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("unpack Google message from Any")
    void unpack_google_message_from_Any() {
        Any any = Any.pack(googleMsg);

        StringValue actual = (StringValue) unpack(any);

        assertEquals(googleMsg, actual);
    }

    @Test
    @DisplayName("return Any if it is passes to pack")
    void return_Any_if_it_is_passed_to_pack() {
        Any any = Any.pack(googleMsg);

        assertSame(any, pack(any));
    }

    @Test
    @DisplayName("fail on attempt to pack null")
    void fail_on_attempt_to_pack_null() {
        assertThrows(NullPointerException.class,
                     () -> pack(Tests.<Message>nullRef()));
    }

    @Test
    @DisplayName("fail on attempt to unpack null")
    void fail_on_attempt_to_unpack_null() {
        assertThrows(NullPointerException.class,
                     () -> unpack(Tests.nullRef()));
    }

    @Test
    @DisplayName("create packing iterator")
    void create_packing_iterator() {
        Iterator<Message> iterator = Lists.<Message>newArrayList(newUuidValue()).iterator();
        assertNotNull(pack(iterator));
    }

    @Test
    @DisplayName("have null accepting function")
    void have_null_accepting_func() {
        assertNull(unpackFunc().apply(null));
    }

    @Test
    @DisplayName("have unpacking function")
    void have_unpacking_func() {
        StringValue value = newUuidValue();

        assertEquals(value, unpackFunc().apply(Any.pack(value)));
    }
}
