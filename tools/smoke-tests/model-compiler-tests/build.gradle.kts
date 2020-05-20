/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import io.spine.gradle.internal.Deps

plugins {
    java
    id("io.spine.tools.spine-model-compiler")
}

dependencies {
    testImplementation(project(":test-factories"))
    Deps.test.truth.forEach { testImplementation(it) }
}

modelCompiler {

    interfaces {
        mark(messages().inFiles(mapOf("suffix" to "documents.proto")), asType("io.spine.tools.protoc.DocumentMessage"))
        mark(messages().inFiles(mapOf("prefix" to "spine/tools/protoc/prefix_generation")), asType("io.spine.tools.protoc.PrefixedMessage"))
        mark(messages().inFiles(mapOf("suffix" to "suffix_generation_test.proto")), asType("io.spine.tools.protoc.SuffixedMessage"))
        mark(messages().inFiles(mapOf("regex" to ".*regex.*test.*")), asType("io.spine.tools.protoc.RegexedMessage"))
    }

    methods {
        applyFactory("io.spine.tools.protoc.TestMethodFactory", messages().inFiles(mapOf("suffix" to "suffix_generation_test.proto")))
        applyFactory("io.spine.tools.protoc.TestMethodFactory", messages().inFiles(mapOf("prefix" to "spine/tools/protoc/prefix_generation")))
        applyFactory("io.spine.tools.protoc.TestMethodFactory", messages().inFiles(mapOf("regex" to ".*regex.*test.*")))
        applyFactory("io.spine.tools.protoc.TestMethodFactory", messages().inFiles(mapOf("regex" to ".*multi.*factory.*test.*")))
    }

    nestedClasses {
        applyFactory("io.spine.tools.protoc.TestNestedClassFactory", messages().inFiles(mapOf("suffix" to "suffix_generation_test.proto")))
        applyFactory("io.spine.tools.protoc.TestNestedClassFactory", messages().inFiles(mapOf("prefix" to "spine/tools/protoc/prefix_generation")))
        applyFactory("io.spine.tools.protoc.TestNestedClassFactory", messages().inFiles(mapOf("regex" to ".*regex.*test.*")))
    }
}
