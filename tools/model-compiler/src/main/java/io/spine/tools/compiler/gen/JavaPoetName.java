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

package io.spine.tools.compiler.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import io.spine.code.gen.java.NestedClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;

import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkState;

/**
 * A value holder of JavaPoet {@link TypeName}.
 */
public final class JavaPoetName {

    private final TypeName value;

    private JavaPoetName(TypeName value) {
        this.value = value;
    }

    public static JavaPoetName of(Type type) {
        TypeName typeName = TypeName.get(type);
        return new JavaPoetName(typeName);
    }

    public static JavaPoetName of(io.spine.code.java.ClassName className) {
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
}
