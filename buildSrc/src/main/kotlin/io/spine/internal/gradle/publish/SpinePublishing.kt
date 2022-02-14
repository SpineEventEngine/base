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
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get

fun Project.spinePublishing2(configuration: SpinePublishing.() -> Unit) {
    extensions.run {
        configuration.invoke(
            findByType() ?: create(SpinePublishing::class.java.simpleName, project)
        )
    }
}

open class SpinePublishing(private val rootProject: Project) {

    private var protoJar: ProtoJar = ProtoJar()
    private val taskName = "publish"
    var modules: Set<String> = setOf()
    var destinations: Set<Repository> = setOf()
    var useSpinePrefix: Boolean = true
    var customPrefix: String = ""

    init {
        rootProject.afterEvaluate {
            val rootPublishTask = publishTask(rootProject)
            val checkCredentialsTask = checkCredentialsTask(rootProject)
            modules.map { name -> rootProject.project(name) }
                .forEach { it.applyMavenPublish(rootPublishTask, checkCredentialsTask) }
        }
    }

    private fun publishTask(project: Project): TaskProvider<Task> = with(project.tasks) {
        try {
            named(taskName)
        } catch (e: UnknownTaskException) {
            register(taskName)
        }
    }

    private fun checkCredentialsTask(project: Project): TaskProvider<Task> =
        project.tasks.register("checkCredentials") {
            doLast {
                destinations.forEach {
                    it.credentials(project)
                        ?: throw InvalidUserDataException(
                            "No valid credentials for repository `${it}`. Please make sure " +
                                    "to pass username/password or a valid `.properties` file."
                        )
                }
            }
        }

    private fun Project.applyMavenPublish(rootPublish: TaskProvider<Task>,
                                          checkCredentials: TaskProvider<Task>) {

        apply(plugin = "maven-publish")

        setUpDefaultArtifacts()
        createMavenPublication()
        setUpRepositories()

        setUpTaskDependencies(rootPublish, checkCredentials)
    }

    private fun setUpTaskDependencies(rootPublish: TaskProvider<Task>,
                                      checkCredentials: TaskProvider<Task>) {

        TODO("Not yet implemented")
    }

    private fun createMavenPublication() {
        TODO("Not yet implemented")
    }

    private fun setUpRepositories() {
        TODO("Not yet implemented")
    }


    private fun Project.setUpDefaultArtifacts() {
        val sourceJar = tasks.createIfAbsent(
            artifactTask = ArtifactTaskName.sourceJar,
            from = sourceSets["main"].allSource,
            classifier = "sources"
        )
        val testOutputJar = tasks.createIfAbsent(
            artifactTask = ArtifactTaskName.testOutputJar,
            from = sourceSets["test"].output,
            classifier = "test"
        )
        val javadocJar = tasks.createIfAbsent(
            artifactTask = ArtifactTaskName.javadocJar,
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

    private fun Project.prepareTasks(publish: Task, checkCredentials: Task) {
        val publishTasks = getTasksByName(Publish.taskName, false)
        publish.dependsOn(publishTasks)
        publishTasks.forEach { it.dependsOn(checkCredentials) }
    }

    private fun TaskContainer.createIfAbsent(
        artifactTask: ArtifactTaskName,
        from: FileCollection,
        classifier: String,
        dependencies: Set<Any> = setOf()
    ): Task {
        val existing = findByName(artifactTask.name)
        if (existing != null) {
            return existing
        }
        println("creating because absent: $artifactTask")
        return create(artifactTask.name, Jar::class) {
            this.from(from)
            archiveClassifier.set(classifier)
            dependencies.forEach { dependsOn(it) }
        }
    }

    fun spineRepositories(select: PublishingRepos.() -> Set<Repository>) = select(PublishingRepos)

    fun protoJar(configuration: ProtoJar.() -> Unit)  = configuration.invoke(protoJar)
}

/**
 * Configures publishing of a JAR containing all the `.proto` definitions
 * found in the project's classpath, which are the definitions from `sourceSets.main.proto`
 * and the proto files extracted from the JAR dependencies of the project.
 */
class ProtoJar {
    /**
     * Set of modules, for which a `proto` JAR will NOT be generated.
     */
    var exclusions: Set<String> = emptySet()

    /**
     * Disables `proto` JAR generation for all publishing modules.
     */
    var disabled = false
}
