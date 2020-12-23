/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.value;

import com.google.errorprone.annotations.Immutable;

/**
 * Abstract base for classes holding a value of a {@link Class}.
 *
 * @param <T> the type of the class
 * @apiNote The name of this class has the 'Type' infix to prevent the clash with
 *          {@link java.lang.ClassValue ClassValue}.
 */
@Immutable
public abstract class ClassTypeValue<T> extends ValueHolder<Class<? extends T>> {

    /* NOTE: the class has the 'Type' infix in the name to prevent the name clash with
       java.lang.ClassValue. */

    private static final long serialVersionUID = 0L;

    protected ClassTypeValue(Class<? extends T> value) {
        super(value);
    }

    @Override
    public Class<? extends T> value() {
        return super.value();
    }

    /**
     * Returns {@linkplain Class#getName() the name} of the enclosed class value.
     *
     * @return the value class name
     */
    @Override
    public String toString() {
        return value().getName();
    }
}
