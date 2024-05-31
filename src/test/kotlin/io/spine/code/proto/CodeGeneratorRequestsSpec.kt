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

import com.google.protobuf.TimestampProto
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.codeGeneratorRequest
import com.google.protobuf.compiler.version
import io.kotest.matchers.shouldBe
import io.spine.string.toBase64Encoded
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`CodeGeneratorRequests` utility should")
internal class CodeGeneratorRequestsSpec {

    private lateinit var requestFile: File
    private lateinit var request: CodeGeneratorRequest

    @BeforeEach
    fun prepareFile(@TempDir dir: File) {
        requestFile = dir.resolve("request.binbp")
        val encodedPath = requestFile.absolutePath.toBase64Encoded()
        request = constructRequest(encodedPath)
    }

    @Test
    fun `provide extension for parsing via 'KClass'`() {
        requestFile.writeBytes(request.toByteArray())
        val input = requestFile.inputStream()
        input.use {
            val parsed = CodeGeneratorRequest::class.parse(it)
            parsed shouldBe request
        }
    }
}

/**
 * Creates a stub instance [CodeGeneratorRequest] initialized with data from [TimestampProto].
 *
 * @param parameterValue
 *         the value to be set to the `parameter` property of the request.
 */
internal fun constructRequest(parameterValue: String): CodeGeneratorRequest =
    codeGeneratorRequest {
        val descr = TimestampProto.getDescriptor()
        protoFile += descr.toProto()
        fileToGenerate += descr.file.name
        compilerVersion = version {
            major = 42
            minor = 314
            patch = 271
        }
        parameter = parameterValue
    }

