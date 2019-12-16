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

package io.spine.tools.validate;

import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.tools.validate.code.CodeExpression;
import io.spine.tools.validate.code.Expression;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class FieldAccess extends CodeExpression<Object> {

    private static final long serialVersionUID = 0L;

    private static final FieldAccess element = new FieldAccess("el", false);

    private final boolean collection;

    private FieldAccess(String value, boolean collection) {
        super(value);
        this.collection = collection;
    }

    public static FieldAccess fieldOfMessage(MessageAccess message, FieldDeclaration field) {
        checkNotNull(message);
        checkNotNull(field);
        FieldName fieldName = field.name();
        if (field.isNotCollection()) {
            return singularField(message, fieldName);
        } else if (field.isMap()) {
            return mapField(message, fieldName);
        } else {
            return repeatedField(message, fieldName);
        }
    }

    private static FieldAccess singularField(Expression<?> receiver, FieldName field) {
        return fromTemplate("%s.get%s()", receiver, field, false);
    }

    private static FieldAccess repeatedField(Expression<?> receiver, FieldName field) {
        return fromTemplate("%s.get%sList()", receiver, field, true);
    }

    private static FieldAccess mapField(Expression<?> receiver, FieldName field) {
        return fromTemplate("%s.get%sMap().values()", receiver, field, true);
    }

    private static FieldAccess
    fromTemplate(String template, Expression<?> receiver, FieldName field, boolean collection) {
        checkNotNull(receiver);
        checkNotNull(field);
        String expression = format(template, receiver, field.toCamelCase());
        return new FieldAccess(expression, collection);
    }

    public FieldAccess validatableValue() {
        return collection ? element : this;
    }
}
