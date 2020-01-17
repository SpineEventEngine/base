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

import com.google.common.collect.ImmutableSet;
import io.spine.test.code.proto.MessageDecl;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(TempDirectory.class)
@DisplayName("`FileSet` should")
class FileSetTest {

    private FileSet fileSet;

    @BeforeEach
    void load() {
        fileSet = FileSet.load();
    }

    @Test
    @DisplayName("load mains resources")
    void loadMainResources() {
        assertFalse(fileSet.isEmpty());
    }

    @Test
    @DisplayName("return all declared top-level messages")
    void returnTopLevelMessages() {
        ImmutableSet<FileName> fileNames =
                ImmutableSet.of(FileName.of("spine/test/code/proto/file_set_test.proto"));
        FileSet set = fileSet.find(fileNames);
        List<MessageType> types = set.topLevelMessages();
        assertThat(types).hasSize(1);

        MessageType onlyElement = types.get(0);
        assertThat(onlyElement.javaClass()).isEqualTo(MessageDecl.class);
    }

    @Test
    @DisplayName("filter message type by predicate")
    void findType() {
        String nameFragment = "Field";
        List<MessageType> types =
                fileSet.findMessageTypes((d) -> d.getName()
                                                 .contains(nameFragment));
        types.forEach(
                type -> assertThat(type.name().value()).contains(nameFragment)
        );
    }
}
