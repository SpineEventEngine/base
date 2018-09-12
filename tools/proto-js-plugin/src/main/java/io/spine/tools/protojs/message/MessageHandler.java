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

package io.spine.tools.protojs.message;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.field.FieldHandler;
import io.spine.tools.protojs.field.FieldHandlers;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

/**
 * The generator of the {@code fromJson(json)} method for the given message.
 *
 * <p>The class also generates {@code fromObject(obj)} method which is used inside {@code fromJson}
 * and can be called to parse the JS proto message from the JS object.
 *
 * <p>The class is effectively {@code final} and is left non-{@code final} only for testing
 * purposes.
 *
 * @author Dmytro Kuzmin
 */
public class MessageHandler {

    /**
     * The argument name of the {@code fromObject} method.
     */
    public static final String FROM_OBJECT_ARG = "obj";

    /**
     * The name of the {@code fromObject} method return value.
     *
     * <p>This value represents the JS Proto message whose fields are parsed and set from the JS
     * object.
     */
    public static final String FROM_OBJECT_RETURN = "msg";

    /**
     * The argument name of the {@code fromJson} method.
     */
    @VisibleForTesting
    static final String FROM_JSON_ARG = "json";

    private final Descriptor message;
    private final JsGenerator jsGenerator;

    private MessageHandler(Descriptor message, JsGenerator jsGenerator) {
        this.message = message;
        this.jsGenerator = jsGenerator;
    }

    /**
     * Creates the {@code MessageHandler} for the given message and {@code JsGenerator}.
     *
     * @param message
     *         the {@code Descriptor} of the message type which will be parsed in JS
     * @param jsGenerator
     *         the {@code JsGenerator} to assist code generation and accumulate all the generated
     *         lines
     * @return the new {@code MessageHandler}
     */
    public static MessageHandler createFor(Descriptor message, JsGenerator jsGenerator) {
        checkNotNull(message);
        checkNotNull(jsGenerator);
        return new MessageHandler(message, jsGenerator);
    }

    /**
     * Generates the JS code necessary to handle the contained {@link #message}.
     *
     * <p>Adds the {@code fromJson(json)} and {@code fromObject(obj)} methods to the
     * {@code JsGenerator} code lines.
     */
    public void generateJs() {
        generateFromJsonMethod();
        generateFromObjectMethod();
    }

    /**
     * Generates the {@code fromJson} method which parses the JSON string via the {@code JSON.parse}
     * functionality and then calls {@code fromObject} for the parsed JS object.
     */
    @VisibleForTesting
    void generateFromJsonMethod() {
        jsGenerator.addEmptyLine();
        String typeName = typeWithProtoPrefix(message);
        String functionName = typeName + ".fromJson";
        addFromJsonCode(typeName, functionName);
    }

    /**
     * Generates the {@code fromObject} method which goes through the JS object fields iteratively,
     * adding the code to parse them and assign to the JS Proto message.
     *
     * <p>If the object is {@code null}, the returned value will be {@code null}.
     *
     * <p>See {@link FieldHandler} implementations.
     */
    @VisibleForTesting
    void generateFromObjectMethod() {
        jsGenerator.addEmptyLine();
        String typeName = typeWithProtoPrefix(message);
        String functionName = typeName + ".fromObject";
        addFromObjectCode(typeName, functionName);
    }

    /**
     * Adds the {@code fromJson} code to the {@link #jsGenerator}.
     */
    private void addFromJsonCode(String typeName, String functionName) {
        jsGenerator.enterFunction(functionName, FROM_JSON_ARG);
        jsGenerator.addLine("let jsonObject = JSON.parse(" + FROM_JSON_ARG + ");");
        jsGenerator.returnValue(typeName + ".fromObject(jsonObject)");
        jsGenerator.exitFunction();
    }

    /**
     * Adds the {@code fromObject} code to the {@link #jsGenerator}.
     */
    private void addFromObjectCode(String typeName, String functionName) {
        jsGenerator.enterFunction(functionName, FROM_OBJECT_ARG);
        checkParsedObject();
        jsGenerator.addEmptyLine();
        jsGenerator.addLine("let " + FROM_OBJECT_RETURN + " = new " + typeName + "();");
        handleMessageFields();
        jsGenerator.returnValue(FROM_OBJECT_RETURN);
        jsGenerator.exitFunction();
    }

    /**
     * Adds the code checking that {@linkplain #FROM_OBJECT_ARG from object argument} is not null.
     */
    private void checkParsedObject() {
        jsGenerator.ifNull(FROM_OBJECT_ARG);
        jsGenerator.returnValue("null");
        jsGenerator.exitBlock();
    }

    /**
     * Generates the code necessary to parse and set the {@link #message} fields.
     */
    @VisibleForTesting
    void handleMessageFields() {
        List<FieldDescriptor> fields = message.getFields();
        for (FieldDescriptor field : fields) {
            jsGenerator.addEmptyLine();
            FieldHandler fieldHandler = FieldHandlers.createFor(field, jsGenerator);
            fieldHandler.generateJs();
        }
    }
}
