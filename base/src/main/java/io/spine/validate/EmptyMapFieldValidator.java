/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;

import java.util.Map;

/**
 * Performs the validation for a {@code map} field which has no values set.
 */
final class EmptyMapFieldValidator extends FieldValidator<Map<?, ?>> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldContext the context of the field to validate
     * @param strict       if {@code true} the validator would assume that the field
     *                     is required even if the corresponding option is not set
     */
    EmptyMapFieldValidator(FieldContext fieldContext, boolean strict) {
        super(fieldContext, ImmutableList.<Map<?, ?>>of(), strict);
    }

    @Override
    protected boolean isNotSet(Map<?, ?> value) {
        return value.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs no action, as there are no specific rules for an empty map validation.
     */
    @Override
    protected void validateOwnRules() {
        // NoOp
    }
}
