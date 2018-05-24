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

package io.spine.test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.lang.reflect.Constructor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The abstract base for test object builders.
 *
 * @param <T> the type of the object to build
 * @author Alexander Yevsyukov
 */
public abstract class ReflectiveBuilder<T> {

    /** The class of the object we create. */
    private final Class<T> resultClass;

    /**
     * Constructor for use by subclasses.
     *
     * @param resultClass the class of the objects to build
     */
    protected ReflectiveBuilder(Class<T> resultClass) {
        this.resultClass = checkNotNull(resultClass);
    }

    /**
     * Obtains constructor for the result object.
     */
    protected abstract Constructor<T> getConstructor();

    /**
     * Obtains the class of the object to build.
     */
    public Class<T> getResultClass() {
        return this.resultClass;
    }

    /**
     * Creates the object being built.
     */
    public abstract T build();
}
