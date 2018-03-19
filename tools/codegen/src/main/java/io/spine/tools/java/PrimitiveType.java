/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.java;

import com.google.common.base.Optional;
import io.spine.tools.proto.ScalarType;

import static io.spine.tools.util.CodePreconditions.checkNotEmptyOrBlank;

/**
 * Enumeration of the Java primitives used for representing Proto scalar types.
 *
 * @author Dmytro Grankin
 * @author Alexander Yevsyukov
 */
public enum PrimitiveType {
    INT(int.class, Integer.class),
    LONG(long.class, Long.class),
    FLOAT(float.class, Float.class),
    DOUBLE(double.class, Double.class),
    BOOLEAN(boolean.class, Boolean.class);

    private final String name;
    private final Class<?> wrapperClass;

    PrimitiveType(Class<?> primitiveType, Class<?> wrapperClass) {
        this.name = primitiveType.getSimpleName();
        this.wrapperClass = wrapperClass;
    }

    /**
     * Returns the boxed {@link Class} for the Protobuf scalar primitive name.
     *
     * @param primitiveType the primitive type name
     * @return the wrapper class or {@link Optional#absent() Optional.absent()}
     * if the specified primitive name does not belong to {@link ScalarType}.
     */
    public static Optional<? extends Class<?>> getWrapperClass(String primitiveType) {
        checkNotEmptyOrBlank(primitiveType);
        for (PrimitiveType simpleType : values()) {
            if (simpleType.matchesName(primitiveType)) {
                return Optional.of(simpleType.getWrapperClass());
            }
        }

        return Optional.absent();
    }

    boolean matchesName(String typeName) {
        final boolean result = getName().equals(typeName);
        return result;
    }

    public String getName() {
        return name;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }
}
