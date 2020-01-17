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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.type.RejectionType;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * A proto file with declarations of {@linkplain io.spine.base.RejectionMessage rejections}.
 */
public final class RejectionsFile extends SourceFile {

    private RejectionsFile(FileDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Creates an instance by the passed {@linkplain SourceFile#isRejections()} rejections file}.
     */
    public static RejectionsFile from(SourceFile file) {
        checkNotNull(file);
        checkArgument(file.isRejections());

        RejectionsFile result = new RejectionsFile(file.descriptor());
        return result;
    }

    /**
     * Obtains rejection messages declared in the file.
     */
    public List<RejectionType> rejectionDeclarations() {
        ImmutableList.Builder<RejectionType> result = ImmutableList.builder();
        FileDescriptor file = descriptor();
        for (Descriptor type : file.getMessageTypes()) {
            RejectionType declaration = new RejectionType(type);
            result.add(declaration);
        }
        return result.build();
    }

    private static boolean isRejections(FileDescriptor file) {
        return FileName.from(file)
                       .isRejections();
    }

    /**
     * Obtains rejection files from the passed set of files.
     */
    public static ImmutableSet<RejectionsFile> findAll(FileSet fileSet) {
        ImmutableSet<RejectionsFile> result =
                fileSet.files()
                       .stream()
                       .filter(RejectionsFile::isRejections)
                       .map(file -> {
                           SourceFile sourceFile = SourceFile.from(file);
                           return from(sourceFile);
                       })
                       .collect(toImmutableSet());
        return result;
    }
}
