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

package io.spine.tools.archive;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.code.proto.FileDescriptorSets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A snapshot of an archive entry.
 */
public final class ArchiveEntry {

    private final byte[] bytes;

    private ArchiveEntry(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Creates a new instance.
     *
     * <p>Note that the passed {@code bytes} are not copied. Do not edit the array after calling
     * this method.
     */
    static ArchiveEntry of(byte[] bytes) {
        checkNotNull(bytes);
        return new ArchiveEntry(bytes);
    }

    /**
     * Attempts to {@linkplain FileDescriptorSets#parse parse} this entry as
     * a {@code FileDescriptorSet}.
     *
     * @return the parsed set
     * @throws java.lang.IllegalStateException if parsing fails
     */
    public FileDescriptorSet asDescriptorSet() {
        FileDescriptorSet descriptorSet = FileDescriptorSets
                .tryParse(bytes)
                .orElseThrow(() -> new IllegalStateException("Failed to parse a descriptor set."));
        return descriptorSet;
    }
}
