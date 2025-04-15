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

package io.spine.gradle.report.coverage

/**
 * Fragments of file path which allow to detect the type of the file.
 */
internal enum class PathMarker(val infix: String) {

    /**
     * Generated files.
     */
    GENERATED("generated"),

    /**
     * Files produced by humans and written in Java.
     */
    JAVA_SRC_FOLDER("/java/"),

    /**
     * Java source files generated by Spine framework.
     */
    SPINE_JAVA_SRC_FOLDER("main/spine/"),

    /**
     * Java source files generated by gRPC plugin.
     */
    GRPC_SRC_FOLDER("/main/grpc/"),

    /**
     * Among compiler output folders, highlights those containing the compilation result
     * of human-produced Java files.
     */
    JAVA_OUTPUT_FOLDER("/main/"),

    /**
     * Anonymous class.
     */
    ANONYMOUS_CLASS("$");

    /**
     * The number of symbols in the marker.
     */
    val length: Int
        get() = this.infix.length
}
