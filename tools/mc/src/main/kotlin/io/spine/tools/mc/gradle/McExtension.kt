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

import io.spine.code.fs.SourceCodeDirectory
import io.spine.logging.Logging
import io.spine.tools.fs.DefaultPaths
import io.spine.tools.gradle.GradleExtension
import io.spine.tools.java.fs.DefaultJavaPaths
import io.spine.tools.mc.gradle.McExtension.Companion.name
import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

/**
 * Extends a Gradle project with the [`modelCompiler`][name] block.
 */
class McExtension private constructor(private val project: Project) : GradleExtension() {

    /**
     * The absolute path to the Protobuf source code under the `main` directory.
     */
    val mainProtoDir: DirectoryProperty

    /**
     * The absolute path to the test Protobuf source directory.
     */
    val testProtoDir: DirectoryProperty

    /**
     * The absolute path to the main Protobuf descriptor set file.
     *
     * The file must have the `.desc` extension.
     */
    val mainDescriptorSetFile: RegularFileProperty

    /**
     * The absolute path to the test Protobuf descriptor set file.
     *
     * The file must have the `.desc` extension.
     */
    val testDescriptorSetFile: RegularFileProperty

    /**
     * The absolute path to the main target generated resources directory.
     */
    val generatedMainResourcesDir: DirectoryProperty

    /**
     * The absolute path to the test target generated resources directory.
     */
    val generatedTestResourcesDir: DirectoryProperty

    init {
        val projectDir: Directory = project.layout.projectDirectory
        val def = defaultsOf(project)
        val src = def.src()
        val file = { f: File -> projectDir.file(f.toString()) }
        val dir = { d: SourceCodeDirectory -> projectDir.dir(d.toString()) }
        val of = project.objects

        mainProtoDir = of.directoryProperty()
            .convention(dir(src.mainProto()))

        testProtoDir = of.directoryProperty()
            .convention(dir(src.testProto()))

        mainDescriptorSetFile = of.fileProperty()
            .convention(file(defaultMainDescriptor(project)))

        testDescriptorSetFile = of.fileProperty()
            .convention(file(defaultTestDescriptor(project)))

        val generated = def.generated()
        generatedMainResourcesDir = of.directoryProperty()
            .convention(dir(generated.mainResources()))

        generatedTestResourcesDir = of.directoryProperty()
            .convention(dir(generated.testResources()))
    }

    private fun register() {
        project.extensions.add(javaClass, name, this)
    }

    override fun defaultPaths(project: Project): DefaultPaths = defaultsOf(project)

    companion object Companion : Logging {

        const val name = "modelCompiler2"

        fun createIn(project: Project) {
            _debug().log("Adding the `$name` extension to the project `$project`.")
            val extension = McExtension(project)
            extension.register()
        }

        private fun defaultsOf(project: Project): DefaultJavaPaths =
            DefaultJavaPaths.at(project.projectDir)
    }
}
