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

import com.google.protobuf.gradle.ProtobufConfigurator.JavaGenerateProtoTaskCollection
import com.google.protobuf.gradle.*
import io.spine.gradle.internal.Deps
import org.gradle.internal.os.OperatingSystem

buildscript {
    apply(from = "$projectDir/../version.gradle.kts")
    val spineVersion: String by extra

    repositories {
        mavenLocal()
        mavenCentral()
    }

    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    val deps = io.spine.gradle.internal.Deps

    dependencies {
        deps.build.protobuf.forEach { classpath(it) }
        classpath(deps.build.guava)
        classpath(deps.build.flogger)
        classpath(deps.build.checkerAnnotations)
        deps.build.errorProneAnnotations.forEach { classpath(it) }
        classpath(deps.build.jsr305Annotations)
        classpath(deps.build.gradlePlugins.protobuf)

        classpath(deps.gen.javaPoet)
        classpath(deps.runtime.flogger.systemBackend)

        // A library for parsing Java sources.
        // Used for parsing Java sources generated from Protobuf files
        // to make their annotation more convenient.
        classpath(deps.build.roasterApi) {
            exclude(group = "com.google.guava")
        }
        classpath (deps.build.roasterJdt) {
            exclude(group = "com.google.guava")
        }

        classpath(files(
                "$projectDir/../tools/protoc-api/build/libs/protoc-api-${spineVersion}.jar",
                "$projectDir/../tools/model-compiler/build/libs/model-compiler-${spineVersion}.jar",
                "$projectDir/../tools/plugin-base/build/libs/plugin-base-${spineVersion}.jar",
                "$projectDir/../tools/tool-base/build/libs/tool-base-${spineVersion}.jar",
                "$projectDir/../base/build/libs/base-${spineVersion}.jar"
        ))
    }
}

plugins {
    java
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("com.google.protobuf").version(io.spine.gradle.internal.Deps.versions.protobufPlugin)
}

apply(plugin = "io.spine.tools.spine-model-compiler")

extensions["modelCompiler"].withGroovyBuilder {
    setProperty("generateValidation", true)
}

repositories {
    // This defines the `libs` directory of upstream projects as a local repository.
    // See `dependencies` section below for definition of the dependency on the JAR produced by
    // the `base` module.
    flatDir {
        val baseRoot = "$projectDir/.."
        dir("$baseRoot/base/build/libs/")
    }
    mavenLocal()
    mavenCentral()
}

val spineVersion: String by extra

dependencies {
    Deps.build.protobuf.forEach { compileOnly(it) }
    compileOnly(Deps.build.guava)
    compileOnly(Deps.build.flogger)
    compileOnly(Deps.build.checkerAnnotations)
    Deps.build.errorProneAnnotations.forEach { compileOnly(it) }
    compileOnly(Deps.build.jsr305Annotations)

    // The below dependency refers to a local artifact.
    // See `repositories.flatDir` definition above.
    compileOnly(name = "base-${spineVersion}", group = "")
}

sourceSets {
    main {
        java.srcDirs("$projectDir/generated/main/spine")
        proto.srcDirs("$projectDir/../base/src/main/proto")
    }
}

protobuf {
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                remove("grpc")
            }
        }
    }
}

val compiledProtoDir = "$projectDir/compiled-proto"

val copyCompiledClasses by tasks.registering(Copy::class) {
    from(sourceSets.main.get().java.outputDir)
    into(compiledProtoDir)

    include {
        it.isDirectory || it.name.endsWith(".class")
    }

    dependsOn(tasks.compileJava)
}

tasks.assemble {
    dependsOn(copyCompiledClasses)
}

tasks.build {
    doLast {
        val directory = "$projectDir/../"
        val os = OperatingSystem.current()
        val script = when {
            os.isWindows -> "gradlew.bat"
            else -> "gradlew"
        }
        val process = ProcessBuilder()
                .command("$directory/$script", ":base:cleanJar", ":base:jar", "--console=plain")
                .directory(file(directory))
                .start()
        if (process.waitFor() != 0) {
            throw GradleException("Unable to rebuild JAR for :base.")
        }
    }
}

val cleanGenerated by tasks.registering(Delete::class) {
    delete(files("$projectDir/generated", "$projectDir/.spine", compiledProtoDir))
}

tasks.clean { dependsOn(cleanGenerated) }

idea.module {
    generatedSourceDirs.add(file(compiledProtoDir))
}
