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

import com.google.protobuf.gradle.ProtobufConfigurator.JavaGenerateProtoTaskCollection
import groovy.lang.Closure
import groovy.lang.GString
import io.spine.gradle.internal.Deps
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    java
    id("com.google.protobuf")
}

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
    // The freshest version of the plugin required for tests
    protocPluginDependency = testCompileOnly("io.spine.tools:spine-protoc-plugin:$spineVersion@jar")
}

protobuf {
    protobuf.generateProtoTasks(
            object : Closure<Any>(this) {
                private fun doCall(tasks: JavaGenerateProtoTaskCollection) {
                    tasks.all().forEach { task ->
                        val scope = task.sourceSet.name
                        task.generateDescriptorSet = true
                        task.descriptorSetOptions.path = GString.EMPTY.plus("$buildDir/descriptors/${scope}/io.spine.tools.spine-model-compiler-${scope}.desc")
                        task.descriptorSetOptions.includeImports = true
                        task.descriptorSetOptions.includeSourceInfo = true
                    }
                }
            }
    )
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

val spineFolder = file(".spine")

// We cannot use standard Copy task here as it resolves the `from` property not lazily.
// Since we use use a dependency in the `from`, it may cause some issues with the Maven plugin
// See https://discuss.gradle.org/t/right-way-to-copy-contents-from-dependency-archives/7449
val copyProtocPluginTestArtifact by tasks.registering {
    description = "Spawns the Spine Protoc plugin artifact in the project directory for tests"
    dependsOn(":protoc-plugin:publishToMavenLocal")

    doLast {
        val from = configurations.testCompileClasspath.get().fileCollection(protocPluginDependency).singleFile
        val srcPath = from.toPath()
        val dest = spineFolder.toPath().resolve(from.name)
        dest.toFile().mkdirs()
        Files.copy(srcPath, dest, StandardCopyOption.REPLACE_EXISTING)
    }

}

// Tests use the Protobuf plugin.
tasks.compileTestJava.configure { dependsOn(copyProtocPluginTestArtifact) }
tasks.test.configure { dependsOn(":errorprone-checks:publishToMavenLocal") }
