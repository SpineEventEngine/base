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

package io.spine.validate.option;

import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.IfMissingOption;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.FieldValue;

import static com.google.common.flogger.FluentLogger.forEnclosingClass;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A constraint that, when applied to a field, checks whether the field is set to a non-default
 * value.
 */
@Immutable
public final class RequiredConstraint extends FieldConstraint<Boolean> {

    private static final FluentLogger log = forEnclosingClass();

    RequiredConstraint(boolean required,
                       FieldDeclaration declaration,
                       ImmutableSet<JavaType> allowedTypes) {
        super(consistentRequired(required, declaration, allowedTypes), declaration);
    }

    private static boolean consistentRequired(boolean requiredRequested,
                                              FieldDeclaration declaration,
                                              ImmutableSet<JavaType> allowedTypes) {
        if (requiredRequested) {
            JavaType type = declaration.javaType();
            boolean expectedType = allowedTypes.contains(type);
            if (!expectedType) {
                log.atWarning()
                   .log("Field `%s` of type `%s` should not be declared as `(required)`.",
                        declaration,
                        type);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public String errorMessage(FieldValue value) {
        IfMissing ifMissing = new IfMissing();
        IfMissingOption option = ifMissing.valueOrDefault(value.descriptor());
        return errorMsgFormat(option, option.getMsgFormat());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRequired(this);
    }
}
