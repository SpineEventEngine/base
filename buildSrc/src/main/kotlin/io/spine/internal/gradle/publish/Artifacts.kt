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

package io.spine.internal.gradle.publish

import io.spine.internal.gradle.sourceSets
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Enumerates [Jar] tasks, which produce publishable Maven artifacts.
 */
internal class Artifacts {

    /**
     * Sources from `main` source set.
     *
     * Includes:
     *
     *  - Kotlin
     *  - Java
     *  - Proto
     */
    fun Project.sourcesJar() = tasks.getOrCreate("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource) // puts Java and Kotlin sources
        from(protoSources()) // puts Proto sources.
    }

    /**
     * Only Proto sources from `main` source set.
     */
    fun Project.protoJar() = tasks.getOrCreate("protoJar") {
        archiveClassifier.set("proto")
        from(protoSources())
    }

    /**
     * Compilation output of `test` source set.
     */
    fun Project.testOutputJar() = tasks.getOrCreate("testOutputJar") {
        archiveClassifier.set("test")
        from(sourceSets["test"].output)
    }

    /**
     * Javadoc, generated upon Java and Kotlin sources from `main` source set.
     */
    fun Project.javadocJar() = tasks.getOrCreate("javadocJar") {
        archiveClassifier.set("javadoc")
        from(files("$buildDir/docs/javadoc"))
        dependsOn("javadoc")
    }

    // Try-catch block is used here because Gradle still does not provide API for checking a task
    // presence without triggering its creation.
    // See: https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
    private fun TaskContainer.getOrCreate(name: String, init: Jar.() -> Unit): TaskProvider<Jar> =
        try {
            named<Jar>(name)
        } catch (e: UnknownTaskException) {
            register<Jar>(name) {
                init()
            }
        }
}
