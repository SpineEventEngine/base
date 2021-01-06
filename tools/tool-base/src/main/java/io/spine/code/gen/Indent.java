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

package io.spine.code.gen;

import com.google.common.base.Strings;
import io.spine.value.StringTypeValue;

/**
 * A positive space-based indentation of generated code.
 */
public final class Indent extends StringTypeValue {

    private static final long serialVersionUID = 0L;
    private static final String SPACE = " ";
    private static final Indent TWO = new Indent(2);
    private static final Indent FOUR = new Indent(4);

    private final int size;

    private Indent(int indent) {
        super(Strings.repeat(SPACE, indent));
        this.size = indent;
    }

    /**
     * Creates an instance for non-negative indent.
     */
    public static Indent of(int indent) {
        if (indent == 4) {
            return of4();
        }
        if (indent == 2) {
            return of2();
        }

        return new Indent(indent);
    }

    /**
     * Obtains the indent with two spaces.
     */
    public static Indent of2() {
        return TWO;
    }

    /**
     * Obtains the indent with four spaces.
     */
    public static Indent of4() {
        return FOUR;
    }

    /**
     * Obtains the size of the indentation.
     */
    public int getSize() {
        return size;
    }
}
