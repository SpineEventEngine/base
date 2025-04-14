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
 * An abstract base class for I/O operations using the
 * [Jackson](https://github.com/FasterXML) library.
 *
 * The primary subclasses of this class are the abstract types
 * [JacksonWriter][io.spine.format.write.JacksonWriter] and
 * [JacksonParser][io.spine.format.parse.JacksonParser], which provide
 * the foundation for reading from and writing to various data formats.
 *
 * To support a new data format beyond the existing [Format]s,
 * you will typically define a pair of classes extending
 * [JacksonWriter][io.spine.format.write.JacksonWriter] and
 * [JacksonParser][io.spine.format.parse.JacksonParser],
 * rather than subclassing this base class directly.
 *
 * ## Adding Support for a New Format
 * 1. Create a new writer class by extending
 *  [JacksonWriter][io.spine.format.write.JacksonWriter].
 * 2. Create a corresponding parser class by extending
 *  [JacksonParser][io.spine.format.parse.JacksonParser].
 * 3. Define an `object` that extends [Format], and provide instances
 *  of the new [writer][Format.writer] and [parser][Format.parser],
 *  along with the appropriate file [extension(s)][Format.extension].
 * 4. **If you are a contributor to the Spine SDK**, nest the new format `object`
 *  under the [Format] class. This helps maintain an enumeration-like structure.
 *  We avoid using a Kotlin `enum` here because we need generic parameters.
 *  Also, be sure to add the new `object` to the [Format.entries] list.
 */
@SPI
public abstract class JacksonSupport {

    /**
     * A factory used to create [JsonGenerator][com.fasterxml.jackson.core.JsonGenerator] and
     * [JsonParser][com.fasterxml.jackson.core.JsonParser] instances for a specific data format.
     *
     * Although the name `JsonFactory` suggests JSON-only support,
     * Jackson provides alternative factories for other data formats such as
     * [XML](https://github.com/FasterXML/jackson-dataformat-xml),
     * [YAML][com.fasterxml.jackson.dataformat.yaml.YAMLFactory], and other
     * [text-based formats](https://github.com/FasterXML/jackson-dataformats-text).
     *
     * Subclasses must implement this property by returning a factory appropriate
     * for the target format.
     */
    internal abstract val factory: JsonFactory

    /**
     * A lazily initialized and cached instance of [ObjectMapper] configured for the target format.
     *
     * Upon initialization, the mapper registers shared [modules], which are intended
     * to be available to all [JacksonSupport] subclasses.
     *
     * To contribute a shared [Module], modify the [modules] list in the companion object
     * **before** accessing this property or any class derived from [JacksonSupport].
     *
     * If a module should only apply to a specific subclass, register it within
     * that subclass using [ObjectMapper.registerModule].
     *
     * By default, this mapper is configured with [SerializationFeature.INDENT_OUTPUT] enabled.
     * To disable indentation, call [ObjectMapper.disable] as needed.
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
         * A shared list of Jackson [Module]s registered with each [ObjectMapper]
         * created by [JacksonSupport].
         *
         * This list is initialized lazily using [ObjectMapper.findModules], which discovers
         * modules via the [ServiceLoader][java.util.ServiceLoader] mechanism on the classpath.
         *
         * Modules that are not compatible with `ServiceLoader` can be added to this list manually.
         * This must be done **before** accessing any instance or `object` derived
         * from [JacksonSupport], to ensure proper registration.
         */
        public val modules: MutableList<Module> by lazy {
            ObjectMapper.findModules()
        }
    }
}
