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
import io.spine.tools.protojs.field.checker.FieldValueCheckers;
import io.spine.tools.protojs.field.parser.FieldValueParser;
import io.spine.tools.protojs.field.parser.FieldValueParsers;

import static io.spine.tools.protojs.field.Fields.isMap;
import static io.spine.tools.protojs.field.Fields.isRepeated;

public final class FieldHandlers {

    private FieldHandlers() {
    }

    public static FieldHandler createFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        if (isMap(fieldDescriptor)) {
            return mapHandlerFor(fieldDescriptor, jsWriter);
        }
        if (isRepeated(fieldDescriptor)) {
            return repeatedHandlerFor(fieldDescriptor, jsWriter);
        }
        return singularHandlerFor(fieldDescriptor, jsWriter);
    }

    private static FieldHandler mapHandlerFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        FieldDescriptor keyDescriptor = getKeyDescriptor(fieldDescriptor);
        FieldDescriptor valueDescriptor = getValueDescriptor(fieldDescriptor);
        FieldValueChecker valueChecker = FieldValueCheckers.createFor(valueDescriptor, jsWriter);
        FieldValueParser keyParser = FieldValueParsers.createFor(keyDescriptor, jsWriter);
        FieldValueParser valueParser = FieldValueParsers.createFor(valueDescriptor, jsWriter);
        return new MapFieldHandler(fieldDescriptor, valueChecker, keyParser, valueParser, jsWriter);
    }

    private static FieldHandler
    repeatedHandlerFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        FieldValueChecker valueChecker = FieldValueCheckers.createFor(fieldDescriptor, jsWriter);
        FieldValueParser valueParser = FieldValueParsers.createFor(fieldDescriptor, jsWriter);
        return new RepeatedFieldHandler(fieldDescriptor, valueChecker, valueParser, jsWriter);
    }

    private static FieldHandler
    singularHandlerFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        FieldValueChecker valueChecker = FieldValueCheckers.createFor(fieldDescriptor, jsWriter);
        FieldValueParser valueParser = FieldValueParsers.createFor(fieldDescriptor, jsWriter);
        return new SingularFieldHandler(fieldDescriptor, valueChecker, valueParser, jsWriter);
    }

    private static FieldDescriptor getKeyDescriptor(FieldDescriptor fieldDescriptor) {
        FieldDescriptor valueDescriptor = fieldDescriptor.getMessageType()
                                                         .findFieldByName("key");
        return valueDescriptor;
    }

    private static FieldDescriptor getValueDescriptor(FieldDescriptor fieldDescriptor) {
        FieldDescriptor valueDescriptor = fieldDescriptor.getMessageType()
                                                         .findFieldByName("value");
        return valueDescriptor;
    }
}
