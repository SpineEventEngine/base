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

package io.spine.js.generate.field.parser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.js.generate.JsOutput;
import io.spine.js.generate.parse.ProtoParsersGenerator;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.js.generate.parse.TypesParsingExtension.PARSERS_IMPORT_NAME;

/**
 * The value parser for the proto fields of well-known {@code message} types.
 *
 * <p>Well-known message types are those standard Protobuf types for which the predefined parsers
 * are present.
 */
final class WellKnownFieldParser implements FieldParser {

    private final TypeUrl typeUrl;
    private final JsOutput jsOutput;

    private WellKnownFieldParser(TypeUrl typeUrl, JsOutput jsOutput) {
        this.typeUrl = typeUrl;
        this.jsOutput = jsOutput;
    }

    /**
     * Creates a new {@code WellKnownFieldParser} for the given field.
     *
     * @param field
     *         the processed field
     * @param jsOutput
     *         the {@code JsOutput} to store the generated code
     */
    static WellKnownFieldParser createFor(FieldDescriptor field, JsOutput jsOutput) {
        checkNotNull(field);
        checkNotNull(jsOutput);
        Descriptor fieldType = field.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(fieldType);
        return new WellKnownFieldParser(typeUrl, jsOutput);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The field value of well-known type is parsed via a predefined parser stored in the
     * dedicated file.
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        String parserMap = PARSERS_IMPORT_NAME + '.' + ProtoParsersGenerator.MAP_NAME;
        jsOutput.declareVariable("parser", parserMap + ".get('" + typeUrl + "')");
        jsOutput.declareVariable(variable, "parser.parse(" + value + ')');
    }
}
