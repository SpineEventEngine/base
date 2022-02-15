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
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class MavenArtifacts {

    fun Project.sourceJar(): TaskProvider<Jar> = createIfAbsent(
        taskName = "sourceJar",
        from = sourceSets["main"].allSource,
        classifier = "sources"
    )

    fun Project.testOutputJar(): TaskProvider<Jar> = createIfAbsent(
        taskName = "testOutputJar",
        from = sourceSets["test"].output,
        classifier = "test"
    )

    fun Project.javadocJar(): TaskProvider<Jar> = createIfAbsent(
        taskName = "javadocJar",
        from = files("$buildDir/docs/javadoc"),
        classifier = "javadoc",
        dependencies = setOf("javadoc")
    )

    private fun Project.createIfAbsent(
        taskName: String,
        from: FileCollection,
        classifier: String,
        dependencies: Set<Any> = setOf()
    ): TaskProvider<Jar> = with(tasks) {
        try {
            named<Jar>(taskName)
        } catch (e: UnknownTaskException) {
            register<Jar>(taskName) {
                this.from(from)
                archiveClassifier.set(classifier)
                dependencies.forEach { dependsOn(it) }
            }
        }
    }
}
