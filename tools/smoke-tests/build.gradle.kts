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

import io.spine.gradle.internal.DependencyResolution
import io.spine.gradle.internal.Deps

buildscript {

    val baseRoot = "$rootDir/../.."
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
        classpath(deps.build.guava)
        classpath(deps.build.gradlePlugins.protobuf) {
            exclude(group = "com.google.guava")
        }
        classpath(deps.build.gradlePlugins.errorProne) {
            exclude(group = "com.google.guava")
        }
        classpath("io.spine.tools:spine-model-compiler:$spineVersion")
    }
}

plugins {
    java
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("com.google.protobuf").version(io.spine.gradle.internal.Deps.versions.protobufPlugin)
}

val baseRoot = "$rootDir/../.."

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
        plugin("com.google.protobuf")
        plugin("io.spine.tools.spine-model-compiler")
        plugin("idea")
        from("$baseRoot/config/gradle/test-output.gradle")
        from("$baseRoot/config/gradle/model-compiler.gradle")
    }

    val spineVersion: String by extra

    dependencies {
        implementation(Deps.build.errorProneCore)
        Deps.build.errorProneAnnotations.forEach { implementation(it) }
        implementation("io.spine:spine-base:$spineVersion")
        testImplementation("io.spine:spine-testlib:$spineVersion")
        testRuntimeOnly(Deps.test.junit5Runner)
    }

    DependencyResolution.forceConfiguration(configurations)

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
