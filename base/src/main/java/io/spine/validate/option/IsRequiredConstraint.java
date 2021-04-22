/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.validate.option;

import com.google.errorprone.annotations.Immutable;
import io.spine.tools.code.proto.FieldContext;
import io.spine.tools.code.proto.FieldName;
import io.spine.tools.code.proto.OneofDeclaration;
import io.spine.type.MessageType;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;

/**
 * A {@code oneof} group constraint which signifies that one of the fields must be set.
 */
@Immutable
public final class IsRequiredConstraint implements Constraint {

    private final OneofDeclaration declaration;

    IsRequiredConstraint(OneofDeclaration declaration) {
        this.declaration = declaration;
    }

    @Override
    public MessageType targetType() {
        return declaration.declaringType();
    }

    @Override
    public String errorMessage(FieldContext field) {
        return String.format("One of fields in group `%s` must be set.", declaration.name());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRequiredOneof(this);
    }

    /**
     * Obtains the name of the {@code oneof} group.
     */
    public FieldName oneofName() {
        return declaration.name();
    }

    /**
     * Obtains the {@code oneof} declaration.
     */
    public OneofDeclaration declaration() {
        return declaration;
    }
}
