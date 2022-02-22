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

import io.spine.internal.gradle.sourceSets
import java.io.File
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * JAR artifacts, which can be published along with the default project compilation output.
 */
internal class MavenArtifacts(private val publishProtoJar: Boolean) {

    fun registerIn(project: Project) = with(project) {

        sourcesJar()
        testOutputJar()
        javadocJar()

        if (hasProto() && publishProtoJar) {
            protoJar()
        }
    }

    /**
     * Sources from `main` source set.
     *
     * Includes:
     *
     *  - Kotlin
     *  - Java
     *  - Proto
     */
    private fun Project.sourcesJar() = jarArtifact("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource) // puts Java and Kotlin sources
        from(protoSources()) // puts Proto sources.
    }

    /**
     * Only Proto sources from `main` source set.
     */
    private fun Project.protoJar() = jarArtifact("protoJar") {
        archiveClassifier.set("proto")
        from(protoSources())
    }

    /**
     * Tells whether there are any Proto sources in `main` source set.
     */
    private fun Project.hasProto(): Boolean {
        val protoSources = protoSources()
        val result = protoSources.any { it.exists() }
        return result
    }

    /**
     * Locates Proto sources in `main` source set.
     *
     * Special treatment for them because they are not Java-related, and, thus, not included
     * into `sourceSets["main"].allSource`.
     */
    private fun Project.protoSources(): Collection<File> {
        val mainSourceSet = sourceSets["main"]
        val protoSourceDirs = mainSourceSet.extensions.getByName("proto") as SourceDirectorySet
        return protoSourceDirs.srcDirs
    }

    /**
     * Compilation output of `test` source set.
     */
    private fun Project.testOutputJar() = jarArtifact("testOutputJar") {
        archiveClassifier.set("test")
        from(sourceSets["test"].output)
    }

    /**
     * Javadoc, generated upon Java and Kotlin sources from `main` source set.
     */
    private fun Project.javadocJar() = jarArtifact("javadocJar") {
        archiveClassifier.set("javadoc")
        from(files("$buildDir/docs/javadoc"))
        dependsOn("javadoc")
    }

    private fun Project.jarArtifact(taskName: String, init: Jar.() -> Unit) {
        val task = tasks.getOrCreate(taskName, init)
        artifacts.add(ConfigurationName.archives, task)
    }

    // Try-catch block is used here because Gradle still does not provide API for checking a task
    // presence without triggering its creation.
    // See: https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
    private fun TaskContainer.getOrCreate(name: String, init: Jar.() -> Unit): TaskProvider<Jar> =
        try {
            named<Jar>(name)
        } catch (e: UnknownTaskException) {
            register<Jar>(name) {
                init()
            }
        }
}
