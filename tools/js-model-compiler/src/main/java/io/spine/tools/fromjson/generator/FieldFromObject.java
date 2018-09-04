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
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.NullValue;
import io.spine.code.proto.FieldName;
import io.spine.tools.fromjson.js.JsOutput;
import io.spine.tools.fromjson.js.JsWriter;
import io.spine.type.TypeUrl;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static io.spine.util.Exceptions.newIllegalStateException;

class FieldFromObject {

    private static final String MESSAGE_VAR_NAME = "message";
    private static final String NULL = "null";

    private final FieldDescriptor fieldDescriptor;
    private JsWriter jsWriter;

    private FieldFromObject(FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
    }

    static FieldFromObject forField(FieldDescriptor fieldDescriptor) {
        return new FieldFromObject(fieldDescriptor);
    }

    JsOutput generateCode() {
        jsWriter = new JsWriter();
        doGenerateCode();
        JsOutput generatedCode = jsWriter.getGeneratedCode();
        return generatedCode;
    }

    private void doGenerateCode() {
        String jsonName = fieldDescriptor.getJsonName();
        String jsonObject = "obj." + jsonName;
        checkJsonObject(jsonObject, fieldDescriptor);
        parseJsonObject(jsonObject, fieldDescriptor);
        jsWriter.exitBlock();
    }

    private void checkJsonObject(String jsonObject, FieldDescriptor fieldDescriptor) {
        Label label = fieldDescriptor.toProto()
                                     .getLabel();
        String nullValueType = NullValue.getDescriptor()
                                        .getFullName();
        boolean isEnumField = fieldDescriptor.getType() == ENUM;
        boolean isNullValue = isEnumField
                && getEnumTypeName(fieldDescriptor).equals(nullValueType);
        boolean isNotMessage = fieldDescriptor.getType() != MESSAGE;
        boolean isRepeatedOrMap = label == LABEL_REPEATED;
        boolean checkForNull = !isNullValue && (isNotMessage || isRepeatedOrMap);
        String condition = checkForNull
                ? jsonObject + " !== undefined && " + jsonObject + " !== null"
                : jsonObject + " !== undefined";
        jsWriter.enterIfBlock(condition);
    }

    private void parseJsonObject(String jsonObject, FieldDescriptor fieldDescriptor) {
        if (isMap(fieldDescriptor)) {
            parseMapField(jsonObject, fieldDescriptor);
        } else if (isRepeated(fieldDescriptor)) {
            parseRepeatedField(jsonObject, fieldDescriptor);
        } else {
            parseField(jsonObject, fieldDescriptor);
        }
    }

    private boolean isMap(FieldDescriptor fieldDescriptor) {
        if (fieldDescriptor.toProto()
                           .getLabel() != LABEL_REPEATED) {
            return false;
        }
        if (fieldDescriptor.getType() != MESSAGE) {
            return false;
        }
        Descriptor fieldType = fieldDescriptor.getMessageType();
        String capitalizedName = capitalizedFieldName(fieldDescriptor);
        String supposedNameForMap = capitalizedName + "Entry";
        boolean isMap = fieldType.getName()
                                 .equals(supposedNameForMap);
        return isMap;
    }

    private void parseMapField(String jsonObject, FieldDescriptor fieldDescriptor) {
        String attributeVarName = iterateOwnAttributes(jsonObject);
        addValueToMap(jsonObject, attributeVarName, fieldDescriptor);
        exitOwnAttributeIteration();
    }

    private String iterateOwnAttributes(String jsonObject) {
        String attributeVarName = "attr";
        jsWriter.enterBlock("for (let " + attributeVarName + " in " + jsonObject + ')');
        jsWriter.enterIfBlock(jsonObject + ".hasOwnProperty(" + attributeVarName + ')');
        return attributeVarName;
    }

    private void exitOwnAttributeIteration() {
        jsWriter.exitBlock();
        jsWriter.exitBlock();
    }

    private void
    addValueToMap(String jsonObject, String attributeVarName, FieldDescriptor fieldDescriptor) {
        String jsonValue = jsonObject + '[' + attributeVarName + ']';
        if (isMessage(fieldDescriptor)) {
            enterNullCheck(jsonValue);
            addToMapField(NULL, attributeVarName, fieldDescriptor);
            jsWriter.enterElseBlock();
        } else {
            enterNotNullCheck(jsonValue);
        }
        String valueVarName = parseValue(jsonValue, fieldDescriptor);
        addToMapField(valueVarName, attributeVarName, fieldDescriptor);
        jsWriter.exitBlock();
    }

    private void addToMapField(String valueVarName, String attributeVarName,
                               FieldDescriptor fieldDescriptor) {
        String getterCall = "get" + capitalizedFieldName(fieldDescriptor) + "Map()";
        String setMapValueCall = "set(" + attributeVarName + ", " + valueVarName + ')';
        jsWriter.addLine(MESSAGE_VAR_NAME + '.' + getterCall + '.' + setMapValueCall + ';');
    }

    private boolean isRepeated(FieldDescriptor fieldDescriptor) {
        FieldDescriptorProto descriptorProto = fieldDescriptor.toProto();
        boolean isRepeated =
                descriptorProto.getLabel() == LABEL_REPEATED && !isMap(fieldDescriptor);
        return isRepeated;
    }

    private void parseRepeatedField(String jsonObject, FieldDescriptor fieldDescriptor) {
        String listItemVarName = iterateListItems(jsonObject);
        addValueToList(listItemVarName, fieldDescriptor);
        exitListItemIteration();
    }

    private String iterateListItems(String jsonObject) {
        jsWriter.addLine(jsonObject + ".forEach(");
        jsWriter.increaseDepth();
        String listItemVarName = "listItem";
        jsWriter.enterBlock('(' + listItemVarName + "index, array) =>");
        return listItemVarName;
    }

    private void exitListItemIteration() {
        jsWriter.exitBlock();
        jsWriter.addLine(");");
        jsWriter.decreaseDepth();
    }

    private void addValueToList(String listItemVarName, FieldDescriptor fieldDescriptor) {
        if (isMessage(fieldDescriptor)) {
            enterNullCheck(listItemVarName);
            addToList(NULL, fieldDescriptor);
            jsWriter.enterElseBlock();
        } else {
            enterNotNullCheck(listItemVarName);
        }
        String valueVarName = parseValue(listItemVarName, fieldDescriptor);
        addToList(valueVarName, fieldDescriptor);
        jsWriter.exitBlock();
    }

    private void addToList(String valueVarName, FieldDescriptor fieldDescriptor) {
        String addFunctionName = "add" + capitalizedFieldName(fieldDescriptor);
        jsWriter.addLine(MESSAGE_VAR_NAME + '.' + addFunctionName + '(' + valueVarName + ");");
    }

    private void parseField(String jsonObject, FieldDescriptor fieldDescriptor) {
        if (isMessage(fieldDescriptor)) {
            enterNullCheck(jsonObject);
            setField(NULL, fieldDescriptor);
            jsWriter.enterElseBlock();
        } else {
            enterNotNullCheck(jsonObject);
        }
        String valueVarName = parseValue(jsonObject, fieldDescriptor);
        setField(valueVarName, fieldDescriptor);
        jsWriter.exitBlock();
    }

    private void setField(String valueVarName, FieldDescriptor fieldDescriptor) {
        String setterName = "set" + capitalizedFieldName(fieldDescriptor);
        jsWriter.addLine(MESSAGE_VAR_NAME + '.' + setterName + '(' + valueVarName + ");");
    }

    private void enterNullCheck(String jsonValue) {
        jsWriter.enterIfBlock(jsonValue + " === null");
    }

    private void enterNotNullCheck(String jsonValue) {
        jsWriter.enterIfBlock(jsonValue + " !== null");
    }

    private String parseValue(String value, FieldDescriptor fieldDescriptor) {
        String valueVarName = "value";
        if (isMessage(fieldDescriptor)) {
            if (isWellKnownType(fieldDescriptor)) {
                setWithParser(valueVarName, value, fieldDescriptor);
            } else {
                setViaRecursiveCall(valueVarName, value, fieldDescriptor);
            }
        } else {
            setValue(valueVarName, value);
        }
        return valueVarName;
    }

    private void setValue(String valueVarName, String jsonObject) {
        jsWriter.addLine("let " + valueVarName + " = " + jsonObject + ';');
    }

    private void setWithParser(String valueVarName, String jsonObject,
                               FieldDescriptor fieldDescriptor) {
        Descriptor fieldType = fieldDescriptor.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(fieldType);

        jsWriter.addLine("let type = " + KnownTypesJsGenerator.FILE_NAME + '.' +
                                    KnownTypesJsGenerator.MAP_NAME + ".get('" + typeUrl + "');");
        jsWriter.addLine("let parser = " + KnownTypeParsersGenerator.FILE_NAME + '.' +
                                    KnownTypeParsersGenerator.MAP_NAME + ".get(type);");
        jsWriter.addLine("let " + valueVarName + " = parser.parse(" + jsonObject + ");");
    }

    private void setViaRecursiveCall(String valueVarName, String jsonObject,
                                     FieldDescriptor fieldDescriptor) {
        Descriptor fieldType = fieldDescriptor.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(fieldType);

        jsWriter.addLine("let type = " + KnownTypesJsGenerator.FILE_NAME + '.' +
                                    KnownTypesJsGenerator.MAP_NAME + ".get('" + typeUrl + "');");
        jsWriter.addLine("let " + valueVarName + " = type.fromObject(" + jsonObject + ");");
    }

    private boolean isMessage(FieldDescriptor fieldDescriptor) {
        FieldDescriptor.Type fieldKind = fieldDescriptor.getType();
        boolean isMessage = fieldKind == MESSAGE;
        return isMessage;
    }

    private boolean isWellKnownType(FieldDescriptor fieldDescriptor) {
        Descriptor fieldType = fieldDescriptor.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(fieldType);
        boolean isWellKnownType = KnownTypeParsersGenerator.WELL_KNOWN_TYPES.contains(typeUrl);
        return isWellKnownType;
    }

    private static String getEnumTypeName(FieldDescriptor fieldDescriptor) {
        FieldDescriptor.Type fieldTypeKind = fieldDescriptor.getType();
        if (fieldTypeKind != ENUM) {
            throw newIllegalStateException("Expected %s field type, instead got %s",
                                           ENUM.name(), fieldTypeKind.name());
        }
        EnumDescriptor fieldTypeDescriptor = fieldDescriptor.getEnumType();
        String fieldTypeName = fieldTypeDescriptor.getFullName();
        return fieldTypeName;
    }

    private static String capitalizedFieldName(FieldDescriptor fieldDescriptor) {
        String name = fieldDescriptor.getName();
        FieldName fieldName = FieldName.of(name);
        String camelCaseName = fieldName.toCamelCase();
        return camelCaseName;
    }
}
