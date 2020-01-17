/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.gen.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A value holder of JavaPoet {@link TypeName}.
 */
public final class JavaPoetName {

    private final TypeName value;

    private JavaPoetName(TypeName value) {
        this.value = value;
    }

    public static JavaPoetName of(TypeName value) {
        checkNotNull(value);
        return new JavaPoetName(value);
    }

    public static JavaPoetName of(Type type) {
        checkNotNull(type);
        TypeName typeName = TypeName.get(type);
        return new JavaPoetName(typeName);
    }

    public static JavaPoetName of(SimpleClassName simpleName) {
        checkNotNull(simpleName);
        TypeName value = toClassName(simpleName);
        return new JavaPoetName(value);
    }

    public static JavaPoetName of(io.spine.code.java.ClassName className) {
        checkNotNull(className);
        PackageName packageName = className.packageName();
        SimpleClassName topLevel = className.topLevelClass();
        String[] nestingChain = NestedClassName.from(className)
                                               .split()
                                               .stream()
                                               .skip(1)
                                               .map(SimpleClassName::value)
                                               .toArray(String[]::new);
        TypeName value = ClassName.get(packageName.value(), topLevel.value(), nestingChain);
        return new JavaPoetName(value);
    }

    public static JavaPoetName parameterized(Class<?> rawType, JavaPoetName... arguments) {
        ClassName rawTypeName = ClassName.get(rawType);
        TypeName[] args = Arrays.stream(arguments)
                                .map(JavaPoetName::value)
                                .toArray(TypeName[]::new);
        ParameterizedTypeName parameterized = ParameterizedTypeName.get(rawTypeName, args);
        return new JavaPoetName(parameterized);
    }

    public TypeName value() {
        return value;
    }

    /**
     * Returns this type name as {@link ClassName}.
     *
     * @throws IllegalStateException
     *         if the held value is not actually a {@link ClassName} (e.g. it could be a primitive
     *         type name instead)
     */
    public ClassName className() {
        checkState(value instanceof ClassName,
                   "The type name is of type `%s`, expected an instance of `%s`.",
                   value.getClass()
                        .getCanonicalName(), ClassName.class.getCanonicalName());
        return (ClassName) value;
    }

    private static ClassName toClassName(SimpleClassName simpleName) {
        return ClassName.get("", simpleName.value());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JavaPoetName name = (JavaPoetName) o;
        return Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
