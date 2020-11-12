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

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import io.spine.gradle.internal.Deps

group = "io.spine.tools"

var protocPluginDependency: Dependency? = null
val spineVersion: String by extra

dependencies {
    implementation(project(":plugin-base"))
    implementation(project(":protoc-api"))
    implementation(Deps.gen.javaPoet)

    // A library for parsing Java sources.
    // Used for parsing Java sources generated from Protobuf files
    // to make their annotation more convenient.
    implementation(Deps.build.roasterApi) {
        exclude(group = "com.google.guava")
    }
    implementation(Deps.build.roasterJdt) {
        exclude(group = "com.google.guava")
    }
    implementation(Deps.build.gradlePlugins.protobuf)
    testImplementation(project(":testlib"))
    testImplementation(Deps.test.junitPioneer)
    testImplementation(gradleTestKit())
    testImplementation(project(":plugin-testlib"))
}

protobuf {
    generateProtoTasks {
        all().forEach { task ->
            val scope = task.sourceSet.name
            task.generateDescriptorSet = true
            task.descriptorSetOptions.path = "$buildDir/descriptors/${scope}/io.spine.tools.spine-model-compiler-${scope}.desc"
            task.descriptorSetOptions.includeImports = true
            task.descriptorSetOptions.includeSourceInfo = true
        }
    }
}

sourceSets {
    main {
        java.srcDir("$projectDir/generated/main/spine")
        resources.srcDir("$projectDir/generated/main/resources")
        resources.srcDir("$buildDir/descriptors/main")
    }
    test {
        java.srcDir("$projectDir/generated/test/spine")
        resources.srcDir("$buildDir/descriptors/test")
    }
}

// Tests use the Protobuf plugin.
tasks.test.configure { dependsOn(":errorprone-checks:publishToMavenLocal") }
