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

package io.spine.js.generate.output.snippet;

import io.spine.code.gen.js.TypeName;
import io.spine.js.generate.output.CodeLine;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A declaration of a variable.
 */
public class VariableDeclaration extends CodeLine {

    /**
     * The modifier which is used to create variables.
     *
     * <p>Currently is set to ES6 {@code let}.
     */
    private static final String VARIABLE_MODIFIER = "let";

    private final String name;
    private final String initializer;

    private VariableDeclaration(String name, String initializer) {
        super();
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    public String content() {
        String result = format("%s %s = %s;", VARIABLE_MODIFIER, name, initializer);
        return result;
    }

    /**
     * Obtains the declaration with the specified name, which is initialized to the value.
     */
    public static VariableDeclaration initialized(String name, String value) {
        checkNotNull(name);
        checkNotNull(value);
        return new VariableDeclaration(name, value);
    }

    /**
     * Obtains the declaration of a variable initialized by instantiation of the type.
     */
    public static VariableDeclaration newInstance(String name, TypeName type) {
        checkNotNull(name);
        checkNotNull(type);
        String initializer = "new " + type + "()";
        return initialized(name, initializer);
    }
}
