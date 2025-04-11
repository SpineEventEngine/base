/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.code.proto

import io.kotest.matchers.shouldBe
import io.spine.io.replaceExtension
import io.spine.string.toBase64Encoded
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.writeBytes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`CodeGeneratorRequestWriter` should")
internal class CodeGeneratorRequestWriterSpec {

    private lateinit var requestFile: File
    private lateinit var writer: CodeGeneratorRequestWriter
    private lateinit var input: InputStream

    @BeforeEach
    fun prepareInput(@TempDir dir: Path) {
        val inputFile = dir.resolve("input.stream")
        // Request the file in the directory which does not exist.
        requestFile = dir.resolve("nested/request.binbp").toFile()
        val request = constructRequest(requestFile.absolutePath.toBase64Encoded())
        inputFile.writeBytes(request.toByteArray())
        input = inputFile.inputStream()
        writer = CodeGeneratorRequestWriter(input)
    }

    @AfterEach
    fun closeInput() {
        input.close()
    }

    @Test
    fun `write binary version of the request`() {
        writer.writeBinary()
        requestFile.exists() shouldBe true
    }

    @Test
    fun `write JSON version of the request`() {
        writer.writeJson()
        requestFile.replaceExtension("pb.json").exists() shouldBe true
    }
}
