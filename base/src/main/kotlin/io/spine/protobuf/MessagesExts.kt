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

@file:JvmName("Messages")

package io.spine.protobuf

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.util.concurrent.UncheckedExecutionException
import com.google.protobuf.Any
import com.google.protobuf.Message
import io.spine.annotation.Internal
import io.spine.type.TypeName

/**
 * The cache of the default instances per [Message] class.
 *
 * Creates and caches objects in a lazy mode.
 */
private val defaultInstances = CacheBuilder.newBuilder()
    .maximumSize(1000.toLong())
    .build(MessageCacheLoader())

/**
 * Obtains the default instance of the passed message class.
 *
 * @param [M] the type of the message.
 * @return default instance of the class.
 */
public fun <M : Message> Class<M>.defaultInstance(): M {
    @Suppress("UNCHECKED_CAST")
    return defaultInstances.getUnchecked(this) as M
}

/**
 * Returns the builder for the passed message class.
 */
@Internal
public fun <M : Message> builderFor(cls: Class<M>): Message.Builder {
    return try {
        val message = cls.defaultInstance()
        message.toBuilder()
    } catch (e: UncheckedExecutionException) {
        throw IllegalArgumentException(
            "Class `${cls.canonicalName}` must be a generated proto message.", e
        )
    }
}

/**
 * Ensures that the passed instance of `Message` is not an instance
 * of [com.google.protobuf.Any], and unwraps the message if `Any` is passed.
 */
public fun Message.ensureUnpacked(): Message =
    if (this is Any) {
        AnyPacker.unpack(this)
    } else {
        this
    }

/**
 * Verifies if the passed message object is its default state and is not `null`.
 *
 * @return `true` if the message is in the default state, `false` otherwise.
 */
public fun Message.isDefault(): Boolean = (defaultInstanceForType == this)

/**
 * Verifies if the passed message object is not its default state and is not `null`.
 *
 * @return `true` if the message is not in the default state, `false` otherwise.
 */
public fun Message.isNotDefault(): Boolean = !isDefault()

/**
 * The loader of the cache of default instances per [Message] class.
 *
 * Loads a default instance of `Message` for the given type passed.
 */
private class MessageCacheLoader : CacheLoader<Class<out Message>, Message>() {

    override fun load(messageClass: Class<out Message>): Message {
        // It is safe to use the `Internal` utility class from Protobuf since it relies on
        // the fact that the generated class has the `getDefaultInstance()` static method.
        return com.google.protobuf.Internal.getDefaultInstance(messageClass)!!
    }
}

/**
 * Tells if this message type is internal to a bounded context.
 */
public fun <T : Message> T.isInternal(): Boolean =
    this::class.java.isInternal()

/**
 * Tells if this class of messages is internal to a bounded context.
 */
public fun <T : Message> Class<T>.isInternal(): Boolean =
    isAnnotationPresent(Internal::class.java)

/**
 * Obtains the name of this message type.
 */
public val <T : Message> T.typeName: TypeName
    get() = TypeName.of(this)
