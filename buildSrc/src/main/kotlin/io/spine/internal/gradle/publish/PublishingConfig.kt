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
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply

/**
 * Information, required to set up publishing of a project using `maven-publish` plugin.
 *
 * @param artifactId a name that a project is known by.
 * @param excludeProtoJar tells whether [protoJar] artifact should be published.
 * @param destinations set of repositories, to which the resulting artifacts will be sent.
 */
internal class PublishingConfig(
    val artifactId: String,
    val excludeProtoJar: Boolean = false,
    val destinations: Collection<Repository>,
)

/**
 * Applies this configuration to the given project.
 *
 * This method does the following:
 *
 *  1. Applies `maven-publish` plugin to the project.
 *  2. Registers [MavenJavaPublication] in Gradle's [PublicationContainer][org.gradle.api.publish.PublicationContainer].
 *  4. Configures "publish" task.
 *
 *  The actual list of resulted artifacts is determined by [registerArtifacts].
 */
internal fun PublishingConfig.apply(project: Project) = with(project) {
    apply(plugin = "maven-publish")
    createPublication(project)
    configurePublishTask(destinations)
}

private fun PublishingConfig.createPublication(project: Project) {
    val artifacts = project.registerArtifacts(excludeProtoJar)
    val publication = MavenJavaPublication(
        artifactId = artifactId,
        jars = artifacts,
        destinations = destinations
    )
    publication.registerIn(project)
}

/**
 * Registers [Jar] tasks, output of which is used as Maven artifacts.
 *
 * By default, only a jar with java compilation output is included into publication. This method
 * registers tasks which produce additional artifacts.
 *
 * The list of additional artifacts:
 *
 *  1. [sourcesJar] – Java, Kotlin and Proto source files.
 *  2. [javadocJar] – documentation, generated upon Java files.
 *  3. [testOutputJar] – compilation output of "test" source set.
 *  4. [protoJar] – only Proto sources. It is an optional artifact.
 *
 * @return the list of the registered tasks.
 */
private fun Project.registerArtifacts(excludeProtoJar: Boolean = false): List<TaskProvider<Jar>> {
    val artifacts = mutableListOf(
        sourcesJar(),
        javadocJar(),
        testOutputJar(),
    )

    // We don't want to have an empty "proto.jar",
    // when a project doesn't have any Proto files at all.

    val publishProto = excludeProtoJar.not()
    if (hasProto() && publishProto) {
        artifacts.add(protoJar())
    }

    return artifacts
}
