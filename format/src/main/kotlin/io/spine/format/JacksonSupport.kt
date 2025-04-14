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
 *
 * Direct descendants of this class are abstract classes
 * [JacksonWriter][io.spine.format.write.JacksonWriter] and
 * [JacksonParser][io.spine.format.parse.JacksonParser] which form the base
 * for reading and writing in operations in various formats.
 *
 * If you intend to support a new data format in addition to already [provided][Format],
 * you are likely to provide a pair of classes derived from
 * [JacksonWriter][io.spine.format.write.JacksonWriter] and
 * [JacksonParser][io.spine.format.parse.JacksonParser],
 * instead of extending this class.
 *
 * ## Supporting a new file format
 *  1. Create a new class for writing new data format by extending
 *  [JacksonWriter][io.spine.format.write.JacksonWriter].
 *  2. Create a new class for parsing the data format by extending
 *  [JacksonParser][io.spine.format.parse.JacksonParser].
 *  3. Create an `object` extending from the [Format] class providing
 *   instances of the new [writer][Format.writer] and [parser][Format.parser]
 *   along with [extension(s)][Format.extension] for the files of the new format.
 *  4. **If you are a committer of the Spine SDK**, please add the format `object`
 *   nesting it under the [Format] class.
 *   We need this to continue mimicking an enumeration.
 *   We cannot do it in a straight way using Kotlin `enum` because we need generic parameters.
 *   Also, *please remember* to add the `object` to the [Format.entries] list.
 */
@SPI
public abstract class JacksonSupport {

    /**
     * The factory class which constructs
     * [writers][com.fasterxml.jackson.core.JsonGenerator] and
     * [readers][com.fasterxml.jackson.core.JsonParser] of the Jackson library.
     *
     * Despite the prefix `Json` in the class name, there are factories that support
     * formats other than JSON, like [XML](https://bit.ly/jackson-xml-factory),
     * [YAML][com.fasterxml.jackson.dataformat.yaml.YAMLFactory], or
     * [another text format](https://github.com/FasterXML/jackson-dataformats-text).
     *
     * Implement this `abstract` property using the factory for your data format.
     */
    internal abstract val factory: JsonFactory

    /**
     * The lazily evaluated cached instance of the object mapper.
     *
     * After creation, the [ObjectMapper] instance registers [modules]
     * shared among object mappers.
     *
     * If you need to have a Jackson [Module] available to all
     * descendants of the [JacksonSupport] class, please see the documentation
     * of the companion object property [modules] on how to add a shared module.
     *
     * If you need a [Module] only specific to your class derived from [JacksonSupport],
     * please call [mapper.registerModule][ObjectMapper.registerModule] from the derived class.
     *
     * The [mapper] is created with [SerializationFeature.INDENT_OUTPUT] enabled.
     * If you do not need the indentation, please call [ObjectMapper.disable].
     *
     * @see modules
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
