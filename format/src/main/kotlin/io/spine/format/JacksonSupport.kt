/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.format

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.spine.annotation.SPI

/**
 * The abstract base for classes dealing with I/O operations based
 * on the [Jackson](https://github.com/FasterXML) library.
 */
@SPI
public abstract class JacksonSupport {

    /**
     * The instance of [JsonFactory] used by the parser.
     */
    internal abstract val factory: JsonFactory

    /**
     * The lazily evaluated cached instance of the object mapper.
     *
     * After creation, the [ObjectMapper] instance registers [modules]
     * shared among object mappers.
     * So, if you need to have a Jackson [Module] available to all descendants
     * of the [JacksonSupport] class, add your module to the companion
     * object property [modules].
     *
     * If you need a [Module] only specific to your class,
     * please call [mapper.registerModule][ObjectMapper.registerModule] from the derived class.
     *
     * @see ObjectMapper.findAndRegisterModules
     */
    protected val mapper: ObjectMapper by lazy {
        ObjectMapper(factory)
            .registerModules(modules)
            .enable(SerializationFeature.INDENT_OUTPUT)
    }

    public companion object {

        /**
         * Provides a shared list of Jackson modules passed to an [ObjectMapper] during
         * its [creation][JacksonSupport.mapper].
         *
         * The initial lazily evaluated list contains the modules
         * [discovered][ObjectMapper.findModules] via the [java.util.ServiceLoader] API.
         * Therefore, custom Jackson modules that support this API should be discovered
         * from the classpath automatically.
         *
         * If a module does not support the [ServiceLoader][java.util.ServiceLoader] API,
         * you can [add][MutableList.add] the module directly.
         * But please make sure to do it _before_ accessing classes or objects
         * derived from [JacksonSupport].
         */
        public val modules: MutableList<Module> by lazy {
            ObjectMapper.findModules()
        }
    }
}
