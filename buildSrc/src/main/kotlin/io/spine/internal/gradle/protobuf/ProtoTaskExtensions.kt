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

package io.spine.internal.gradle.protobuf

import com.google.protobuf.gradle.GenerateProtoTask
import java.io.File
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

private val Project.generatedDir: String
    get() = "${projectDir}/generated"

/**
 * Configures protobuf code generation task for the code which cannot use Spine Model Compiler
 * (e.g. the `base` project).
 *
 * The task configuration consists of the following steps:
 *
 * 1. Adding `"kotlin"` to the list of involved `protoc` builtins.
 *
 * 2. Generation of descriptor set file is turned on for each source set.
 *    These files are placed under the `build/descriptors` directory.
 *
 * 3. Removing source code generated for `com.google` package for both Java and Kotlin.
 *    This is done at the final steps of the code generation.
 *
 * 4. Making `processResource` tasks depend on corresponding `generateProto` tasks.
 *    If the source set of the configured task isn't `main`, appropriate infix for
 *    the task names is used.
 *
 * The usage of this extension in a <em>module build file</em> would be:
 * ```
 *  protobuf {
 *      generateProtoTasks {
 *         for (task in all()) {
 *            task.setup()
 *         }
 *     }
 * }
 * ```
 * Using the same code under `subprojects` in a root build file does not seem to work because
 * test descriptor set files are not copied to resources. Performing this configuration from
 * a module build script solves the issue.
 *
 * IMPORTANT: In addition to calling `setup`, a submodule must contain a descriptor set reference
 * file (`desc.ref`) files placed under `resources`. The descriptor reference file must contain
 * a reference to the descriptor set file generated by the corresponding `GenerateProtoTask`.
 *
 * For example, for the `test` source set, the reference would be `known_types_test.desc`, and
 * for the `main` source set, the reference would be `known_types_main.desc`.
 *
 * See `io.spine.code.proto.DescriptorReference` and `io.spine.code.proto.FileDescriptors` classes
 * under the `base` project for more details.
 */
@Suppress("unused")
fun GenerateProtoTask.setup() {
    builtins.maybeCreate("kotlin")
    setupDescriptorSetFileCreation()
    doLast {
        copyGeneratedFiles()
    }
    excludeProtocOutput()
    setupKotlinCompile()
    dependOnProcessResourcesTask()
}

/**
 * Tell `protoc` to generate descriptor set files under the project build dir.
 */
private fun GenerateProtoTask.setupDescriptorSetFileCreation() {
    val ssn = sourceSet.name
    generateDescriptorSet = true
    with(descriptorSetOptions) {
        path = "${project.buildDir}/descriptors/${ssn}/known_types_${ssn}.desc"
        includeImports = true
        includeSourceInfo = true
    }
}

/**
 * Copies files from the [outputBaseDir][GenerateProtoTask.outputBaseDir] into
 * a subdirectory of [generatedDir][Project.generatedDir] for
 * the current [sourceSet][GenerateProtoTask.sourceSet].
 *
 * Also removes sources belonging to the `com.google` package in the target directory.
 */
private fun GenerateProtoTask.copyGeneratedFiles() {
    val generatedDir = project.generatedDir
    project.copy {
        from(outputBaseDir)
        into("$generatedDir/${sourceSet.name}")
    }
    deleteComGoogle(generatedDir, "java")
    deleteComGoogle(generatedDir, "kotlin")
}

/**
 * Remove the code generated for Google Protobuf library types.
 *
 * Java code for the `com.google` package was generated because we wanted
 * to have descriptors for all the types, including those from Google Protobuf library.
 * We want all the descriptors so that they are included into the resources used by
 * the `io.spine.type.KnownTypes` class.
 *
 * Now, as we have the descriptors _and_ excessive Java or Kotlin code, we delete it to avoid
 * classes that duplicate those coming from Protobuf library JARs.
 */
private fun Task.deleteComGoogle(generatedDir: String, language: String) {
    val comDirectory = File("${generatedDir}/$language/com")
    val googlePackage = comDirectory.resolve("google")
    project.delete(googlePackage)

    // If the `com` directory becomes empty, delete it too.
    if (comDirectory.exists() && comDirectory.isDirectory && comDirectory.list()!!.isEmpty()) {
        project.delete(comDirectory)
    }
}

/**
 * Exclude [GenerateProtoTask.outputBaseDir] from Java source set directories to avoid
 * duplicated source code files.
 */
private fun GenerateProtoTask.excludeProtocOutput() {
    val protocOutputDir = File(outputBaseDir)
    val generatedDir = project.generatedDir
    val ssn = sourceSet.name
    val java: SourceDirectorySet = sourceSet.java

    // The predicate to filter out files from `build/generated/source/proto` directory.
    val belongsToProtocOutput: (File) -> Boolean = { file -> file.residesIn(protocOutputDir) }

    val newSourceDirectories = java.sourceDirectories
        .filter { !belongsToProtocOutput(it) }
        .toSet()

    java.setSrcDirs(listOf<String>())
    java.srcDirs(newSourceDirectories)

    // Add copied files to the Java source set.
    val ssnJava = File("$generatedDir/$ssn/java/")
    val ssnKotlin = File("$generatedDir/$ssn/kotlin/")

    java.srcDir(ssnJava)
    java.srcDir(ssnKotlin)
}

/**
 * Make sure Kotlin compilation explicitly depends on this `GenerateProtoTask` to avoid racing.
 */
private fun GenerateProtoTask.setupKotlinCompile() {
    val kotlinCompile = project.kotlinCompileFor(sourceSet)
    kotlinCompile?.dependsOn(this)
}

/**
 * Make the tasks `processResources` depend on `generateProto` tasks explicitly so that:
 *  1) Descriptor set files get into resources, avoiding the racing conditions
 *     during the build.
 *
 *  2) We don't have the warning "Execution optimizations have been disabled..." issued
 *     by Gradle during the build because Protobuf Gradle Plugin does not set
 *     dependencies between `generateProto` and `processResources` tasks.
 */
private fun GenerateProtoTask.dependOnProcessResourcesTask() {
    val processResources = processResourceTaskName(sourceSet.name)
    project.tasks[processResources].dependsOn(this)
}

/**
 * Obtains the name of the task `processResource` task for the given source set name.
 */
private fun processResourceTaskName(sourceSetName: String): String {
    val infix = if (sourceSetName == "main") "" else sourceSetName.capitalized()
    return "process${infix}Resources"
}

/**
 * Attempts to obtain the Kotlin compilation Gradle task for the given source set.
 *
 * Typically, the task is named by a pattern: `compile<SourceSet name>Kotlin`, or just
 * `compileKotlin` if the source set name is `"main"`. If the task does not fit this described
 * pattern, this method will not find it.
 */
private fun Project.kotlinCompileFor(sourceSet: SourceSet): KotlinCompile<*>? {
    val taskName = sourceSet.getCompileTaskName("Kotlin")
    return tasks.findByName(taskName) as KotlinCompile<*>?
}

private fun File.residesIn(directory: File): Boolean =
    canonicalFile.startsWith(directory.absolutePath)
