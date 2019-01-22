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

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A rule that entities of type {@code T} can be validated against.
 *
 * @param <T>
 *         type of entities that can be validated against this rule
 */
class ValidationRule<T> {

    private final Predicate<T> valueMatches;
    private final Function<T, List<ConstraintViolation>> onDoesNotMatch;
    private final ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();

    /**
     * Returns a new instance of a validation rule.
     *
     * @param matches
     *         predicate that defines whether a value matches some rule
     * @param onDoesNotMatch
     *         a function that returns a list of constraint violations if the entity
     *         does not match the validation rules
     */
    ValidationRule(Predicate<T> matches, Function<T, List<ConstraintViolation>> onDoesNotMatch) {
        valueMatches = matches;
        this.onDoesNotMatch = onDoesNotMatch;
    }

    /** Returns violations that were produced during the validation. */
    List<ConstraintViolation> onDoesNotMatch() {
        return violations.build();
    }

    /** Returns {@code true} if the specified value is valid against this rule. */
    boolean valueDoesNotMatch(T value) {
        if (!this.valueMatches.test(value)) {
            violations.addAll(onDoesNotMatch.apply(value));
            return true;
        }
        return false;
    }
}
