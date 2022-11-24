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

import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.Dokka
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.Flogger
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.kotlin.applyJvmToolchain
import io.spine.internal.gradle.kotlin.setFreeCompilerArgs
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.coverage.JacocoConfig
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.standardToSpineSdk
import io.spine.internal.gradle.testing.registerTestTasks
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    standardSpineSdkRepositories()
    io.spine.internal.gradle.doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `java-library`
    kotlin("jvm")
    idea
    protobuf
    errorprone
    `gradle-doctor`
}


spinePublishing {
    modules = setOf(
        "base",
        "testlib"
    )
    destinations = with(PublishingRepos) {
        setOf(
            cloudRepo,
            cloudArtifactRegistry,
            gitHub("base")
        )
    }
    dokkaJar {
        enabled = true
    }
}

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
    }
    apply(from = "$rootDir/version.gradle.kts")

    group = "io.spine"
    version = rootProject.extra["versionToPublish"]!!

    repositories.standardToSpineSdk()
}

object BuildSettings {
    private const val JVM_VERSION = 11
    val javaVersion = JavaLanguageVersion.of(JVM_VERSION)
}

subprojects {
    applyPlugins()
    configureJava(BuildSettings.javaVersion)
    configureKotlin(BuildSettings.javaVersion)
    addDependencies()
    forceConfigurations()

    val generatedDir = "$projectDir/generated"
    applyGeneratedDir(generatedDir)
    configureProtobuf(generatedDir)
    setTaskDependencies(generatedDir)
    setupTests()

    configureGitHubPages()
}

JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)

typealias Subproject = Project

fun Subproject.applyPlugins() {
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
        plugin("dokka-for-java")
    }

    CheckStyleConfig.applyTo(project)
    JavadocConfig.applyTo(project)
    LicenseReporter.generateReportIn(project)
}

fun Subproject.configureJava(javaVersion: JavaLanguageVersion) {
    java {
        toolchain {
            version = javaVersion
        }
    }

    tasks {
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }
}

fun Subproject.configureKotlin(javaVersion: JavaLanguageVersion) {
    kotlin {
        applyJvmToolchain(javaVersion.asInt())
        explicitApi()
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            kotlinOptions.jvmTarget = javaVersion.toString()
            setFreeCompilerArgs()
        }
    }
}

/**
 * These dependencies are applied to all subprojects and do not have to
 * be included explicitly.
 *
 * We expose production code dependencies as API because they are used
 * by the framework parts that depend on `base`.
 */
fun Subproject.addDependencies() = dependencies {
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

fun Subproject.forceConfigurations() {
    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                force(
                    JUnit.bom,
                    JUnit.runner,
                    Dokka.BasePlugin.lib
                )
            }
        }
    }
}

fun Subproject.applyGeneratedDir(generatedDir: String) {
    val generatedJavaDir = "$generatedDir/main/java"
    val generatedKotlinDir = "$generatedDir/main/kotlin"
    val generatedTestJavaDir = "$generatedDir/test/java"
    val generatedTestKotlinDir = "$generatedDir/test/kotlin"

    sourceSets {
        main {
            java.srcDir(generatedKotlinDir)
            resources.srcDirs(
                "$generatedDir/main/resources",
                "$buildDir/descriptors/main"
            )
        }
        test {
            java.srcDir(generatedTestJavaDir)
            resources.srcDirs(
                "$generatedDir/test/resources",
                "$buildDir/descriptors/test"
            )
        }
    }

    idea {
        module {
            generatedSourceDirs.add(file(generatedJavaDir))
            generatedSourceDirs.add(file(generatedKotlinDir))
            testSources.from(
                project.file(generatedTestJavaDir),
                project.file(generatedTestKotlinDir)
            )
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}

fun Subproject.configureProtobuf(generatedDir: String) {
    protobuf {
        configurations.excludeProtobufLite()
        generatedFilesBaseDir = generatedDir
        protoc {
            artifact = Protobuf.compiler
        }
    }
}

fun Subproject.setupTests() {
    tasks {
        registerTestTasks()
        test.configure {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
        }
    }
}

fun Subproject.setTaskDependencies(generatedDir: String) {
    tasks {
        val cleanGenerated by registering(Delete::class) {
            delete(generatedDir)
        }
        clean.configure {
            dependsOn(cleanGenerated)
        }

        named("publish") {
            dependsOn("${project.path}:updateGitHubPages")
        }
    }

    configureTaskDependencies()
}

fun Subproject.configureGitHubPages() {
    val docletVersion = project.version.toString()
    updateGitHubPages(docletVersion) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}
