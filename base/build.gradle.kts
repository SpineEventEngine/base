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

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import io.spine.internal.dependency.AutoService
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.gradle.RunBuild
import io.spine.internal.gradle.Scripts
import io.spine.internal.gradle.excludeProtobufLite
import java.nio.file.Files.isSameFile
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar

plugins {
    `java-library`
    id("com.google.protobuf")
}

group = "io.spine"

apply(from = Scripts.testArtifacts(project))
apply<IncrementGuard>()

configurations.excludeProtobufLite()

dependencies {
    Protobuf.libs.forEach { protobuf(it) }
    annotationProcessor(AutoService.processor)
    compileOnly(AutoService.annotations)
    testImplementation(project(":testlib"))
    testImplementation(project(":mute-logging"))
}

sourceSets {
    main {
        resources.srcDir("$buildDir/descriptors/main")
        proto.setSrcDirs(listOf("$projectDir/src/main/proto"))
    }
    test {
        resources.srcDir("$buildDir/descriptors/test")
        proto.setSrcDirs(listOf("$projectDir/src/test/proto"))
    }
}

/**
 * The JAR task assembles class files with a respect to the re-built message classes.
 *
 * The task checks each input file for a newer version in the `base-validating-builders`.
 * If such a version is found, the older version is excluded.
 */
tasks.jar.configure {
    // See `base-validating-builders/README.md`
    val compiledProtoPath = "$rootDir/base-validating-builders/compiled-proto"
    val compiledProtos = fileTree(compiledProtoPath)

    from(compiledProtos)

    eachFile {
        logger.info("Appending $this")
        val classFile = file.toPath()
        val isProto = compiledProtos.filter { it.path.endsWith(relativePath.toString()) }
                                    .filter { !isSameFile(it.toPath(), classFile) }
        if (!isProto.isEmpty) {
            logger.info("File $classFile is excluded")
            this.exclude()
        } else {
            logger.debug("File $classFile is not excluded")
        }
    }
}

apply(from = Scripts.publishProto(project))

val rebuildProtobuf by tasks.registering(RunBuild::class) {
    directory = "$rootDir/base-validating-builders"
    dependsOn(rootProject.subprojects.map { p -> p.tasks["publishToMavenLocal"] })
}

tasks.publish.get().dependsOn(rebuildProtobuf)
tasks.build.get().finalizedBy(rebuildProtobuf)

val compiledProtoRoot = "$projectDir/generated"
val googlePackagePrefix = "com/google"

val pruneGoogleProtos by tasks.registering(type = Delete::class) {
    delete("$compiledProtoRoot/main/java/$googlePackagePrefix")
    tasks.compileJava.get().dependsOn(this)
}

val pruneTestGoogleProtos by tasks.registering(type = Delete::class) {
    delete("$compiledProtoRoot/test/java/$googlePackagePrefix")
    tasks.compileTestJava.get().dependsOn(this)
}

protobuf {
    generatedFilesBaseDir = compiledProtoRoot
    generateProtoTasks {
        val mainTasks = mutableListOf<Task>()
        val testTasks = mutableListOf<Task>()
        for (task in all()) {
            val scope = task.sourceSet.name
            task.generateDescriptorSet = true
            with(task.descriptorSetOptions) {
                path = "$buildDir/descriptors/$scope/known_types_${scope}.desc"
                includeImports = true
                includeSourceInfo = true
            }

            if (scope.contains("test")) {
                testTasks.add(task)
            } else {
                mainTasks.add(task)
            }
        }
        pruneGoogleProtos.get().dependsOn(mainTasks)
        pruneTestGoogleProtos.get().dependsOn(testTasks)
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
 * From all artifacts, exclude Google `.proto` sources.
 */
tasks.withType(Jar::class) {
    exclude { it.isGoogleProtoSource() }
}
