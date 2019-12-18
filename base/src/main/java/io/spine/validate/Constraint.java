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

package io.spine.validate;

import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldContext;
import io.spine.type.MessageType;

/**
 * A validation rule attributed to a message type.
 *
 * <p>A {@code Constraint} may cover the values of one or more fields of a message.
 */
@Immutable
public interface Constraint {

    /**
     * The associated message type.
     */
    MessageType targetType();

    /**
     * Produces an error message for the given field validation context.
     *
     * <p>Implementations may choose to ignore the field context or to embed its parts into
     * the error message.
     *
     * @param field the validated field
     */
    String errorMessage(FieldContext field);

    /**
     * Accepts the given {@link ConstraintTranslator}.
     *
     * <p>{@code Constraint} and {@code ConstraintTranslator} implement the visitor pattern.
     * Implementations should call the appropriate method of {@code ConstraintTranslator}.
     */
    void accept(ConstraintTranslator<?> visitor);
}
