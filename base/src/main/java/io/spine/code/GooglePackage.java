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

package io.spine.code;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.java.ClassName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A set of utilities for working with the Google packages in Java and Protobuf.
 */
public final class GooglePackage {

    private static final String JAVA_STYLE = "com.google";
    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in another context.
    private static final String PROTOBUF_STYLE = "google";

    /**
     * Prevents the utility class instantiation.
     */
    private GooglePackage() {
    }

    /**
     * Checks that the given Protobuf file is not declared in Google package.
     */
    public static boolean notInGooglePackage(FileDescriptorProto descriptor) {
        checkNotNull(descriptor);
        return notInGooglePackage(descriptor.getPackage());
    }

    /**
     * Checks that the given Java class is not declared in Google package.
     */
    public static boolean notInGooglePackage(ClassName cls) {
        checkNotNull(cls);
        return notInGooglePackage(cls.packageName().value());
    }

    private static boolean notInGooglePackage(String packageName) {
        checkNotNull(packageName);
        return !packageName.startsWith(PROTOBUF_STYLE) && !packageName.startsWith(JAVA_STYLE);
    }
}
