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
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.publish.excludeGoogleProtoFromArtifacts
import org.gradle.configurationcache.extensions.capitalized

plugins {
    `java-library`
    id("com.google.protobuf")
}

apply<IncrementGuard>()

dependencies {
    Protobuf.libs.forEach { protobuf(it) }
    annotationProcessor(AutoService.processor)
    compileOnly(AutoService.annotations)
    implementation(Kotlin.reflect)
    testImplementation(project(":testlib"))
}

val generatedDir by extra("$projectDir/generated")

sourceSets {
    val generatedSpineDir by extra("$generatedDir/main/java")
    val generatedTestSpineDir by extra("$generatedDir/test/java")

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
    configurations.excludeProtobufLite()
    generatedFilesBaseDir = generatedDir

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
             * Remove the code generated for Google Protobuf library types.
             *
             * Java code for the `com.google` package was generated because we wanted
             * to have descriptors for all the types, including those from Google Protobuf library.
             * We want all the descriptors so that they are included into the resources used by
             * the `io.spine.type.KnownTypes` class.
             *
             * Now, as we have the descriptors _and_ excessive Java code, we delete it to avoid
             * classes that duplicate those coming from Protobuf library JARs.
             */
            task.doLast {
                val comPackage = File("${generatedDir}/${ssn}/java/com")
                val googlePackage = comPackage.resolve("google")

                delete(googlePackage)

                // We don't need an empty `com` package.
                if (comPackage.exists() && comPackage.list()?.isEmpty() == true) {
                    delete(comPackage)
                }
            }

            /*
             * Make the tasks `processResources` depend on `generateProto` tasks explicitly so that:
             *  1) descriptor set files get into resources, avoiding the racing conditions
             *     during the build.
             *  2) we don't have the warning "Execution optimizations have been disabled..." issued
             *     by Gradle during the build because Protobuf Gradle Plugin does not set
             *     dependencies between `generateProto` and `processResources` tasks.
             */
            val processResources = processResourceTaskName(ssn)
            tasks[processResources].dependsOn(task)
        }
    }
}

fun processResourceTaskName(sourceSetName: String): String {
    val infix = if (sourceSetName == "main") "" else sourceSetName.capitalized()
    return "process${infix}Resources"
}

tasks {
    excludeGoogleProtoFromArtifacts()
}
