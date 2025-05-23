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
import io.spine.base.ListOfAnys
import io.spine.base.listOfAnys
import io.spine.protobuf.TypeConverter.toAny

/**
 * Converts a list of [kotlin.Any] to [ListOfAnys] proto message.
 *
 * Note that the [backward conversion][toObject] from [ListOfAnys]
 * to a list of [kotlin.Any] is not supported.
 */
@Internal
internal class ListConverter : ProtoConverter<ListOfAnys, List<Any>>() {

    override fun toObject(input: ListOfAnys): List<Any> =
        throw UnsupportedOperationException(
            "`${javaClass.name}` does not support conversion of Protobuf messages to `List`."
        )

    override fun toMessage(input: List<Any>): ListOfAnys {
        val values = input.map { toAny(it) }
        return listOfAnys {
            value.addAll(values)
        }
    }
}
