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

package io.spine.string;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@code Stringifier}s.
 */
public final class Stringifiers {

    /** Prevents instantiation of this utility class. */
    private Stringifiers() {
    }

    /**
     * Converts the passed value to the string representation.
     *
     * <p>Use this method for converting non-generic objects. For generic objects,
     * please use {@link #toString(Object, Type)}.
     *
     * @param object
     *         the object to convert
     * @param <T>
     *         the type of the object
     * @return the string representation of the passed object
     */
    public static <T> String toString(T object) {
        checkNotNull(object);
        return toString(object, object.getClass());
    }

    /**
     * Converts the passed value to the string representation.
     *
     * <p>This method must be used if the passed object is a generic type.
     *
     * @param object
     *         to object to convert
     * @param typeOfT
     *         the type of the passed object
     * @param <T>
     *         the type of the object to convert
     * @return the string representation of the passed object
     * @throws MissingStringifierException
     *         if passed value cannot be converted
     */
    public static <T> String toString(T object, Type typeOfT) {
        checkNotNull(object);
        checkNotNull(typeOfT);
        Stringifier<T> stringifier = StringifierRegistry.getStringifier(typeOfT);
        String result = stringifier.convert(object);
        return result;
    }

    /**
     * Converts string value to the specified type.
     *
     * @param str
     *         the string to convert
     * @param typeOfT
     *         the type into which to convert the string
     * @param <T>
     *         the type of the value to return
     * @return the parsed value from string
     * @throws MissingStringifierException
     *         if passed value cannot be converted
     */
    public static <T> T fromString(String str, Class<T> typeOfT) {
        checkNotNull(str);
        checkNotNull(typeOfT);
        Stringifier<T> stringifier = StringifierRegistry.getStringifier(typeOfT);
        T result = stringifier.reverse()
                              .convert(str);
        return result;
    }

    /**
     * Obtains {@code Stringifier} for the map with default delimiter for the passed map elements.
     *
     * @param keyClass
     *         the class of keys are maintained by this map
     * @param valueClass
     *         the class  of mapped values
     * @param <K>
     *         the type of keys are maintained by this map
     * @param <V>
     *         the type of the values stored in this map
     * @return the stringifier for the map
     */
    public static <K, V>
    Stringifier<Map<K, V>> newForMapOf(Class<K> keyClass, Class<V> valueClass) {
        checkNotNull(keyClass);
        checkNotNull(valueClass);
        Stringifier<Map<K, V>> result = new MapStringifier<>(keyClass, valueClass);
        return result;
    }

    /**
     * Obtains {@code Stringifier} for the map with custom delimiter for the passed map elements.
     *
     * @param keyClass
     *         the class of keys are maintained by this map
     * @param valueClass
     *         the class  of mapped values
     * @param delimiter
     *         the delimiter for the passed map elements via string
     * @param <K>
     *         the type of keys are maintained by this map
     * @param <V>
     *         the type of mapped values
     * @return the stringifier for the map
     */
    public static <K, V>
    Stringifier<Map<K, V>> newForMapOf(Class<K> keyClass, Class<V> valueClass, char delimiter) {
        checkNotNull(keyClass);
        checkNotNull(valueClass);
        Stringifier<Map<K, V>> result = new MapStringifier<>(keyClass, valueClass, delimiter);
        return result;
    }

    /**
     * Obtains {@code Stringifier} for {@code Boolean} values.
     */
    public static Stringifier<Boolean> forBoolean() {
        return BooleanStringifier.getInstance();
    }

    /**
     * Obtains {@code Stringifier} for {@code Integer} values.
     */
    public static Stringifier<Integer> forInteger() {
        return IntegerStringifier.getInstance();
    }

    /**
     * Obtains {@code Stringifier} for {@code Long} values.
     */
    public static Stringifier<Long> forLong() {
        return LongStringifier.getInstance();
    }

    /**
     * Obtains {@code Stringifier} for {@code String} values.
     *
     * <p>Simply returns passed strings.
     */
    static Stringifier<String> forString() {
        return NoOpStringifier.getInstance();
    }

    /**
     * Obtains the default stringifier for {@code Duration} instances.
     *
     * <p>This stringifier is automatically registered in the
     * {@link StringifierRegistry StringifierRegistry}.
     *
     * @see com.google.protobuf.util.Durations#toString(Duration) Durations.toString(Duration)
     * @see com.google.protobuf.util.Durations#parse(String) Durations.parse(String)
     */
    public static Stringifier<Duration> forDuration() {
        return DurationStringifier.getInstance();
    }

    /**
     * Obtains a stringifier that coverts a Timestamp into to RFC 3339 date string format.
     *
     * @see com.google.protobuf.util.Timestamps#toString(Timestamp) Timestamps.toString(Timestamp)
     * @see com.google.protobuf.util.Timestamps#parse(String) Timestamps.parse(String)
     */
    public static Stringifier<Timestamp> forTimestamp() {
        return TimestampStringifier.getInstance();
    }

    /**
     * Obtains {@code Stringifier} for list with default delimiter for the passed list elements.
     *
     * @param elementClass
     *         the class of the list elements
     * @param <T>
     *         the type of the elements in this list
     * @return the stringifier for the list
     */
    public static <T> Stringifier<List<T>> newForListOf(Class<T> elementClass) {
        checkNotNull(elementClass);
        Stringifier<List<T>> result = new ListStringifier<>(elementClass);
        return result;
    }

    /**
     * Obtains {@code Stringifier} for list with the custom delimiter for the passed list elements.
     *
     * @param elementClass
     *         the class of the list elements
     * @param delimiter
     *         the delimiter or the list elements passed via string
     * @param <T>
     *         the type of the elements in this list
     * @return the stringifier for the list
     */
    public static <T> Stringifier<List<T>> newForListOf(Class<T> elementClass, char delimiter) {
        checkNotNull(elementClass);
        Stringifier<List<T>> result = new ListStringifier<>(elementClass, delimiter);
        return result;
    }

    /**
     * Obtains a {@code Stringifier} for the passed {@code enum} class.
     *
     * @param enumClass
     *         the {@code enum} class
     * @param <T>
     *         the type of the {@code enum}
     * @return the stringifier for the passed {@code enum} class
     */
    public static <T extends Enum<T>> Stringifier<T> newForEnum(Class<T> enumClass) {
        checkNotNull(enumClass);
        EnumStringifier<T> result = new EnumStringifier<>(enumClass);
        return result;
    }

    /**
     * Obtains the default {@code Stringifier} for the {@code Message} classes.
     *
     * @param messageClass
     *         the message class
     * @param <T>
     *         the type of the message
     * @return the default stringifier
     */
    static <T extends Message> Stringifier<T> newForMessage(Class<T> messageClass) {
        checkNotNull(messageClass);
        DefaultMessageStringifier<T> result = new DefaultMessageStringifier<>(messageClass);
        return result;
    }

    /**
     * Creates the {@code Escaper} which escapes contained '\' and passed characters.
     *
     * @param charToEscape
     *         the char to escape
     * @return the constructed escaper
     */
    static Escaper createEscaper(char charToEscape) {
        String escapedChar = "\\" + charToEscape;
        Escaper result = Escapers.builder()
                                 .addEscape('\"', "\\\"")
                                 .addEscape(charToEscape, escapedChar)
                                 .build();
        return result;
    }
}
