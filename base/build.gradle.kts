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

import com.google.protobuf.gradle.ProtobufConfigurator
import groovy.lang.Closure
import groovy.lang.GString
import io.spine.gradle.internal.Deps
import io.spine.gradle.internal.RunBuild
import java.nio.file.Files.isSameFile

plugins {
    id("com.google.protobuf")
}

group = "io.spine"

apply(from = Deps.scripts.testArtifacts(project))

configurations {
    // Avoid collisions of Java classes defined both in `protobuf-lite` and `protobuf-java`
    named("runtimeClasspath").get().exclude(group = "com.google.protobuf", module = "protobuf-lite")
    named("testRuntimeClasspath").get().exclude(group = "com.google.protobuf", module = "protobuf-lite")
}

dependencies {
    Deps.build.protobuf.forEach { "protobuf"(it) }
    "annotationProcessor"(Deps.build.autoService.processor)
    "compileOnly"(Deps.build.autoService.annotations)
    Deps.build.errorProneAnnotations.forEach { "api"(it) }
    "testImplementation"(project(":testlib"))
    "testImplementation"(project(":mute-logging"))
}

the<SourceSetContainer>().apply {
    named("main") {
        resources.srcDir("$buildDir/descriptors/main")
        proto.setSrcDirs(listOf("$projectDir/src/main/proto"))
    }
    named("test") {
        resources.srcDir("$buildDir/descriptors/test")
        proto.setSrcDirs(listOf("$projectDir/src/test/proto"))
    }
}


/**
 * The JAR task assembles class files with a respect to the re-built message classes.
 *
 * The task checks each input file for a newer version in the `base-validating-builders`. If such
 * a version is found, the older version is excluded.
 */
val jar by tasks.getting(type = Jar::class)

jar.apply {
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

apply(from = Deps.scripts.publishProto(project))

tasks.register(name = "rebuildProtobuf", type = RunBuild::class) {
    directory = "$rootDir/base-validating-builders"
    dependsOn(rootProject.subprojects.map { p -> p.tasks["publishToMavenLocal"] })
}

tasks["publish"].dependsOn("rebuildProtobuf")
tasks["build"].finalizedBy("rebuildProtobuf")

val compiledProtoRoot = "$projectDir/generated"
val googlePackagePrefix = "com/google"

val pruneGoogleProtos by tasks.registering(type = Delete::class) {
    delete("$compiledProtoRoot/main/java/$googlePackagePrefix")
    tasks["compileJava"].dependsOn(this)
}

val pruneTestGoogleProtos by tasks.registering(type = Delete::class) {
    delete("$compiledProtoRoot/test/java/$googlePackagePrefix")
    tasks["compileTestJava"].dependsOn(this)
}

protobuf {
    protobuf.generatedFilesBaseDir = compiledProtoRoot
    protobuf.generateProtoTasks(object : Closure<Any>(this) {
        fun doCall(tasks: ProtobufConfigurator.JavaGenerateProtoTaskCollection) {
            for (task in tasks.all()) {
                val scope = task.sourceSet.name
                task.generateDescriptorSet = true
                task.descriptorSetOptions.path = GString.EMPTY.plus("$buildDir/descriptors/$scope/known_types_${scope}.desc")
                task.descriptorSetOptions.includeImports = true
                task.descriptorSetOptions.includeSourceInfo = true

                if (scope.contains("test")) {
                    pruneTestGoogleProtos.configure { dependsOn(task) }
                } else {
                    pruneGoogleProtos.configure { dependsOn(task) }
                }
            }
        }
    })
}

/**
 * Checks if the given file belongs to the Google `.proto` sources.
 */
fun isGoogleProtoSource(file: FileTreeElement): Boolean {
    val pathSegments = file.relativePath.segments
    return pathSegments.isNotEmpty() && pathSegments[0].equals("google")
}

/**
 * From all artifacts, exclude Google `.proto` sources.
 */
tasks.withType(Jar::class) {
    exclude { isGoogleProtoSource(it) }
}

