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

package io.spine.validate.diags;

import io.spine.validate.ConstraintViolation;

import java.util.List;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * Provides error diagnostic text for a violation of a validation constraint.
 *
 * <p>If a {@link ConstraintViolation} has nested violations, they are listed in separate lines.
 */
public final class ViolationText {

    private final ConstraintViolation violation;

    /**
     * Creates a new instance of the text for the passed violation.
     */
    public static ViolationText of(ConstraintViolation violation) {
        checkNotNull(violation);
        return new ViolationText(violation);
    }

    /**
     * Creates text with diagnostics for the passed violations, starting each of them from
     * a new line.
     */
    public static String ofAll(Iterable<ConstraintViolation> violations) {
        String result =
                StreamSupport.stream(violations.spliterator(), false)
                             .map(ViolationText::of)
                             .map(ViolationText::toString)
                             .collect(joining(lineSeparator()));
        return result;
    }

    private ViolationText(ConstraintViolation violation) {
        this.violation = violation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(formattedMessage());
        for (ConstraintViolation v : violation.getViolationList()) {
            builder.append(lineSeparator());
            ViolationText nested = of(v);
            builder.append(nested.toString());
        }
        return builder.toString();
    }

    private String formattedMessage() {
        String format = violation.getMsgFormat();
        List<String> params = violation.getParamList();
        String result = format(format, params.toArray());
        return result;
    }
}
