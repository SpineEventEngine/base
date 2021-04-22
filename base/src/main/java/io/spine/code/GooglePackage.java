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

import com.google.protobuf.Descriptors.FileDescriptor;

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
     * Verifies if the passed package name belongs to Google code.
     *
     * <p>Handles Protobuf and Java package names.
     */
    public static boolean test(String packageName) {
        boolean result =
                packageName.startsWith(PROTOBUF_STYLE)
                || packageName.startsWith(JAVA_STYLE);
        return result;
    }

    /**
     * Verifies if the passed file declares types under the "google" package.
     */
    public static boolean isGoogle(FileDescriptor file) {
        return test(file.toProto().getPackage());
    }

    /**
     * Verifies if the passed file declares types NOT under the "google" package.
     */
    public static boolean isNotGoogle(FileDescriptor file) {
        return !isGoogle(file);
    }
}
