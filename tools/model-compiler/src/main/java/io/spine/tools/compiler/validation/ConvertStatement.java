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

package io.spine.tools.compiler.validation;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.spine.code.proto.FieldName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.validate.Validate.checkNameNotEmptyOrBlank;

/**
 * A statement to convert a raw value.
 *
 * @see io.spine.validate.AbstractValidatingBuilder#convert(String, java.lang.reflect.Type)}
 */
class ConvertStatement {

    /** The name of the variable to convert. */
    private final String variableName;
    /** The type of the value after conversion. */
    private final TypeName type;

    private ConvertStatement(String variableName, TypeName type) {
        this.variableName = checkNameNotEmptyOrBlank(variableName);
        this.type = checkNotNull(type);
    }

    static ConvertStatement convert(String variableName, TypeName type) {
        return new ConvertStatement(variableName, type);
    }

    String value() {
        String result = CodeBlock.of("$T $N = convert($N, $T.class)",
                                     type, convertedVariableName(), variableName, type)
                                 .toString();
        return result;
    }

    String convertedVariableName() {
        FieldName valueField = FieldName.of(variableName);
        String convertedValue = "converted" + valueField.toCamelCase();
        return convertedValue;
    }
}
