/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.reflect;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Utility class for working with {@code Type}s.
 */
public final class Types {

    /** Prevents instantiation of this utility class. */
    private Types() {
    }

    /**
     * Creates the parametrized {@code Type} of the map.
     *
     * @param keyClass
     *         the class of keys are maintained by this map
     * @param valueClass
     *         the class  of mapped values
     * @param <K>
     *         the type of keys are maintained by this map
     * @param <V>
     *         the type of the values stored in this map
     * @return the type of the map
     */
    public static <K, V> Type mapTypeOf(Class<K> keyClass, Class<V> valueClass) {
        checkNotNull(keyClass);
        checkNotNull(valueClass);
        // @formatter:off
        var type = new TypeToken<Map<K, V>>() {}
                .where(new TypeParameter<>() {}, keyClass)
                .where(new TypeParameter<>() {}, valueClass)
                .getType();
        // @formatter:on
        return type;
    }

    /**
     * Creates the parametrized {@code Type} of the list.
     *
     * @param elementClass
     *         the class of the list elements
     * @param <T>
     *         the type of the elements in this list
     * @return the type of the list
     */
    public static <T> Type listTypeOf(Class<T> elementClass) {
        checkNotNull(elementClass);
        // @formatter:off
        var type = new TypeToken<List<T>>() {}
                    .where(new TypeParameter<>() {}, elementClass)
                    .getType();
        // @formatter:on
        return type;
    }

    /**
     * Checks if the given type is a {@code enum} {@code Class}.
     *
     * @param type
     *         the type to check
     * @return {@code true} if the given type is a {@code enum} class and {@code false} otherwise
     */
    public static boolean isEnumClass(Type type) {
        checkNotNull(type);
        if (type instanceof Class) {
            var cls = (Class<?>) type;
            var isEnum = cls.isEnum();
            return isEnum;
        }
        return false;
    }

    /**
     * Checks that the type is a {@code Class} of the {@code Message}.
     *
     * @deprecated Please use {@code Type.isMessageClass()} Kotlin extension function
     *         and its Java analogue {@code isMessageClass(Type type)}.
     */
    @Deprecated(forRemoval = true)
    public static boolean isMessageClass(Type type) {
        checkNotNull(type);
        if (type instanceof Class) {
            var cls = (Class<?>) type;
            try {
                var messageClass = Class.forName("com.google.protobuf.Message");
                var isMessage = messageClass.isAssignableFrom(cls);
                return isMessage;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Obtains parameter values of a parameterized type.
     *
     * <p>If the parameters are generic types themselves, their arguments are preserved.
     *
     * <p>For non-parameterized types, the empty list is returned.
     *
     * @implNote
     * The arguments of a raw class will be resolved to generic variable declarations, as the
     * information on their actual values is erased.
     *
     * @param type
     *         the parameterized type
     * @return the list of the type argument values
     */
    public static ImmutableList<Type> resolveArguments(Type type) {
        checkNotNull(type);
        var token = TypeToken.of(type);
        TypeVariable<? extends Class<?>>[] params = token.getRawType()
                                                         .getTypeParameters();
        var result = Arrays.stream(params)
                .map(token::resolveType)
                .map(TypeToken::getType)
                .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains the class of a generic type argument which is specified in the inheritance chain
     * of the passed class.
     *
     * @param cls
     *         the end class for which we find the generic argument
     * @param genericSuperclass
     *         the superclass of the passed which has generic parameters
     * @param argNumber
     *         the index of the generic parameter in the superclass
     * @param <T>
     *         the type of superclass
     * @return the class of the generic type argument
     */
    static
    <T> Class<?> argumentIn(Class<? extends T> cls, Class<T> genericSuperclass, int argNumber) {
        checkNotNull(cls);
        checkNotNull(genericSuperclass);
        var supertypeToken = TypeToken.of(cls).getSupertype(genericSuperclass);
        var typeArgs = resolveArguments(supertypeToken.getType());
        var argValue = typeArgs.get(argNumber);
        var result = TypeToken.of(argValue).getRawType();
        return result;
    }
}
