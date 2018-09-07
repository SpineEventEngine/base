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

import static io.spine.tools.protojs.field.Fields.isMap;
import static io.spine.tools.protojs.field.Fields.isRepeated;
import static io.spine.tools.protojs.field.checker.FieldValueCheckers.checkerFor;
import static io.spine.tools.protojs.field.parser.FieldValueParsers.parserFor;

public final class FieldHandlers {

    private FieldHandlers() {
    }

    public static FieldHandler createFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        if (isMap(fieldDescriptor)) {
            return map(fieldDescriptor, jsWriter);
        }
        if (isRepeated(fieldDescriptor)) {
            return repeated(fieldDescriptor, jsWriter);
        }
        return singular(fieldDescriptor, jsWriter);
    }

    private static FieldHandler map(FieldDescriptor descriptor, JsWriter jsWriter) {
        FieldDescriptor keyDescriptor = keyDescriptor(descriptor);
        FieldDescriptor valueDescriptor = valueDescriptor(descriptor);
        FieldValueChecker valueChecker = checkerFor(valueDescriptor, jsWriter);
        FieldValueParser keyParser = parserFor(keyDescriptor, jsWriter);
        FieldValueParser valueParser = parserFor(valueDescriptor, jsWriter);
        FieldHandler handler = new MapFieldHandler(descriptor,
                                                   valueChecker,
                                                   keyParser,
                                                   valueParser,
                                                   jsWriter);
        return handler;
    }

    private static FieldHandler repeated(FieldDescriptor descriptor, JsWriter jsWriter) {
        FieldValueChecker valueChecker = checkerFor(descriptor, jsWriter);
        FieldValueParser valueParser = parserFor(descriptor, jsWriter);
        FieldHandler handler = new RepeatedFieldHandler(descriptor,
                                                        valueChecker,
                                                        valueParser,
                                                        jsWriter);
        return handler;
    }

    private static FieldHandler singular(FieldDescriptor descriptor, JsWriter jsWriter) {
        FieldValueChecker valueChecker = checkerFor(descriptor, jsWriter);
        FieldValueParser valueParser = parserFor(descriptor, jsWriter);
        FieldHandler handler = new SingularFieldHandler(descriptor,
                                                        valueChecker,
                                                        valueParser,
                                                        jsWriter);
        return handler;
    }

    private static FieldDescriptor keyDescriptor(FieldDescriptor descriptor) {
        FieldDescriptor valueDescriptor = descriptor.getMessageType()
                                                         .findFieldByName("key");
        return valueDescriptor;
    }

    private static FieldDescriptor valueDescriptor(FieldDescriptor descriptor) {
        FieldDescriptor valueDescriptor = descriptor.getMessageType()
                                                         .findFieldByName("value");
        return valueDescriptor;
    }
}
