/*
 * Copyright 2023, TeamDev. All rights reserved.
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

import com.google.protobuf.gradle.protobuf
import io.spine.internal.dependency.AutoService
import io.spine.internal.dependency.Kotlin
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.protobuf.setup
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.publish.excludeGoogleProtoFromArtifacts

plugins {
    protobuf
    idea
    errorprone
    `pmd-settings`
    `detekt-code-analysis`
    jacoco
    `project-report`
    `dokka-for-java`
}

apply {
    plugin<IncrementGuard>()
}

dependencies {
    /* Have `protobuf` dependency instead of `api` or `implementation` so that proto
       files from the library are included into the compilation. We need this because we
       build our descriptor set files using those standard proto files too.

       See Protobuf Gradle Plugin documentation for details:
           https://github.com/google/protobuf-gradle-plugin#protos-in-dependencies
    */
    Protobuf.libs.forEach {
        protobuf(it)
    }
    annotationProcessor(AutoService.processor)
    compileOnly(AutoService.annotations)
    implementation(Kotlin.reflect)
    testImplementation(project(":testlib"))
}

protobuf {
    configurations.excludeProtobufLite()
    protoc {
            artifact = Protobuf.compiler
        }
    generateProtoTasks.all().configureEach {
        setup()
    }
}

tasks {
    excludeGoogleProtoFromArtifacts()
}
