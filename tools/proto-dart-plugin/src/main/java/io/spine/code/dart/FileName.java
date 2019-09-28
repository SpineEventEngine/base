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

package io.spine.code.dart;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.AbstractFileName;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FileName extends AbstractFileName<FileName> {

    private static final long serialVersionUID = 0L;

    private static final String GENERATED_EXTENSION = ".pb.dart";

    private FileName(String value) {
        super(value);
    }

    private FileName(Path path) {
        this(path.toString());
    }

    public static FileName relative(io.spine.code.proto.FileName file) {
        String relativePath = file.nameWithoutExtension() + GENERATED_EXTENSION;
        return new FileName(relativePath);
    }

    public static FileName relative(FileDescriptor file) {
        io.spine.code.proto.FileName protoName = io.spine.code.proto.FileName.from(file);
        return relative(protoName);
    }

    public FileName prefixed(Path filePrefix) {
        checkNotNull(filePrefix);
        return new FileName(filePrefix.resolve(this.value()));
    }
}
