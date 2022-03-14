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
import io.spine.internal.gradle.isSnapshot
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

/**
 * Creates and configures "mavenJava" publication in the given project.
 *
 * The only prerequisite for the project is to have `maven-publish` plugin applied.
 *
 * "mavenJava" is a conventional name for [MavenPublication], which contains a java module.
 *
 *  @param project the project, in which this publication will be registered.
 *  @param artifactId a name that the project is known by.
 *  @param jars list of artifacts to be published along with the compilation output.
 *  @param destinations Maven repositories to which the produced artifacts will be sent.
 */
internal class MavenJavaPublication(
    private val project: Project,
    private val artifactId: String,
    private val jars: Collection<TaskProvider<Jar>>,
    private val destinations: Collection<Repository>,
) {

    init {
        val gradlePublishing = project.extensions.getByType<PublishingExtension>()
        createPublication(gradlePublishing)
        registerDestinations(gradlePublishing)
    }

    /**
     * Creates a new "mavenJava" [MavenPublication] in this Gradle project.
     */
    private fun createPublication(gradlePublishing: PublishingExtension) {
        val gradlePublications = gradlePublishing.publications
        gradlePublications.create<MavenPublication>("mavenJava") {
            specifyMavenCoordinates()
            specifyJars()
        }
    }

    private fun MavenPublication.specifyMavenCoordinates() {
        groupId = project.group.toString()
        artifactId = this@MavenJavaPublication.artifactId
        version = project.version.toString()
    }

    private fun MavenPublication.specifyJars() {

        // Adds a jar with the compilation output of `main` source set.
        from(project.components.getAt("java"))

        // Adds any other jars. Usually includes `sources.jar`, `test.jar`, `javadoc.jar` etc.
        // They are not provided by `java` component out of the box.
        setArtifacts(jars)
    }

    /**
     * Goes through the given [destinations] and registers each as a repository for publishing
     * in this Gradle project.
     */
    private fun registerDestinations(gradlePublishing: PublishingExtension) {
        val isSnapshot = project.version.toString().isSnapshot()
        val gradleRepositories = gradlePublishing.repositories
        destinations.forEach { destination ->
            gradleRepositories.register(destination, isSnapshot)
        }
    }

    private fun RepositoryHandler.register(repository: Repository, isSnapshot: Boolean) {
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
}
