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

package io.spine.protobuf

import io.spine.annotation.Internal
import io.spine.base.MapOfAnys
import io.spine.base.MapOfAnysKt.entry
import io.spine.base.mapOfAnys
import io.spine.protobuf.TypeConverter.toAny

/**
 * Converts Kotlin's map of [Any] to [MapOfAnys] proto message.
 */
@Internal
internal class MapConverter : ProtoConverter<MapOfAnys, Map<Any, Any>>() {

    override fun toObject(input: MapOfAnys): Map<Any, Any> =
        throw UnsupportedOperationException(
            "`${javaClass.name}` does not support conversion of Protobuf messages to `Map`."
        )

    override fun toMessage(input: Map<Any, Any>): MapOfAnys {
        val entries = input.map { it.toProtoEntry() }
        return mapOfAnys {
            entry.addAll(entries)
        }
    }
}

private fun Map.Entry<Any, Any>.toProtoEntry() = entry {
    key = toAny(this@toProtoEntry.key)
    value = toAny(this@toProtoEntry.value)
}
