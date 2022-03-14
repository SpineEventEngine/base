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
 * Locates or creates `sourcesJar` task in this [Project].
 *
 * The output of this task is a `jar` archive. The archive contains sources from `main` source set.
 * The task makes sure that sources from the directories below will be included into
 * a resulted archive:
 *
 *  - Kotlin
 *  - Java
 *  - Proto
 *
 * Java and Kotlin sources are default to `main` source set since it is created by `java` plugin.
 * Thus, we need a [special treatment][protoSources] for Proto sources to be included.
 */
fun Project.sourcesJar() = tasks.getOrCreate("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource) // Puts Java and Kotlin sources.
    from(protoSources()) // Puts Proto sources.
}

/**
 * Locates or creates `protoJar` task in this [Project].
 *
 * The output of this task is a `jar` archive. The archive contains only
 * [Proto sources][protoSources] from `main` source set.
 */
fun Project.protoJar() = tasks.getOrCreate("protoJar") {
    archiveClassifier.set("proto")
    from(protoSources())
}

/**
 * Locates or creates `testOutputJar` task in this [Project].
 *
 * The output of this task is a `jar` archive. The archive contains compilation output
 * of `test` source set.
 */
fun Project.testOutputJar() = tasks.getOrCreate("testOutputJar") {
    archiveClassifier.set("test")
    from(sourceSets["test"].output)
}

/**
 * Locates or creates `javadocJar` task in this [Project].
 *
 * The output of this task is `jar` archive. The archive contains Javadoc,
 * generated upon Java and Kotlin sources from `main` source set.
 */
fun Project.javadocJar() = tasks.getOrCreate("javadocJar") {
    archiveClassifier.set("javadoc")
    from(files("$buildDir/docs/javadoc"))
    dependsOn("javadoc")
}

/**
 * Locates a task in this [TaskContainer] by the given name.
 *
 * If the task is not present in the container, creates a new one using the given initializer.
 *
 * Try-catch block is used here because Gradle still does not provide API for checking a task
 * presence without triggering its creation.
 *
 * See: [Task Configuration Avoidance](https://docs.gradle.org/current/userguide/task_configuration_avoidance.html)
 */
private fun TaskContainer.getOrCreate(name: String, init: Jar.() -> Unit): TaskProvider<Jar> =
    try {
        named<Jar>(name)
    } catch (e: UnknownTaskException) {
        register<Jar>(name) {
            init()
        }
    }
