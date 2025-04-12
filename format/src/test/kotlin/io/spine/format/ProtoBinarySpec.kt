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

package io.spine.format

import com.google.protobuf.Timestamp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ProtoBinary` format should")
internal class ProtoBinarySpec : ProtobufFormatTest(Format.ProtoBinary) {

    /**
     * This test describes the behavior of [Format.ProtoBinary] when
     * another type is attempted to be parsed from an array of types.
     *
     * Unlike [Format.ProtoJson] an attempt to parse with another format
     * leads to creating an empty instance of another type with
     * [unknownFields][com.google.protobuf.GeneratedMessage.unknownFields] populated
     * with the data of the parsed bytes.
     *
     * @see io.spine.format.parse.ProtoBinaryParser.doParse
     * @see ProtoJsonSpec
     */
    @Test
    fun `have required a matching type but it could not`() {
        write(file, format, message)
        // We wrote `StringValue`. Now parsing `Timestamp`.
        val timestamp = parse<Timestamp>(file)
        timestamp.seconds shouldBe 0L
        timestamp.nanos shouldBe 0
    }
}
