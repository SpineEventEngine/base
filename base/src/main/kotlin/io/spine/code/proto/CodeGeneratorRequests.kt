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

@file:JvmName("CodeGeneratorRequests")

package io.spine.code.proto

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import io.spine.type.parse
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Redirects to [io.spine.type.parse].
 */
@Deprecated(
    message = "Please use `io.spine.type.parse()`",
    replaceWith = ReplaceWith(
        "this.parse(input)",
        imports = ["io.spine.type.parse"]
    ),
)
public fun KClass<CodeGeneratorRequest>.parse(input: InputStream): CodeGeneratorRequest {
    return parse(input)
}

/**
 * Redirects to [io.spine.type.parse].
 */
@Deprecated(
    message = "Please use `io.spine.type.parse()`",
    replaceWith = ReplaceWith(
        "CodeGeneratorRequest::class.parse(input)",
        imports = ["io.spine.type.parse"]
    )
)
public fun parseCodeGeneratorRequest(input: InputStream): CodeGeneratorRequest =
    CodeGeneratorRequest::class.parse(input)
