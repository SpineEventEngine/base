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

package io.spine.code.proto;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.java.SimpleClassName;
import io.spine.type.ClassName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * A code generation metadata on a rejection.
 */
public final class RejectionType extends MessageType {

    /**
     * The suffix for the outer class name for the generated rejection messages.
     */
    @VisibleForTesting
    static final String OUTER_CLASS_NAME_SUFFIX = "Rejections";

    private final SimpleClassName outerJavaClass;

    /**
     * Creates a new instance.
     *  @param message
     *         the declaration of the rejection message
     *
     */
    RejectionType(Descriptor message) {
        super(message);
        this.outerJavaClass = SimpleClassName.outerOf(message.getFile());
    }

    /**
     * Returns {@code true} if the class name ends with {@code “Rejections”},
     * {@code false} otherwise.
     */
    public static boolean isValidOuterClassName(SimpleClassName className) {
        boolean result = className.value()
                                  .endsWith(OUTER_CLASS_NAME_SUFFIX);
        return result;
    }

    /**
     * Obtains the class name for the
     * {@link io.spine.base.RejectionMessage RejectionMessage}.
     *
     * @return the fully qualified class name for the rejection message
     */
    public ClassName messageClass() {
        ClassName outerClass = ClassName.of(javaPackage(), outerJavaClass);
        return outerClass.withNested(simpleJavaClassName());
    }

    /**
     * Obtains the class name of the {@linkplain io.spine.base.ThrowableMessage rejection}.
     *
     * @return the fully qualified class name for a throwable message
     */
    public ClassName throwableClass() {
        return ClassName.of(javaPackage().value() + '.' + descriptor().getName());
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(outerJavaClass);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RejectionType)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        RejectionType other = (RejectionType) obj;
        return Objects.equals(this.outerJavaClass, other.outerJavaClass);
    }
}
