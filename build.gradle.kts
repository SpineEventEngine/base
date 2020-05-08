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
import io.spine.gradle.internal.PublishingRepos
import org.gradle.internal.os.OperatingSystem

buildscript {
    apply(from = "$rootDir/version.gradle.kts")

    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    val resolution = io.spine.gradle.internal.DependencyResolution
    @Suppress("RemoveRedundantQualifierName")
    val deps = io.spine.gradle.internal.Deps
    resolution.defaultRepositories(repositories)
    dependencies {
        classpath(deps.build.gradlePlugins.protobuf)
        classpath(deps.build.gradlePlugins.errorProne)
    }
    resolution.forceConfiguration(configurations)
}

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    java
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("com.google.protobuf").version(io.spine.gradle.internal.Deps.versions.protobufPlugin)
}

apply(from = "$rootDir/version.gradle.kts")

extra.apply {
    this["groupId"] = "io.spine"
    this["publishToRepository"] = PublishingRepos.cloudRepo
    this["projectsToPublish"] = listOf(
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

    val sourcesRootDir by extra("$projectDir/src")
    val generatedRootDir by extra("$projectDir/generated")
    val generatedJavaDir by extra("$generatedRootDir/main/java")
    val generatedTestJavaDir by extra("$generatedRootDir/test/java")
    val generatedSpineDir by extra("$generatedRootDir/main/spine")
    val generatedTestSpineDir by extra("$generatedRootDir/test/spine")

    // TODO: Move to the usage site.
    extra["runsOnWindows"] = OperatingSystem.current().isWindows

    apply {
        plugin("java-library")
        plugin("pmd")
        plugin("com.google.protobuf")
        plugin("net.ltgt.errorprone")
        plugin("maven-publish")
        from(Deps.scripts.projectLicenseReport(project))
    }

    the<JavaPluginExtension>().apply {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    DependencyResolution.defaultRepositories(repositories)
    dependencies {
        "errorprone"(Deps.build.errorProneCore)
        "errorproneJavac"(Deps.build.errorProneJavac)
        Deps.build.protobuf.forEach { "api"(it) }
        "api"(Deps.build.flogger)
        "implementation"(Deps.build.guava)
        "implementation"(Deps.build.checkerAnnotations)
        "implementation"(Deps.build.jsr305Annotations)
        Deps.build.errorProneAnnotations.forEach { "implementation"(it) }
        "testImplementation"(Deps.test.guavaTestlib)
        "testImplementation"(Deps.test.junit5Runner)
        "testImplementation"(Deps.test.junitPioneer)
        Deps.test.junit5Api.forEach { "testImplementation"(it) }
        "runtimeOnly"(Deps.runtime.floggerSystemBackend)
    }

    DependencyResolution.forceConfiguration(configurations)
    configurations {
        named("runtime").get().exclude(group = "com.google.protobuf", module = "protobuf-lite")
        named("testRuntime").get().exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }

    java {
        sourceSets["main"].apply {
            java.srcDirs(generatedJavaDir, "$sourcesRootDir/main/java", generatedSpineDir)
            resources.srcDirs("$sourcesRootDir/main/resources", "$generatedRootDir/main/resources")
        }
        sourceSets["test"].apply {
            java.srcDirs(generatedTestJavaDir, "$sourcesRootDir/test/java", generatedTestSpineDir)
            resources.srcDirs("$sourcesRootDir/test/resources", "$generatedRootDir/test/resources")
        }
    }

    protobuf {
        protobuf.generatedFilesBaseDir = generatedRootDir

        protobuf.protoc(object : groovy.lang.Closure<Any>(this) {
            private fun doCall(locator: com.google.protobuf.gradle.ExecutableLocator) {
                locator.artifact = Deps.build.protoc
            }
        })
    }

    (tasks["test"] as Test).apply {
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

    tasks["clean"].dependsOn(cleanGenerated)

    apply(from = Deps.scripts.pmd(project))
}

apply {
    from(Deps.scripts.jacoco(project))
    from(Deps.scripts.publish(project))
    from(Deps.scripts.runBuild(project))
    from(Deps.scripts.generatePom(project))
    from(Deps.scripts.repoLicenseReport(project))
}

val smokeTests = "smokeTests"

tasks.register(smokeTests) {
    doLast {
        val runBuild: (String) -> Unit by extra
        runBuild("$rootDir/tools/smoke-tests")
    }
}

tasks.register("buildAll") {
    dependsOn("build", smokeTests)
}
