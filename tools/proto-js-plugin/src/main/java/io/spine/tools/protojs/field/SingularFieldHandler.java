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

import static io.spine.tools.protojs.field.Fields.capitalizedName;
import static io.spine.tools.protojs.message.MessageHandler.MESSAGE;

public class SingularFieldHandler extends AbstractFieldHandler {

    SingularFieldHandler(FieldDescriptor field,
                         FieldValueChecker fieldValueChecker,
                         FieldValueParser fieldValueParser,
                         JsGenerator jsGenerator) {
        super(field, fieldValueChecker, fieldValueParser, jsGenerator);
    }

    @Override
    public void generateJs() {
        String jsObject = acquireJsObject();
        checkNotUndefined(jsObject);
        setValue(jsObject);
        exitUndefinedCheck();
    }

    @Override
    String setterFormat() {
        String fieldName = capitalizedName(field());
        String setterName = "set" + fieldName;
        String setFieldFormat = MESSAGE + '.' + setterName + "(%s);";
        return setFieldFormat;
    }

    private void checkNotUndefined(String jsObject) {
        jsWriter().enterIfBlock(jsObject + " !== undefined");
    }

    private void exitUndefinedCheck() {
        jsWriter().exitBlock();
    }
}
