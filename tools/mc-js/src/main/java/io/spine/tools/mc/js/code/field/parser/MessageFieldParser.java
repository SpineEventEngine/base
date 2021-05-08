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

package io.spine.tools.mc.js.code.field.parser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.text.Let;
import io.spine.tools.mc.js.code.text.Parser;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.mc.js.code.text.Parser.TYPE_PARSERS_IMPORT_NAME;
import static java.lang.String.format;

/**
 * The value parser for the proto fields of {@code message} type.
 *
 * <p>Handles all {@code message} fields by calling {@code TypeParsers} registry.
 */
final class MessageFieldParser implements FieldParser {

    private static final String PARSER_BY_URL_METHOD = "parserFor";

    private final Descriptor message;
    private final CodeWriter writer;

    /**
     * Creates the {@code MessageFieldParser} for the given {@code field}.
     *
     * @param field
     *         the processed field
     * @param writer
     *         the output which accumulates all the generated code
     */
    MessageFieldParser(FieldDescriptor field, CodeWriter writer) {
        checkNotNull(field);
        this.message = field.getMessageType();
        this.writer = checkNotNull(writer);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        writer.append(parsedVariable(variable, value));
    }

    private Let parsedVariable(String name, String valueToParse) {
        TypeUrl typeUrl = TypeUrl.from(message);
        String obtainParser = format("%s.%s('%s')",
                                     TYPE_PARSERS_IMPORT_NAME, PARSER_BY_URL_METHOD, typeUrl);
        String parserCall = Parser.parseMethodCall(obtainParser, valueToParse);
        return Let.withValue(name, parserCall);
    }
}
