/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

@file:JvmName("Binary")

package io.spine.type

import com.google.protobuf.Message
import com.google.protobuf.Parser
import io.spine.protobuf.defaultInstance
import io.spine.type.ExtensionRegistryHolder.extensionRegistry
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Obtains a binary form [Parser] for this message class.
 */
@Suppress("UNCHECKED_CAST") // The generated code ensures the correct parser type.
public val <M : Message> Class<M>.parser: Parser<M>
    get() = defaultInstance.parserForType as Parser<M>

/**
 * Creates new message instance by parsing it from the given input stream.
 *
 * This function uses [ExtensionRegistry][extensionRegistry] with all known
 * custom Protobuf options.
 *
 * @param M The type of the message.
 * @see io.spine.type.ExtensionRegistryHolder
 */
public fun <M : Message> Class<M>.parse(input: InputStream): M =
    parser.parseFrom(input, extensionRegistry)

/**
 * Creates new message instance by parsing it from the given input stream.
 *
 * This function uses [ExtensionRegistry][extensionRegistry] with all known
 * custom Protobuf options.
 *
 * @param M The type of the message.
 * @see io.spine.type.ExtensionRegistryHolder
 */
public fun <M : Message> KClass<M>.parse(input: InputStream): M =
    java.parse(input)
