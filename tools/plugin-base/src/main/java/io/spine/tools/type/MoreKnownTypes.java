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

package io.spine.tools.type;

import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.type.KnownTypes;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A utility for extending the system {@link KnownTypes}.
 *
 * <p>When performing build-time model processing, it may be necessary to add all the processed
 * types to the set of known types. This utility provides the only way to add new types to
 * the system.
 */
public final class MoreKnownTypes {

    /**
     * Prevents the utility class instantiation.
     */
    private MoreKnownTypes() {
    }

    /**
     * Reads a {@code FileDescriptorSet} from the given file and adds the described types
     * to the known types.
     *
     * @param descriptorSetFile
     *         the descriptor file to read
     * @implNote This operation is potentially time consuming. Minimize calls to this method
     *           if possible.
     */
    public static void extendWith(File descriptorSetFile) {
        checkNotNull(descriptorSetFile);
        checkArgument(descriptorSetFile.exists());

        FileSet protoFiles = FileSet.parse(descriptorSetFile);
        TypeSet types = TypeSet.messagesAndEnums(protoFiles);
        KnownTypes.Holder.extendWith(types);
    }
}
