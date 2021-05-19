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

package io.spine.reflect;

import com.google.common.base.Splitter;
import io.spine.annotation.Internal;
import kotlin.Metadata;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KCallable;
import kotlin.reflect.KClass;
import kotlin.reflect.KParameter;
import kotlin.reflect.KType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * A utility for integrating Java and Kotlin reflection API.
 */
@Internal
public final class J2Kt {

    /**
     * Prevents the utility class instantiation.
     */
    private J2Kt() {
    }

    /**
     * Tries to find a Kotlin {@code KCallable} which represents the same method as the given
     * Java {@code Method}.
     *
     * @param javaMethod the method to look up
     * @return a Kotlin representation of the method or {@code Optional.empty()} if this method is
     *         not declared in Kotlin
     */
    public static Optional<KCallable<?>> findKotlinMethod(Method javaMethod) {
        checkNotNull(javaMethod);
        Class<?> javaClass = javaMethod.getDeclaringClass();
        if (!isKotlin(javaClass)) {
            return Optional.empty();
        }
        KClass<?> kotlinClass = Reflection.getOrCreateKotlinClass(javaClass);
        if (isStatic(javaMethod.getModifiers())) {
            Optional<KClass<?>> companion = objectOrCompanion(kotlinClass);
            if (!companion.isPresent()) {
                return Optional.empty();
            }
            kotlinClass = companion.get();
        }
        Optional<KCallable<?>> result = tryMatch(javaMethod, kotlinClass);
        return result;
    }

    private static Optional<KCallable<?>> tryMatch(Method javaMethod, KClass<?> kotlinClass) {
        return kotlinClass.getMembers()
                          .stream()
                          .filter(new SameMethod(javaMethod))
                          .findFirst();
    }

    /**
     * Obtains either the given object class or the companion of the given class.
     *
     * @return the {@code kotlinClass} if it represents an object type, otherwise the class of
     *         the companion object of {@code kotlinClass} or {@code Optional.empty()} if the class
     *         has no companion object
     */
    private static Optional<KClass<?>> objectOrCompanion(KClass<?> kotlinClass) {
        if (kotlinClass.getObjectInstance() != null) {
            return Optional.of(kotlinClass);
        }
        return kotlinClass.getNestedClasses()
                          .stream()
                          .filter(KClass::isCompanion)
                          .findAny();
    }

    /**
     * Checks if this class is compiled from Kotlin source.
     *
     * <p>Relies on the {@code kotlin.Metadata} annotation to be present on the class.
     *
     * @return {@code true} if the given class is annotated with the {@code kotlin.Metadata}
     *          annotation, {@code false} otherwise
     */
    private static boolean isKotlin(Class<?> cls) {
        return cls.isAnnotationPresent(Metadata.class);
    }

    /**
     * A predicate which compares a Kotlin {@code KCallable} to a Java {@code Method} and checks if
     * they represent the same method or not.
     */
    private static final class SameMethod implements Predicate<KCallable<?>> {

        private static final String KOTLIN_NAME_MODULE_SEPARATOR = "$";
        private static final Splitter nameSplitter = Splitter.on(KOTLIN_NAME_MODULE_SEPARATOR);

        private final String name;
        private final List<KType> javaParamTypes;

        private SameMethod(Method javaMethod) {
            this.name = deObscureName(javaMethod.getName());
            this.javaParamTypes = stream(javaMethod.getParameterTypes())
                    .map(Reflection::typeOf)
                    .collect(toList());
        }

        @Override
        public boolean test(KCallable<?> method) {
            boolean nameMatches = name.equals(method.getName());
            if (!nameMatches) {
                return false;
            }
            boolean paramsMatch = paramTypesOf(method).equals(javaParamTypes);
            return paramsMatch;
        }

        private static List<KType> paramTypesOf(KCallable<?> method) {
            List<KParameter> params = method.getParameters();
            return params.stream()
                         .skip(1) // `this` instance as the first parameter.
                         .map(KParameter::getType)
                         .collect(toList());
        }

        private static String deObscureName(String name) {
            if (!name.contains(KOTLIN_NAME_MODULE_SEPARATOR)) {
                return name;
            }
            return nameSplitter.splitToList(name).get(0);
        }
    }
}
