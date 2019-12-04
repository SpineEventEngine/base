/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.validate.code;

import io.spine.code.proto.FieldName;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class GetterExpression extends CodeExpression<Object> {

    private static final long serialVersionUID = 0L;

    private GetterExpression(String value) {
        super(value);
    }

    public static GetterExpression of(String expression) {
        checkNotNull(expression);
        return new GetterExpression(expression);
    }

    public static GetterExpression singularField(Expression<?> receiver, FieldName field) {
        return fromTemplate("%s.get%s()", receiver, field);
    }

    public static GetterExpression repeatedField(Expression<?> receiver, FieldName field) {
        return fromTemplate("%s.get%sList()", receiver, field);
    }

    public static GetterExpression mapField(Expression<?> receiver, FieldName field) {
        return fromTemplate("%s.get%sMap().values()", receiver, field);
    }

    private static GetterExpression
    fromTemplate(String template, Expression<?> receiver, FieldName field) {
        checkNotNull(receiver);
        checkNotNull(field);
        String expression = format(template, receiver, field.toCamelCase());
        return new GetterExpression(expression);
    }
}
