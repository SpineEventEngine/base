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

package io.spine.tools.mc.js.code.snippet;

import com.google.errorprone.annotations.Immutable;
import io.spine.tools.js.code.TypeName;
import io.spine.tools.code.CodeLine;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A declaration of a variable.
 */
@Immutable
public final class Let extends CodeLine {

    /**
     * The key word used to create variables.
     *
     * <p>Currently is set to ES6 {@code let}.
     */
    private static final String KEY_WORD = "let";

    private final String name;
    private final String initializer;

    private Let(String name, String initializer) {
        super();
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    public String content() {
        String result = format("%s %s = %s;", KEY_WORD, name, initializer);
        return result;
    }

    /**
     * Creates a declaration with the specified name, initialized with the passed the value.
     */
    public static Let withValue(String name, String value) {
        checkNotNull(name);
        checkNotNull(value);
        return new Let(name, value);
    }

    /**
     * Creates a declaration of a variable initialized by instantiation of the type.
     */
    public static Let newInstance(String name, TypeName type) {
        checkNotNull(name);
        checkNotNull(type);
        String initializer = "new " + type + "()";
        return withValue(name, initializer);
    }
}
