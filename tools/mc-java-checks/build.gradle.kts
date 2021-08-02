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

import io.spine.internal.dependency.AutoService
import io.spine.internal.dependency.ErrorProne
import java.net.URI

group = "io.spine.tools"

repositories {
    maven { url = URI(io.spine.internal.gradle.Repos.sonatypeSnapshots) }
    mavenLocal()
    mavenCentral()
}

dependencies {
    annotationProcessor(AutoService.processor)
    compileOnlyApi(AutoService.annotations)
    implementation(project(":base"))
    implementation(project(":plugin-base"))
    api(ErrorProne.core)
    ErrorProne.annotations.forEach { api(it) }
    testImplementation(ErrorProne.testHelpers)
}

fun getResolvedArtifactFor(dependency: String): String {
    val resolvedTestClasspath = configurations.testRuntimeClasspath.get().resolvedConfiguration
    val javacDependency = resolvedTestClasspath.resolvedArtifacts.filter {
        it.name == dependency
    }
    if (javacDependency.isEmpty()) {
        throw MissingResourceException(
            "The `javac` dependency was not found among the resolved artifacts.")
    }
    return javacDependency[0].file.absolutePath
}

val test: Test = tasks.test.get()
afterEvaluate {
    val javacPath = getResolvedArtifactFor("javac")
    test.jvmArgs("-Xbootclasspath/p:$javacPath")
}

//TODO:2021-07-22:alexander.yevsyukov: Turn to WARN and investigate duplicates.
// see https://github.com/SpineEventEngine/base/issues/657
val dupStrategy = DuplicatesStrategy.INCLUDE
tasks.processTestResources.get().duplicatesStrategy = dupStrategy
tasks.sourceJar.get().duplicatesStrategy = dupStrategy
