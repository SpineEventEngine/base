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

package io.spine.tools.mc.java.codegen;

import io.spine.annotation.Internal;
import io.spine.tools.protoc.JavaClassName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A test factory of {@code JavaClassName}s.
 */
@Internal
public final class Names {

    /**
     * Prevents the utility class instantiation.
     */
    private Names() {
    }

    /**
     * Obtains a {@code JavaClassName} with the given value.
     */
    public static JavaClassName className(String name) {
        checkNotEmptyOrBlank(name, "Class name must not be null or empty.");
        return JavaClassName
                .newBuilder()
                .setCanonical(name)
                .build();
    }

    /**
     * Obtains a {@code JavaClassName} for the given class.
     */
    public static JavaClassName className(Class<?> cls) {
        checkNotNull(cls, "Class must not be null.");
        return className(cls.getCanonicalName());
    }
}
