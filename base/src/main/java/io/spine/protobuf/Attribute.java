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

package io.spine.protobuf;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.util.NamedProperty;

import java.util.Map;
import java.util.Optional;

import static io.spine.protobuf.TypeConverter.toAny;
import static io.spine.protobuf.TypeConverter.toObject;

/**
 * An attribute stored in a protobuf {@code map<string, Any>}.
 *
 * @param <T> the type of the attribute value, which can be {@code Integer}, {@code Long},
 *            {@code Float}, {@code Double}, {@code Boolean}, {@code String}, or a class
 *            implementing {@code Message}
 * @param <M> the type of the message object to which the attribute belongs
 * @param <B> the type of the message builder
 * @author Alexander Yevsyukov
 */
public abstract class Attribute<T, M extends Message, B extends Message.Builder>
        extends NamedProperty<T, M> {

    /**
     * Creates a new instance with the passed name.
     *
     * @param name the key in the attribute map
     */
    protected Attribute(String name) {
        super(name);
    }

    /**
     * Obtains attribute map from the enclosing object.
     */
    protected abstract Map<String, Any> getMap(M obj);

    /**
     * Returns the attribute value or {@code Optional.empty()} if the attribute is not set.
     */
    @Override
    public final Optional<T> getValue(M obj) {
        Map<String, Any> map = getMap(obj);
        Optional<T> result = getFromMap(map);
        return result;
    }

    private Optional<T> getFromMap(Map<String, Any> map) {
        Any any = map.get(getName());
        if (any == null || Any.getDefaultInstance()
                              .equals(any)) {
            return Optional.empty();
        }

        T result = toObject(any, getValueClass());
        return Optional.of(result);
    }

    /**
     * Sets the value of the attribute in the passed builder.
     */
    public void setValue(B builder, T value) {
        Any packed = toAny(value);
        getMapModifier(builder).putEntry(getName(), packed);
    }

    /**
     * Implementation should return a reference for a builder method that
     * puts an entry into a map.
     *
     * <p>If a proto map is declared:
     * <pre>{@code
     *     map<string, google.protobuf.Any> item = 5;
     * }</pre>
     * the method reference would be {@code builder::putItem}.
     */
    protected abstract MapModifier<B> getMapModifier(B builder);

    /**
     * A functional interface for obtaining map mutation method reference.
     *
     * @param <B> the type of the builder of a message which contains a map with attributes
     */
    @FunctionalInterface
    public interface MapModifier<B extends Message.Builder> {

        @SuppressWarnings("UnusedReturnValue") // returned builder instance can be ignored
        @CanIgnoreReturnValue
        B putEntry(String key, Any value);
    }
}
