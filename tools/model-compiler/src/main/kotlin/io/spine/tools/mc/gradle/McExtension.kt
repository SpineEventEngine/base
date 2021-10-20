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

package io.spine.tools.mc.gradle

import io.spine.logging.Logging
import io.spine.tools.gradle.defaultMainDescriptors
import io.spine.tools.gradle.defaultTestDescriptors
import io.spine.tools.mc.gradle.McExtension.Companion.name
import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty

/**
 * Extends a Gradle project with the [`modelCompiler`][name] block.
 */
public class McExtension private constructor(private val project: Project) {

    /**
     * The absolute path to the main Protobuf descriptor set file.
     *
     * The file must have the `.desc` extension.
     */
    public val mainDescriptorSetFile: RegularFileProperty

    /**
     * The absolute path to the test Protobuf descriptor set file.
     *
     * The file must have the `.desc` extension.
     */
    public val testDescriptorSetFile: RegularFileProperty

    init {
        val projectDir: Directory = project.layout.projectDirectory
        val file = { f: File -> projectDir.file(f.toString()) }
        val of = project.objects

        mainDescriptorSetFile = of.fileProperty()
            .convention(file(project.defaultMainDescriptors))

        testDescriptorSetFile = of.fileProperty()
            .convention(file(project.defaultTestDescriptors))
    }

    private fun register() {
        project.extensions.add(javaClass, name, this)
    }

    public companion object : Logging {

        public const val name: String = "modelCompiler2"

        public fun createIn(project: Project) {
            _debug().log("Adding the `$name` extension to the project `$project`.")
            val extension = McExtension(project)
            extension.register()
        }
    }
}
