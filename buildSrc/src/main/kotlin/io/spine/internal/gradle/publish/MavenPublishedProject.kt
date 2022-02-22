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
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
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
        createAndRegister().apply {
            setMavenCoordinates()
            setJars()
        }
    }

    /**
     * Creates and registers new Maven publication in this Gradle project.
     */
    private fun createAndRegister(): MavenPublication {
        val gradlePublishing = extensions.getByType<PublishingExtension>()
        val gradlePublications = gradlePublishing.publications
        val mavenPublication = gradlePublications.create<MavenPublication>("mavenJava")
        return mavenPublication
    }

    private fun MavenPublication.setMavenCoordinates() {
        groupId = this@MavenPublishedProject.group.toString()
        artifactId = this@MavenPublishedProject.artifactId
        version = this@MavenPublishedProject.version.toString()
    }

    private fun MavenPublication.setJars() {
        from(project.components.getAt("java"))
        setArtifacts(customJars)
    }
}
