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

package io.spine.code.java;

import io.spine.annotation.Internal;

import static io.spine.code.java.AccessorTemplate.prefix;
import static io.spine.code.java.AccessorTemplate.prefixAndPostfix;

/**
 * A factory of commonly used {@link AccessorTemplate} instances.
 */
@Internal
public final class AccessorTemplates {

    private static final AccessorTemplate GET = prefix("get");
    private static final AccessorTemplate GET_LIST = prefixAndPostfix("get", "List");
    private static final AccessorTemplate GET_MAP = prefixAndPostfix("get", "Map");

    private static final AccessorTemplate SET = prefix("set");
    private static final AccessorTemplate ADD = prefix("add");
    private static final AccessorTemplate PUT = prefix("put");
    private static final AccessorTemplate REMOVE = prefix("remove");
    private static final AccessorTemplate ADD_ALL = prefix("addAll");
    private static final AccessorTemplate PUT_ALL = prefix("putAll");
    private static final AccessorTemplate CLEAR = prefix("clear");

    /**
     * Prevents the utility class instantiation.
     */
    private AccessorTemplates() {
    }

    /** Obtains {@code get...} template. */
    public static AccessorTemplate getter() {
        return GET;
    }

    /** Obtains {@code get...List} template. */
    public static AccessorTemplate listGetter() {
        return GET_LIST;
    }

    /** Obtains {@code get...Map} template. */
    public static AccessorTemplate mapGetter() {
        return GET_MAP;
    }

    /** Obtains {@code set...} template. */
    public static AccessorTemplate setter() {
        return SET;
    }

    /** Obtains {@code add...} template. */
    public static AccessorTemplate adder() {
        return ADD;
    }

    /** Obtains {@code addAll...} template. */
    public static AccessorTemplate allAdder() {
        return ADD_ALL;
    }

    /** Obtains {@code put...} template. */
    public static AccessorTemplate putter() {
        return PUT;
    }

    /** Obtains {@code putAll...} template. */
    public static AccessorTemplate allPutter() {
        return PUT_ALL;
    }

    /** Obtains {@code remove...} template. */
    public static AccessorTemplate remover() {
        return REMOVE;
    }

    /** Obtains {@code clear...} template. */
    public static AccessorTemplate clearer() {
        return CLEAR;
    }
}
