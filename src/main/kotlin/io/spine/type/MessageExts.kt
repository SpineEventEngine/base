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

package io.spine.type

import com.google.protobuf.Message
import io.spine.annotation.Internal
import io.spine.protobuf.defaultInstance

/**
 * Obtains the name of this message type.
 */
public val <T : Message> T.typeName: TypeName
    get() = TypeName.of(this)

/**
 * Tells if this message type is internal to a bounded context.
 */
@Suppress("ReturnCount") // We may want to be able to set breakpoints in this method.
public fun <T : Message> T.isInternal(): Boolean {
    if (javaClass.isAnnotationPresent(Internal::class.java)) {
        return true
    }
    val descriptor = descriptorForType
    descriptor.isInternal()?.let {
        return it
    }
    descriptor.file.allTypesAreInternal()?.let {
        return it
    }
    return false
}

/**
 * Tells if this class of messages is internal to a bounded context.
 */
public fun <M : Message> Class<M>.isInternal(): Boolean = defaultInstance.isInternal()
