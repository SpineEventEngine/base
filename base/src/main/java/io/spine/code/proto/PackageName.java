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

package io.spine.code.proto;

import io.spine.value.StringTypeValue;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A Protobuf package name.
 */
public final class PackageName extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    /**
     * The package used to identify standard Protobuf types.
     */
    public static final PackageName googleProtobuf = of("google.protobuf");
    /**
     * The delimeter used in package names.
     */
    public static final String SEPARATOR = ".";

    private PackageName(String value) {
        super(value);
    }

    /**
     * Creates a new instance.
     *
     * @param value
     *         the dot separated package name
     * @return a new instance
     */
    public static PackageName of(String value) {
        checkNotEmptyOrBlank(value);
        return new PackageName(value);
    }

    /**
     * Tells whether the package is nested in the specified package.
     *
     * @param target
     *         the package name to check
     * @return {@code true} if this package is nested in the specified package,
     *         {@code false} otherwise
     */
    public boolean isNestedIn(PackageName target) {
        boolean result = value().startsWith(target.value());
        return result;
    }
}
