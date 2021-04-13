/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.internal.gradle

import io.spine.internal.gradle.DefaultArtifact.javadocJar
import io.spine.internal.gradle.DefaultArtifact.sourceJar
import io.spine.internal.gradle.DefaultArtifact.testOutputJar
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

/**
 * This plugin allows to publish artifacts to remote Maven repositories.
 *
 * Apply this plugin to the root project. Specify the projects which produce publishable artifacts
 * and the target Maven repositories via the `publishing` DSL:
 * ```
 * import io.spine.gradle.internal.PublishingRepos
 * import io.spine.gradle.internal.spinePublishing
 *
 * spinePublishing {
 *     projectsToPublish.addAll(
 *         "submodule1",
 *         "submodule2",
 *         "nested:submodule3"
 *     )
 *     targetRepositories.addAll(
 *         PublishingRepos.cloudRepo,
 *         PublishingRepos.gitHub("LibraryName")
 *     )
 * }
 * ```
 *
 * By default, we publish artifacts produced by tasks `sourceJar`, `testOutputJar`,
 * and `javadocJar`, along with the default project compilation output. If any of these tasks is not
 * declared, it's created with sensible default settings by the plugin.
 *
 * To publish more artifacts for a certain project, add them to the archives configuration:
 * ```
 * artifacts {
 *     archives(myCustomJarTask)
 * }
 * ```
 *
 * If any plugins applied to the published project declare any other artifacts, those artifacts
 * are published as well.
 */
class Publish : Plugin<Project> {

    companion object {

        const val taskName = "publish"
        const val extensionName = "spinePublishing"

        private const val ARCHIVES = "archives"
    }

    override fun apply(project: Project) {
        val extension = PublishExtension.create(project)
        project.extensions.add(PublishExtension::class.java, extensionName, extension)

        val publish = project.createPublishTask()
        val checkCredentials = project.createCheckTask(extension)

        project.afterEvaluate {
            extension.projectsToPublish
                .get()
                .map { project.project(it) }
                .forEach { p ->
                    p.logger.debug("Applying `maven-publish` plugin to ${name}.")

                    p.apply(plugin = "maven-publish")

                    p.setUpDefaultArtifacts()

                    val action = {
                        val publishingExtension = p.extensions.getByType(PublishingExtension::class)
                        publishingExtension.createMavenPublication(p, extension)
                        publishingExtension.setUpRepositories(p, extension)
                        p.prepareTasks(publish, checkCredentials)
                    }
                    if (p.state.executed) {
                        action()
                    } else {
                        p.afterEvaluate { action() }
                    }
                }
        }
    }

    private fun Project.createPublishTask(): Task =
        rootProject.tasks.create(taskName)

    private fun Project.createCheckTask(extension: PublishExtension): Task {
        val checkCredentials = tasks.create("checkCredentials")
        checkCredentials.doLast {
            extension.targetRepositories
                .get()
                .forEach {
                    it.credentials(this@createCheckTask)
                        ?: throw InvalidUserDataException(
                            "No valid credentials for repository `${it}`. Please make sure " +
                                    "to pass username/password or a valid `.properties` file."
                        )
                }
        }
        return checkCredentials
    }

    private fun Project.prepareTasks(publish: Task, checkCredentials: Task) {
        val publishTasks = getTasksByName(taskName, false)
        publish.dependsOn(publishTasks)
        publishTasks.forEach { it.dependsOn(checkCredentials) }
    }

    private fun Project.setUpDefaultArtifacts() {
        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class)
        val sourceSets = javaConvention.sourceSets

        val sourceJar = tasks.createIfAbsent(
            artifactTask = sourceJar,
            from = sourceSets["main"].allSource,
            classifier = "sources"
        )
        val testOutputJar = tasks.createIfAbsent(
            artifactTask = testOutputJar,
            from = sourceSets["test"].output,
            classifier = "test"
        )
        val javadocJar = tasks.createIfAbsent(
            artifactTask = javadocJar,
            from = files("$buildDir/docs/javadoc"),
            classifier = "javadoc",
            dependencies = setOf("javadoc")
        )

        artifacts {
            add(ARCHIVES, sourceJar)
            add(ARCHIVES, testOutputJar)
            add(ARCHIVES, javadocJar)
        }
    }

    private fun TaskContainer.createIfAbsent(artifactTask: DefaultArtifact,
                                             from: FileCollection,
                                             classifier: String,
                                             dependencies: Set<Any> = setOf()): Task {
        val existing = findByName(artifactTask.name)
        if (existing != null) {
            return existing
        }
        return create(artifactTask.name, Jar::class) {
            this.from(from)
            archiveClassifier.set(classifier)
            dependencies.forEach { dependsOn(it) }
        }
    }

    private fun PublishingExtension.createMavenPublication(project: Project,
                                                           extension: PublishExtension
    ) {
        val artifactIdForPublishing = if (extension.spinePrefix.get()) {
            "spine-${project.name}"
        } else {
            project.name
        }
        publications {
            create("mavenJava", MavenPublication::class.java) {
                groupId = project.group.toString()
                artifactId = artifactIdForPublishing
                version = project.version.toString()

                from(project.components.getAt("java"))

                setArtifacts(project.configurations.getAt(ARCHIVES).allArtifacts)
            }
        }
    }

    private fun PublishingExtension.setUpRepositories(
        project: Project,
        extension: PublishExtension
    ) {
        val snapshots = project.version
            .toString()
            .matches(Regex(".+[-.]SNAPSHOT([+.]\\d+)?"))
        repositories {
            extension.targetRepositories.get().forEach { repo ->
                maven {
                    initialize(repo, project, snapshots)
                }
            }
        }
    }

    private fun MavenArtifactRepository.initialize(repo: Repository,
                                                   project: Project,
                                                   snapshots: Boolean) {
        val publicRepo = if(snapshots) {
            repo.snapshots
        } else {
            repo.releases
        }
        // Special treatment for CloudRepo URL.
        // Reading is performed via public repositories, and publishing via
        // private ones that differ in the `/public` infix.
        url = project.uri(publicRepo.replace("/public", ""))
        val creds = repo.credentials(project.rootProject)
        credentials {
            username = creds?.username
            password = creds?.password
        }
    }
}

/**
 * The extension for configuring the `Publish` plugin.
 */
class PublishExtension
private constructor(
    val projectsToPublish: SetProperty<String>,
    val targetRepositories: SetProperty<Repository>,
    val spinePrefix: Property<Boolean>
) {

    internal companion object {
        fun create(project: Project): PublishExtension {
            val factory = project.objects
            return PublishExtension(
                projectsToPublish = factory.setProperty(String::class),
                targetRepositories = factory.setProperty(Repository::class),
                spinePrefix = factory.property(Boolean::class)
            )
        }
    }

    init {
        spinePrefix.convention(true)
    }
}

/**
 * Configures the `spinePublishing` extension.
 *
 * As `Publish` is a class-plugin in `buildSrc`, we don't get strongly typed generated helper
 * methods for the `spinePublishing` configuration. Thus, we proviude this helper function for use
 * in Kotlin build scripts.
 */
@Suppress("unused")
fun Project.spinePublishing(action: PublishExtension.() -> Unit) {
    apply<Publish>()

    val extension = extensions.getByType(PublishExtension::class)
    extension.action()
}

/**
 * Default artifact task names.
 *
 * These tasks, if not present on a project already, are created by the `Publish` plugin. Their
 * output is published as project's artifacts.
 */
private enum class DefaultArtifact {

    sourceJar,
    testOutputJar,
    javadocJar;
}
