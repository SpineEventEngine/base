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
import java.util.*
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply

/**
 * Sets up publishing for a given project.
 *
 * The setting up publishing for a project requires four steps:
 *
 *  1. Applying `maven-publish` plugin.
 *  2. Registering [Jar][org.gradle.api.tasks.bundling.Jar] tasks, which generate artifacts.
 *  3. Make `rootProject.tasks.publish` depend on this project's `publish` task.
 */
class PublishedProject(
    project: Project,
    artifactId: String,
    publishProto: Boolean = true,
    private val destinations: Collection<Repository>,
) {

    init {
        with(project) {
            apply(plugin = "maven-publish")
            MavenJavaPublication(
                project = this,
                artifactId = artifactId,
                jars = jars(publishProto),
                destinations = destinations
            )
            setTaskDependencies()
        }
    }

    private fun Project.jars(publishProto: Boolean): List<TaskProvider<Jar>> {
        val selected = mutableListOf(
            sourcesJar(),
            javadocJar(),
            testOutputJar(),
        )

        if (hasProto() && publishProto) {
            selected.add(protoJar())
        }

        return selected
    }

    private fun Project.setTaskDependencies() {
        val rootPublish = rootProject.tasks.getOrCreatePublishTask()
        val localPublish = tasks.getOrCreatePublishTask()
        val checkCredentials = tasks.registerCheckCredentialsTask()
        rootPublish.configure { dependsOn(localPublish) }
        localPublish.configure { dependsOn(checkCredentials) }
    }

    // Try-catch block is used here because Gradle still does not provide API for checking a task
    // presence without triggering its creation.
    // See: https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
    private fun TaskContainer.getOrCreatePublishTask() =
        try {
            named("publish")
        } catch (e: UnknownTaskException) {
            register("publish")
        }

    private fun TaskContainer.registerCheckCredentialsTask() = register("checkCredentials") {
        doLast {
            destinations.forEach { it.assertCredentials(project) }
        }
    }

    private fun Repository.assertCredentials(project: Project) {
        val credentials = credentials(project)
        if (Objects.isNull(credentials)) {
            throw InvalidUserDataException(
                "No valid credentials for repository `${this}`. Please make sure " +
                        "to pass username/password or a valid `.properties` file."
            )
        }
    }
}
