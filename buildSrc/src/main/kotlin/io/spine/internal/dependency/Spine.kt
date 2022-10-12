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

package io.spine.internal.dependency

import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.extra

/**
 * Dependencies on Spine modules.
 *
 * @constructor
 * Creates a new instance of `Spine` taking the property values
 * of versions from the given project's extra properties.
 */
@Suppress("unused")
class Spine(p: ExtensionAware) {

    companion object {
        const val group = "io.spine"
        const val toolsGroup = "io.spine.tools"
    }

    val base = "$group:spine-base:${p.baseVersion}"
    val testlib = "$toolsGroup:spine-testlib:${p.baseVersion}"

    @Deprecated("Please use `validation.runtime`", replaceWith = ReplaceWith("validation.runtime"))
    val validate = "$group:spine-validate:${p.baseVersion}"

    val toolBase = "$toolsGroup:spine-tool-base:${p.toolBaseVersion}"
    val pluginBase = "$toolsGroup:spine-plugin-base:${p.toolBaseVersion}"
    val pluginTestlib = "$toolsGroup:spine-plugin-testlib:${p.toolBaseVersion}"

    val modelCompiler = "$toolsGroup:spine-model-compiler:${p.mcVersion}"

    val validation = Validation(p)

    private fun String.asExtra(p: ExtensionAware): String = p.extra[this] as String

    private val ExtensionAware.baseVersion: String get() = "baseVersion".asExtra(this)
    private val ExtensionAware.mcVersion: String get() = "mcVersion".asExtra(this)
    private val ExtensionAware.toolBaseVersion: String get() = "toolBaseVersion".asExtra(this)

    /**
     * Dependencies on Spine validation modules.
     *
     * See [`SpineEventEngine/validation`](https://github.com/SpineEventEngine/validation/).
     */
    class Validation(p: ExtensionAware) {

        companion object {
            const val group = "io.spine.validation"
        }

        val runtime = "$group:spine-validation-java-runtime:${p.validationVersion}"
        val java = "$group:spine-validation-java:${p.validationVersion}"
        val model = "$group:spine-validation-model:${p.validationVersion}"
        val config = "$group:spine-validation-configuration:${p.validationVersion}"

        private fun String.asExtra(p: ExtensionAware): String = p.extra[this] as String

        private val ExtensionAware.validationVersion: String
            get() = "validationVersion".asExtra(this)
    }

    /**
     * Dependencies on ProtoData modules.
     *
     * See [`SpineEventEngine/ProtoData`](https://github.com/SpineEventEngine/ProtoData/).
     */
    object ProtoData {

        const val pluginId = "io.spine.protodata"

        /**
         * The version of ProtoData.
         *
         * We declare ProtoData version here instead of `versions.gradle.kts` because we later use
         * it in a `plugins` section in a build script.
         */
        const val version = "0.2.18"
        const val pluginLib = "$group:protodata:$version"
    }
}
