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

package io.spine.code.java;

import com.google.common.truth.StringSubject;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Timestamp;
import io.spine.test.code.OuterClassTest;
import io.spine.testing.UtilityClassTest;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.code.java.ClassName.OUTER_CLASS_DELIMITER;
import static io.spine.code.java.Names.containingClassPrefix;
import static io.spine.code.java.Names.outerClassPrefix;

@DisplayName("Names utility class should")
class NamesTest extends UtilityClassTest<Names> {

    NamesTest() {
        super(Names.class);
    }

    private static StringSubject assertOuterClassPrefix(Descriptor type) {
        String prefix = outerClassPrefix(type.getFile());
        return assertThat(prefix);
    }

    private static StringSubject assertContainingClassPrefix(@Nullable Descriptor type) {
        String prefix = containingClassPrefix(type);
        return assertThat(prefix);
    }

    @Test
    @DisplayName("obtain outer class prefix")
    void outerPrefix() {
        StringSubject assertPrefix = assertOuterClassPrefix(
                OuterClassTest.NtLevelOne.getDescriptor()
        );

        assertPrefix.contains(OuterClassTest.class.getSimpleName());
        assertPrefix.endsWith(String.valueOf(OUTER_CLASS_DELIMITER));
    }

    @Test
    @DisplayName("return empty string if no outer class")
    void noOuterPrefix() {
        assertOuterClassPrefix(Timestamp.getDescriptor())
                .isEmpty();
    }

    @Test
    @DisplayName("obtain containing class prefix")
    void containingPrefix() {
        StringSubject assertPrefix = assertContainingClassPrefix(
                OuterClassTest.NtLevelOne.getDescriptor()
        );

        assertPrefix.contains(OuterClassTest.NtLevelOne.class.getSimpleName());
        assertPrefix.endsWith(String.valueOf(ClassName.OUTER_CLASS_DELIMITER));
    }

    @Test
    @DisplayName("return empty prefix if no containing message")
    void topLevel() {
        assertContainingClassPrefix(null).isEmpty();
    }
}
