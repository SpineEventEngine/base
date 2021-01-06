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

package io.spine.type;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FileName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A code generation metadata on a rejection.
 */
public final class RejectionType extends MessageType {

    /**
     * The suffix for the outer class name for the generated rejection messages.
     */
    private static final String OUTER_CLASS_NAME_SUFFIX = "Rejections";

    private final SimpleClassName outerJavaClass;

    /**
     * Verifies if the passed message type is a rejection.
     *
     * <p>The message must be a top level, and declared in the a file with
     * {@linkplain FileName#isRejections() corresponding name}.
     */
    public static boolean test(Descriptor type) {
        checkNotNull(type);
        boolean topLevel = isTopLevel(type);
        boolean inRejectionsFile = FileName.from(type.getFile())
                                           .isRejections();
        return topLevel && inRejectionsFile;
    }

    /**
     * Creates a new instance.
     *
     * @param message
     *         the declaration of the rejection message
     */
    public RejectionType(Descriptor message) {
        super(message);
        checkArgument(
                test(message),
                "Cannot create rejection type from the type `%s`.", message.getFullName()
        );
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
