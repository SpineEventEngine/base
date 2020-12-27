/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    apply(from = "$rootDir/version.gradle.kts")

    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.gradle.internal.DependencyResolution.apply {
        defaultRepositories(repositories)
        forceConfiguration(configurations)
    }

    val kotlinVersion: String by extra
    configurations.all {
        resolutionStrategy {
            force(
                "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
                "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"
            )
        }
    }
}

// Apply some plugins to make type-safe extension accessors available in this script file.
@Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
plugins {
    `java-library`
    kotlin("jvm") version "1.4.21"
    idea
    io.spine.gradle.internal.Deps.versions.apply {
        id("com.google.protobuf") version protobufPlugin
        id("net.ltgt.errorprone") version errorPronePlugin
    }
}

apply(from = "$rootDir/version.gradle.kts")
val kotlinVersion: String by extra

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

    val srcDir by extra("$projectDir/src")
    val generatedDir by extra("$projectDir/generated")
    val generatedJavaDir by extra("$generatedDir/main/java")
    val generatedTestJavaDir by extra("$generatedDir/test/java")
    val generatedSpineDir by extra("$generatedDir/main/spine")
    val generatedTestSpineDir by extra("$generatedDir/test/spine")

    apply {
        plugin("java-library")
        plugin("kotlin")
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

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    DependencyResolution.defaultRepositories(repositories)

    /**
     * These dependencies are applied to all sub-projects and do not have to
     * be included explicitly.
     */
    dependencies {
        Deps.build.apply {
            errorprone(errorProneCore)
            errorproneJavac(errorProneJavac)

            protobuf.forEach { api(it) }
            api(flogger)
            implementation(guava)
            implementation(checkerAnnotations)
            implementation(jsr305Annotations)
            errorProneAnnotations.forEach { implementation(it) }
        }
        Deps.test.apply {
            testImplementation(guavaTestlib)
            testImplementation(junit5Runner)
            testImplementation(junitPioneer)
            junit5Api.forEach { testImplementation(it) }
        }
        runtimeOnly(Deps.runtime.flogger.systemBackend)
    }

    DependencyResolution.apply {
        forceConfiguration(configurations)
        excludeProtobufLite(configurations)
    }
    configurations {
        all {
            resolutionStrategy {
                force(
                    "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
                    "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"
                )
            }
        }
    }

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
        with(Deps.scripts) {
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
        with(Deps.scripts) {
            from(pmd(project))
            from(updateGitHubPages(project))
        }
    }
    project.tasks["publish"].dependsOn("${project.path}:updateGitHubPages")
}

apply {
    with(Deps.scripts) {
        from(jacoco(project))
        from(publish(project))
        from(generatePom(project))
        from(repoLicenseReport(project))
    }
}

val smokeTests by tasks.registering(RunBuild::class) {
    directory = "$rootDir/tools/smoke-tests"
}

tasks.register("buildAll") {
    dependsOn(tasks.build, smokeTests)
}
