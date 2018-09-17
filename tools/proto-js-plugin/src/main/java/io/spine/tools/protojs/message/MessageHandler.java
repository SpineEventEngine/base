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
import io.spine.tools.protojs.generate.JsOutput;
import io.spine.tools.protojs.field.FieldHandler;
import io.spine.tools.protojs.field.FieldHandlers;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

/**
 * The generator of the {@code fromJson(json)} method for the given message type.
 *
 * <p>The class also generates the {@code fromObject(obj)} method which is used inside the
 * {@code fromJson} and can be called to parse the JS proto message from the JS object.
 *
 * <p>The class is effectively {@code final} and is left non-{@code final} only for testing
 * purposes.
 *
 * @apiNote
 * Like the other handlers and generators of this module, the {@code MessageHandler} is meant to
 * operate on the common {@link JsOutput} passed on construction and thus its methods do not return
 * any generated code.
 *
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// The generated code duplicates the code used in test that checks it.
public class MessageHandler {

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
     * <p>This value represents the JS Proto message whose fields are parsed and set from the JS
     * object.
     */
    public static final String MESSAGE = "msg";

    /**
     * The argument name of the {@code fromJson} method.
     */
    @VisibleForTesting
    static final String FROM_JSON_ARG = "json";

    private final Descriptor message;
    private final JsOutput jsOutput;

    private MessageHandler(Descriptor message, JsOutput jsOutput) {
        this.message = message;
        this.jsOutput = jsOutput;
    }

    /**
     * Creates the {@code MessageHandler} for the given message and {@code JsOutput}.
     *
     * @param message
     *         the {@code Descriptor} of the message type which will be parsed in JS
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all the generated lines
     * @return the new {@code MessageHandler}
     */
    public static MessageHandler createFor(Descriptor message, JsOutput jsOutput) {
        checkNotNull(message);
        checkNotNull(jsOutput);
        return new MessageHandler(message, jsOutput);
    }

    /**
     * Generates the JS code necessary to handle the contained {@code message}.
     *
     * <p>Adds the {@code fromJson(json)} and {@code fromObject(obj)} methods to the
     * {@code JsOutput} code lines.
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
        jsOutput.addEmptyLine();
        String typeName = typeWithProtoPrefix(message);
        addFromJsonCode(typeName);
    }

    /**
     * Generates the {@code fromObject} method, going through the JS object fields iteratively,
     * adding the code to parse them and assign to the JS Proto message.
     *
     * <p>If the object is {@code null}, the returned value will be {@code null}.
     */
    @VisibleForTesting
    void generateFromObjectMethod() {
        jsOutput.addEmptyLine();
        String typeName = typeWithProtoPrefix(message);
        addFromObjectCode(typeName);
    }

    /**
     * Adds the {@code fromJson} code to the {@code jsOutput}.
     */
    private void addFromJsonCode(String typeName) {
        String methodName = typeName + '.' + FROM_JSON;
        jsOutput.enterMethod(methodName, FROM_JSON_ARG);
        jsOutput.declareVariable("jsObject", "JSON.parse(" + FROM_JSON_ARG + ')');
        jsOutput.returnValue(typeName + '.' + FROM_OBJECT + "(jsonObject)");
        jsOutput.exitFunction();
    }

    /**
     * Adds the {@code fromObject} code to the {@code jsOutput}.
     */
    private void addFromObjectCode(String typeName) {
        String methodName = typeName + '.' + FROM_OBJECT;
        jsOutput.enterMethod(methodName, FROM_OBJECT_ARG);
        checkParsedObject();
        jsOutput.addEmptyLine();
        jsOutput.declareVariable(MESSAGE, "new " + typeName + "()");
        handleMessageFields();
        jsOutput.returnValue(MESSAGE);
        jsOutput.exitFunction();
    }

    /**
     * Adds the code checking that {@code fromObject} argument is not null.
     */
    private void checkParsedObject() {
        jsOutput.ifNull(FROM_OBJECT_ARG);
        jsOutput.returnValue("null");
        jsOutput.exitBlock();
    }

    /**
     * Adds the code necessary to parse and set the message fields.
     */
    @VisibleForTesting
    void handleMessageFields() {
        List<FieldDescriptor> fields = message.getFields();
        for (FieldDescriptor field : fields) {
            jsOutput.addEmptyLine();
            FieldHandler fieldHandler = FieldHandlers.createFor(field, jsOutput);
            fieldHandler.generateJs();
        }
    }
}
