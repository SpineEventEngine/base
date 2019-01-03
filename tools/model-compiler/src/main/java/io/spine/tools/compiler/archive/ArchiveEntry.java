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

package io.spine.tools.compiler.archive;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.code.proto.FileDescriptorSets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A snapshot of an archive entry.
 */
public final class ArchiveEntry {

    private final FileDescriptorSet descriptorSet;

    private ArchiveEntry(FileDescriptorSet descriptorSet) {
        this.descriptorSet = descriptorSet;
    }

    /**
     * Creates a new instance of {@code ArchiveEntry}.
     */
    static ArchiveEntry of(byte[] bytes) {
        checkNotNull(bytes);
        FileDescriptorSet descriptorSet = FileDescriptorSets.parse(bytes);
        return new ArchiveEntry(descriptorSet);
    }

    public FileDescriptorSet asDescriptorSet() {
        return descriptorSet;
    }
}
