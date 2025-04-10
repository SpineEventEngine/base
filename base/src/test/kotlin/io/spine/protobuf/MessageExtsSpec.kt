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
import com.google.protobuf.StringValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.spine.option.EntityOption
import io.spine.test.messages.MessageWithStringValue
import io.spine.testing.Assertions.assertIllegalArgument
import io.spine.testing.TestValues
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Extensions for `Message`-related types should")
internal class MessageExtsSpec {

    @Nested inner class
    `create a new builder` {

        @Test
        fun `for the passed Java message class`() = assertMessageBuilder {
            builderFor(MessageWithStringValue::class.java)
        }

        @Test
        fun `for this Java message class`() = assertMessageBuilder {
            MessageWithStringValue::class.java.newBuilder()
        }

        @Test
        fun `for this Kotlin message class`() = assertMessageBuilder {
            MessageWithStringValue::class.newBuilder()
        }
    }

    @Test
    fun `throw when try to get builder for a non-generated message`() {
        assertIllegalArgument {
            builderFor(Message::class.java)
        }
    }

    @Test
    fun `ensure 'Message' is unpacked`() {
        val value = TestValues.newUuidValue()

        AnyPacker.pack(value).ensureUnpacked() shouldBe value
        value.ensureUnpacked() shouldBe value
    }

    @Nested inner class
    `verify that` {

        @Test
        fun `a message is not in the default state`() {
            val msg = TypeConverter.toMessage("check_if_message_is_not_in_default_state")

            msg.isNotDefault() shouldBe true
            StringValue.getDefaultInstance().isNotDefault() shouldBe false
        }

        @Test
        fun `a message is in the default state`() {
            val nonDefault: Message = TestValues.newUuidValue()

            StringValue.getDefaultInstance().isDefault() shouldBe true
            nonDefault.isDefault() shouldBe false
        }

        @Test
        fun `an enum is not the default instance`() {
            EntityOption.Kind.ENTITY.isNotDefault() shouldBe true
        }

        @Test
        fun `an enum is the default instance`() {
            EntityOption.Kind.KIND_UNKNOWN.isDefault() shouldBe true
        }
    }

    @Test
    fun `tell if this 'Type' is a message class`() {
        StringValue::class.java.isMessageClass() shouldBe true
        TaskStatus::class.java.isMessageClass() shouldBe false
    }
}

@Suppress("unused")
private enum class TaskStatus {
    OPEN,
    DONE
}

private fun assertMessageBuilder(newBuilder: () -> Message.Builder) {
    val builder = newBuilder()
    builder.build()::class shouldBe MessageWithStringValue::class
    newBuilder() shouldNotBe newBuilder() // A new instance should always be created.
}
