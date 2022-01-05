/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.validate.diags;

import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.base.Field;
import io.spine.option.OptionsProto;
import io.spine.validate.ConstraintViolation;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.string.Diags.backtick;
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

    private ViolationText(ConstraintViolation violation) {
        this.violation = violation;
    }

    /**
     * Creates a new instance of the text for the passed violation.
     */
    public static ViolationText of(ConstraintViolation violation) {
        checkNotNull(violation);
        return new ViolationText(violation);
    }

    /**
     * Creates text with diagnostics for the passed violations; each diagnostics message starts with
     * a new line.
     */
    public static String ofAll(Collection<ConstraintViolation> violations) {
        var result = violations.stream()
                .map(ViolationText::of)
                .map(ViolationText::toString)
                .collect(joining(lineSeparator()));
        return result;
    }

    @Override
    public String toString() {
        var builder = buildMessage();
        for (var violation : this.violation.getViolationList()) {
            builder.append(lineSeparator());
            var nested = of(violation);
            builder.append(nested);
        }
        return builder.toString();
    }

    private StringBuilder buildMessage() {
        var typeName = violation.getTypeName();
        var path = violation.getFieldPath();
        var fieldPath = path.getFieldNameCount() == 0
                ? ""
                : Field.withPath(path).toString();
        var format = violation.getMsgFormat();
        List<String> params = violation.getParamList();
        var formattedMessage = format(format, params.toArray());

        var result = new StringBuilder();
        appendPrefix(result, typeName);
        appendPrefix(result, fieldPath);
        result.append(formattedMessage);
        return result;
    }

    private static void appendPrefix(StringBuilder target, String prefix) {
        if (!prefix.isEmpty()) {
            target.append("At ")
                  .append(backtick(prefix))
                  .append(": ");
        }
    }

    /**
     * Returns a validation error message which may have formatting placeholders.
     *
     * <p>A custom message is returned if it is present in the option. Otherwise,
     * default message is returned.
     *
     * @param option
     *         a validation option used to get the default message
     * @param customMsg
     *         a user-defined error message
     */
    @Internal
    public static String errorMessage(Message option, String customMsg) {
        var defaultMsg = option.getDescriptorForType()
                               .getOptions()
                               .getExtension(OptionsProto.defaultMessage);
        var msg = customMsg.isEmpty() ? defaultMsg : customMsg;
        return msg;
    }
}
