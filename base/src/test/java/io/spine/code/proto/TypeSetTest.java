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

package io.spine.code.proto;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.test.code.proto.MessageDecl;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("TypeSet should")
class TypeSetTest {

    private static final FileSet fileSet = FileSet.load();

    @Test
    @DisplayName("obtain messages and enums from a file")
    void fromFile() {
        @SuppressWarnings("OptionalGetWithoutIsPresent") /* The file is present in resources. */
        FileDescriptor file = fileSet.tryFind(FileName.of("google/protobuf/descriptor.proto"))
                                     .get();
        TypeSet typeSet = TypeSet.from(file);
        assertNotEmpty(typeSet);
        assertThat(typeSet.contains(TypeName.from(FileDescriptorSet.getDescriptor())))
                .isTrue();
    }

    @Test
    @DisplayName("obtain message and enums")
    void fromSet() {
        TypeSet typeSet = TypeSet.from(fileSet);
        assertNotEmpty(typeSet);
        // We have a number of test service declarations for testing annotations.
        assertThat(typeSet.serviceTypes())
                .isNotEmpty();
    }

    @Test
    @DisplayName("obtain top-level messages of the file")
    void topLevelMessages() {
        ImmutableSet<FileName> fileNames =
                ImmutableSet.of(FileName.of("spine/test/code/proto/type_set_test.proto"));
        FileSet set = fileSet.find(fileNames);
        ImmutableCollection<MessageType> types = TypeSet.topLevelMessages(set);
        assertThat(types).hasSize(1);

        MessageType onlyElement = types.asList()
                                       .get(0);
        assertThat(onlyElement.javaClass()).isEqualTo(MessageDecl.class);
    }

    void assertNotEmpty(TypeSet typeSet) {
        assertThat(typeSet.isEmpty())
                .isFalse();
        assertThat(typeSet.allTypes())
                .isNotEmpty();
        assertThat(typeSet.messageTypes())
                .isNotEmpty();
        assertThat(typeSet.enumTypes())
                .isNotEmpty();
    }
}
