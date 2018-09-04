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

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.fromjson.js.JsWriter;

import static io.spine.tools.fromjson.generator.MessageHandler.FROM_OBJECT_ARG;

abstract class AbstractFieldHandler implements FieldHandler {

    private final FieldDescriptor fieldDescriptor;
    private final JsObjectAccessor jsObjectAccessor;
    private final FieldSetter fieldSetter;
    private final JsWriter jsWriter;

    AbstractFieldHandler(FieldDescriptor fieldDescriptor,
                         JsObjectAccessor jsObjectAccessor,
                         FieldSetter fieldSetter,
                         JsWriter jsWriter) {
        this.fieldDescriptor = fieldDescriptor;
        this.jsObjectAccessor = jsObjectAccessor;
        this.fieldSetter = fieldSetter;
        this.jsWriter = jsWriter;
    }

    // todo check js object for null
    @Override
    public void writeJs() {
        String jsonName = fieldDescriptor.getJsonName();
        String jsObject = FROM_OBJECT_ARG + '.' + jsonName;

        String value = jsObjectAccessor.extractOrIterateValue(jsObject);
        performNullCheck(value);

        String fieldValue = parseFieldValue(value);
        fieldSetter.setField(fieldValue);

        exitNullCheck();
        jsObjectAccessor.exitToTopLevel();
    }

    FieldDescriptor fieldDescriptor() {
        return fieldDescriptor;
    }

    FieldSetter fieldSetter() {
        return fieldSetter;
    }

    JsWriter jsWriter() {
        return jsWriter;
    }

    abstract void performNullCheck(String jsObject);

    abstract void exitNullCheck();

    abstract String parseFieldValue(String jsObject);
}
