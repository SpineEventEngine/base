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

package io.spine.tools.fromjson.generator;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.fromjson.js.JsWriter;

import java.util.List;

// todo add check not null everywhere
final class MessageHandler {

    static final String FROM_OBJECT_ARG = "obj";
    static final String MESSAGE_VAR = "message";

    private final Descriptor messageDescriptor;
    private final JsWriter jsWriter;

    MessageHandler(Descriptor messageDescriptor, JsWriter jsWriter) {
        this.messageDescriptor = messageDescriptor;
        this.jsWriter = jsWriter;
    }

    void generateJs() {
        generateFromJson();
        generateFromObject();
    }

    private void generateFromJson() {
        jsWriter.addEmptyLine();

        String fullTypeName = messageDescriptor.getFullName();
        String typeWithProtoPrefix = "proto." + fullTypeName;
        String methodName = typeWithProtoPrefix + ".fromJson";

        jsWriter.enterFunction(methodName, "json");
        jsWriter.addLine("let jsonObject = JSON.parse(json);");
        jsWriter.addLine("return " + typeWithProtoPrefix + ".fromObject(jsonObject);");
        jsWriter.exitFunction();
    }

    private void generateFromObject() {
        jsWriter.addEmptyLine();

        String fullTypeName = messageDescriptor.getFullName();
        String typeWithProtoPrefix = "proto." + fullTypeName;

        jsWriter.enterFunction(typeWithProtoPrefix + ".fromObject", FROM_OBJECT_ARG);
        jsWriter.addLine("let " + MESSAGE_VAR + " = new " + typeWithProtoPrefix + "();");

        List<FieldDescriptor> fields = messageDescriptor.getFields();
        for (FieldDescriptor fieldDescriptor : fields) {
            jsWriter.addEmptyLine();
            // todo try jsWriter as a writeJs() param
            FieldHandler fieldHandler = FieldHandlers.createFor(fieldDescriptor, jsWriter);
            fieldHandler.writeJs();
        }
        jsWriter.exitFunction();
    }
}
