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

import com.google.common.io.Files
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import io.spine.gradle.internal.Deps
import java.util.*
import kotlin.collections.HashMap

group = "io.spine.tools"

dependencies {
    api(gradleApi())
    api(Deps.build.gradlePlugins.protobuf)
    api(project(":tool-base"))

    testImplementation(project(":testlib"))
    testImplementation(project(":plugin-testlib"))
}

protobuf {
    generateProtoTasks {
        for (task in all()) {
            task.generateDescriptorSet = true
            task.descriptorSetOptions.path = "$buildDir/descriptors/${task.sourceSet.name}/known_types.desc"
        }
    }
}

val spineBaseVersion: String by extra

val prepareProtocConfigVersions by tasks.registering {
    description = "Prepares the versions.properties file."

    val file = file("$projectDir/generated/main/resources/versions.properties")
    outputs.file(file)

    val versions = Properties()
    versions.setProperty("baseVersion", spineBaseVersion)
    versions.setProperty("protobufVersion", Deps.versions.protobuf)
    versions.setProperty("gRPCVersion", Deps.versions.grpc)

    @Suppress("UNCHECKED_CAST")
    inputs.properties(HashMap(versions) as MutableMap<String, *>)

    doLast {
        Files.createParentDirs(file)
        file.createNewFile()
        file.outputStream().use {
            versions.store(it, "Versions of dependencies of the Model Compiler plugin and the Spine Protoc plugin.")
        }
    }
}

tasks.processResources.get().dependsOn(prepareProtocConfigVersions)

sourceSets.test {
    resources.srcDir("$projectDir/src/test/resources")
}