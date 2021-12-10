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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.validate.ComparableNumber;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.BoundType.CLOSED;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A constraint that puts a numeric field value into a range.
 *
 * <p>A field which violates this constraint of its value is out of the range.
 *
 * @param <T>
 *         value of the option
 */
@Immutable
public abstract class RangedConstraint<@ImmutableTypeParameter T> extends FieldConstraint<T> {

    private static final String OR_EQUAL_TO = "or equal to ";

    private final Range<ComparableNumber> range;

    RangedConstraint(T optionValue, Range<ComparableNumber> range, FieldDeclaration field) {
        super(optionValue, field);
        verifyType(field, range);
        this.range = range;
    }

    @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
    private static void verifyType(FieldDeclaration field, Range<ComparableNumber> range) {
        var fieldType = field.javaType();
        switch (fieldType) {
            case INT:  // Fallthrough intended.
            case LONG: {
                if (range.hasLowerBound()) {
                    checkInteger(range.lowerEndpoint(), field);
                }
                if (range.hasUpperBound()) {
                    checkInteger(range.upperEndpoint(), field);
                }
                return;
            }
            case FLOAT: // Fallthrough intended.
            case DOUBLE:
                return;
            default:
                throw newIllegalArgumentException("Field `%s` cannot have a number bound.", field);
        }
    }

    private static void checkInteger(ComparableNumber number, FieldDeclaration field) {
        checkState(number.isInteger(),
                   "An integer bound expected for field `%s`, but got `%s`.", field, number);
    }

    public final Range<ComparableNumber> range() {
        return range;
    }

    @Override
    public String errorMessage(FieldContext field) {
        return compileErrorMessage(range);
    }

    protected abstract String compileErrorMessage(Range<ComparableNumber> range);

    static String orEqualTo(BoundType type) {
        return type == CLOSED ? OR_EQUAL_TO : "";
    }
}
