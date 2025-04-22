/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.gradle.publish

import LicenseSettings
import io.spine.gradle.Repository
import io.spine.gradle.isSnapshot
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply

/**
 * The name of the Maven Publishing Gradle plugin.
 */
private const val MAVEN_PUBLISH = "maven-publish"

/**
 * Abstract base for handlers of publications in a project
 * with [spinePublishing] settings declared.
 */
internal sealed class PublicationHandler(
    protected val project: Project,
    private val destinations: Set<Repository>
) {

    fun apply() = with(project) {
        if (!hasCustomPublishing) {
            apply(plugin = MAVEN_PUBLISH)
        }

        pluginManager.withPlugin(MAVEN_PUBLISH) {
            handlePublications()
            registerDestinations()
            configurePublishTask(destinations)
        }
    }

    /**
     * Either handles publications already declared in the given project,
     * or creates new ones.
     */
    abstract fun handlePublications()

    /**
     * Goes through the [destinations] and registers each as a repository for publishing
     * in the given Gradle project.
     */
    private fun registerDestinations() {
        val repositories = project.publishingExtension.repositories
        destinations.forEach { destination ->
            repositories.register(project, destination)
        }
    }

    /**
     * Copies the attributes of Gradle [Project] to this [MavenPublication].
     *
     * The following project attributes are copied:
     *  * [group][Project.getGroup];
     *  * [version][Project.getVersion];
     *  * [description][Project.getDescription].
     *
     * Also, this function adds the [artifactPrefix][SpinePublishing.artifactPrefix] to
     * the [artifactId][MavenPublication.setArtifactId] of this publication,
     * if the prefix is not added yet.
     *
     * Finally, the Apache Software License 2.0 is set as the only license
     * under which the published artifact is distributed.
     */
    protected fun MavenPublication.copyProjectAttributes() {
        groupId = project.group.toString()
        val prefix = project.spinePublishing.artifactPrefix
        if (!artifactId.startsWith(prefix)) {
            artifactId = prefix + artifactId
        }
        version = project.version.toString()
        pom.description.set(project.description)

        pom.licenses {
            license {
                name.set(LicenseSettings.name)
                url.set(LicenseSettings.url)
            }
        }
    }
}

/**
 * Adds a Maven repository to the project specifying credentials, if they are
 * [available][Repository.credentials] from the root project.
 */
private fun RepositoryHandler.register(project: Project, repository: Repository) {
    val isSnapshot = project.version.toString().isSnapshot()
    val target = if (isSnapshot) repository.snapshots else repository.releases
    val credentials = repository.credentials(project.rootProject)
    maven {
        url = project.uri(target)
        credentials {
            username = credentials?.username
            password = credentials?.password
        }
    }
}
