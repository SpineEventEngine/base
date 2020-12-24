/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.field.parser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.VariableDeclaration;
import io.spine.js.generate.parse.GeneratedParser;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.js.generate.parse.GenerateKnownTypeParsers.TYPE_PARSERS_IMPORT_NAME;
import static java.lang.String.format;

/**
 * The value parser for the proto fields of {@code message} type.
 *
 * <p>Handles all {@code message} fields by calling {@code TypeParsers} registry.
 */
final class MessageFieldParser implements FieldParser {

    private static final String PARSER_BY_URL_METHOD = "parserFor";

    private final Descriptor message;
    private final CodeLines jsOutput;

    private MessageFieldParser(Descriptor message, CodeLines jsOutput) {
        this.message = message;
        this.jsOutput = jsOutput;
    }

    /**
     * Creates the {@code MessageFieldParser} for the given {@code field}.
     *
     * @param field
     *         the processed field
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all the generated code
     */
    static MessageFieldParser createFor(FieldDescriptor field, CodeLines jsOutput) {
        checkNotNull(field);
        checkNotNull(jsOutput);
        Descriptor messageType = field.getMessageType();
        return new MessageFieldParser(messageType, jsOutput);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        jsOutput.append(parsedVariable(variable, value));
    }

    private VariableDeclaration parsedVariable(String name, String valueToParse) {
        TypeUrl typeUrl = TypeUrl.from(message);
        String obtainParser = format("%s.%s('%s')",
                                     TYPE_PARSERS_IMPORT_NAME, PARSER_BY_URL_METHOD, typeUrl);
        String parserCall = GeneratedParser.parseMethodCall(obtainParser, valueToParse);
        return VariableDeclaration.initialized(name, parserCall);
    }
}
