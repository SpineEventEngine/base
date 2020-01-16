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

package io.spine.validate;

import io.spine.validate.option.DistinctConstraint;
import io.spine.validate.option.GoesConstraint;
import io.spine.validate.option.PatternConstraint;
import io.spine.validate.option.RangedConstraint;
import io.spine.validate.option.RequiredConstraint;
import io.spine.validate.option.RequiredFieldConstraint;
import io.spine.validate.option.ValidateConstraint;

/**
 * Translates validation constraints into an output form.
 *
 * <p>The format of the output is implementation-dependant. For example, given message value,
 * a translator may perform validation and output any found {@linkplain ConstraintViolation
 * violations}. Another example would be printing validation code in a target language (e.g. Java).
 *
 * <p>A instance of {@code ConstraintTranslator} should only be used for a single type of messages.
 * A translator need not be thread-safe. An instance of translator is, most likely, a mutable
 * object. If any resources must be closed when finishing the translation job, {@link #translate()}
 * is the right place to do so.
 *
 * @param <T>
 *         the type of the translation result
 */
public interface ConstraintTranslator<T> {

    /**
     * Translates the given {@link RangedConstraint}.
     *
     * @param constraint
     *         the constraint of a number field
     */
    void visitRange(RangedConstraint<?> constraint);

    /**
     * Translates the given {@link RequiredConstraint}.
     *
     * @param constraint
     *         the constraint of a field
     */
    void visitRequired(RequiredConstraint constraint);

    /**
     * Translates the given {@link PatternConstraint}.
     *
     * @param constraint
     *         the constraint of a string field
     */
    void visitPattern(PatternConstraint constraint);

    /**
     * Translates the given {@link DistinctConstraint}.
     *
     * @param constraint
     *         the constraint of a collection field
     */
    void visitDistinct(DistinctConstraint constraint);

    /**
     * Translates the given {@link GoesConstraint}.
     *
     * @param constraint
     *         the constraint of a field
     */
    void visitGoesWith(GoesConstraint constraint);

    /**
     * Translates the given {@link ValidateConstraint}.
     *
     * @param constraint
     *         the constraint of a message field
     */
    void visitValidate(ValidateConstraint constraint);

    /**
     * Translates the given {@link RequiredFieldConstraint}.
     *
     * @param constraint
     *         the constraint of a field combination
     */
    void visitRequiredField(RequiredFieldConstraint constraint);

    /**
     * Translates the given {@link CustomConstraint}.
     *
     * @param constraint
     *         the self-validating constraint
     */
    void visitCustom(CustomConstraint constraint);

    /**
     * Finalizes the translation for a message type.
     */
    T translate();
}
