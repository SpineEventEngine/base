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
    fun setUp() = with(project) {
        apply(plugin = "maven-publish")
        declareArtifacts()
        createMavenPublication()
        specifyRepositories()
        setTaskDependencies()
    }

    private fun Project.declareArtifacts() = with(MavenArtifacts()) {
        val archives = ConfigurationName.archives
        artifacts {
            add(archives, sourceJar())
            add(archives, testOutputJar())
            add(archives, javadocJar())
        }
    }

    private fun Project.createMavenPublication() {
        val artifact = listOf(prefix, name).joinToString("-")
        val publishing = extensions.getByType<PublishingExtension>()
        publishing.publications.create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = artifact
            version = project.version.toString()

            from(project.components.getAt("java"))

            val archivesConfig = project.configurations.getAt(ConfigurationName.archives)
            val allArtifacts = archivesConfig.allArtifacts
            val deduplicated = allArtifacts.deduplicate()
            setArtifacts(deduplicated)
        }
    }

    /**
     * Obtains an [Iterable] containing artifacts that have the same `extension` and `classifier`.
     *
     * Such a situation may occur when applying both `com.gradle.plugin-publish` plugin AND
     * `spinePublishing` in the same project. `com.gradle.plugin-publish` adds `sources` and `javadoc`
     * artifacts, and we do it too in [Project.setUpDefaultArtifacts].
     *
     * At the time when we add artifacts in [Project.setUpDefaultArtifacts], those added by
     * `com.gradle.plugin-publish` are not yet visible to our code. Hence, we have to perform
     * the deduplication before we set the artifacts in [PublishingExtension.createMavenPublication].
     */
    private fun PublishArtifactSet.deduplicate(): Iterable<PublishArtifact> =
        distinctBy { it.extension to it.classifier }

    private fun Project.specifyRepositories() {
        val isSnapshot = project.version
            .toString()
            .matches(Regex(".+[-.]SNAPSHOT([+.]\\d+)?"))
        val publishing = extensions.getByType<PublishingExtension>()
        destinations.forEach { repository ->
            publishing.repositories.maven { initialize(repository, project, isSnapshot) }
        }
    }

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
        val creds = repository.credentials(project.rootProject)
        credentials {
            username = creds?.username
            password = creds?.password
        }
    }

    private fun Project.setTaskDependencies() {
        val rootPublish = rootProject.tasks.getOrCreatePublishTask()
        val localPublish = tasks.getOrCreatePublishTask()
        val checkCredentials = registerCheckCredentialsTask()
        rootPublish.configure { dependsOn(localPublish) }
        localPublish.configure { dependsOn(checkCredentials) }
    }

    private fun TaskContainer.getOrCreatePublishTask() =
        try {
            named("publish")
        } catch (e: UnknownTaskException) {
            register("publish")
        }

    private fun Project.registerCheckCredentialsTask() = tasks.register("checkCredentials") {
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
}
