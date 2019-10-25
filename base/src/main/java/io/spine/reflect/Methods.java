/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.spine.util.Exceptions.newIllegalStateException;

public final class Methods {

    private Methods() {
    }

    /**
     *
     * @throws IllegalArgumentException
     *         if the target is wrong
     * @throws IllegalStateException
     */
    public static Object setAccessibleAndInvoke(Method method, Object target) {
        try {
            method.setAccessible(true);
            Object result = method.invoke(target);
            method.setAccessible(false);
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw newIllegalStateException(
                    e,
                    "Method `%s` invocation on target `%s` of class `%s` failed.",
                    method.getName(), target, target.getClass().getCanonicalName());
        }
    }
}
