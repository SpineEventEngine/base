/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A proto file with declarations of {@linkplain io.spine.base.RejectionMessage rejections}.
 *
 * @author Alexander Yevsyukov
 */
public final class RejectionsFile extends SourceFile {

    private RejectionsFile(FileDescriptorProto descriptor) {
        super(descriptor);
    }

    /**
     * Creates an instance by the passed {@linkplain SourceFile#isRejections()} rejections file}.
     */
    public static RejectionsFile from(SourceFile file) {
        checkNotNull(file);
        checkArgument(file.isRejections());

        final RejectionsFile result = new RejectionsFile(file.getDescriptor());
        return result;
    }

    /**
     * Obtains rejection messages declared in the file.
     */
    public List<RejectionDeclaration> getRejectionDeclarations() {
        final ImmutableList.Builder<RejectionDeclaration> result = ImmutableList.builder();
        final FileDescriptorProto file = getDescriptor();
        for (DescriptorProto type : getDescriptor().getMessageTypeList()) {
            final RejectionDeclaration declaration = new RejectionDeclaration(type, file);
            result.add(declaration);
        }
        return result.build();
    }
}
