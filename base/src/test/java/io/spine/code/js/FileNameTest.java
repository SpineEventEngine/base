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

package io.spine.code.js;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.PackageName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FileName should")
class FileNameTest {

    private FileDescriptor file;

    @BeforeEach
    void setUp() {
        file = Any.getDescriptor()
                  .getFile();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(FileName.class);
    }

    @Test
    @DisplayName("replace `.proto` extension with predefined suffix")
    void appendSuffix() {
        FileName fileName = FileName.from(file);
        String expected = "google/protobuf/any_pb.js";
        assertEquals(expected, fileName.value());
    }

    @Test
    @DisplayName("return path elements")
    void returnPathElements() {
        FileName fileName = FileName.from(file);
        String[] pathElements = fileName.pathElements();
        List<String> pathElementList = Arrays.asList(pathElements);
        assertThat(pathElementList).contains("google");
        assertThat(pathElementList).contains("protobuf");
        assertThat(pathElementList).contains("any_pb.js");
    }

    @Test
    @DisplayName("obtain Protobuf package")
    void obtainProtoPackage() {
        FileName fileName = FileName.from(file);
        PackageName protoPackage = fileName.protoPackage();
        assertEquals(PackageName.googleProtobuf, protoPackage);
    }
}
