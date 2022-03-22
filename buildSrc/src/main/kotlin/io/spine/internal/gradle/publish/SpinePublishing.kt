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
 *
 * This extension sets up publishing of artifacts to Maven repositories.
 *
 * The extension can be configured for single- and multi-module projects.
 *
 * When used with a multi-module project, the extension should be opened in a root project's
 * build file. The published modules are specified explicitly by their names:
 *
 * ```
 * spinePublishing {
 *     modules = setOf(
 *         "subprojectA",
 *         "subprojectB",
 *     )
 *     destinations = setOf(
 *         PublishingRepos.cloudRepo,
 *         PublishingRepos.cloudArtifactRegistry,
 *     )
 * }
 * ```
 *
 * When used with a single-module project, the extension should be opened in a project's build file.
 * Only destinations should be specified:
 *
 * ```
 * spinePublishing {
 *     destinations = setOf(
 *         PublishingRepos.cloudRepo,
 *         PublishingRepos.cloudArtifactRegistry,
 *     )
 * }
 * ```
 *
 * It is worth to mention, that publishing of a module can be configured only from a single place.
 * For example, declaring `subprojectA` as published in a root project and opening
 * `spinePublishing` extension within `subprojectA` itself would lead to an exception.
 *
 * In each of published modules, the extension will create a [publication][MavenJavaPublication]
 * named "mavenJava". Along with the compilation output of "main" source set, the extension adds
 * the next artifacts:
 *
 * 1. [sourcesJar] – sources from "main" source set. Includes "hand-made" Java,
 *    Kotlin and Proto files. In order to include the generated code into this artifact, a module
 *    should specify those files as a part of "main" source set.
 *
 *    Here's an example of how to do that:
 *
 *    ```
 *    sourceSets {
 *        val generatedDir by extra("$projectDir/generated")
 *        val generatedSpineDir by extra("$generatedDir/main/java")
 *        main {
 *            java.srcDir(generatedSpineDir)
 *        }
 *    }
 *    ```
 *
 * 2. [testOutputJar] – compilation output of "test" source set.
 * 3. [javadocJar] - javadoc, generated upon Java sources from `main` source set.
 *    If javadoc for Kotlin is also needed, apply Dokka plugin. It tunes `javadoc` task to generate
 *    docs upon Kotlin sources as well.
 * 4. [protoJar] – only Proto sources from "main" source set. It's published only if
 *    Proto files are actually present in the source set. Publication of this artifact is optional,
 *    and can be disabled.
 *
 *    Here's an example of how to disable it for some of published modules:
 *
 *    ```
 *    spinePublishing {
 *        modules = setOf(
 *            "subprojectA",
 *            "subprojectB",
 *        )
 *        protoJar {
 *            exclusions = setOf(
 *                "subprojectB",
 *            )
 *        }
 *    }
 *    ```
 *
 *    For all modules, or when the extension is configured within a published module itself:
 *
 *    ```
 *    spinePublishing {
 *        protoJar {
 *            disabled = true
 *        }
 *    }
 *    ```
 *
 *    The resulting artifact is available under "proto" classifier. I.e., in Gradle 7+, one could
 *    depend on it like this:
 *
 *     ```
 *     implementation("io.spine:spine-client:$version@proto")
 *     ```
 *
 * @see [registerArtifacts]
 */
fun Project.spinePublishing(configuration: SpinePublishing.() -> Unit) {
    val name = SpinePublishing::class.java.simpleName
    val extension = with(extensions) { findByType<SpinePublishing>() ?: create(name, project) }
    extension.run {
        configuration()
        configured()
    }
}

/**
 * A Gradle extension for setting up publishing of spine modules using `maven-publish` plugin.
 *
 * @see spinePublishing
 */
open class SpinePublishing(private val project: Project) {

    private var protoJar: ProtoJar = ProtoJar()

    /**
     * Set of modules to be published.
     *
     * Both module's name or path can be used.
     *
     * Use this property if the extension is configured from a root project's build file.
     *
     * If left empty, the [project], in which the extension is opened, will be published.
     *
     * Empty by default.
     */
    var modules: Set<String> = emptySet()

    /**
     * Set of repositories, to which the resulting artifacts will be sent.
     *
     * Usually, Spine-related projects are published to one or more repositories,
     * declared in [PublishingRepos]:
     *
     * ```
     * destinations = setOf(
     *     PublishingRepos.cloudRepo,
     *     PublishingRepos.cloudArtifactRegistry,
     *     PublishingRepos.gitHub("base"),
     * )
     * ```
     *
     * Empty by default.
     */
    var destinations: Set<Repository> = emptySet()

    /**
     * A prefix to be added before the name of each artifact.
     *
     * The added prefix is followed by a hyphen.
     *
     * Default value is "spine".
     */
    var artifactPrefix: String = "spine"

    /**
     * Allows disabling publishing of a dedicated [protoJar] artifact,
     * containing all Proto sources from `sourceSets.main.proto`.
     *
     * Can be used in two ways:
     *
     * - [specify][ProtoJar.exclusions] set of modules, for which the publishing will be disabled;
     * - [disable][ProtoJar.disabled] it for all published modules.
     */
    fun protoJar(configuration: ProtoJar.() -> Unit)  = protoJar.run(configuration)

    /**
     * Called to notify the extension that its configuration is completed.
     *
     * On this stage the extension will validate the received configuration and set up
     * `maven-publish` plugin for each published module.
     */
    internal fun configured() {

        ensureProtoExclusionsArePublished()
        ensuresModulesNotDuplicated()

        val protoJarExclusions = protoJar.exclusions
        val publishedModules = modules.ifEmpty { setOf(project.name) }

        publishedModules.forEach { module ->
            val excludeProtoJar = (protoJarExclusions.contains(module) || protoJar.disabled)
            setUpPublishing(module, excludeProtoJar)
        }
    }

    /**
     * Sets up `maven-publish` plugin for the given module.
     *
     * Firstly, an instance of [PublishingConfig] is assembled for the module. Then, this
     * config is applied to the module.
     *
     * This method utilizes `project.afterEvaluate` closure. General rule of thumb is to avoid using
     * of this closure, as it configures a project when its configuration is considered completed.
     * Which is quite counter-intuitive.
     *
     * The root cause why it is used here is a possibility to configure publishing of multiple
     * modules from a root project. When this possibility is employed, in fact, we configure
     * publishing for a module, build file of which has not been even evaluated by that time.
     * That leads to an unexpected behavior.
     *
     * The simplest example here is specifying of `version` and `group` for Maven coordinates.
     * Let's suppose, they are declared in a module's build file. It is a common practice.
     * But publishing of the module is configured from a root project's build file. By the time,
     * when we need to specify them, we just don't know them. As a result, we have to use
     * `project.afterEvaluate` in order to guarantee that a module will be configured by the time
     * we configure publishing for it.
     */
    private fun setUpPublishing(module: String, excludeProtoJar: Boolean) {
        val project = project.project(module)
        val artifactId = artifactId(project)
        val publishingConfig = PublishingConfig(artifactId, excludeProtoJar, destinations)
        project.afterEvaluate {
            publishingConfig.apply(project)
        }
    }

    /**
     * Obtains an artifact ID for the given project, taking into account
     * the value of [artifactPrefix] property.
     *
     * If [artifactPrefix] is set to a non-empty string, it will be used before
     * the published project name, followed by a hyphen. Otherwise, just project name is returned.
     */
    internal fun artifactId(project: Project): String {
        if (artifactPrefix.isEmpty()) {
            return project.name
        }

        val id = "$artifactPrefix-${project.name}"
        return id
    }

    /**
     * Ensures that all modules, marked as excluded from proto JAR publishing,
     * are actually published.
     *
     * It makes no sense to exclude a module from [protoJar] publication, if a module
     * is not published at all.
     */
    private fun ensureProtoExclusionsArePublished() {
        val nonPublishedExclusions = protoJar.exclusions.minus(modules)
        if (nonPublishedExclusions.isNotEmpty()) {
            throw IllegalStateException("One or more modules are marked as `excluded from proto " +
                    "JAR publication`, but they are not even published: $nonPublishedExclusions")
        }
    }

    /**
     * Ensures that publishing of a module is configured only from a single place.
     *
     * We allow configuration of publishing from two places - a root project and module itself.
     * Here we verify that publishing of a module is not configured in both places simultaneously.
     */
    private fun ensuresModulesNotDuplicated() {
        val rootProject = project.rootProject
        if (rootProject == project) {
            return
        }

        val rootExtension = with(rootProject.extensions) { findByType<SpinePublishing>() }
        rootExtension?.let { rootPublishing ->
            val thisProject = setOf(project.name, project.path)
            if (thisProject.minus(rootPublishing.modules).size != 2) {
                throw IllegalStateException("Publishing of `$thisProject` module is already " +
                            "configured in a root project!")
            }
        }
    }
}

/**
 * Allows disabling publication of a dedicated [protoJar] artifact,
 * containing all the `.proto` definitions from `sourceSets.main.proto`.
 */
class ProtoJar {

    /**
     * Set of modules, for which a proto JAR will NOT be published.
     */
    var exclusions: Set<String> = emptySet()

    /**
     * Disables proto JAR publishing for all published modules.
     */
    var disabled = false
}
