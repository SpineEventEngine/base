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

package io.spine.tools.javadoc;

import com.sun.javadoc.MethodDoc;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.MethodDocImpl;

/**
 * Enumeration of method names used in {@linkplain Standard} doclet implementation,
 * that cast parameter represented by interface to concrete implementation type.
 *
 * <p>For example {@linkplain MethodDocImpl#overrides(MethodDoc)}:
 * <pre> {@code
 *   public boolean overrides(MethodDoc meth) {
 *       MethodSymbol overridee = ((MethodDocImpl) meth).sym;
 *       // Remaining part omitted.
 *   }
 * }</pre>
 *
 * <p>Because we use proxy to filter Javadocs, we should unwrap proxy
 * of parameters passed to these methods to prevent {@code ClassCastException}.
 */
@SuppressWarnings({"unused", "RedundantSuppression"}) // Used in implicit form.
enum IgnoredMethod {
    COMPARE_TO("compareTo"),
    EQUALS("equals"),
    OVERRIDES("overrides"),
    SUBCLASS_OF("subclassOf");

    private final String methodName;

    IgnoredMethod(String methodName) {
        this.methodName = methodName;
    }

    String methodName() {
        return methodName;
    }

    /**
     * Returns {@code true} if the passed method name is one of {@code IgnoredMethod}s.
     *
     * @param methodName
     *         the method name to test
     * @return {@code true} if the method name is one of {@code IgnoredMethod}s
     */
    static boolean isIgnored(String methodName) {
        for (IgnoredMethod ignoredMethod : values()) {
            if (methodName.equals(ignoredMethod.methodName())) {
                return true;
            }
        }
        return false;
    }
}
