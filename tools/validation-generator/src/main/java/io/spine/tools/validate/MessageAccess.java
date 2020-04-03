/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import com.google.protobuf.ProtocolMessageEnum;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.CodeExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.type.OneofDeclaration;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.FieldAccess.fieldOfMessage;
import static io.spine.tools.validate.code.Expression.formatted;

/**
 * An expression which yields a message.
 */
public final class MessageAccess extends CodeExpression<Message> {

    private static final long serialVersionUID = 0L;

    private MessageAccess(String value) {
        super(value);
    }

    public static MessageAccess of(String value) {
        checkNotNull(value);
        return new MessageAccess(value);
    }

    /**
     * Obtains an expression which yields a field of this method.
     */
    public FieldAccess get(FieldDeclaration field) {
        return fieldOfMessage(this, field);
    }

    /**
     * Builds an expression which yields the {@code oneof} case for the given {@code oneof}.
     *
     * <p>The case is represented by an enum value. See the Protobuf doc for more info on the enum.
     */
    public Expression<ProtocolMessageEnum> oneofCase(OneofDeclaration declaration) {
        return formatted("%s.get%sCase()", this, declaration.name().toCamelCase());
    }
}
