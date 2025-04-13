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

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import io.spine.io.replaceExtension
import io.spine.string.decodeBase64
import io.spine.type.ExtensionRegistryHolder.extensionRegistry
import io.spine.type.parse
import io.spine.type.toJson
import java.io.File
import java.io.InputStream
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import kotlin.io.path.writeBytes

/**
 * Parses a [CodeGeneratorRequest] from given [input] and writes it into
 * files in [binary][writeBinary] and [JSON][writeJson] format.
 *
 * @param input The input stream containing binary version of the request.
 */
public class CodeGeneratorRequestWriter(
    private val input: InputStream
) {
    /**
     * Lazily evaluated [CodeGeneratorRequest] parsed from [input] using [extensionRegistry].
     */
    public val request: CodeGeneratorRequest by lazy {
        CodeGeneratorRequest::class.parse(input)
    }

    /**
     * The target file for writing the request in the binary form.
     *
     * The name of the request is passed as the [parameter][CodeGeneratorRequest.getParameter] of
     * the request as a Base64 encoded file path.
     */
    public val requestFile: File by lazy {
        File(request.parameter.decodeBase64())
    }

    /**
     * The path to the request file in JSON format.
     *
     * The file has the same name as [requestFile] and the extension of `".pb.json"`.
     */
    public val requestFileInJson: File by lazy {
        requestFile.replaceExtension("pb.json")
    }

    /**
     * Writes the request into the location specified in [requestFile].
     */
    public fun writeBinary() {
        ensureDirectory()
        requestFile.toPath().writeBytes(request.toByteArray(), CREATE, TRUNCATE_EXISTING)
    }

    /**
     * Writes the request in JSON format to the location specified in [requestFileInJson].
     */
    public fun writeJson() {
        val json = request.toJson()
        ensureDirectory()
        requestFileInJson.writeText(json)
    }

    private fun ensureDirectory() {
        val targetDir = requestFile.parentFile
        targetDir.mkdirs()
    }
}
