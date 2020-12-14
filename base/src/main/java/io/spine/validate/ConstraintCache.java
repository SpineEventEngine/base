/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.spine.code.proto.FieldContext;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * Cache of constraints per {@link MessageType} in a {@link FieldContext}.
 *
 * @implNote The cache consists of a {@code 1000} entries, which is, most likely, not all
 * the combinations of {@link MessageType}s and {@link FieldContext}s. However, as any kind of
 * caching is a trade off between speed and the allocated memory, it's assumed that {@code 1000}
 * entries is just enough.
 */
final class ConstraintCache {

    private static final int CACHE_SIZE = 1000;

    private static final LoadingCache<Key, Constraints> allConstraints = CacheBuilder
            .newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new ConstraintLoader());
    private static final LoadingCache<Key, Constraints> customConstraints = CacheBuilder
            .newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new CustomConstraintLoader());

    /**
     * Prevents the utility class instantiation.
     */
    private ConstraintCache() {
    }

    /**
     * Obtains the constraints for the given type and field context from cache.
     *
     * <p>If there is no cache entry for these params, loads the value into the cache and returns
     * it.
     */
    static Constraints forType(MessageType type, FieldContext context) {
        checkNotNull(type);
        checkNotNull(context);

        return fromCache(allConstraints, type, context);
    }

    /**
     * Obtains non-standard constraints for the given type and field context from cache.
     *
     * <p>If there is no cache entry for these params, loads the value into the cache and returns
     * it.
     */
    static Constraints customForType(MessageType type, FieldContext context) {
        checkNotNull(type);
        checkNotNull(context);

        return fromCache(customConstraints, type, context);
    }

    private static Constraints
    fromCache(LoadingCache<Key, Constraints> constraints, MessageType type, FieldContext context) {
        Key key = new Key(type, context);
        try {
            return constraints.get(key);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock")
                // `get(..)` Can throw checked and unchecked Exceptions and Errors.
                Throwable e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * The constraints cache key.
     */
    private static final class Key {

        private final MessageType type;
        private final FieldContext fieldContext;

        private Key(MessageType type, FieldContext context) {
            this.type = checkNotNull(type);
            this.fieldContext = checkNotNull(context);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equal(type, key.type) &&
                    Objects.equal(fieldContext, key.fieldContext);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type, fieldContext);
        }
    }

    /**
     * Loads a cache of all constraints per type per field context.
     */
    private static final class ConstraintLoader extends CacheLoader<Key, Constraints> {

        @Override
        public Constraints load(Key key) {
            return Constraints.loadFor(key.type, key.fieldContext);
        }
    }

    /**
     * Loads a cache of non-standard constraints per type per field context.
     */
    private static final class CustomConstraintLoader extends CacheLoader<Key, Constraints> {

        @Override
        public Constraints load(Key key) {
            return Constraints.loadCustomFor(key.type, key.fieldContext);
        }
    }
}
