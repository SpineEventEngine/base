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
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.PublishingRepos
import io.spine.internal.gradle.RunBuild
import io.spine.internal.gradle.Scripts
import io.spine.internal.gradle.spinePublishing
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.plugins.JavaPluginExtension

@Suppress("RemoveRedundantQualifierName") // Cannot use imported things here.
buildscript {
    apply(from = "$rootDir/version.gradle.kts")
    io.spine.internal.gradle.doApplyStandard(repositories)
    io.spine.internal.gradle.doForceVersions(configurations)
}

repositories {
    repositories.applyStandard()
}

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `java-library`
    kotlin("jvm") version io.spine.internal.dependency.Kotlin.version
    idea

    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id) version version
    }
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id) version version
    }
}

apply(from = "$rootDir/version.gradle.kts")

spinePublishing {
    targetRepositories.addAll(setOf(
            PublishingRepos.cloudRepo,
            PublishingRepos.gitHub("base")
    ))
    projectsToPublish.addAll(
        "base",
        "tool-base",
        "testlib",
        "mute-logging",
        "errorprone-checks",

        // Gradle plugins
        "plugin-base",
        "javadoc-filter",
        "javadoc-prettifier",
        "proto-dart-plugin",
        "proto-js-plugin",
        "mc-java",

        "plugin-testlib",

        // Protoc compiler plugin
        "mc-java-validation",
        "protoc-plugin"
    )
}

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
    }

    group = "io.spine"
    version = rootProject.extra["versionToPublish"]!!
}

subprojects {
    buildscript {
        apply(from = "$rootDir/version.gradle.kts")
        repositories.applyStandard()
        dependencies {
            classpath(Protobuf.GradlePlugin.lib)
            classpath(ErrorProne.GradlePlugin.lib)
        }
        configurations.forceVersions()
    }

    apply(from = "$rootDir/version.gradle.kts")

    val srcDir by extra("$projectDir/src")
    val generatedDir by extra("$projectDir/generated")
    val generatedJavaDir by extra("$generatedDir/main/java")
    val generatedTestJavaDir by extra("$generatedDir/test/java")
    val generatedSpineDir by extra("$generatedDir/main/spine")
    val generatedTestSpineDir by extra("$generatedDir/test/spine")

    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("com.google.protobuf")
        plugin("net.ltgt.errorprone")
        plugin("pmd")
        plugin("maven-publish")
    }

    apply {
        with(Scripts) {
            from(projectLicenseReport(project))
            from(checkstyle(project))
        }
    }

    val javaVersion = JavaVersion.VERSION_1_8

    the<JavaPluginExtension>().apply {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlin {
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            useIR = true
            freeCompilerArgs = listOf("-Xskip-prerelease-check")
        }
    }

    repositories.applyStandard()

    /**
     * These dependencies are applied to all sub-projects and do not have to
     * be included explicitly.
     *
     * We expose production code dependencies as API because they are used
     * by the framework parts that depend on `base`.
     */
    dependencies {
        errorprone(ErrorProne.core)
        errorproneJavac(ErrorProne.javacPlugin)

        Protobuf.libs.forEach { api(it) }
        api(Flogger.lib)
        api(Guava.lib)
        api(CheckerFramework.annotations)
        api(JavaX.annotations)
        ErrorProne.annotations.forEach { api(it) }
        api(kotlin("stdlib-jdk8"))

        testImplementation(Guava.testLib)
        testImplementation(JUnit.runner)
        testImplementation(JUnit.pioneer)
        JUnit.api.forEach { testImplementation(it) }

        runtimeOnly(Flogger.Runtime.systemBackend)
    }

    configurations.forceVersions()
    configurations.excludeProtobufLite()

    sourceSets {
        main {
            java.srcDirs(generatedJavaDir, "$srcDir/main/java", generatedSpineDir)
            resources.srcDirs("$srcDir/main/resources", "$generatedDir/main/resources")
        }
        test {
            java.srcDirs(generatedTestJavaDir, "$srcDir/test/java", generatedTestSpineDir)
            resources.srcDirs("$srcDir/test/resources", "$generatedDir/test/resources")
        }
    }

    protobuf {
        generatedFilesBaseDir = generatedDir
        protoc {
            artifact = Protobuf.compiler
        }
    }

    tasks.test.configure {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        include("**/*Test.class")
    }

    apply {
        with(Scripts) {
            from(testOutput(project))
            from(javadocOptions(project))
            from(javacArgs(project))
        }
    }

    tasks.create("sourceJar", Jar::class) {
        from(sourceSets["main"].allJava)
        archiveClassifier.set("sources")
    }

    tasks.create("testOutputJar", Jar::class) {
        from(sourceSets["test"].output)
        archiveClassifier.set("test")
    }

    tasks.register("javadocJar", Jar::class) {
        from("$projectDir/build/docs/javadoc")
        archiveClassifier.set("javadoc")
        dependsOn("javadoc")
    }

    idea {
        module {
            generatedSourceDirs.add(project.file(generatedJavaDir))
            testSourceDirs.add(project.file(generatedTestJavaDir))
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    val cleanGenerated by tasks.registering(Delete::class) {
        delete("$projectDir/generated")
    }

    tasks.clean.configure {
        dependsOn(cleanGenerated)
    }

    ext["allowInternalJavadoc"] = true
    apply {
        with(Scripts) {
            from(pmd(project))
            from(updateGitHubPages(project))
        }
    }
    project.tasks["publish"].dependsOn("${project.path}:updateGitHubPages")
}

apply {
    with(Scripts) {
        // Aggregated coverage report across all subprojects.
        from(jacoco(project))
        // Generate a repository-wide report of 3rd-party dependencies and their licenses.
        from(repoLicenseReport(project))
        // Generate a `pom.xml` file containing first-level dependency of all projects
        // in the repository.
        from(generatePom(project))
    }
}

val tests by tasks.registering(RunBuild::class) {
    directory = "$rootDir/tests"
}

tasks.register("buildAll") {
    dependsOn(tasks.build, tests)
}
