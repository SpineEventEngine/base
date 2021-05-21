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

package io.spine.tools.mc.java.field;

import static io.spine.tools.mc.java.field.Accessor.prefix;
import static io.spine.tools.mc.java.field.Accessor.prefixAndPostfix;

/**
 * A factory of commonly used {@link Accessor} instances.
 */
final class Accessors {

    private static final String GET_PREFIX = "get";

    private static final Accessor GET = prefix(GET_PREFIX);
    private static final Accessor GET_LIST = prefixAndPostfix(GET_PREFIX, "List");
    private static final Accessor GET_MAP = prefixAndPostfix(GET_PREFIX, "Map");
    private static final Accessor GET_COUNT = prefixAndPostfix(GET_PREFIX, "Count");

    private static final Accessor SET = prefix("set");
    private static final Accessor ADD = prefix("add");
    private static final Accessor PUT = prefix("put");
    private static final Accessor REMOVE = prefix("remove");
    private static final Accessor ADD_ALL = prefix("addAll");
    private static final Accessor PUT_ALL = prefix("putAll");
    private static final Accessor CLEAR = prefix("clear");

    /**
     * Prevents the utility class instantiation.
     */
    private Accessors() {
    }

    /** Obtains {@code get...} template. */
    public static Accessor getter() {
        return GET;
    }

    /** Obtains {@code get...List} template. */
    public static Accessor listGetter() {
        return GET_LIST;
    }

    /** Obtains {@code get...Map} template. */
    public static Accessor mapGetter() {
        return GET_MAP;
    }

    /** Obtains {@code get...Count} template. */
    public static Accessor countGetter() {
        return GET_COUNT;
    }

    /** Obtains {@code set...} template. */
    public static Accessor setter() {
        return SET;
    }

    /** Obtains {@code add...} template. */
    public static Accessor adder() {
        return ADD;
    }

    /** Obtains {@code addAll...} template. */
    public static Accessor allAdder() {
        return ADD_ALL;
    }

    /** Obtains {@code put...} template. */
    public static Accessor putter() {
        return PUT;
    }

    /** Obtains {@code putAll...} template. */
    public static Accessor allPutter() {
        return PUT_ALL;
    }

    /** Obtains {@code remove...} template. */
    public static Accessor remover() {
        return REMOVE;
    }

    /** Obtains {@code clear...} template. */
    public static Accessor clearer() {
        return CLEAR;
    }
}
