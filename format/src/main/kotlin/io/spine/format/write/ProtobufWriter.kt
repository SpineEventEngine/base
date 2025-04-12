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

package io.spine.format.write

import com.google.protobuf.Message
import io.spine.format.Format
import io.spine.format.Format.ProtoBinary
import io.spine.format.Format.ProtoJson
import io.spine.type.toJson
import java.io.File

/**
 * The interface common to writers of Protobuf messages.
 */
internal interface ProtobufWriter: Writer<Message>

/**
 * Writes a message using the [ProtoBinary] format.
 */
internal object ProtoBinaryWriter : ProtobufWriter {

    override val format: Format<Message> = ProtoBinary

    override fun write(file: File, value: Message) =
        file.writeBytes(value.toByteArray())
}

/**
 * Writes a message using [ProtoJson] format.
 */
internal object ProtoJsonWriter : ProtobufWriter {

    override val format: Format<Message> = ProtoJson

    override fun write(file: File, value: Message) =
        file.writeText(value.toJson())
}
