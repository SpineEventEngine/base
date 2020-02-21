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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.test.protobuf.MessageToPack;
import io.spine.testing.Tests;
import io.spine.testing.UtilityClassTest;
import io.spine.type.TypeUrl;
import io.spine.type.UnexpectedTypeException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.function.Function;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.base.Identifier.newUuid;
import static io.spine.protobuf.AnyPacker.pack;
import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.AnyPacker.unpackFunc;
import static io.spine.testing.TestValues.newUuidValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AnyPacker utility class should")
class AnyPackerTest extends UtilityClassTest<AnyPacker> {

    /** A message with type URL standard to Google Protobuf. */
    private final StringValue googleMsg = StringValue.of(newUuid());

    /** A message with different type URL. */
    private final MessageToPack spineMsg = MessageToPack.newBuilder()
                                                        .setValue(newUuidValue())
                                                        .build();

    AnyPackerTest() {
        super(AnyPacker.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(Message.class, Empty.getDefaultInstance())
              .setDefault(Any.class, Any.pack(Empty.getDefaultInstance()))
              .setDefault(Class.class, Empty.class);
    }

    @Test
    @DisplayName("pack Spine message to Any")
    void packSpineMessageToAny() {
        Any actual = pack(spineMsg);
        TypeUrl typeUrl = TypeUrl.of(spineMsg);

        assertEquals(Any.pack(spineMsg)
                        .getValue(), actual.getValue());
        assertEquals(typeUrl.value(), actual.getTypeUrl());
    }

    @Test
    @DisplayName("unpack Spine message from Any")
    void unpackSpineMessageFromAny() {
        Any any = pack(spineMsg);

        MessageToPack actual = (MessageToPack) unpack(any);

        assertEquals(spineMsg, actual);
    }

    @Test
    @DisplayName("pack Google message to Any")
    void packGoogleMessageToAny() {
        Any expected = Any.pack(googleMsg);

        Any actual = pack(googleMsg);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("unpack Google message from Any")
    void unpackGoogleMessageFromAny() {
        Any any = Any.pack(googleMsg);

        StringValue actual = (StringValue) unpack(any);

        assertEquals(googleMsg, actual);
    }

    @Test
    @DisplayName("return Any if it is passes to pack")
    void returnAnyIfItIsPassedToPack() {
        Any any = Any.pack(googleMsg);

        assertSame(any, pack(any));
    }

    @Test
    @DisplayName("fail on attempt to pack null")
    void failOnAttemptToPackNull() {
        assertThrows(NullPointerException.class,
                     () -> pack(Tests.<Message>nullRef()));
    }

    @Test
    @DisplayName("fail on attempt to unpack null")
    void failOnAttemptToUnpackNull() {
        assertThrows(NullPointerException.class,
                     () -> unpack(Tests.nullRef()));
    }

    @Test
    @DisplayName("create packing iterator")
    void createPackingIterator() {
        StringValue value = newUuidValue();
        Iterator<Message> iterator = Lists.<Message>newArrayList(value).iterator();
        Iterator<Any> packingIterator = pack(iterator);
        assertThat(ImmutableList.copyOf(packingIterator))
             .containsExactly(pack(value));
    }

    @Test
    @DisplayName("have null accepting function")
    void haveNullAcceptingFunc() {
        assertNull(unpackFunc().apply(null));
    }

    @Test
    @DisplayName("provide unpacking function")
    void unpackingFunc() {
        StringValue value = newUuidValue();
        assertThat(unpackFunc().apply(Any.pack(value)))
                .isEqualTo(value);
    }

    @Test
    @DisplayName("provide typed unpacking function")
    void typedUnpackingFunc() {
        StringValue value = newUuidValue();
        Function<Any, StringValue> function = unpackFunc(StringValue.class);
        StringValue unpacked = function.apply(Any.pack(value));
        assertThat(unpacked)
                  .isEqualTo(value);
    }

    @Nested
    @DisplayName("throw UnexpectedTypeException if")
    class Throw {

        @Test
        @DisplayName("type URL and class do not match")
        void type() {
            Any any = Any.pack(spineMsg);
            assertThrows(UnexpectedTypeException.class, () -> unpack(any, Empty.class));
        }

        @Test
        @DisplayName("type URL and the predefined class do not match")
        void typeInFunction() {
            Any any = Any.pack(spineMsg);
            Function<@Nullable Any, @Nullable Empty> fun = unpackFunc(Empty.class);
            assertThrows(UnexpectedTypeException.class, () -> fun.apply(any));
        }

        @Test
        @DisplayName("bytes don't match the type URL")
        void malformed() {
            Any malformed = Any
                    .newBuilder()
                    .setTypeUrl(TypeUrl.of(Empty.class).value())
                    .setValue(ByteString.copyFromUtf8("malformed bytes"))
                    .build();
            assertThrows(UnexpectedTypeException.class, () -> unpack(malformed));
        }

        @Test
        @DisplayName("bytes don't match the function's predefined class")
        void malformedInFunction() {
            Any malformed = Any
                    .newBuilder()
                    .setTypeUrl(TypeUrl.of(Empty.class).value())
                    .setValue(ByteString.copyFromUtf8("malformed bytes"))
                    .build();
            Function<@Nullable Any, @Nullable Empty> fun = unpackFunc(Empty.class);
            assertThrows(UnexpectedTypeException.class, () -> fun.apply(malformed));
        }
    }

    @Test
    @DisplayName("throw UnexpectedTypeException on a type URL and class mismatch")
    void unexpectedTypeInFunction() {
        Any any = Any.pack(spineMsg);
        assertThrows(UnexpectedTypeException.class, () -> unpack(any, Empty.class));
    }
}
