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


plugins {
    java
    id("io.spine.mc-java")
}

dependencies {
    testImplementation(project(":factories"))
}

modelCompiler {

    java {
        val methodFactory = "io.spine.tools.protoc.TestMethodFactory"
        val nestedClassFactory = "io.spine.tools.protoc.TestNestedClassFactory"

        forMessages(by().suffix("documents.proto")) {
            markAs("io.spine.tools.protoc.DocumentMessage")
        }
        
        forMessages(by().prefix("spine/tools/protoc/prefix_generation")) {
            markAs("io.spine.tools.protoc.PrefixedMessage")
            generateMethodsWith(methodFactory)
            generateNestedClassesWith(nestedClassFactory)
        }

        forMessages(by().suffix("suffix_generation_test.proto")) {
            markAs("io.spine.tools.protoc.SuffixedMessage")
            generateMethodsWith(methodFactory)
            generateNestedClassesWith(nestedClassFactory)
        }
        
        forMessages(by().regex(".*regex.*test.*")) {
            markAs("io.spine.tools.protoc.RegexedMessage")
            generateMethodsWith(methodFactory)
            generateNestedClassesWith(nestedClassFactory)
        }

        forMessages(by().regex(".*multi.*factory.*test.*")) {
            generateMethodsWith(methodFactory)
        }
    }
}
