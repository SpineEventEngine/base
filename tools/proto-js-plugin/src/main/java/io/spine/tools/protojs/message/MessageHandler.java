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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsWriter;
import io.spine.tools.protojs.field.FieldHandler;
import io.spine.tools.protojs.field.FieldHandlers;

import java.util.List;

import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

// todo add check not null everywhere
public final class MessageHandler {

    public static final String FROM_OBJECT_ARG = "obj";
    public static final String MESSAGE_VAR = "message";

    private final Descriptor messageDescriptor;
    private final JsWriter jsWriter;

    public MessageHandler(Descriptor messageDescriptor, JsWriter jsWriter) {
        this.messageDescriptor = messageDescriptor;
        this.jsWriter = jsWriter;
    }

    public void generateJs() {
        generateFromJsonMethod();
        generateFromObjectMethod();
    }

    private void generateFromJsonMethod() {
        jsWriter.addEmptyLine();
        String typeName = typeWithProtoPrefix(messageDescriptor);
        String functionName = typeName + ".fromJson";
        generateFromJsonCode(typeName, functionName);
    }

    private void generateFromJsonCode(String typeName, String functionName) {
        jsWriter.enterFunction(functionName, "json");
        jsWriter.addLine("let jsonObject = JSON.parse(json);");
        // todo add return helper to jsWriter
        jsWriter.addLine("return " + typeName + ".fromObject(jsonObject);");
        jsWriter.exitFunction();
    }

    private void generateFromObjectMethod() {
        jsWriter.addEmptyLine();
        String typeName = typeWithProtoPrefix(messageDescriptor);
        String functionName = typeName + ".fromObject";
        generateFromObjectCode(typeName, functionName);
    }

    private void generateFromObjectCode(String typeName, String functionName) {
        jsWriter.enterFunction(functionName, FROM_OBJECT_ARG);
        jsWriter.addLine("let " + MESSAGE_VAR + " = new " + typeName + "();");
        generateFieldsCode();
        jsWriter.addLine("return " + MESSAGE_VAR + ';');
        jsWriter.exitFunction();
    }

    private void generateFieldsCode() {
        List<FieldDescriptor> fields = messageDescriptor.getFields();
        for (FieldDescriptor fieldDescriptor : fields) {
            jsWriter.addEmptyLine();
            FieldHandler fieldHandler = FieldHandlers.createFor(fieldDescriptor, jsWriter);
            fieldHandler.generateJs();
        }
    }
}
