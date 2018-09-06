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

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FieldName;
import io.spine.tools.fromjson.js.JsWriter;

import static io.spine.tools.fromjson.generator.MessageHandler.FROM_OBJECT_ARG;
import static io.spine.tools.fromjson.generator.MessageHandler.MESSAGE_VAR;

public class RepeatedFieldHandler implements FieldHandler {

    private static final String LIST_ITEM_VAR = "listItem";

    private final FieldDescriptor fieldDescriptor;
    private final FieldValueChecker fieldValueChecker;
    private final FieldValueParser fieldValueParser;
    private final JsWriter jsWriter;

    public RepeatedFieldHandler(FieldDescriptor fieldDescriptor,
                                FieldValueChecker fieldValueChecker,
                                FieldValueParser fieldValueParser,
                                JsWriter jsWriter) {
        this.fieldDescriptor = fieldDescriptor;
        this.fieldValueChecker = fieldValueChecker;
        this.fieldValueParser = fieldValueParser;
        this.jsWriter = jsWriter;
    }

    @Override
    public void writeJs() {
        String fieldJsonName = fieldDescriptor.getJsonName();
        String jsObject = FROM_OBJECT_ARG + '.' + fieldJsonName;

        String value = iterateListValues(jsObject);
        String addToListFormat = addToListFormat();
        fieldValueChecker.performNullCheck(value, addToListFormat);

        String fieldValue = "fieldValue";
        fieldValueParser.parseFieldValue(value, fieldValue);
        addToList(fieldValue);
        fieldValueChecker.exitNullCheck();
        exitListValueIteration();
    }

    private String iterateListValues(String jsObject) {
        jsWriter.enterIfBlock(jsObject + " !== undefined && " + jsObject + " !== null");
        jsWriter.addLine(jsObject + ".forEach(");
        jsWriter.increaseDepth();
        jsWriter.enterBlock('(' + LIST_ITEM_VAR + ", index, array) =>");
        return LIST_ITEM_VAR;
    }

    private void exitListValueIteration() {
        jsWriter.exitBlock();
        jsWriter.decreaseDepth();
        jsWriter.addLine(");");
        jsWriter.exitBlock();
    }

    private void addToList(String fieldValue) {
        String addToListFormat = addToListFormat();
        String addToList = String.format(addToListFormat, fieldValue);
        jsWriter.addLine(addToList);
    }

    private String addToListFormat() {
        FieldDescriptorProto fieldDescriptorProto = fieldDescriptor.toProto();
        String capitalizedFieldName = FieldName.of(fieldDescriptorProto)
                                               .toCamelCase();
        String addFunctionName = "add" + capitalizedFieldName;
        String addToListFormat = MESSAGE_VAR + '.' + addFunctionName + "(%s);";
        return addToListFormat;
    }
}
