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

@Suppress("RemoveRedundantQualifierName") // Cannot use imported things here.
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

apply(from = "$rootDir/version.gradle.kts")

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

    group = "io.spine"
    version = rootProject.extra["versionToPublish"]!!

    repositories.standardToSpineSdk()
}

subprojects {
    buildscript {
        repositories.standardToSpineSdk()
        dependencies {
            classpath(Protobuf.GradlePlugin.lib)
        }
        configurations.forceVersions()
    }

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

    val docletVersion = project.version.toString()
    updateGitHubPages(docletVersion) {
        allowInternalJavadoc.set(true)
        rootFolder.set(rootDir)
    }

    val javaVersion = JavaVersion.VERSION_11.toString()
    kotlin {
        applyJvmToolchain(javaVersion)
        explicitApi()
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            kotlinOptions.jvmTarget = javaVersion
            setFreeCompilerArgs()
        }
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
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
                force(
                    JUnit.bom,
                    JUnit.runner,
                    Dokka.BasePlugin.lib
                )
            }
        }
    }

    val generatedDir by extra("$projectDir/generated")
    val generatedJavaDir by extra("$generatedDir/main/java")
    val generatedKotlinDir by extra("$generatedDir/main/kotlin")
    val generatedTestJavaDir by extra("$generatedDir/test/java")
    val generatedTestKotlinDir by extra("$generatedDir/test/kotlin")

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

    protobuf {
        configurations.excludeProtobufLite()
        generatedFilesBaseDir = generatedDir
        protoc {
            artifact = Protobuf.compiler
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

    tasks {
        registerTestTasks()
        test.configure {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
        }

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

    project.configureTaskDependencies()
}

JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)
