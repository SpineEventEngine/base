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

package io.spine.tools.protoc;

import io.spine.code.java.ClassName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotDefaultArg;

/**
 * An utility for working with {@link UuidConfig} and {@link ConfigByPattern} code generation
 * task configurations.
 */
public final class ProtocTaskConfigs {

    /** Prevents instantiation of this utility class. */
    private ProtocTaskConfigs() {
    }

    /**
     * Creates a new {@code UuidConfig} instance from the supplied {@code className}.
     */
    public static UuidConfig uuidConfig(ClassName className) {
        checkNotNull(className);
        return UuidConfig.newBuilder()
                .setValue(className.value())
                .build();
    }

    /**
     * Creates a new {@code EntityStateConfig} instance from the supplied {@code className}.
     */
    public static EntityStateConfig entityStateConfig(ClassName className) {
        checkNotNull(className);
        return EntityStateConfig.newBuilder()
                .setValue(className.value())
                .build();
    }

    /**
     * Creates a new configuration pattern for making messages defined in the files matching
     * the passed pattern, implement the interface with the passed name.
     *
     * @param pattern
     *          the pattern of file names where message type of interest are defined
     * @param interfaceToImplement
     *          the name of the interface to be implemented by the generated message classes
     * @throws IllegalArgumentException
     *          if the file pattern is empty
     */
    public static ConfigByPattern
    byPatternConfig(FilePattern pattern, ClassName interfaceToImplement) {
        checkNotNull(interfaceToImplement);
        checkNotDefaultArg(pattern);
        return ConfigByPattern.newBuilder()
                .setValue(interfaceToImplement.value())
                .setPattern(pattern)
                .build();
    }
}
