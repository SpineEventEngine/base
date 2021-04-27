/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import io.spine.tools.java.protoc.MessageSelectorFactory.prefix
import io.spine.tools.java.protoc.MessageSelectorFactory.regex
import io.spine.tools.java.protoc.MessageSelectorFactory.suffix

plugins {
    java
    id("io.spine.tools.mc-java")
}

dependencies {
    testImplementation(project(":factories"))
}

modelCompiler {

    generateValidation = true

    interfaces {
        mark(messages().inFiles(suffix("documents.proto")), asType("io.spine.tools.protoc.DocumentMessage"))
        mark(messages().inFiles(prefix("spine/tools/protoc/prefix_generation")), asType("io.spine.tools.protoc.PrefixedMessage"))
        mark(messages().inFiles(suffix("suffix_generation_test.proto")), asType("io.spine.tools.protoc.SuffixedMessage"))
        mark(messages().inFiles(regex(".*regex.*test.*")), asType("io.spine.tools.protoc.RegexedMessage"))
    }

    methods {
        val factory = "io.spine.tools.mc.java.protoc.given.TestMethodFactory"
        applyFactory(factory, messages().inFiles(suffix("suffix_generation_test.proto")))
        applyFactory(factory, messages().inFiles(prefix("spine/tools/protoc/prefix_generation")))
        applyFactory(factory, messages().inFiles(regex(".*regex.*test.*")))
        applyFactory(factory, messages().inFiles(regex(".*multi.*factory.*test.*")))
    }

    nestedClasses {
        val factory = "io.spine.tools.mc.java.protoc.given.TestNestedClassFactory"
        applyFactory(factory, messages().inFiles(suffix("suffix_generation_test.proto")))
        applyFactory(factory, messages().inFiles(prefix("spine/tools/protoc/prefix_generation")))
        applyFactory(factory, messages().inFiles(regex(".*regex.*test.*")))
    }
}
