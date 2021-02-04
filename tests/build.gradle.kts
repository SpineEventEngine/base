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

buildscript {
    val baseRoot = "$rootDir/../"
    val versionGradle = "$baseRoot/version.gradle.kts"

    apply(from = versionGradle)
    apply(from = "$baseRoot/config/gradle/dependencies.gradle")

    repositories {
        gradlePluginPortal()
        jcenter()
        mavenLocal()
    }

    val spineVersion: String by extra

    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    val deps = io.spine.gradle.internal.Deps
    dependencies {
        deps.build.apply {
            classpath(guava.lib)
            classpath(gradlePlugins.protobuf) {
                exclude(group = "com.google.guava")
            }
            classpath(gradlePlugins.errorProne) {
                exclude(group = "com.google.guava")
            }
        }
        classpath("io.spine.tools:spine-model-compiler:$spineVersion")
    }
}

plugins {
    java
    kotlin("jvm") version io.spine.gradle.internal.Kotlin.version
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("com.google.protobuf").version(io.spine.gradle.internal.Protobuf.gradlePluginVersion)
}

val baseRoot = "$rootDir/../"

allprojects {
    apply(from = "$baseRoot/version.gradle.kts")
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "project-report")

    repositories {
        mavenLocal()
        jcenter()
    }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("com.google.protobuf")
        plugin("kotlin")
        plugin("io.spine.tools.spine-model-compiler")
        plugin("idea")
        from("$baseRoot/config/gradle/test-output.gradle")
        from("$baseRoot/config/gradle/model-compiler.gradle")
    }

    the<JavaPluginExtension>().apply {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    val spineVersion: String by extra
    @Suppress("RemoveRedundantQualifierName")
    // Similarly to `buildscript`, instead of import.
    val deps = io.spine.gradle.internal.Deps

    /**
     * These dependencies are applied to all sub-projects and does not have to be included
     * explicitly.
     */
    dependencies {
        deps.build.errorProne.annotations.forEach { compileOnly(it) }
        implementation("io.spine:spine-base:$spineVersion")
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("io.spine:spine-testlib:$spineVersion")
        deps.test.truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(deps.test.junit.runner)
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
}

apply(from = "$baseRoot/config/gradle/jacoco.gradle")
