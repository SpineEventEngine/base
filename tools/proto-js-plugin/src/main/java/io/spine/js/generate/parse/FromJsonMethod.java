/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.js.generate.parse;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.js.MethodReference;
import io.spine.code.js.TypeName;
import io.spine.js.generate.Snippet;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.Method;
import io.spine.js.generate.output.snippet.Return;
import io.spine.js.generate.output.snippet.VariableDeclaration;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.js.generate.output.CodeLine.emptyLine;

/**
 * The generator of the {@code fromJson(json)} method for the given message type.
 *
 * <p>The class also generates the {@code fromObject(obj)} method which is used inside the
 * {@code fromJson} and can be called to parse the generated JS message from the JS object.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// The generated code duplicates the code used in test that checks it.
public final class FromJsonMethod implements Snippet {

    /**
     * The {@code fromJson} method name.
     */
    @VisibleForTesting
    static final String FROM_JSON = "fromJson";

    /**
     * The {@code fromObject} method name.
     */
    @VisibleForTesting
    static final String FROM_OBJECT = "fromObject";

    /**
     * The argument name of the {@code fromJson} method.
     */
    @VisibleForTesting
    static final String FROM_JSON_ARG = "json";

    private final Descriptor message;
    private final GeneratedParser parser;

    private FromJsonMethod(Descriptor message) {
        this.message = message;
        this.parser = new GeneratedParser(message);
    }

    /**
     * Creates a new instance.
     *
     * @param message
     *         the {@code Descriptor} of the message type which will be parsed in JS
     * @return the new instance
     */
    public static FromJsonMethod createFor(Descriptor message) {
        checkNotNull(message);
        return new FromJsonMethod(message);
    }

    /**
     * Generates the JS code necessary to handle the contained {@code message}.
     *
     * <p>Adds the {@code fromJson(json)} and {@code fromObject(obj)} methods to the
     * {@code JsOutput} code lines.
     */
    @Override
    public CodeLines value() {
        CodeLines lines = new CodeLines();
        lines.append(fromJsonMethod());
        lines.append(emptyLine());
        lines.append(fromObjectMethod());
        lines.append(emptyLine());
        lines.append(parser);
        return lines;
    }

    /**
     * Generates the {@code fromObject} method, that calls the parser for the type.
     */
    @VisibleForTesting
    Method fromObjectMethod() {
        TypeName typeName = TypeName.from(message);
        MethodReference reference = MethodReference.onType(typeName, FROM_OBJECT);
        String parameterName = "obj";
        String parserVariable = "parser";
        VariableDeclaration newParser =
                VariableDeclaration.newInstance(parserVariable, parser.typeName());
        String callParser = GeneratedParser.parseMethodCall(parserVariable, parameterName);
        return Method
                .newBuilder(reference)
                .withParameters(parameterName)
                .appendToBody(newParser)
                .appendToBody(Return.value(callParser))
                .build();
    }

    /**
     * Obtains {@code fromJson} method for the specified message.
     *
     * <p>The {@code fromJson} parses the JSON string via the {@code JSON.parse}
     * functionality and then calls {@code fromObject} for the parsed JS object.
     */
    @VisibleForTesting
    Method fromJsonMethod() {
        TypeName typeName = TypeName.from(message);
        MethodReference reference = MethodReference.onType(typeName, FROM_JSON);
        return Method.newBuilder(reference)
                     .appendToBody(parsedValue())
                     .appendToBody(fromJsonReturn(typeName))
                     .build();
    }

    private static VariableDeclaration parsedValue() {
        String initializer = "JSON.parse(" + FROM_JSON_ARG + ')';
        return VariableDeclaration.initialized("jsObject", initializer);
    }

    private static Return fromJsonReturn(TypeName typeName) {
        String value = typeName.value() + '.' + FROM_OBJECT + "(jsObject)";
        return Return.value(value);
    }
}
