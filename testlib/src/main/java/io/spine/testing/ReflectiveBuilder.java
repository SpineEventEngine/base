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

package io.spine.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.lang.reflect.Constructor;

/**
 * The abstract base for test object builders.
 *
 * @param <T>
 *         the result class
 */
public abstract class ReflectiveBuilder<T> {

    /** The class of the object we create. */
    private Class<T> resultClass;

    /** Constructor for use by subclasses. */
    protected ReflectiveBuilder() {
    }

    /**
     * Obtains constructor for the result object.
     */
    protected abstract Constructor<T> constructor();

    /**
     * Obtains the class of the object to build.
     */
    public Class<T> resultClass() {
        return this.resultClass;
    }

    /**
     * Sets the class of the object to build.
     */
    @CanIgnoreReturnValue
    protected ReflectiveBuilder<T> setResultClass(Class<T> resultClass) {
        this.resultClass = resultClass;
        return this;
    }

    /**
     * Creates the object being built.
     */
    public abstract T build();
}
