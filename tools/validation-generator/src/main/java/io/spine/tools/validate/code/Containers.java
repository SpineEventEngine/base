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

package io.spine.tools.validate.code;

import io.spine.protobuf.Messages;

import static io.spine.tools.validate.code.BooleanExpression.fromCode;

/**
 * Set of utilities for working with values of container types.
 *
 * <p>Container types are types composed of homogeneous elements: a collection, a string, etc.
 */
public final class Containers {

    /**
     * Prevents the utility class instantiation.
     */
    private Containers() {
    }

    /**
     * Obtains the expression which calls {@code isEmpty()} method on the given {@code value}.
     */
    public static BooleanExpression isEmpty(Expression<?> value) {
        return fromCode("$L.isEmpty()", value.toCode());
    }

    /**
     * Obtains the expression which calls {@code Messages.isDefault())} method on the given
     * {@code value}.
     */
    public static BooleanExpression isDefault(Expression<?> value) {
        return fromCode("$T.isDefault($L)", Messages.class, value.toCode());
    }
}
