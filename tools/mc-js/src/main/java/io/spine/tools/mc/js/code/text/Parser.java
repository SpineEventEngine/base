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

package io.spine.tools.mc.js.code.text;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.js.code.MethodReference;
import io.spine.tools.js.code.TypeName;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.Snippet;
import io.spine.tools.mc.js.code.field.FieldGenerator;
import io.spine.tools.mc.js.code.field.FieldGenerators;
import io.spine.tools.mc.js.code.field.FieldToParse;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.code.CodeLine.emptyLine;
import static java.lang.String.format;

/**
 * The code of a generated parser for a Protobuf message.
 *
 * <p>This parser should be generated for all messages except standard ones
 * like {@code Any}, {@code int32}, {@code Timestamp}. The parsers for these
 * standard types are manually created and require no code generation.
 *
 * <p>Code provided by the class is in {@code ES5} standard
 * since Protobuf compiler generates JavaScript in {@code ES5}.
 */
public final class Parser implements Snippet {

    /**
     * The name of the import of parsers registry.
     *
     * <p>Visible so the other generators such as a
     * {@linkplain FieldGenerator field} can use the import.
     */
    public static final String TYPE_PARSERS_IMPORT_NAME = "TypeParsers";
    /** The name of the {@code object-parser.js} import. */
    public static final String ABSTRACT_PARSER_IMPORT_NAME = "ObjectParser";
    /**
     * The name of the {@code fromObject} method return value.
     *
     * <p>This value represents the generated JS message whose fields are parsed and set from the
     * JS object.
     */
    private static final String MESSAGE = "msg";
    /** The parameter name of the {@code fromObject} method. */
    @VisibleForTesting
    static final String FROM_OBJECT_ARG = "obj";
    /** The name of the method declared on an abstract parser. */
    @VisibleForTesting
    static final String PARSE_METHOD = "fromObject";
    /**
     * The relative path from the Protobuf root directory to the folder
     * containing sources related to parsing.
     *
     * <p>The path depends on the Spine Web layout.
     */
    private static final String IMPORT_PATH_PREFIX = "../client/parser/";

    public static final String OBJECT_PARSER_FILE = IMPORT_PATH_PREFIX + "object-parser.js";

    public static final String TYPE_PARSERS_FILE = IMPORT_PATH_PREFIX + "type-parsers.js";

    /** The message to generate the parser for. */
    private final Descriptor message;

    public Parser(Descriptor message) {
        checkNotNull(message);
        this.message = message;
    }

    @Override
    public CodeWriter value() {
        CodeWriter lines = new CodeWriter();
        lines.append(constructor());
        lines.append(initPrototype());
        lines.append(initConstructor());
        lines.append(fromObjectMethod());
        return lines;
    }

    /**
     * Obtains the string representing a call to a method parsing an object into a message.
     *
     * <p>The method can be used to reference the a call to a handcrafted parser as well
     * as for a generated parser.
     *
     * @param parserVariable
     *         the name of the parser variable
     * @param valueToParse
     *         the object to parse
     */
    public static String parseMethodCall(String parserVariable, String valueToParse) {
        String result = format("%s.%s(%s)", parserVariable, PARSE_METHOD, valueToParse);
        return result;
    }

    /**
     * Obtains the type of the parser to be generated.
     */
    TypeName typeName() {
        return TypeName.ofParser(message);
    }

    private Method constructor() {
        MethodReference reference = MethodReference.constructor(typeName());
        String callSuper = format("%s.call(this);", superClass());
        return Method.newBuilder(reference)
                .appendToBody(callSuper)
                .build();
    }

    private String initPrototype() {
        String result = format("%s = Object.create(%s.prototype);",
                               prototypeReference(), superClass());
        return result;
    }

    private String initConstructor() {
        MethodReference reference = MethodReference.onPrototype(typeName(), "constructor");
        String result = format("%s = %s;", reference, typeName());
        return result;
    }

    /**
     * Generates the {@code fromObject} method, going through the JS object fields iteratively,
     * adding the code to parse them and assign to the JS message.
     *
     * <p>If the object is {@code null}, the returned value will be {@code null}.
     */
    @VisibleForTesting
    CodeWriter fromObjectMethod() {
        String methodName = MethodReference.onPrototype(typeName(), PARSE_METHOD)
                                           .value();
        CodeWriter lines = new CodeWriter();
        lines.enterMethod(methodName, FROM_OBJECT_ARG);
        checkParsedObject(lines);
        lines.append(emptyLine());
        lines.append(initializedMessageInstance(message));
        lines.append(parseFields(message));
        lines.append(Return.value(MESSAGE));
        lines.exitMethod();
        return lines;
    }

    /**
     * Adds the code checking that {@code fromObject} argument is not null.
     */
    private static void checkParsedObject(CodeWriter output) {
        output.ifNull(FROM_OBJECT_ARG);
        output.append(Return.nullReference());
        output.exitBlock();
    }

    private static Let initializedMessageInstance(Descriptor message) {
        TypeName typeName = TypeName.from(message);
        return Let.newInstance(MESSAGE, typeName);
    }

    /**
     * Obtains the code necessary to parse and set the message fields.
     */
    private static CodeWriter parseFields(Descriptor message) {
        CodeWriter lines = new CodeWriter();
        for (FieldDescriptor field : message.getFields()) {
            lines.append(emptyLine());
            FieldToParse fieldToParse = new FieldToParse(field, FROM_OBJECT_ARG, MESSAGE);
            FieldGenerator generator = FieldGenerators.createFor(fieldToParse, lines);
            generator.generate();
        }
        return lines;
    }

    /**
     * Obtains the reference to the prototype of the parser.
     */
    private String prototypeReference() {
        return typeName() + ".prototype";
    }

    /**
     * Obtains the name of the imported abstract parser.
     */
    private static String superClass() {
        return ABSTRACT_PARSER_IMPORT_NAME;
    }
}
