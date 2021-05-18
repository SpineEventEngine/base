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

package io.spine.code.java;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors;
import com.google.protobuf.StringValue;
import io.spine.net.Uri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`ClassName` should")
class ClassNameTest {

    @DisplayName("reject value")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "    "})
    void rejectEmptyAndBlankValues(String value) {
        assertIllegalArgument(() -> ClassName.of(value));
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        Descriptors.Descriptor descriptor = StringValue.getDescriptor();
        new NullPointerTester()
                .setDefault(SimpleClassName.class, SimpleClassName.ofMessage(descriptor))
                .setDefault(PackageName.class, PackageName.resolve(descriptor.getFile()
                                                                             .toProto()))
                .testAllPublicStaticMethods(ClassName.class);
    }

    @Test
    @DisplayName("provide binary name and canonical name")
    void provideBinaryAndCanonical() {
        Class<Uri.Protocol> cls = Uri.Protocol.class;
        ClassName className = ClassName.of(cls);
        assertThat(className.binaryName()).isEqualTo(cls.getName());
        assertThat(className.canonicalName()).isEqualTo(cls.getCanonicalName());
    }

    @Test
    @DisplayName("throw ISE when parsing an invalid name")
    void throwOnInvalid() {
        ClassName className = ClassName.of("NotQualifiedName");
        assertIllegalState(className::packageName);
    }

    @Test
    @DisplayName("obtain a package of a class")
    void gettingPackage() {
        assertThat(ClassName.of(String.class).packageName())
                .isEqualTo(PackageName.of(String.class));
    }
}
