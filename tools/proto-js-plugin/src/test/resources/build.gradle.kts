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

import com.google.protobuf.gradle.*
import groovy.lang.GString
import io.spine.gradle.internal.Deps

plugins {
    java
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("com.google.protobuf").version(io.spine.gradle.internal.Deps.versions.protobufPlugin)
}

// NOTE: this file is copied from the root project in the test setup.
apply(from = "$rootDir/test-env.gradle")

val enclosingRootDir: String by extra
apply(from = "$enclosingRootDir/version.gradle.kts")

repositories {
    mavenLocal()
    jcenter()
}

tasks.compileJava { enabled = false }
tasks.compileTestJava { enabled = false }

val compileProtoToJs by tasks.registering

protobuf {
    protobuf.generatedFilesBaseDir = "$projectDir/generated"
    protobuf.protoc {
        artifact = Deps.build.protoc
    }

    protobuf.generateProtoTasks {
        // Copy the task collection to avoid `ConcurrentModificationException`.
        ArrayList(all()).forEach { task ->
            task.builtins {
                remove("java")
                id("js") {
                    option("import_style=commonjs")
                }
            }
            task.generateDescriptorSet = true
            task.descriptorSetOptions.path = GString.EMPTY.plus("${projectDir}/build/descriptors/${task.sourceSet.name}/known_types.desc")
            task.descriptorSetOptions.includeImports = true
            task.descriptorSetOptions.includeSourceInfo = true

            compileProtoToJs.get().dependsOn(task)
        }
    }
}

tasks.build {
    dependsOn(compileProtoToJs)
}

val spineVersion: String by extra

dependencies {
    protobuf(files("$enclosingRootDir/base/src/main/proto"))
    implementation("io.spine:spine-base:$spineVersion")
}
