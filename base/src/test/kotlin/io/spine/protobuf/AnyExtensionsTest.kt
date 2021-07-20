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

package io.spine.protobuf

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.ProtoTruth.assertThat
import com.google.protobuf.Message
import com.google.protobuf.StringValue
import io.spine.test.protobuf.MessageToPack
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `'Any' extensions should` {

    @Test
    fun `unpack Any into a concrete type`() {
        val msg = MessageToPack
            .newBuilder()
            .setValue(StringValue.of("bla bla"))
            .build()
        val any = AnyPacker.pack(msg)

        val unpacked = any.unpack<MessageToPack>()
        assertThat(unpacked)
            .isEqualTo(msg)
    }

    @Test
    fun `unpack Any without a concrete type`() {
        val msg = MessageToPack
            .newBuilder()
            .setValue(StringValue.of("foo bar"))
            .build()
        val any = AnyPacker.pack(msg)

        val unpacked = any.unpackGuessingType()
        val assertUnpacked = assertThat(unpacked)
        assertUnpacked
            .isInstanceOf(MessageToPack::class.java)
        assertUnpacked
            .isEqualTo(msg)
    }

    @Test
    fun `fail to unpack Any with an interface`() {
        val msg = MessageToPack
            .newBuilder()
            .setValue(StringValue.of("la la la"))
            .build()
        val any = AnyPacker.pack(msg)

        assertThrows<IllegalArgumentException> { any.unpack<Message>() }
    }

    @Test
    fun `pack message into Any`() {
        val msg = MessageToPack
            .newBuilder()
            .setValue(StringValue.of("la la la"))
            .build()
        val any = msg.pack()
        assertThat(any.typeUrl)
            .isEqualTo("types.spine.io/spine.test.protobuf.MessageToPack")
        assertThat(any.unpack<MessageToPack>())
            .isEqualTo(msg)
    }
}
