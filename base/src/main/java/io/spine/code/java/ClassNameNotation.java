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

package io.spine.code.java;

import io.spine.annotation.Internal;

/**
 * Tools working with the Java class names.
 *
 * <p>A Java class name may be represented with several types: {@code ClassName},
 * {@code NestedClassName}, {@code SimpleClassName}, etc. This utility handles the common string
 * processing problems for those types.
 */
@Internal
public final class ClassNameNotation {

    /**
     * Separates class name from package, and outer class name with nested when such a class is
     * referenced as a parameter.
     */
    public static final char DOT_SEPARATOR = '.';


    /**
     * Prevents the utility class instantiation.
     */
    private ClassNameNotation() {
    }

    /**
     * Obtain the part of the name after the last {@link #DOT_SEPARATOR .} (dot) symbol.
     *
     * @param fullName
     *         a full class name
     * @return the last part of the name
     */
    public static String afterDot(String fullName) {
        int lastDotIndex = fullName.lastIndexOf(DOT_SEPARATOR);
        return fullName.substring(lastDotIndex + 1);
    }
}
