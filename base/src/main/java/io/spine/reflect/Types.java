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

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@code Type}s.
 *
 * @author Illia Shepilov
 */
public final class Types {

    /** Prevent instantiation of this utility class. */
    private Types() {
    }

    /**
     * Creates the parametrized {@code Type} of the map.
     *
     * @param keyClass   the class of keys are maintained by this map
     * @param valueClass the class  of mapped values
     * @param <K>        the type of keys are maintained by this map
     * @param <V>        the type of the values stored in this map
     * @return the type of the map
     */
    public static <K, V> Type mapTypeOf(Class<K> keyClass, Class<V> valueClass) {
        checkNotNull(keyClass);
        checkNotNull(valueClass);
        // @formatter:off
        Type type = new TypeToken<Map<K, V>>() {}
                .where(new TypeParameter<K>() {}, keyClass)
                .where(new TypeParameter<V>() {}, valueClass)
                .getType();
        // @formatter:on
        return type;
    }

    /**
     * Creates the parametrized {@code Type} of the list.
     *
     * @param elementClass the class of the list elements
     * @param <T>          the type of the elements in this list
     * @return the type of the list
     */
    public static <T> Type listTypeOf(Class<T> elementClass) {
        checkNotNull(elementClass);
        // @formatter:off
        Type type = new TypeToken<List<T>>() {}
                    .where(new TypeParameter<T>() {}, elementClass)
                    .getType();
        // @formatter:on
        return type;
    }

    /**
     * Obtains the class of a generic type argument which is specified in the inheritance chain
     * of the passed class.
     *
     * @param cls               the end class for which we find the generic argument
     * @param genericSuperclass the superclass of the passed which has generic parameters
     * @param argNumber         the index of the generic parameter in the superclass
     * @param <T>               the type of superclass
     * @return the class of the generic type argument
     */
    static <T> Class<?> getArgument(Class<? extends T> cls,
                                    Class<T> genericSuperclass,
                                    int argNumber) {
        checkNotNull(cls);
        checkNotNull(genericSuperclass);
        TypeToken<?> supertypeToken =
                TypeToken.of(cls)
                         .getSupertype(genericSuperclass);
        ParameterizedType genericSupertype =
                (ParameterizedType) supertypeToken.getType();
        Type[] typeArguments = genericSupertype.getActualTypeArguments();
        Type typeArgument = typeArguments[argNumber];
        @SuppressWarnings("unchecked") // The type is ensured by the calling code.
                Class<?> result = (Class<?>) typeArgument;
        return result;
    }
}
