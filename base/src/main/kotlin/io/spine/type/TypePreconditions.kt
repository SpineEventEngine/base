/*
 * Copyright 2023, TeamDev. All rights reserved.
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

@file:JvmName("TypePreconditions")

package io.spine.type

import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.google.protobuf.Message
import io.spine.annotation.Internal
import io.spine.protobuf.defaultInstance
import io.spine.protobuf.isInternal

/**
 * Verifies that the given message instance is annotated with
 * [io.spine.annotation.Internal] and if so, returns it.
 *
 * @throws IllegalArgumentException
 *          if the message is not internal.
 */
@CanIgnoreReturnValue
public fun requireInternal(msg: Message): Message {
    require(msg.isInternal()) {
        "The message class `${msg::class.java.canonicalName}` is not" +
                " annotated as `${Internal::class.java.canonicalName}`."
    }
    return msg
}

/**
 * Verifies if the given message is not internal to a bounded context,
 * returning it if so.
 *
 * @throws UnpublishedLanguageException
 *          if the given message is internal.
 */
@CanIgnoreReturnValue
public fun requirePublished(msg: Message): Message {
    if (msg.isInternal()) {
        throw UnpublishedLanguageException(msg)
    }
    return msg
}

/**
 * Verifies if the given class of messages is a part of published language
 * of a bounded context, returning it if so.
 *
 * @throws UnpublishedLanguageException
 *          if the message class is internal to the bounded context.
 */
@CanIgnoreReturnValue
public fun <T : Message> requirePublished(clazz: Class<T>): Class<T> {
    if (clazz.isInternal()) {
        val msg = clazz.defaultInstance()
        throw UnpublishedLanguageException(msg)
    }
    return clazz
}


