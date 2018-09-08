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

package io.spine.tools.protojs.field.checker;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Value;
import io.spine.tools.protojs.code.JsGenerator;

public class MessageFieldChecker implements FieldValueChecker {

    private final FieldDescriptor field;
    private final JsGenerator jsGenerator;

    public MessageFieldChecker(FieldDescriptor field, JsGenerator jsGenerator) {
        this.field = field;
        this.jsGenerator = jsGenerator;
    }

    @Override
    public void performNullCheck(String fieldValue, String setterFormat) {
        if (isProtobufValueType()) {
            return;
        }
        jsGenerator.enterIfBlock(fieldValue + " === null");
        String setFieldToNull = String.format(setterFormat, "null");
        jsGenerator.addLine(setFieldToNull);
        jsGenerator.enterElseBlock();
    }

    @Override
    public void exitNullCheck() {
        if (!isProtobufValueType()) {
            jsGenerator.exitBlock();
        }
    }

    private boolean isProtobufValueType() {
        String valueType = Value.getDescriptor()
                                .getFullName();
        String fieldType = field.getMessageType()
                                .getFullName();
        boolean isValueType = fieldType.equals(valueType);
        return isValueType;
    }
}
