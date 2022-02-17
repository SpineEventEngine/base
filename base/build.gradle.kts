/*
 * Copyright 2022, TeamDev. All rights reserved.
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

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import io.spine.internal.dependency.AutoService
import io.spine.internal.dependency.Kotlin
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.publish.Publish.Companion.publishProtoArtifact
import io.spine.internal.gradle.testing.exposeTestArtifacts

plugins {
    `java-library`
    id("com.google.protobuf")
}

project.exposeTestArtifacts()
apply<IncrementGuard>()

configurations.excludeProtobufLite()

dependencies {
    Protobuf.libs.forEach { protobuf(it) }
    annotationProcessor(AutoService.processor)
    compileOnly(AutoService.annotations)
    implementation(Kotlin.reflect)
    testImplementation(project(":testlib"))
}

val generatedDir by extra("$projectDir/generated")
val generatedSpineDir by extra("$generatedDir/main/spine")
val generatedTestSpineDir by extra("$generatedDir/test/spine")

sourceSets {
    main {
        java.srcDir(generatedSpineDir)
        resources.srcDir("$buildDir/descriptors/main")
    }
    test {
        java.srcDir(generatedTestSpineDir)
        resources.srcDir("$buildDir/descriptors/test")
    }
}

protobuf {
    val generatedRoot = "$projectDir/generated"
    val googlePackagePrefix = "com/google"

    generatedFilesBaseDir = generatedRoot
    generateProtoTasks {
        for (task in all()) {
            val ssn = task.sourceSet.name
            task.generateDescriptorSet = true
            with(task.descriptorSetOptions) {
                path = "$buildDir/descriptors/${ssn}/known_types_${ssn}.desc"
                includeImports = true
                includeSourceInfo = true
            }

            /**
                Remove the code generated for Google Protobuf library types.

                Java code for the `com.google` package was generated because we wanted
                to have descriptors for all the types, including those from Google Protobuf library.
                We want all the descriptors so that they are included into the resources used by
                the `io.spine.type.KnownTypes` class.

                Now, as we have the descriptors _and_ excessive Java code, we delete it to avoid
                classes that duplicate those coming from Protobuf library JARs.
            */
            task.doLast {
                delete("${generatedRoot}/${ssn}/java/${googlePackagePrefix}")
            }
        }
    }
}

/**
 * Checks if the given file belongs to the Google `.proto` sources.
 */
fun FileTreeElement.isGoogleProtoSource(): Boolean {
    val pathSegments = relativePath.segments
    return pathSegments.isNotEmpty() && pathSegments[0].equals("google")
}

/**
 * Exclude Google `.proto` sources from all the artifacts.
 */
tasks.withType(Jar::class) {
    exclude { it.isGoogleProtoSource() }
}

publishProtoArtifact(project)
