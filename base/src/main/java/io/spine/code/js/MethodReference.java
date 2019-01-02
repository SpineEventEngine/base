/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.js;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A reference to a method of a Protobuf type.
 *
 * <p>The reference allows to identify a method in code
 * since it includes a type name and a method name.
 */
public final class MethodReference {

    /** The type declaring the method. */
    private final TypeName typeName;
    /** The name of the method. */
    private final String methodName;
    /** Whether the method is defined on the prototype or not. */
    private final boolean onPrototype;

    private MethodReference(TypeName typeName, String methodName, boolean onPrototype) {
        checkNotNull(typeName);
        checkNotNull(methodName);
        this.typeName = typeName;
        this.methodName = methodName;
        this.onPrototype = onPrototype;
    }

    /**
     * Obtains the reference to the instance method of the specified type.
     */
    public static MethodReference onType(TypeName typeName, String methodName) {
        return new MethodReference(typeName, methodName, false);
    }

    /**
     * Obtains the reference to the static method of the specified type.
     */
    public static MethodReference onPrototype(TypeName typeName, String methodName) {
        return new MethodReference(typeName, methodName, true);
    }

    /**
     * Obtains the value of the reference.
     */
    public String value() {
        String delimiter = onPrototype
                           ? ".prototype."
                           : ".";
        return typeName + delimiter + methodName;
    }

    @Override
    public String toString() {
        return value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodReference)) {
            return false;
        }
        MethodReference reference = (MethodReference) o;
        return onPrototype == reference.onPrototype &&
                typeName.equals(reference.typeName) &&
                methodName.equals(reference.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, methodName, onPrototype);
    }
}
