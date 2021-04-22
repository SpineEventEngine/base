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
import io.spine.tools.code.proto.FieldDeclaration;
import io.spine.option.PatternOption;
import io.spine.option.PatternOption.Modifier;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.diags.ViolationText;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

/**
 * A constraint, which when applied to a string field, checks whether that field matches the
 * specified pattern.
 */
@Immutable
public final class PatternConstraint extends FieldConstraint<PatternOption> {

    PatternConstraint(PatternOption optionValue, FieldDeclaration field) {
        super(optionValue, field);
    }

    @Override
    public String errorMessage(FieldContext field) {
        PatternOption option = optionValue();
        return ViolationText.errorMessage(option, option.getMsgFormat());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitPattern(this);
    }

    /**
     * Obtains the regular expression as a string.
     */
    public String regex() {
        return optionValue().getRegex();
    }

    /**
     * Checks if the pattern allows a partial match.
     *
     * <p>If {@code true}, the whole string value does not have to match the regex, but only its
     * substring.
     */
    public boolean allowsPartialMatch() {
        PatternOption option = optionValue();
        Modifier modifier = option.getModifier();
        return modifier.getPartialMatch();
    }

    /**
     * Obtains the pattern modifiers as a bit mask for the {@link Pattern} flags.
     *
     * <p>If no modifiers are specified, returns {@code 0}.
     */
    public int flagsMask() {
        int result = 0;
        PatternOption option = optionValue();
        Modifier modifier = option.getModifier();
        if (modifier.getDotAll()) {
            result |= DOTALL;
        }
        if (modifier.getUnicode()) {
            result |= UNICODE_CHARACTER_CLASS;
        }
        if (modifier.getCaseInsensitive()) {
            result |= CASE_INSENSITIVE;
        }
        if (modifier.getMultiline()) {
            result |= MULTILINE;
        }
        return result;
    }
}
