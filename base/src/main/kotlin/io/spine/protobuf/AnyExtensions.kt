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

import com.google.protobuf.Message
import com.google.protobuf.Any as AnyProto

/**
 * Unpacks this `Any` into the given message type.
 *
 * @param T the concrete type of the message stored in the `Any`
 * @see unpackGuessingType
 */
public inline fun <reified T : Message> AnyProto.unpack(): T {
    val cls = T::class
    if (!cls.isFinal) {
        throw IllegalArgumentException(
            "Message type must be a concrete message, not a general interface. " +
                    "Use `unpackGuessingType()` if concrete message type is not available.")
    }
    return AnyPacker.unpack(this, cls.java)
}

/**
 * Unpacks this `Any`.
 *
 * The concrete type of the message is looked up among the known types by the `Any.type_url`.
 */
public fun AnyProto.unpackGuessingType(): Message =
    AnyPacker.unpack(this)

/**
 * Packs this message into an `Any`.
 */
public fun Message.pack(): AnyProto =
    AnyPacker.pack(this)
