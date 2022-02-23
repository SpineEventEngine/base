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
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

/**
 * A [Project] that is published to one or more Maven repositories.
 */
internal class MavenPublishedProject(
    project: Project,
    private val artifactId: String,
    private val customJars: Collection<Jar>,
    private val destinations: Collection<Repository>,
) : Project by project {

    init {
        apply(plugin = "maven-publish")
        val gradlePublishing = extensions.getByType<PublishingExtension>()
        createPublication(gradlePublishing)
        setDestinations(gradlePublishing)
    }

    /**
     * Creates a new Maven publication in this Gradle project.
     */
    private fun createPublication(gradlePublishing: PublishingExtension) {
        val gradlePublications = gradlePublishing.publications
        gradlePublications.create<MavenPublication>("mavenJava") {
            specifyMavenCoordinates()
            specifyJars()
        }
    }

    private fun MavenPublication.specifyMavenCoordinates() {
        groupId = this@MavenPublishedProject.group.toString()
        artifactId = this@MavenPublishedProject.artifactId
        version = this@MavenPublishedProject.version.toString()
    }

    private fun MavenPublication.specifyJars() {
        from(project.components.getAt("java"))
        setArtifacts(customJars)
    }

    private fun setDestinations(gradlePublishing: PublishingExtension) {
        val isSnapshot = version.toString().isSnapshot()
        gradlePublishing.repositories {
            destinations.forEach { register(it, isSnapshot) }
        }
    }

    private fun RepositoryHandler.register(repository: Repository, isSnapshot: Boolean) {
        val target = if (isSnapshot) repository.snapshots else repository.releases
        val credentials = repository.credentials(project.rootProject)
        maven {
            url = uri(target)
            credentials {
                username = credentials?.username
                password = credentials?.password
            }
        }
    }
}
