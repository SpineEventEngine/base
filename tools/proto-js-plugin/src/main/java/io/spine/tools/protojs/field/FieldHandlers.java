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

import static io.spine.tools.protojs.field.Fields.isMap;
import static io.spine.tools.protojs.field.Fields.isRepeated;
import static io.spine.tools.protojs.field.checker.FieldValueCheckers.checkerFor;
import static io.spine.tools.protojs.field.parser.FieldValueParsers.parserFor;

public final class FieldHandlers {

    private FieldHandlers() {
    }

    public static FieldHandler createFor(FieldDescriptor field, JsGenerator jsGenerator) {
        if (isMap(field)) {
            return mapHandler(field, jsGenerator);
        }
        if (isRepeated(field)) {
            return repeatedHandler(field, jsGenerator);
        }
        return singularHandler(field, jsGenerator);
    }

    private static FieldHandler mapHandler(FieldDescriptor field, JsGenerator jsGenerator) {
        FieldValueParser keyParser = keyParser(field, jsGenerator);
        FieldValueParser valueParser = valueParser(field, jsGenerator);
        FieldValueChecker valueChecker = valueChecker(field, jsGenerator);
        FieldHandler handler = MapFieldHandler
                .newBuilder()
                .setField(field)
                .setChecker(valueChecker)
                .setKeyParser(keyParser)
                .setParser(valueParser)
                .setJsGenerator(jsGenerator)
                .build();
        return handler;
    }

    private static FieldHandler repeatedHandler(FieldDescriptor field, JsGenerator jsGenerator) {
        FieldValueChecker checker = checkerFor(field, jsGenerator);
        FieldValueParser parser = parserFor(field, jsGenerator);
        FieldHandler handler = RepeatedFieldHandler
                .newBuilder()
                .setField(field)
                .setChecker(checker)
                .setParser(parser)
                .setJsGenerator(jsGenerator)
                .build();
        return handler;
    }

    private static FieldHandler singularHandler(FieldDescriptor field, JsGenerator jsGenerator) {
        FieldValueChecker checker = checkerFor(field, jsGenerator);
        FieldValueParser parser = parserFor(field, jsGenerator);
        FieldHandler handler = SingularFieldHandler
                .newBuilder()
                .setField(field)
                .setChecker(checker)
                .setParser(parser)
                .setJsGenerator(jsGenerator)
                .build();
        return handler;
    }

    private static FieldValueParser keyParser(FieldDescriptor field, JsGenerator jsGenerator) {
        FieldDescriptor keyDescriptor = keyDescriptor(field);
        FieldValueParser parser = parserFor(keyDescriptor, jsGenerator);
        return parser;
    }

    private static FieldValueParser valueParser(FieldDescriptor field, JsGenerator jsGenerator) {
        FieldDescriptor valueDescriptor = valueDescriptor(field);
        FieldValueParser parser = parserFor(valueDescriptor, jsGenerator);
        return parser;
    }

    private static FieldValueChecker valueChecker(FieldDescriptor field, JsGenerator jsGenerator) {
        FieldDescriptor valueDescriptor = valueDescriptor(field);
        FieldValueChecker checker = checkerFor(valueDescriptor, jsGenerator);
        return checker;
    }

    private static FieldDescriptor keyDescriptor(FieldDescriptor field) {
        FieldDescriptor descriptor = field.getMessageType()
                                          .findFieldByName("key");
        return descriptor;
    }

    private static FieldDescriptor valueDescriptor(FieldDescriptor field) {
        FieldDescriptor descriptor = field.getMessageType()
                                          .findFieldByName("value");
        return descriptor;
    }
}
