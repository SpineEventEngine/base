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

package io.spine.tools.protojs.field;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsWriter;
import io.spine.tools.protojs.field.checker.FieldValueChecker;
import io.spine.tools.protojs.field.parser.FieldValueParser;

import static io.spine.tools.protojs.field.Fields.capitalizedName;
import static io.spine.tools.protojs.message.MessageHandler.MESSAGE_VAR;

public class MapFieldHandler extends AbstractFieldHandler {

    private static final String ATTRIBUTE_VAR = "attribute";
    private static final String MAP_KEY_VAR = "mapKey";

    private final FieldValueParser keyParser;

    // todo create builder for ctors with arg count > 3.
    MapFieldHandler(FieldDescriptor fieldDescriptor,
                    FieldValueChecker fieldValueChecker,
                    FieldValueParser keyParser,
                    FieldValueParser valueParser,
                    JsWriter jsWriter) {
        super(fieldDescriptor, fieldValueChecker, valueParser, jsWriter);
        this.keyParser = keyParser;
    }

    // todo try string format instead of concatenation everywhere
    // todo check js object for null
    @Override
    public void generateJs() {
        String jsObject = acquireJsObject();
        String value = iterateOwnAttributes(jsObject);
        parseMapKey();
        setValue(value);
        exitOwnAttributeIteration();
    }

    @Override
    String setterFormat() {
        String fieldName = capitalizedName(fieldDescriptor());
        String getMapCall = "get" + fieldName + "Map()";
        String setMapValueCall = "set(" + MAP_KEY_VAR + ", %s)";
        String addToMapFormat = MESSAGE_VAR + '.' + getMapCall + '.' + setMapValueCall + ';';
        return addToMapFormat;
    }

    private void parseMapKey() {
        keyParser.parseFieldValue(ATTRIBUTE_VAR, MAP_KEY_VAR);
    }

    private String iterateOwnAttributes(String jsObject) {
        jsWriter().enterIfBlock(jsObject + " !== undefined && " + jsObject + " !== null");
        jsWriter().enterBlock("for (let " + ATTRIBUTE_VAR + " in " + jsObject + ')');
        jsWriter().enterIfBlock(jsObject + ".hasOwnProperty(" + ATTRIBUTE_VAR + ')');
        String fieldValue = jsObject + '[' + ATTRIBUTE_VAR + ']';
        return fieldValue;
    }

    private void exitOwnAttributeIteration() {
        jsWriter().exitBlock();
        jsWriter().exitBlock();
        jsWriter().exitBlock();
    }
}
