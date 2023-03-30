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
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import io.spine.internal.gradle.dokka.dokkaJar

/**
 * Information, required to set up publishing of a project using `maven-publish` plugin.
 *
 * @param destinations
 *         set of repositories, to which the resulting artifacts will be sent.
 * @param includeProtoJar
 *         tells whether [protoJar] artifact should be published.
 * @param includeTestJar
 *         tells whether [testJar] artifact should be published.
 * @param includeDokkaJar
 *         tells whether [dokkaJar] artifact should be published.
 * @param customPublishing
 *         tells whether subproject declares own publishing and standard one
 *         should not be applied.
 */
internal class PublishingConfig private constructor(
    val destinations: Set<Repository>,
    val customPublishing: Boolean,
    val includeTestJar: Boolean,
    val includeDokkaJar: Boolean,
    val includeProtoJar: Boolean
) {
    /**
     * Creates an instance for standard publishing of a project module,
     * specified under [SpinePublishing.modules].
     */
    constructor(
        destinations: Set<Repository>,
        includeProtoJar: Boolean = true,
        includeTestJar: Boolean = false,
        includeDokkaJar: Boolean = false
    ) : this(destinations, customPublishing = false,
        includeTestJar, includeDokkaJar, includeProtoJar)

    /**
     * Creates an instance for publishing a module specified
     * under [SpinePublishing.modulesWithCustomPublishing].
     */
    constructor(destinations: Set<Repository>) :
            this(destinations, customPublishing = true,
                includeTestJar = false, includeDokkaJar = false, includeProtoJar = false)
}
/**
 * Applies this configuration to the given project.
 *
 * This method does the following:
 *
 *  1. Applies `maven-publish` plugin to the project.
 *  2. Registers [StandardJavaPublicationHandler] in Gradle's
 *     [PublicationContainer][org.gradle.api.publish.PublicationContainer].
 *  4. Configures "publish" task.
 *
 *  The actual list of resulted artifacts is determined by [registerArtifacts].
 */
internal fun PublishingConfig.apply(project: Project) = with(project) {
    apply(plugin = "maven-publish")
    handlePublication(project)
    configurePublishTask(destinations)
}

private fun PublishingConfig.handlePublication(project: Project) {
    if (customPublishing) {
        handleCustomPublication(project)
    } else {
        createStandardPublication(project)
    }
}

private fun PublishingConfig.createStandardPublication(project: Project) {
    val jarTasks = project.registerArtifacts(includeProtoJar, includeTestJar, includeDokkaJar)
    val publication = StandardJavaPublicationHandler(project, jars = jarTasks, destinations)
    publication.registerAtProject()
}

private fun PublishingConfig.handleCustomPublication(project: Project) {
    project.logger.info("The project `${project.name}` is set to provide custom publishing.")
    val publications = CustomPublicationHandler(project, destinations)
    publications.registerAtProject()
}

/**
 * Registers [Jar] tasks, output of which is used as Maven artifacts.
 *
 * By default, only a jar with java compilation output is included into publication. This method
 * registers tasks which produce additional artifacts.
 *
 * The list of additional artifacts to be registered:
 *
 *  1. [sourcesJar] – Java, Kotlin and Proto source files.
 *  2. [protoJar] – only Proto source files.
 *  3. [javadocJar] – documentation, generated upon Java files.
 *  4. [testJar] – compilation output of "test" source set.
 *  5. [dokkaJar] - documentation generated by Dokka.
 *
 * Registration of [protoJar], [testJar] and [dokkaJar] is optional. It can be controlled by the
 * method's parameters.
 *
 * @return the list of the registered tasks.
 */
private fun Project.registerArtifacts(
    includeProtoJar: Boolean = true,
    includeTestJar: Boolean = false,
    includeDokkaJar: Boolean = false
): Set<TaskProvider<Jar>> {

    val tasks = mutableSetOf<TaskProvider<Jar>>()

    val java = project.extensions.findByType(JavaPluginExtension::class.java)
    java?.run {
        withSourcesJar()
        withJavadocJar()
    }
    
    // We don't want to have an empty "proto.jar" when a project doesn't have any Proto files.
    if (hasProto() && includeProtoJar) {
        tasks.add(protoJar())
    }

    // Here, we don't have the corresponding `hasTests()` check, since this artifact is disabled
    // by default. And turning it on means "We have tests and need them to be published."
    if (includeTestJar) {
        tasks.add(testJar())
    }

    if (includeDokkaJar) {
        tasks.add(dokkaJar())
    }

    return tasks
}
