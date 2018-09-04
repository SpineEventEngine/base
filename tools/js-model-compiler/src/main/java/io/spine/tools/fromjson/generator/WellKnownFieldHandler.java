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
import io.spine.type.TypeUrl;

final class WellKnownFieldHandler extends AbstractFieldHandler {

    private final Descriptor fieldType;

    // todo create builder for ctors with arg count > 3.
    WellKnownFieldHandler(Descriptor fieldType,
                          FieldDescriptor fieldDescriptor,
                          JsObjectAccessor jsObjectAccessor,
                          FieldSetter fieldSetter,
                          JsWriter jsWriter) {
        super(fieldDescriptor, jsObjectAccessor, fieldSetter, jsWriter);
        this.fieldType = fieldType;
    }

    @Override
    void performNullCheck(String jsObject) {
        jsWriter().enterIfBlock(jsObject + " === null");
        fieldSetter().setField("null");
        jsWriter().enterElseBlock();
    }

    @Override
    void exitNullCheck() {
        jsWriter().exitBlock();
    }

    @Override
    String parseFieldValue(String jsObject) {
        // todo maybe extract common base with MessageFieldHandler
        String fieldValue = "fieldValue";
        TypeUrl typeUrl = TypeUrl.from(fieldType);
        jsWriter().addLine("let type = " + KnownTypesJsGenerator.FILE_NAME + '.' +
                                   KnownTypesJsGenerator.MAP_NAME + ".get('" + typeUrl + "');");
        jsWriter().addLine("let parser = " + KnownTypeParsersGenerator.FILE_NAME + '.' +
                                   KnownTypeParsersGenerator.MAP_NAME + ".get(type);");
        jsWriter().addLine("let " + fieldValue + " = parser.parse(" + jsObject + ");");
        return fieldValue;
    }
}
