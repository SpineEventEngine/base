/*
 * Copyright 2023, TeamDev. All rights reserved.
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

import io.spine.dependency.lib.AutoService
import io.spine.dependency.lib.AutoServiceKsp
import io.spine.dependency.lib.Guava
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Logging
import io.spine.dependency.local.Reflect
import io.spine.dependency.local.TestLib
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.excludeGoogleProtoFromArtifacts
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator
import io.spine.gradle.standardToSpineSdk

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    module
    `compile-protobuf`
    `project-report`
    ksp
}
apply<IncrementGuard>()

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

dependencies {
    compileOnly(AutoService.annotations)
    ksp(AutoServiceKsp.processor)

    Protobuf.libs.forEach { api(it) }
    api(Guava.lib)
    
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
