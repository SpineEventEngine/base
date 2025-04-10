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
import io.spine.format.Format.JSON
import io.spine.format.Format.PROTO_BINARY
import io.spine.format.Format.PROTO_JSON
import io.spine.format.Format.TEXT
import io.spine.format.Format.YAML
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
        PROTO_BINARY.extensions.shouldContainInOrder(
            "binpb", "pb", "bin"
        )
        PROTO_JSON.extensions.shouldContainInOrder(
            "pb.json"
        )
        JSON.extensions.shouldContainInOrder(
            "json"
        )
        YAML.extensions.shouldContainInOrder(
            "yml", "yaml"
        )
        TEXT.extensions.shouldContainInOrder(
            "txt"
        )
    }

    @Test
    fun `match files`() {
        fun assertMatches(format: Format, vararg fileNames: String) = fileNames.forEach {
            format.matches(Path(it)) shouldBe true
        }
        assertMatches(PROTO_BINARY, "my.binpb", "dir/sub/file.pb", "app/bin/settings.bin")
        assertMatches(PROTO_JSON, "my.pb.json", "dir/sub/file.pb.json", "app/bin/settings.pb.json")
        // Even though the "extension" for the first file is `pb.json` which corresponds
        // to the `PROTO_JSON` format, it is also compatible with `JSON`.
        assertMatches(JSON, "my.pb.json", "dir/sub/file.json", "bin/settings.pb.json")
        assertMatches(YAML, "my.yml", "dir/sub/file.yaml")
        assertMatches(TEXT, "file.txt")
    }

    @Test
    fun `not match files with wrong extensions`() {
        fun assertDoesNotMatch(format: Format, vararg fileNames: String) = fileNames.forEach {
            format.matches(Path(it)) shouldBe false
        }
        assertDoesNotMatch(PROTO_BINARY, "my.txt", "dir/sub/file.pb.json")
        assertDoesNotMatch(PROTO_JSON, "my.pb", "dir/sub/file.json", "bin/settings.pb")
        assertDoesNotMatch(JSON, "my.yaml", "dir/sub/file.txt")
        assertDoesNotMatch(YAML, "my.json", "dir/file.txt")
        assertDoesNotMatch(TEXT, "my.json", "file.bp.json")
    }

    @Test
    fun `provide extensions for 'File' and 'Path' for checking a supported format`() {
        fun assertSupported(vararg fileNames: String) = fileNames.forEach {
            File(it).hasSupportedFormat() shouldBe true
        }

        assertSupported(
            "file.binpb", "file.pb", "file.bin",
            "file.pb.json", "file.json", "file.yml", "file.yaml", "file.txt"
        )

        File("picture.gif").hasSupportedFormat() shouldBe false
    }

    @Test
    fun `provide factory methods`() {
        "file.binpb" shouldProduceFormat PROTO_BINARY
        "file.pb" shouldProduceFormat PROTO_BINARY
        "file.bin" shouldProduceFormat PROTO_BINARY
        "file.pb.json" shouldProduceFormat PROTO_JSON
        "file.json" shouldProduceFormat JSON
        "file.yml" shouldProduceFormat YAML
        "file.yaml" shouldProduceFormat YAML
        "file.txt" shouldProduceFormat TEXT

        // Check that an unsupported extension causes the exception.
        assertThrows<IllegalStateException> {
            Format.of(File("photo.jpeg"))
        }
    }

    private infix fun String.shouldProduceFormat(expectedFormat: Format) =
        Format.of(File(this)) shouldBe expectedFormat

}
