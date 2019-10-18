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

package io.spine.code.java;

import io.spine.value.StringTypeValue;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class MethodName extends StringTypeValue implements CamelCasedName {

    private static final long serialVersionUID = 0L;

    private static final String GETTER_PREFIX = "get|is";
    private static final Pattern GETTER_PREFIX_PATTERN = Pattern.compile(GETTER_PREFIX);

    private final Class<?> declaringClass;

    private MethodName(String value, Class<?> declaringClass) {
        super(value);
        this.declaringClass = declaringClass;
    }

    public static MethodName of(Method method) {
        checkNotNull(method);
        return new MethodName(method.getName(), method.getDeclaringClass());
    }

    public String fullyQualifiedName() {
        String result = format("%s.%s", declaringClass.getCanonicalName(), value());
        return result;
    }

    public boolean isGetter() {
        String firstWord = words().get(0);
        boolean result = GETTER_PREFIX_PATTERN.matcher(firstWord)
                                              .matches();
        return result;
    }
}
