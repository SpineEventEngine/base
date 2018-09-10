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

public final class MessageHandler {

    public static final String FROM_OBJECT_ARG = "obj";

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    public static final String MESSAGE = "message";

    private final Descriptor message;
    private final JsGenerator jsGenerator;

    private MessageHandler(Descriptor message, JsGenerator jsGenerator) {
        this.message = message;
        this.jsGenerator = jsGenerator;
    }

    public static MessageHandler createFor(Descriptor message, JsGenerator jsGenerator) {
        checkNotNull(message);
        checkNotNull(jsGenerator);
        return new MessageHandler(message, jsGenerator);
    }

    public void generateJs() {
        generateFromJsonMethod();
        generateFromObjectMethod();
    }

    @VisibleForTesting
    void generateFromJsonMethod() {
        jsGenerator.addEmptyLine();
        String typeName = typeWithProtoPrefix(message);
        String functionName = typeName + ".fromJson";
        addFromJsonCode(typeName, functionName);
    }

    @VisibleForTesting
    void generateFromObjectMethod() {
        jsGenerator.addEmptyLine();
        String typeName = typeWithProtoPrefix(message);
        String functionName = typeName + ".fromObject";
        addFromObjectCode(typeName, functionName);
    }

    private void addFromJsonCode(String typeName, String functionName) {
        jsGenerator.enterFunction(functionName, "json");
        jsGenerator.addLine("let jsonObject = JSON.parse(json);");
        jsGenerator.returnValue(typeName + ".fromObject(jsonObject)");
        jsGenerator.exitFunction();
    }

    private void addFromObjectCode(String typeName, String functionName) {
        jsGenerator.enterFunction(functionName, FROM_OBJECT_ARG);
        checkParsedObject();
        jsGenerator.addEmptyLine();
        jsGenerator.addLine("let " + MESSAGE + " = new " + typeName + "();");
        handleMessageFields();
        jsGenerator.returnValue(MESSAGE);
        jsGenerator.exitFunction();
    }

    private void checkParsedObject() {
        jsGenerator.ifNull(FROM_OBJECT_ARG);
        jsGenerator.returnValue("null");
        jsGenerator.exitBlock();
    }

    private void handleMessageFields() {
        List<FieldDescriptor> fields = message.getFields();
        for (FieldDescriptor field : fields) {
            jsGenerator.addEmptyLine();
            FieldHandler fieldHandler = FieldHandlers.createFor(field, jsGenerator);
            fieldHandler.generateJs();
        }
    }
}
