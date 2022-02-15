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

import io.spine.internal.gradle.Repository
import io.spine.internal.gradle.sourceSets
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

/**
 * A [Project] that publishes one or more artifacts using `maven-publish` plugin.
 */
class MavenPublishingProject(
    private val project: Project,
    private val prefix: String,
    private val excludeProtoJar: Boolean,
    private val destinations: Set<Repository>
) {

    /**
     * Applies the plugin, configures publications and prepares `publish*` tasks.
     */
    fun setUp() {
        project.apply(plugin = "maven-publish")
        project.declareArtifacts()
        createMavenPublication()
        specifyRepositories()
    }

    private fun Project.declareArtifacts() = tasks.run {
        val sourceJar = createIfAbsent(
            task = ArtifactTaskName.sourceJar,
            from = sourceSets["main"].allSource,
            classifier = "sources"
        )
        val testOutputJar = createIfAbsent(
            task = ArtifactTaskName.testOutputJar,
            from = sourceSets["test"].output,
            classifier = "test"
        )
        val javadocJar = createIfAbsent(
            task = ArtifactTaskName.javadocJar,
            from = files("$buildDir/docs/javadoc"),
            classifier = "javadoc",
            dependencies = setOf("javadoc")
        )

        artifacts {
            val archives = ConfigurationName.archives
            add(archives, sourceJar)
            add(archives, testOutputJar)
            add(archives, javadocJar)
        }
    }

    private fun TaskContainer.createIfAbsent(
        task: ArtifactTaskName,
        from: FileCollection,
        classifier: String,
        dependencies: Set<Any> = setOf()
    ): Task =
        findByName(task.name) ?: create(task.name, Jar::class) {
            this.from(from)
            archiveClassifier.set(classifier)
            dependencies.forEach { dependsOn(it) }
        }

    private fun createMavenPublication() {
    }

    private fun specifyRepositories() {
    }
}
