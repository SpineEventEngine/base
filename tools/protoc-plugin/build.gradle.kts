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

group = "io.spine.tools"

dependencies {
    implementation(project(":tool-base"))
    implementation(project(":tools-api"))
    implementation(project(":protoc-api"))
    implementation(project(":validation-generator"))
    implementation(Deps.gen.javaPoet)
    implementation(Deps.gen.javaxAnnotation)

    testImplementation(project(":base"))
    testImplementation(project(":testlib"))
    testImplementation(project(":mute-logging"))
}

tasks.jar {
    dependsOn(
            ":protoc-api:jar",
            ":tools-api:jar",
            ":tool-base:jar",
            ":validation-generator:jar"
    )

    manifest {
        attributes(mapOf("Main-Class" to "io.spine.tools.protoc.Plugin"))
    }
    // Assemble "Fat-JAR" artifact containing all the dependencies.
    from(configurations.runtimeClasspath.get().map {
        when {
            it.isDirectory -> it
            else -> zipTree(it)
        }
    })
    // We should provide a classifier or else Protobuf Gradle plugin will substitute it with
    // an OS-specific one.
    archiveClassifier.set("exe")
}
