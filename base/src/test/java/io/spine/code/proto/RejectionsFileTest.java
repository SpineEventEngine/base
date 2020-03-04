/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.EmptyProto;
import io.spine.test.code.proto.FakeRejectionsProto;
import io.spine.test.code.proto.MoreFakeRejections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`RejectionsFile` should")
class RejectionsFileTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nonNull() {
        new NullPointerTester()
                .testAllPublicStaticMethods(RejectionsFile.class);
    }

    @Test
    @DisplayName("accept only files ending with `rejections.proto`")
    void checkFileName() {
        SourceFile sourceFile = SourceFile.from(EmptyProto.getDescriptor());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> RejectionsFile.from(sourceFile));
        assertThat(exception)
                .hasMessageThat()
                .contains("`rejections.proto`");
    }

    @Test
    @DisplayName("accept only files with `Rejections` outer class name")
    void checkOuterClassName() {
        SourceFile sourceFile = SourceFile.from(FakeRejectionsProto.getDescriptor());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> RejectionsFile.from(sourceFile));
        assertThat(exception)
                .hasMessageThat()
                .contains("`Rejections`");
    }

    @Test
    @DisplayName("accept only files with `java_multiple_files = false`")
    void checkMultipleFiles() {
        SourceFile sourceFile = SourceFile.from(MoreFakeRejections.getDescriptor());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> RejectionsFile.from(sourceFile));
        assertThat(exception)
                .hasMessageThat()
                .contains("`java_multiple_files`");
    }
}
