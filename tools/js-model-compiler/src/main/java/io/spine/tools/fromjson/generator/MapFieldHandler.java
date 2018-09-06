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

public class MapFieldHandler implements FieldHandler {

    private static final String ATTRIBUTE_VAR = "attribute";
    private static final String MAP_KEY_VAR = "mapKey";

    private final FieldDescriptor fieldDescriptor;
    private final FieldValueChecker fieldValueChecker;
    private final FieldValueParser keyParser;
    private final FieldValueParser valueParser;
    private final JsWriter jsWriter;

    // todo create builder for ctors with arg count > 3.

    public MapFieldHandler(FieldDescriptor fieldDescriptor,
                           FieldValueChecker fieldValueChecker,
                           FieldValueParser keyParser,
                           FieldValueParser valueParser,
                           JsWriter jsWriter) {
        this.fieldDescriptor = fieldDescriptor;
        this.fieldValueChecker = fieldValueChecker;
        this.keyParser = keyParser;
        this.valueParser = valueParser;
        this.jsWriter = jsWriter;
    }

    // todo try string format instead of concatenation everywhere
    // todo check js object for null
    @Override
    public void writeJs() {
        String fieldJsonName = fieldDescriptor.getJsonName();
        String jsObject = FROM_OBJECT_ARG + '.' + fieldJsonName;

        String value = iterateOwnAttributes(jsObject);

        keyParser.parseFieldValue(ATTRIBUTE_VAR, MAP_KEY_VAR);
        String addToMapFormat = addToMapFormat();
        fieldValueChecker.performNullCheck(value, addToMapFormat);

        String fieldValue = "fieldValue";
        valueParser.parseFieldValue(value, fieldValue);
        addToMap(fieldValue);
        fieldValueChecker.exitNullCheck();
        exitOwnAttributeIteration();
    }

    private String iterateOwnAttributes(String jsObject) {
        jsWriter.enterIfBlock(jsObject + " !== undefined && " + jsObject + " !== null");
        jsWriter.enterBlock("for (let " + ATTRIBUTE_VAR + " in " + jsObject + ')');
        jsWriter.enterIfBlock(jsObject + ".hasOwnProperty(" + ATTRIBUTE_VAR + ')');
        String fieldValue = jsObject + '[' + ATTRIBUTE_VAR + ']';
        return fieldValue;
    }

    private void exitOwnAttributeIteration() {
        jsWriter.exitBlock();
        jsWriter.exitBlock();
        jsWriter.exitBlock();
    }

    private void addToMap(String value) {
        String addToMapFormat = addToMapFormat();
        String addToMap = String.format(addToMapFormat, value);
        jsWriter.addLine(addToMap);
    }

    private String addToMapFormat() {
        FieldDescriptorProto fieldDescriptorProto = fieldDescriptor.toProto();
        String capitalizedFieldName = FieldName.of(fieldDescriptorProto)
                                               .toCamelCase();
        String getMapCall = "get" + capitalizedFieldName + "Map()";
        String setMapValueCall = "set(" + MAP_KEY_VAR + ", %s)";
        String addStatementFormat = MESSAGE_VAR + '.' + getMapCall + '.' + setMapValueCall + ';';
        return addStatementFormat;
    }
}
