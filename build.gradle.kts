/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import com.google.devtools.ksp.KspExperimental
import io.spine.dependency.build.CheckerFramework
import io.spine.dependency.build.Dokka
import io.spine.dependency.build.ErrorProne
import io.spine.dependency.lib.AutoService
import io.spine.dependency.lib.AutoServiceKsp
import io.spine.dependency.lib.Guava
import io.spine.dependency.lib.JavaX
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Reflect
import io.spine.dependency.local.TestLib
import io.spine.dependency.test.JUnit
import io.spine.dependency.test.Jacoco
import io.spine.dependency.test.Kotest
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.github.pages.updateGitHubPages
import io.spine.gradle.javac.configureErrorProne
import io.spine.gradle.javac.configureJavac
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.excludeGoogleProtoFromArtifacts
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator
import io.spine.gradle.standardToSpineSdk
import io.spine.gradle.testing.registerTestTasks
import BuildSettings.javaVersion
import io.spine.gradle.kotlin.applyJvmToolchain
import io.spine.gradle.kotlin.setFreeCompilerArgs
import io.spine.gradle.testing.configureLogging

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `java-library`
    kotlin("jvm")
    `compile-protobuf`
    ksp
    id("net.ltgt.errorprone")
    idea
    id("pmd-settings")
    id("project-report")
    id("dokka-for-java")
    id("io.kotest")
    id("org.jetbrains.kotlinx.kover")
    id("detekt-code-analysis")
    id("dokka-for-kotlin")
    `project-report`
}
apply<IncrementGuard>()

LicenseReporter.generateReportIn(project)
JavadocConfig.applyTo(project)
CheckStyleConfig.applyTo(project)

ksp {
    @OptIn(KspExperimental::class)
    useKsp2.set(true)
}

spinePublishing {
    destinations = with(PublishingRepos) {
        setOf(
            cloudArtifactRegistry,
            gitHub("base")
        )
    }
    dokkaJar {
        java = true
    }
}

apply(from = "$rootDir/version.gradle.kts")
group = "io.spine"
version = rootProject.extra["versionToPublish"]!!
repositories.standardToSpineSdk()
configurations.forceVersions()

project.run {
    configureJava(javaVersion)
    configureKotlin(javaVersion)
    addDependencies()
    forceConfigurations()

    val generatedDir = "$projectDir/generated"
    setTaskDependencies(generatedDir)
    setupTests()

    configureGitHubPages()
}

typealias Module = Project

fun Module.configureJava(javaVersion: JavaLanguageVersion) {
    java {
        toolchain.languageVersion.set(javaVersion)
    }

    tasks {
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }
}

fun Module.configureKotlin(javaVersion: JavaLanguageVersion) {
    kotlin {
        applyJvmToolchain(javaVersion.asInt())
        explicitApi()
        compilerOptions {
            jvmTarget.set(BuildSettings.jvmTarget)
            setFreeCompilerArgs()
        }
    }

    kover {
        useJacoco(version = Jacoco.version)
    }

    koverReport {
        defaults {
            xml {
                onCheck = true
            }
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
fun Module.addDependencies() = dependencies {
    errorprone(ErrorProne.core)

    Protobuf.libs.forEach { api(it) }
    api(Guava.lib)

    compileOnlyApi(CheckerFramework.annotations)
    compileOnlyApi(JavaX.annotations)
    ErrorProne.annotations.forEach { compileOnlyApi(it) }

    implementation(Logging.lib)

    testImplementation(Guava.testLib)
    testImplementation(JUnit.runner)
    testImplementation(JUnit.pioneer)
    JUnit.api.forEach { testImplementation(it) }

    testImplementation(TestLib.lib)
    testImplementation(Kotest.frameworkEngine)
    testImplementation(Kotest.datatest)
    testImplementation(Kotest.runnerJUnit5Jvm)
    testImplementation(JUnit.runner)
}

fun Module.forceConfigurations() {
    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                force(
                    JUnit.bom,
                    JUnit.runner,
                    Dokka.BasePlugin.lib,
                    Reflect.lib,
                )
            }
        }
    }
}

fun Module.setupTests() {
    tasks {
        registerTestTasks()
        test.configure {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            configureLogging()
        }
    }
}

fun Module.setTaskDependencies(generatedDir: String) {
    tasks {
        val cleanGenerated by registering(Delete::class) {
            delete(generatedDir)
        }
        clean.configure {
            dependsOn(cleanGenerated)
        }

        project.afterEvaluate {
            val publish = tasks.findByName("publish")
            publish?.dependsOn("${project.path}:updateGitHubPages")
        }
    }
    configureTaskDependencies()
}

fun Module.configureGitHubPages() {
    val docletVersion = project.version.toString()
    updateGitHubPages(docletVersion) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }
}

dependencies {
    compileOnly(AutoService.annotations)
    ksp(AutoServiceKsp.processor)

    implementation(Logging.lib)
    implementation(Reflect.lib)
    implementation(Kotlin.reflect)

    /* Have `protobuf` dependency instead of `api` or `implementation` so that proto
       files from the library are included in the compilation. We need this because we
       build our descriptor set files using those standard proto files too.

       See Protobuf Gradle Plugin documentation for details:
           https://github.com/google/protobuf-gradle-plugin#protos-in-dependencies
    */
    protobuf(Protobuf.protoSrcLib)

    testImplementation(TestLib.lib)
    testImplementation(Logging.smokeTest)
    testImplementation(Logging.testLib)?.because("We need `tapConsole`.")
}

configurations.all {
    resolutionStrategy {
        force(Reflect.lib)
        force(Logging.lib)
        force(Logging.libJvm)
    }
}

tasks {
    excludeGoogleProtoFromArtifacts()
}

LicenseReporter.mergeAllReports(project)
PomGenerator.applyTo(project)
