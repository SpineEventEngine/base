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

package io.spine.tools.validate.field;

import com.google.common.collect.ImmutableList;
import io.spine.code.proto.FieldDeclaration;
import io.spine.protobuf.Messages;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.GetterExpression;

import static io.spine.option.OptionsProto.validate;
import static io.spine.tools.validate.code.BooleanExpression.fromCode;

/**
 * A {@link FieldValidatorFactory} for message and enum fields.
 */
final class MessageFieldValidatorFactory extends SingularFieldValidatorFactory {

    MessageFieldValidatorFactory(FieldDeclaration field,
                                 GetterExpression fieldAccess,
                                 FieldCardinality cardinality) {
        super(field, fieldAccess, cardinality);
    }

    @Override
    protected ImmutableList<Constraint> constraints() {
        ImmutableList.Builder<Constraint> rules = ImmutableList.builder();
        if (isRequired()) {
            rules.add(requiredRule());
        }
        FieldDeclaration field = field();
        if (field.isMessage() && field.findOption(validate)) {
            rules.add(new MessageConstraints(field, fieldAccess()));
        }
        return rules.build();
    }

    @Override
    public BooleanExpression isNotSet() {
        return fromCode("$T.isDefault($L)", Messages.class, fieldAccess());
    }
}
