/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.internal.gradle.dokka

import io.spine.internal.gradle.publish.getOrCreate
import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskContainer
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder

/**
 * Finds the `dokkaHtml` Gradle task.
 */
fun TaskContainer.dokkaHtmlTask(): DokkaTask? = this.findByName("dokkaHtml") as DokkaTask?

/**
 * Returns only Java source roots out of all present in the source set.
 *
 * It is a helper method for generating documentation by Dokka only for Java code.
 * It is helpful when both Java and Kotlin source files are present in a source set.
 * Dokka can properly generate documentation for either Kotlin or Java depending on
 * the configuration, but not both.
 */
internal fun GradleDokkaSourceSetBuilder.onlyJavaSources(): FileCollection {
    return sourceRoots.filter(File::isJavaSourceDirectory)
}

private fun File.isJavaSourceDirectory(): Boolean {
    return isDirectory && name == "java"
}

/**
 * Locates or creates `dokkaJar` task in this [Project].
 *
 * The output of this task is a `jar` archive. The archive contains the Dokka output, generated upon
 * Java sources from `main` source set. Requires Dokka to be configured in the target project by
 * applying `dokka-for-java` plugin.
 */
internal fun Project.dokkaJar() = tasks.getOrCreate("dokkaJar") {
    archiveClassifier.set("dokka")
    from(files("$buildDir/docs/dokka"))

    tasks.dokkaHtmlTask()?.let{ dokkaTask ->
        this@getOrCreate.dependsOn(dokkaTask)
    }
}
