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

package io.spine.query;

import io.spine.code.proto.FieldDeclaration;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * The name of the column of the record.
 */
public final class ColumnName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private ColumnName(String value) {
        super(value);
    }

    /**
     * Creates new instance of {@code ColumnName}.
     *
     * <p>Value passed must not be empty or blank.
     */
    public static ColumnName of(String value) {
        checkNotEmptyOrBlank(value);
        return new ColumnName(value);
    }

    /**
     * Creates a new instance for the field, which declaration is passed.
     */
    public static ColumnName of(FieldDeclaration field) {
        checkNotNull(field);
        return of(field.name()
                       .value());
    }
}
