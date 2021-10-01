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

package io.spine.internal.gradle.github.pages

import java.io.File
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property

/**
 * Configures the `updateGitHubPages` extension.
 */
@Suppress("unused")
fun Project.updateGitHubPages(action: UpdateGitHubPagesExtension.() -> Unit) {
    apply<UpdateGitHubPages>()

    val extension = extensions.getByType(UpdateGitHubPagesExtension::class)
    extension.action()
}

/**
 * The extension for configuring the [UpdateGitHubPages] plugin.
 */
class UpdateGitHubPagesExtension
private constructor(

    /**
     * Tells whether the types marked `@Internal` should be included into the doc generation.
     */
    val allowInternalJavadoc: Property<Boolean>,

    /**
     * The root folder of the repository to which the updated `Project` belongs.
     */
    var rootFolder: Property<File>,

    /**
     * The external inputs, which output should be included
     * into the GitHub Pages update.
     *
     * The values are interpreted according to [org.gradle.api.tasks.Copy.from] specification.
     *
     * This property is optional.
     */
    var includeInputs: SetProperty<Any>
) {

    internal companion object {
        fun create(project: Project): UpdateGitHubPagesExtension {
            val factory = project.objects
            return UpdateGitHubPagesExtension(
                allowInternalJavadoc = factory.property(Boolean::class),
                rootFolder = factory.property(File::class),
                includeInputs = factory.setProperty(Any::class.java)
            )
        }
    }

    /**
     * Returns `true` if the `@Internal`-annotated types should be included into the generated
     * documentation, `false` otherwise.
     */
    fun allowInternalJavadoc(): Boolean {
        return allowInternalJavadoc.get()
    }

    /**
     * Returns the local root folder of the repository, to which the handled Gradle Project belongs.
     */
    fun rootFolder(): File {
        return rootFolder.get()
    }

    /**
     * Returns the external inputs, which results should be included
     * into the GitHub Pages update.
     */
    fun includedInputs(): Set<Any> {
        return includeInputs.get()
    }
}
