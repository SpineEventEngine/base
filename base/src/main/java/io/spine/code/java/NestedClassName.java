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

package io.spine.code.java;

import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A name of a potentially nested class with outer class names separated with dots.
 *
 * <p>A top level class name would have equal to {@link io.spine.code.java.SimpleClassName}.
 */
public final class NestedClassName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private NestedClassName(String value) {
        super(value);
    }

    static NestedClassName create(String value) {
        checkNotEmptyOrBlank(value);
        int dotIndex = value.indexOf(ClassName.DOT_SEPARATOR);
        checkArgument(dotIndex !=0, "Nested class name cannot start with a dot.");
        return new NestedClassName(value);
    }
}
