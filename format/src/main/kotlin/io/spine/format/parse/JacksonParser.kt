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

package io.spine.format.parse

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.io.ByteSource
import java.nio.charset.Charset.defaultCharset

/**
 * The abstract base parsers of text-based formats backed by the Jackson library.
 */
internal sealed class JacksonParser : Parser {

    /**
     * The instance of [JsonFactory] used by the parser.
     */
    protected abstract val factory: JsonFactory

    /**
     * The lazily evaluated cached instance of the object matter.
     *
     * @see ObjectMapper.findAndRegisterModules
     */
    private val mapper: ObjectMapper by lazy {
        ObjectMapper(factory).findAndRegisterModules()
    }

    final override fun <T> parse(source: ByteSource, cls: Class<T>): T {
        val charSource = source.asCharSource(defaultCharset())
        return charSource.openBufferedStream().use {
            mapper.readValue(it, cls)
        }
    }
}

/**
 * Settings parser for JSON.
 */
internal data object JsonParser : JacksonParser() {
    override val factory: JsonFactory by lazy {
        JsonFactory()
    }
}

/**
 * Settings parser for YAML.
 */
internal data object YamlParser : JacksonParser() {
    override val factory: JsonFactory by lazy {
        YAMLFactory()
    }
}
