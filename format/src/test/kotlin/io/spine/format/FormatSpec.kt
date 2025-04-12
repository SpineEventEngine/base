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

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.spine.format.Format.Json
import io.spine.format.Format.ProtoBinary
import io.spine.format.Format.ProtoJson
import io.spine.format.Format.Yaml
import java.io.File
import kotlin.io.path.Path
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`Format` should")
class FormatSpec {

    /**
     * This test verifies the order of format extensions.
     *
     * The order is important because, when writing a file in a specific format,
     * the first extension will be used.
     */
    @Test
    fun `provide allowed extensions`() {
        ProtoBinary.extensions.shouldContainInOrder(
            "binpb", "pb", "bin"
        )
        ProtoJson.extensions.shouldContainInOrder(
            "pb.json"
        )
        Json.extensions.shouldContainInOrder(
            "json"
        )
        Yaml.extensions.shouldContainInOrder(
            "yml", "yaml"
        )
    }

    @Test
    fun `match files`() {
        fun assertMatches(format: Format<*>, vararg fileNames: String) = fileNames.forEach {
            format.matches(Path(it)) shouldBe true
        }
        assertMatches(ProtoBinary, "my.binpb", "dir/sub/file.pb", "app/bin/settings.bin")
        assertMatches(ProtoJson, "my.pb.json", "dir/sub/file.pb.json", "app/bin/settings.pb.json")
        // Even though the "extension" for the first file is `pb.json` which corresponds
        // to the `PROTO_JSON` format, it is also compatible with `JSON`.
        assertMatches(Json, "my.pb.json", "dir/sub/file.json", "bin/settings.pb.json")
        assertMatches(Yaml, "my.yml", "dir/sub/file.yaml")
    }

    @Test
    fun `not match files with wrong extensions`() {
        fun assertDoesNotMatch(format: Format<*>, vararg fileNames: String) = fileNames.forEach {
            format.matches(Path(it)) shouldBe false
        }
        assertDoesNotMatch(ProtoBinary, "my.txt", "dir/sub/file.pb.json")
        assertDoesNotMatch(ProtoJson, "my.pb", "dir/sub/file.json", "bin/settings.pb")
        assertDoesNotMatch(Json, "my.yaml", "dir/sub/file.txt")
        assertDoesNotMatch(Yaml, "my.json", "dir/file.txt")
    }

    @Test
    fun `provide extensions for 'File' and 'Path' for checking a supported format`() {
        fun assertSupported(vararg fileNames: String) = fileNames.forEach {
            File(it).hasSupportedFormat() shouldBe true
        }

        assertSupported(
            "file.binpb", "file.pb", "file.bin",
            "file.pb.json", "file.json", "file.yml", "file.yaml"
        )

        File("picture.gif").hasSupportedFormat() shouldBe false
    }

    @Test
    fun `provide factory methods`() {
        "file.binpb" shouldProduceFormat ProtoBinary
        "file.pb" shouldProduceFormat ProtoBinary
        "file.bin" shouldProduceFormat ProtoBinary
        "file.pb.json" shouldProduceFormat ProtoJson
        "file.json" shouldProduceFormat Json
        "file.yml" shouldProduceFormat Yaml
        "file.yaml" shouldProduceFormat Yaml
    }

    private infix fun String.shouldProduceFormat(expectedFormat: Format<*>) =
        Format.of(File(this)) shouldBe expectedFormat

    @Test
    fun `throw an exception for unsupported format`() {
        val file = File("photo.jpeg")
        val exception = assertThrows<IllegalStateException> {
            Format.of(file)
        }
        exception.message shouldBe "Unsupported file format: `${file.name}`."
    }
}
