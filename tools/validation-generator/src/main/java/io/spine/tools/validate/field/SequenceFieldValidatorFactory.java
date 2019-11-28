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

import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.Expression;

import static io.spine.tools.validate.field.IsEmpty.isEmpty;

/**
 * An abstract base for {@link FieldValidatorFactory}s which produce validation code for primitive
 * sequence fields.
 *
 * <p>Such fields are {@code string} and {@code bytes}. A {@code string} is a sequence of characters
 * and {@code bytes} is a sequence of bytes.
 */
abstract class SequenceFieldValidatorFactory extends SingularFieldValidatorFactory {

    SequenceFieldValidatorFactory(FieldDeclaration field,
                                  Expression fieldAccess,
                                  FieldCardinality cardinality) {
        super(field, fieldAccess, cardinality);
    }

    @Override
    public Expression<Boolean> isNotSet() {
        return isEmpty(fieldAccess());
    }
}
