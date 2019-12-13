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

import com.google.protobuf.Message;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.GetterExpression;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.code.GetterExpression.mapField;
import static io.spine.tools.validate.code.GetterExpression.repeatedField;
import static io.spine.tools.validate.code.GetterExpression.singularField;

public final class FieldAccess {

    private final GetterExpression expression;

    private FieldAccess(GetterExpression expression) {
        this.expression = checkNotNull(expression);
    }

    public static FieldAccess fieldOfMessage(MessageAccess message, FieldDeclaration field) {
        checkNotNull(message);
        checkNotNull(field);
        FieldName fieldName = field.name();
        Expression<? extends Message> messageExpression = message.expression();
        if (field.isNotCollection()) {
            return new FieldAccess(singularField(messageExpression, fieldName));
        } else if (field.isMap()) {
            return new FieldAccess(mapField(messageExpression, fieldName));
        } else {
            return new FieldAccess(repeatedField(messageExpression, fieldName));
        }
    }

    public <T> T let(Function<GetterExpression, T> actionGenerator) {
        return actionGenerator.apply(expression);
    }

    public GetterExpression expression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression.toString();
    }
}
