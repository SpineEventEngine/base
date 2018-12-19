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
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.js.TypeName;
import io.spine.js.generate.CodeLines;
import io.spine.js.generate.Return;
import io.spine.js.generate.Snippet;
import io.spine.js.generate.parse.field.FieldGenerator;
import io.spine.js.generate.parse.field.FieldGenerators;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The generator of the {@code fromJson(json)} method for the given message type.
 *
 * <p>The class also generates the {@code fromObject(obj)} method which is used inside the
 * {@code fromJson} and can be called to parse the generated JS message from the JS object.
 *
 * <p>The class is effectively {@code final} and is left non-{@code final} only for testing
 * purposes.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// The generated code duplicates the code used in test that checks it.
public class FromJsonMethod implements Snippet {

    /**
     * The {@code fromJson} method name.
     */
    public static final String FROM_JSON = "fromJson";

    /**
     * The {@code fromObject} method name.
     */
    public static final String FROM_OBJECT = "fromObject";

    /**
     * The argument name of the {@code fromObject} method.
     */
    public static final String FROM_OBJECT_ARG = "obj";

    /**
     * The name of the {@code fromObject} method return value.
     *
     * <p>This value represents the generated JS message whose fields are parsed and set from the
     * JS object.
     */
    public static final String MESSAGE = "msg";

    /**
     * The argument name of the {@code fromJson} method.
     */
    @VisibleForTesting
    static final String FROM_JSON_ARG = "json";

    private final Descriptor message;

    private FromJsonMethod(Descriptor message) {
        this.message = message;
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
        CodeLines snippet = generateFromJsonMethod();
        snippet.addLinesFrom(generateFromObjectMethod());
        return snippet;
    }

    /**
     * Generates the {@code fromJson} method which parses the JSON string via the {@code JSON.parse}
     * functionality and then calls {@code fromObject} for the parsed JS object.
     */
    @VisibleForTesting
    CodeLines generateFromJsonMethod() {
        CodeLines snippet = new CodeLines();
        snippet.addEmptyLine();
        addFromJsonCode(message, snippet);
        return snippet;
    }

    /**
     * Generates the {@code fromObject} method, going through the JS object fields iteratively,
     * adding the code to parse them and assign to the JS message.
     *
     * <p>If the object is {@code null}, the returned value will be {@code null}.
     */
    @VisibleForTesting
    CodeLines generateFromObjectMethod() {
        CodeLines snippet = new CodeLines();
        snippet.addEmptyLine();
        addFromObjectCode(message, snippet);
        return snippet;
    }

    /**
     * Adds the {@code fromJson} code to the {@code jsOutput}.
     */
    private static void addFromJsonCode(Descriptor message, CodeLines output) {
        TypeName typeName = TypeName.from(message);
        String methodName = typeName.value() + '.' + FROM_JSON;
        output.enterMethod(methodName, FROM_JSON_ARG);
        output.declareVariable("jsObject", "JSON.parse(" + FROM_JSON_ARG + ')');
        output.returnValue(typeName.value() + '.' + FROM_OBJECT + "(jsObject)");
        output.exitMethod();
    }

    /**
     * Adds the {@code fromObject} code to the {@code jsOutput}.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") // Duplication in different context.
    private static void addFromObjectCode(Descriptor message, CodeLines output) {
        TypeName typeName = TypeName.from(message);
        String methodName = typeName.value() + '.' + FROM_OBJECT;
        output.enterMethod(methodName, FROM_OBJECT_ARG);
        checkParsedObject(output);
        output.addEmptyLine();
        output.declareVariable(MESSAGE, "new " + typeName + "()");
        handleMessageFields(output, message);
        output.returnValue(MESSAGE);
        output.exitMethod();
    }

    /**
     * Adds the code checking that {@code fromObject} argument is not null.
     */
    private static void checkParsedObject(CodeLines output) {
        output.ifNull(FROM_OBJECT_ARG);
        output.append(Return.nullReference());
        output.exitBlock();
    }

    /**
     * Adds the code necessary to parse and set the message fields.
     */
    @VisibleForTesting
    static void handleMessageFields(CodeLines output, Descriptor message) {
        for (FieldDescriptor field : message.getFields()) {
            output.addEmptyLine();
            FieldGenerator generator = FieldGenerators.createFor(field, output);
            generator.generate();
        }
    }
}
