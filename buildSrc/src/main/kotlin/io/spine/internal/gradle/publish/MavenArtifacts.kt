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

import io.spine.internal.gradle.publish.proto.isProtoFileOrDir
import io.spine.internal.gradle.publish.proto.protoClasspath
import io.spine.internal.gradle.sourceSets
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class MavenArtifacts {

    fun Project.sourcesJar() = jarArtifact("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
        from(protoClasspath()) {
            include {
                it.file.isProtoFileOrDir()
            }
        }
    }

    fun Project.testOutputJar() = jarArtifact("testOutputJar") {
        archiveClassifier.set("test")
        from(sourceSets["test"].output)
    }

    fun Project.javadocJar() = jarArtifact("javadocJar") {
        archiveClassifier.set("javadoc")
        from(files("$buildDir/docs/javadoc"))
        dependsOn("javadoc")
    }

    fun Project.protoJar() = jarArtifact("protoJar") {
        archiveClassifier.set("proto")
        from(protoClasspath()) {
            include {
                it.file.isProtoFileOrDir()
            }
        }
    }

    private fun Project.jarArtifact(taskName: String, init: Jar.() -> Unit) {
        val task = tasks.getOrCreate(taskName, init)
        artifacts.add(ConfigurationName.archives, task)
    }

    private fun TaskContainer.getOrCreate(name: String, init: Jar.() -> Unit): TaskProvider<Jar> =
        try {
            named<Jar>(name)
        } catch (e: UnknownTaskException) {
            register<Jar>(name) {
                init()
                manifest {
                    attributes("Automatic-Module-Name" to "org.gradle.sample")
                }
            }
        }
}
