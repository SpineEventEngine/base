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
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply

/**
 * Configuration, required to set up publishing of a [Project] using `maven-publish` plugin.
 *
 * @param artifactId a name that a project is known by.
 * @param publishProto tells whether [protoJar] artifact should be published.
 * @param destinations set of repositories, to which the resulting artifacts will be sent.
 */
internal class MavenPublishingConfig(
    private val artifactId: String,
    private val publishProto: Boolean = true,
    private val destinations: Collection<Repository>,
) {

    /**
     * Applies this configuration to the given project.
     *
     * In order to enable project publish to Maven, several steps are to be performed:
     *
     *  1. Apply `maven-publish` plugin.
     *  2. Set dependencies for `publish` task.
     *  3. Register [Jar][org.gradle.api.tasks.bundling.Jar] tasks, which produce actual artifacts.
     *  4. Create [MavenJavaPublication].
     */
    fun apply(project: Project) = with(project) {
        apply(plugin = "maven-publish")
        setTaskDependencies()

        val artifacts = chooseJars(publishProto)
        val publication = MavenJavaPublication(
            artifactId = artifactId,
            jars = artifacts,
            destinations = destinations
        )

        publication.registerIn(project)
    }

    /**
     * Registers [Jar] tasks which produce Maven artifacts.
     *
     * This method determines which artifacts the resulting publication will contain.
     *
     * @return the list of the registered tasks.
     */
    private fun Project.chooseJars(publishProto: Boolean): List<TaskProvider<Jar>> {
        val artifacts = mutableListOf(
            sourcesJar(),
            javadocJar(),
            testOutputJar(),
        )

        // We don't want to have an empty `proto.jar`,
        // when a project doesn't have any proto files at all.
        if (hasProto() && publishProto) {
            artifacts.add(protoJar())
        }

        return artifacts
    }

    private fun Project.setTaskDependencies() {
        val rootPublish = rootProject.tasks.getOrCreatePublishTask()
        val localPublish = tasks.getOrCreatePublishTask()
        val checkCredentials = tasks.registerCheckCredentialsTask()
        rootPublish.configure { dependsOn(localPublish) }
        localPublish.configure { dependsOn(checkCredentials) }
    }

    private fun TaskContainer.getOrCreatePublishTask() =
        if (names.contains("publish")) {
            named("publish")
        } else {
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
