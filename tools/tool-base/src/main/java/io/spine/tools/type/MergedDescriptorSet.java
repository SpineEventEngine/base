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

package io.spine.tools.type;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.annotation.Internal;
import io.spine.code.proto.FileSet;

/**
 * A view on a {@code FileDescriptorSet} after merging.
 */
@Internal
public final class MergedDescriptorSet {

    private final ImmutableSet<FileDescriptorProto> descriptors;
    private final FileSet fileSet;

    MergedDescriptorSet(FileDescriptorSet descriptorSet) {
        this.descriptors = ImmutableSet.copyOf(descriptorSet.getFileList());
        this.fileSet = FileSet.of(descriptors);
    }

    /**
     * Extends the {@code KnownTypes} of this application with the descriptor set.
     */
    public void loadIntoKnownTypes() {
        MoreKnownTypes.extendWith(fileSet);
    }

    /**
     * Obtains the file set from the descriptor set.
     */
    public FileSet fileSet() {
        return fileSet;
    }

    @VisibleForTesting
    ImmutableSet<FileDescriptorProto> descriptors() {
        return descriptors;
    }
}
