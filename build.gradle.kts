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

@file:Suppress("RemoveRedundantQualifierName") // Cannot use imports in some places.

import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.Flogger
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.JavadocConfig
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.kotlin.applyJvmToolchain
import io.spine.internal.gradle.kotlin.setFreeCompilerArgs
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.publish.spinePublishing2
import io.spine.internal.gradle.report.coverage.JacocoConfig
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.testing.registerTestTasks
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("RemoveRedundantQualifierName") // Cannot use imported things here.
buildscript {
    apply(from = "$rootDir/version.gradle.kts")
    io.spine.internal.gradle.doApplyStandard(repositories)
    io.spine.internal.gradle.doForceVersions(configurations)
}

repositories.applyStandard()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `java-library`
    kotlin("jvm")
    idea

    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id)
    }
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id)
    }
    `force-jacoco`
}

apply(from = "$rootDir/version.gradle.kts")

// An example of publishing a single module.
// Configuration is done in a module's build file.
//spinePublishing2 {
//    destinations = spineRepositories {
//        setOf(
//            cloudRepo,
//            cloudArtifactRegistry,
//            gitHub("base")
//        )
//    }
//    protoJar {
//        disabled = true
//    }
//}

// An example of publishing several modules.
// Configuration is done in a root project's build file.
spinePublishing2 {
    modules = setOf(
        "base",
        "testlib"
    )
    destinations = spineRepositories {
        setOf(
            cloudRepo,
            cloudArtifactRegistry,
            gitHub("base")
        )
    }
//    protoJar {
//        exclusions = setOf(
//            "base"
//        )
//    }
}

// Original extension.
//spinePublishing {
//    with(PublishingRepos) {
//        targetRepositories.addAll(
//            cloudRepo,
//            cloudArtifactRegistry,
//            gitHub("base")
//        )
//    }
//    projectsToPublish.addAll(subprojects.map { it.path })
//}

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
    }

    group = "io.spine"
    version = rootProject.extra["versionToPublish"]!!

    repositories.applyStandard()
}

private object PublishingTask {
    const val publish = "publish"
    const val publishToMavenLocal = "publishToMavenLocal"
}

subprojects {
    buildscript {
        apply(from = "$rootDir/version.gradle.kts")
        repositories.applyStandard()
        dependencies {
            classpath(Protobuf.GradlePlugin.lib)
        }
        configurations.forceVersions()
    }

    apply(from = "$rootDir/version.gradle.kts")

    // Apply standard plugins.
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("com.google.protobuf")
        plugin("net.ltgt.errorprone")
        plugin("pmd")
        plugin("maven-publish")
    }

    // Apply custom Kotlin script plugins.
    apply {
        plugin("pmd-settings")
    }

    CheckStyleConfig.applyTo(project)
    JavadocConfig.applyTo(project)
    LicenseReporter.generateReportIn(project)

    val docletVersion = project.version.toString()
    updateGitHubPages(docletVersion) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }

    val javaVersion = 11
    kotlin {
        applyJvmToolchain(javaVersion)
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        setFreeCompilerArgs()
    }

    tasks.withType<JavaCompile> {
        configureJavac()
        configureErrorProne()
    }

    kotlin {
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs = listOf("-Xskip-prerelease-check")
        }
    }

    /**
     * These dependencies are applied to all subprojects and do not have to
     * be included explicitly.
     *
     * We expose production code dependencies as API because they are used
     * by the framework parts that depend on `base`.
     */
    dependencies {
        errorprone(ErrorProne.core)

        Protobuf.libs.forEach { api(it) }
        api(Flogger.lib)
        api(Guava.lib)

        compileOnlyApi(CheckerFramework.annotations)
        compileOnlyApi(JavaX.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }

        testImplementation(Guava.testLib)
        testImplementation(JUnit.runner)
        testImplementation(JUnit.pioneer)
        JUnit.api.forEach { testImplementation(it) }

        runtimeOnly(Flogger.Runtime.systemBackend)
    }

    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                force("org.junit:junit-bom:${JUnit.version}")
            }
        }
    }

    val generatedDir by extra("$projectDir/generated")
    val generatedJavaDir by extra("$generatedDir/main/java")
    val generatedTestJavaDir by extra("$generatedDir/test/java")

    sourceSets {
        main {
            resources.srcDirs("$generatedDir/main/resources")
        }
        test {
            resources.srcDirs("$generatedDir/test/resources")
        }
    }

    protobuf {
        generatedFilesBaseDir = generatedDir
        protoc {
            artifact = Protobuf.compiler
        }
    }

    idea {
        module {
            generatedSourceDirs.add(project.file(generatedJavaDir))
            testSourceDirs.add(project.file(generatedTestJavaDir))
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    tasks {
        registerTestTasks()
        test {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
        }
    }

    val cleanGenerated by tasks.registering(Delete::class) {
        delete(generatedDir)
    }

    tasks.clean.configure {
        dependsOn(cleanGenerated)
    }

    project.tasks[PublishingTask.publish].dependsOn("${project.path}:updateGitHubPages")
}

JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)
