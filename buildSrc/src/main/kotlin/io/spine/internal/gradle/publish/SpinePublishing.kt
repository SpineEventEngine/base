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
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType

/**
 * Configures [SpinePublishing] extension.
 */
fun Project.spinePublishing2(configuration: SpinePublishing.() -> Unit) {
    val name = SpinePublishing::class.java.simpleName
    val extension = with(extensions) { findByType<SpinePublishing>() ?: create(name, project) }
    extension.run {
        configuration()
        configured()
    }
}

/**
 * A scope for setting up publishing of spine modules using `maven-publish` plugin.
 */
open class SpinePublishing(private val project: Project) {

    private var protoJar: ProtoJar = ProtoJar()
    var modules: Set<String> = emptySet()
    var destinations: Set<Repository> = emptySet()
    var customPrefix: String = ""

    fun spineRepositories(select: PublishingRepos.() -> Set<Repository>) = select(PublishingRepos)

    fun protoJar(configuration: ProtoJar.() -> Unit)  = protoJar.run(configuration)

    /**
     * Called to notify the extension that its configuration is completed.
     *
     * On this stage the extension will validate the received configuration and set up
     * `maven-publish` plugin for each module.
     */
    internal fun configured() {

        assertProtoExclusions()
        assertModulesNotDuplicated()

        val protoJarExclusions = protoJar.exclusions
        val publishingProjects = modules.ifEmpty { setOf(project.path) }
            .map { path ->
                val isProtoJarExcluded = (protoJarExclusions.contains(path) || protoJar.disabled)
                MavenPublishingProject(
                    project = project.project(path),
                    prefix = customPrefix.ifEmpty { "spine" },
                    publishProtoJar = isProtoJarExcluded.not(),
                    destinations
                )
            }

        project.afterEvaluate {
            publishingProjects.forEach { it.setUp() }
        }
    }

    /**
     * Assert that publishing of this module is not already configured in a root project.
     */
    private fun assertModulesNotDuplicated() {
        val rootProject = project.rootProject
        if (rootProject == project) {
            return
        }

        val extension = with(rootProject.extensions) { findByType<SpinePublishing>() }
        extension?.let {
            val subproject = project.name
            if (it.modules.contains(subproject)) {
                throw IllegalStateException("Publishing of `$subproject` module is already " +
                            "configured in a root project!")
            }
        }
    }

    /**
     * Asserts that all modules, marked as excluded from proto JAR generation,
     * are actually specified for publishing.
     */
    private fun assertProtoExclusions() {
        val nonPublishedExclusions = protoJar.exclusions.minus(modules)
        if (nonPublishedExclusions.isNotEmpty()) {
            throw IllegalStateException("One or more modules are marked as `excluded from proto " +
                    "JAR generation`, but they are not even published: $nonPublishedExclusions")
        }
    }
}

/**
 * Configures publishing of a JAR containing all the `.proto` definitions
 * found in the project's classpath, which are the definitions from `sourceSets.main.proto`
 * and the proto files extracted from the JAR dependencies of the project.
 */
class ProtoJar {

    /**
     * Set of modules, for which a `proto` JAR will NOT be generated.
     *
     * Use this set only if the set of [published modules][SpinePublishing.modules]
     * is specified explicitly. Otherwise, use [disabled] flag.
     */
    var exclusions: Set<String> = emptySet()

    /**
     * Disables `proto` JAR generation for all publishing modules.
     */
    var disabled = false
}
