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

import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Truth
import io.spine.internal.gradle.Scripts

buildscript {

    val baseRoot = "${rootDir}/.."
    val versionGradle = "${baseRoot}/version.gradle.kts"
    val commonPath = io.spine.internal.gradle.Scripts.commonPath

    apply(from = versionGradle)
    apply(from = "${baseRoot}/${commonPath}/dependencies.gradle")

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }

    val spineVersion: String by extra

    dependencies {
        classpath(io.spine.internal.dependency.Guava.lib)
        @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
        classpath(io.spine.internal.dependency.Protobuf.GradlePlugin.lib) {
            exclude(group = "com.google.guava")
        }
        @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
        classpath(io.spine.internal.dependency.ErrorProne.GradlePlugin.lib) {
            exclude(group = "com.google.guava")
        }
        classpath("io.spine.tools:spine-mc-java:$spineVersion")
    }
}

plugins {
    java
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id).version(version)
    }
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id) version version
    }
}

val baseRoot = "$rootDir/.."

allprojects {
    apply(from = "$baseRoot/version.gradle.kts")
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "project-report")

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {

    val commonPath = Scripts.commonPath
    apply {
        plugin(ErrorProne.GradlePlugin.id)
        plugin(Protobuf.GradlePlugin.id)
        plugin("io.spine.mc-java")
        plugin("idea")
        from("${baseRoot}/${commonPath}/test-output.gradle")
    }

    val spineVersion: String by extra

    /**
     * These dependencies are applied to all sub-projects and does not have to be included
     * explicitly.
     */
    dependencies {
        errorprone(ErrorProne.core)
        errorproneJavac(ErrorProne.javacPlugin)
        ErrorProne.annotations.forEach { compileOnly(it) }
        implementation("io.spine:spine-base:$spineVersion")
        testImplementation("io.spine.tools:spine-testlib:$spineVersion")
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }

    idea.module {
        generatedSourceDirs.addAll(files(
                "$projectDir/generated/main/java",
                "$projectDir/generated/main/spine",
                "$projectDir/generated/test/java",
                "$projectDir/generated/test/spine"
        ))
    }

    sourceSets {
        main {
            proto.srcDir("$projectDir/src/main/proto")
            java.srcDirs("$projectDir/generated/main/java",
                         "$projectDir/generated/main/spine",
                         "$projectDir/src/main/java")
            resources.srcDir("$projectDir/generated/main/resources")
        }

        test {
            proto.srcDir("$projectDir/src/test/proto")
            java.srcDirs("$projectDir/generated/test/java",
                         "$projectDir/generated/test/spine",
                         "$projectDir/src/test/java")
            resources.srcDir("$projectDir/generated/test/resources")
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }

        include("**/*Test.class")
    }

    //TODO:2021-07-22:alexander.yevsyukov: Turn to WARN and investigate duplicates.
    // see https://github.com/SpineEventEngine/base/issues/657
    tasks.processTestResources.get().duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val scriptsPath = Scripts.commonPath
apply(from = "${baseRoot}/${scriptsPath}/jacoco.gradle")
