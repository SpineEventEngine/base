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
import java.util.Objects.isNull
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.PublishArtifactSet
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

/**
 * A [Project] that publishes one or more JAR artifacts using `maven-publish` plugin.
 *
 * @param project producer of artifacts
 * @param artifactId the name of jars without version
 * @param publishProtoJar tells whether to publish a dedicated [Artifacts.protoJar]
 * @param destinations Maven repositories, to which the resulting artifacts are sent
 */
class MavenPublishingProject(
    private val project: Project,
    private val artifactId: String,
    private val publishProtoJar: Boolean,
    private val destinations: Set<Repository>
) {

    /**
     * Applies `maven-publish` plugin, sets up `mavenJava` publication and declares artifacts.
     *
     * Here `project.afterEvaluate` closure is used. General rule of thumb is to avoid using
     * of this closure, as it configures a project when its configuration is considered completed.
     * That's quite counter-intuitive.
     *
     * The root cause why it is used here is a possibility to set up publishing of multiple modules
     * from a root project. When this possibility is employed, in fact, we configure publishing
     * for a module, build file of which has not been even evaluated by that time. This leads to
     * an unexpected behavior.
     *
     * The simplest example here is specifying of `version` and `group`. Let's suppose, they are
     * specified in a module's build. It is a common practice. But publishing of this module is
     * set up from a root project. By the time, when we specify them for published artifacts,
     * we don't know them. As a result, we have to use `project.afterEvaluate` in order
     * to guarantee that a module will be configured by the time we set up publishing for it.
     */
    fun setUp() = project.afterEvaluate {
        apply(plugin = "maven-publish")
        declareArtifacts()
        createMavenPublication()
        specifyRepositories()
    }

    // moved
    private fun Project.declareArtifacts() {
//        val artifacts = MavenArtifacts(publishProtoJar)
//        artifacts.registerIn(this)
    }

    // moved
    private fun Project.createMavenPublication() {
        extensions.getByType<PublishingExtension>()
            .publications
            .create<MavenPublication>("mavenJava") {
                groupId = project.group.toString()
                artifactId = this@MavenPublishingProject.artifactId
                version = project.version.toString()

                from(project.components.getAt("java"))

                val archivesConfig = project.configurations.getAt("archives")
                val allArtifacts = archivesConfig.allArtifacts
                val deduplicated = allArtifacts.deduplicate()
                setArtifacts(deduplicated)
            }
    }

    /**
     * Obtains an [Iterable] containing artifacts that have the same `extension` and `classifier`.
     *
     * Such a situation may occur when applying both `com.gradle.plugin-publish` plugin AND
     * `spinePublishing` in the same project. `com.gradle.plugin-publish` adds `sources` and
     * `javadoc` artifacts, and we do it too in [declareArtifacts].
     *
     * At the time when we add artifacts in [declareArtifacts], those added by
     * `com.gradle.plugin-publish` are not yet visible to our code. Hence, we have to perform
     * the deduplication before we specify the artifacts in [createMavenPublication].
     */
    private fun PublishArtifactSet.deduplicate(): Iterable<PublishArtifact> =
        distinctBy { it.extension to it.classifier }

    // moved
    private fun Project.specifyRepositories() {
        val isSnapshot = project.version.toString()
            .matches(Regex(".+[-.]SNAPSHOT([+.]\\d+)?"))
        with(extensions.getByType<PublishingExtension>()) {
            destinations.forEach { destination ->
                repositories.maven { initialize(destination, project, isSnapshot) }
            }
        }
    }

    // moved
    private fun MavenArtifactRepository.initialize(
        repository: Repository,
        project: Project,
        isSnapshot: Boolean
    ) {
        val destination = with(repository) {
            if (isSnapshot) snapshots else releases
        }

        // Special treatment for CloudRepo URL.
        // Reading is performed via public repositories, and publishing via
        // private ones that differ in the `/public` infix.
        url = project.uri(destination.replace("/public", ""))

        val repoCreds = repository.credentials(project.rootProject)
        credentials {
            username = repoCreds?.username
            password = repoCreds?.password
        }
    }
}
