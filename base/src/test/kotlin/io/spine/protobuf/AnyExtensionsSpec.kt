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

package io.spine.protobuf

import com.google.protobuf.Message
import com.google.protobuf.stringValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.spine.test.protobuf.MessageToPack
import io.spine.test.protobuf.messageToPack
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


@DisplayName("`Any` Kotlin extensions should")
class AnyExtensionsSpec {

    @Test
    fun `unpack Any into a concrete type`() {
        val msg = messageToPack {
            value = stringValue { value = "bla bla" }
        }
        val any = AnyPacker.pack(msg)

        val unpacked = any.unpack<MessageToPack>()

        unpacked shouldBe msg
    }

    @Test
    fun `unpack Any without a concrete type`() {
        val msg = messageToPack {
            value = stringValue { value = "foo bar" }
        }
        val any = AnyPacker.pack(msg)

        val unpacked = any.unpackKnownType()

        unpacked shouldBe instanceOf<MessageToPack>()
        unpacked shouldBe msg
    }

    @Test
    fun `fail to unpack Any with an interface`() {
        val msg = messageToPack {
            value = stringValue { value = "la la la" }
        }
        val any = AnyPacker.pack(msg)

        assertThrows<IllegalArgumentException> {
            any.unpack<Message>()
        }
    }

    @Test
    fun `pack message into Any`() {
        val msg = messageToPack {
            value = stringValue { value = "pack in fun" }
        }
        val any = msg.pack()

        any.typeUrl shouldBe "type.spine.io/spine.test.protobuf.MessageToPack"
        any.unpack<MessageToPack>() shouldBe msg
    }
}
