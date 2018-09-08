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
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.field.checker.FieldValueChecker;
import io.spine.tools.protojs.field.parser.FieldValueParser;

import static io.spine.tools.protojs.message.MessageHandler.FROM_OBJECT_ARG;
import static java.lang.String.format;

abstract class AbstractFieldHandler implements FieldHandler {

    private static final String FIELD_VALUE = "fieldValue";

    private final FieldDescriptor field;
    private final FieldValueChecker checker;
    private final FieldValueParser parser;
    private final JsGenerator jsGenerator;

    AbstractFieldHandler(FieldDescriptor field,
                         FieldValueChecker checker,
                         FieldValueParser parser,
                         JsGenerator jsGenerator) {
        this.field = field;
        this.checker = checker;
        this.parser = parser;
        this.jsGenerator = jsGenerator;
    }

    String acquireJsObject() {
        String fieldJsonName = field.getJsonName();
        String jsObject = FROM_OBJECT_ARG + '.' + fieldJsonName;
        return jsObject;
    }

    void setValue(String value) {
        checker.performNullCheck(value, setterFormat());
        parser.parseIntoVariable(value, FIELD_VALUE);
        callSetter(FIELD_VALUE);
        checker.exitNullCheck();
    }

    FieldDescriptor field() {
        return field;
    }

    JsGenerator jsWriter() {
        return jsGenerator;
    }

    private void callSetter(String value) {
        String setterFormat = setterFormat();
        String setValue = format(setterFormat, value);
        jsGenerator.addLine(setValue);
    }

    abstract String setterFormat();
}
