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

import io.spine.internal.dependency.AutoService
import io.spine.internal.dependency.Kotlin
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Spine
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.excludeGoogleProtoFromArtifacts
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.standardToSpineSdk

buildscript {
    standardSpineSdkRepositories()
    doForceVersions(configurations)
}

repositories.standardToSpineSdk()

// Apply some plugins to make type-safe extension accessors available in this script file.
plugins {
    `compile-protobuf`
    `jvm-module`
    idea
    `gradle-doctor`
    `project-report`
}
apply<IncrementGuard>()

spinePublishing {
    destinations = with(PublishingRepos) {
        setOf(
            cloudRepo,
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
    annotationProcessor(AutoService.processor)
    compileOnly(AutoService.annotations)

    implementation(Spine.Logging.lib)
    implementation(Spine.reflect)
    implementation(Kotlin.reflect)

    /* Have `protobuf` dependency instead of `api` or `implementation` so that proto
       files from the library are included in the compilation. We need this because we
       build our descriptor set files using those standard proto files too.

       See Protobuf Gradle Plugin documentation for details:
           https://github.com/google/protobuf-gradle-plugin#protos-in-dependencies
    */
    protobuf(Protobuf.protoSrcLib)

    testImplementation(Spine.testlib)
    testImplementation(Spine.Logging.smokeTest)
}

tasks {
    excludeGoogleProtoFromArtifacts()
}

LicenseReporter.mergeAllReports(project)
PomGenerator.applyTo(project)
