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

import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import io.spine.gradle.internal.DependencyResolution
import io.spine.gradle.internal.Deps
import io.spine.gradle.internal.PublishingRepos
import io.spine.gradle.internal.RunBuild

buildscript {
    apply(from = "$rootDir/version.gradle.kts")

    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    val resolution = io.spine.gradle.internal.DependencyResolution
    resolution.defaultRepositories(repositories)
    resolution.forceConfiguration(configurations)
}

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `java-library`
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("com.google.protobuf").version(io.spine.gradle.internal.Deps.versions.protobufPlugin)
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("net.ltgt.errorprone").version(io.spine.gradle.internal.Deps.versions.errorPronePlugin)
}

apply(from = "$rootDir/version.gradle.kts")

extra.apply {
    this["groupId"] = "io.spine"
    this["publishToRepository"] = PublishingRepos.cloudRepo
    this["projectsToPublish"] = listOf(
            "base",
            "tool-base",
            "tools-api",
            "testlib",
            "mute-logging",
            "errorprone-checks",

            // Gradle plugins
            "plugin-base",
            "javadoc-filter",
            "javadoc-prettifier",
            "proto-dart-plugin",
            "proto-js-plugin",
            "model-compiler",

            "plugin-testlib",

            // Protoc compiler plugin
            "protoc-api",
            "validation-generator",
            "protoc-plugin"
    )
}

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
        from("$rootDir/config/gradle/dependencies.gradle")
    }
    version = rootProject.extra["versionToPublish"]!!
}

subprojects {
    buildscript {
        apply(from = "$rootDir/version.gradle.kts")

        DependencyResolution.defaultRepositories(repositories)
        dependencies {
            classpath(Deps.build.gradlePlugins.protobuf)
            classpath(Deps.build.gradlePlugins.errorProne)
        }
        DependencyResolution.forceConfiguration(configurations)
    }

    apply(from = "$rootDir/version.gradle.kts")

    val sourcesRootDir by extra("$projectDir/src")
    val generatedRootDir by extra("$projectDir/generated")
    val generatedJavaDir by extra("$generatedRootDir/main/java")
    val generatedTestJavaDir by extra("$generatedRootDir/test/java")
    val generatedSpineDir by extra("$generatedRootDir/main/spine")
    val generatedTestSpineDir by extra("$generatedRootDir/test/spine")

    apply {
        plugin("java-library")
        plugin("pmd")
        plugin("com.google.protobuf")
        plugin("net.ltgt.errorprone")
        plugin("maven-publish")
        from(Deps.scripts.projectLicenseReport(project))
        from(Deps.scripts.checkstyle(project))
    }

    the<JavaPluginExtension>().apply {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    DependencyResolution.defaultRepositories(repositories)

    /**
     * These dependencies are applied to all sub-projects and does not have to be included
     * explicitly.
     */
    dependencies {
        errorprone(Deps.build.errorProneCore)
        errorproneJavac(Deps.build.errorProneJavac)

        Deps.build.protobuf.forEach { api(it) }
        api(Deps.build.flogger)
        compileOnlyApi(Deps.build.checkerAnnotations)
        compileOnlyApi(Deps.build.jsr305Annotations)
        Deps.build.errorProneAnnotations.forEach { compileOnlyApi(it) }
        implementation(Deps.build.guava)
        runtimeOnly(Deps.runtime.flogger.systemBackend)

        testImplementation(Deps.test.guavaTestlib)
        testImplementation(Deps.test.junitPioneer)
        Deps.test.junit5Api.forEach { testImplementation(it) }
        Deps.test.truth.forEach { testImplementation(it) }
        testRuntimeOnly(Deps.test.junit5Runner)
    }

    DependencyResolution.forceConfiguration(configurations)
    DependencyResolution.excludeProtobufLite(configurations)

    sourceSets {
        main {
            java.srcDirs(generatedJavaDir, "$sourcesRootDir/main/java", generatedSpineDir)
            resources.srcDirs("$sourcesRootDir/main/resources", "$generatedRootDir/main/resources")
        }
        test {
            java.srcDirs(generatedTestJavaDir, "$sourcesRootDir/test/java", generatedTestSpineDir)
            resources.srcDirs("$sourcesRootDir/test/resources", "$generatedRootDir/test/resources")
        }
    }

    protobuf {
        generatedFilesBaseDir = generatedRootDir

        protoc {
            artifact = Deps.build.protoc
        }
    }

    tasks.test.configure {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        include("**/*Test.class")
    }

    apply {
        from(Deps.scripts.testOutput(project))
        from(Deps.scripts.javadocOptions(project))
        from(Deps.scripts.javacArgs(project))
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
        from(Deps.scripts.pmd(project))
        from(Deps.scripts.updateGitHubPages(project))
    }
    project.tasks["publish"].dependsOn("${project.path}:updateGitHubPages")
}

apply {
    from(Deps.scripts.jacoco(project))
    from(Deps.scripts.publish(project))
    from(Deps.scripts.generatePom(project))
    from(Deps.scripts.repoLicenseReport(project))
}

val smokeTests by tasks.registering(RunBuild::class) {
    directory = "$rootDir/tools/smoke-tests"
}

tasks.register("buildAll") {
    dependsOn(tasks.build, smokeTests)
}
