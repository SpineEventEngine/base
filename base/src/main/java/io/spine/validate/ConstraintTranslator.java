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

import io.spine.annotation.Internal;
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
 * @see <a href="https://en.wikipedia.org/wiki/Translator_(computing)">
 *         Definition of a "Translator"</a>
 */
@Internal
public interface ConstraintTranslator<T> {

    void visitRange(RangedConstraint<?> constraint);

    void visitRequired(RequiredConstraint constraint);

    void visitPattern(PatternConstraint constraint);

    void visitDistinct(DistinctConstraint constraint);

    void visitGoesWith(GoesConstraint constraint);

    void visitValidate(ValidateConstraint constraint);

    void visitRequiredField(RequiredFieldConstraint constraint);

    /**
     * Finalizes the translation for a message type.
     */
    T translate();
}
