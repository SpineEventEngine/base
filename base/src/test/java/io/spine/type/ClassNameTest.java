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

package io.spine.type;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors;
import com.google.protobuf.StringValue;
import io.spine.code.java.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ClassName should")
class ClassNameTest {

    @DisplayName("reject value")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "    "})
    void rejectEmptyAndBlankValues(String value) {
        assertThrows(IllegalArgumentException.class,
                     () -> ClassName.of(value));
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void pass_null_tolerance_check() {
        Descriptors.Descriptor descriptor = StringValue.getDescriptor();
        new NullPointerTester()
                .setDefault(SimpleClassName.class, SimpleClassName.ofMessage(descriptor))
                .setDefault(PackageName.class, PackageName.resolve(descriptor.getFile()
                                                                             .toProto()))
                .testAllPublicStaticMethods(ClassName.class);
    }
}
