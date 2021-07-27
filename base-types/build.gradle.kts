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



import com.google.protobuf.gradle.*
import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.Flogger
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Protobuf

@Suppress("RemoveRedundantQualifierName") // cannot use imports under `buildScript`
buildscript {
    apply(from = "$projectDir/../version.gradle.kts")
    val spineVersion: String by extra

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        io.spine.internal.dependency.Protobuf.libs.forEach {
            classpath(it)
        }

        classpath(io.spine.internal.dependency.Guava.lib)
        classpath(io.spine.internal.dependency.Flogger.lib)
        classpath(io.spine.internal.dependency.CheckerFramework.annotations)

        io.spine.internal.dependency.ErrorProne.annotations.forEach {
            classpath(it)
        }

        classpath(io.spine.internal.dependency.JavaX.annotations)
        classpath(io.spine.internal.dependency.Protobuf.GradlePlugin.lib)

        classpath(io.spine.internal.dependency.JavaPoet.lib)
        classpath(io.spine.internal.dependency.Flogger.Runtime.systemBackend)

        // A library for parsing Java sources.
        // Used for parsing Java sources generated from Protobuf files
        // to make their annotation more convenient.
        with(io.spine.internal.dependency.Roaster) {
            classpath(api) {
                exclude(group = "com.google.guava")
            }
            classpath(jdt) {
                exclude(group = "com.google.guava")
            }
        }
        classpath("io.spine.tools:spine-mc-java:$spineVersion")
//        classpath("io.spine.tools:spine-mc-java-checks:$spineVersion")
    }
}

plugins {
    `java-library`
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id) version version
    }
}

val spineVersion: String by extra

apply(plugin = "io.spine.mc-java")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    Protobuf.libs.forEach { compileOnly(it) }
    compileOnly(Guava.lib)
    compileOnly(Flogger.lib)
    compileOnly(CheckerFramework.annotations)
    ErrorProne.annotations.forEach { compileOnly(it) }
    compileOnly(JavaX.annotations)
    api("io.spine:spine-base:$spineVersion")
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

tasks.withType<JavaCompile> {
    val currentJavaVersion = JavaVersion.current()
    if (currentJavaVersion != JavaVersion.VERSION_1_8) {
        throw GradleException(
            "Base types must be built using Java 8 (as the main project)." +
                    " The version of Java in this project: $currentJavaVersion."
        )
    }

    // Explicitly sets the encoding of the source and test source files, ensuring
    // correct execution of the `javac` task.
    options.encoding = "UTF-8"
}
