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

package io.spine.tools.compiler;

import com.google.common.base.Predicate;
import io.spine.code.proto.MessageType;
import io.spine.code.proto.SourceFile;
import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A predicate determining if the given message type has been collected from the source
 * file in the specified module.
 *
 * <p>Each predicate instance requires to specify the root folder of Protobuf definitions
 * for the module. This value is used to match the given {@code VBMetadata}.
 */
public class SourceProtoBelongsToModule implements Predicate<MessageType>, Logging {

    /**
     *  An absolute path to the root folder for the {@code .proto} files in the module.
     */
    private final File rootPath;

    public SourceProtoBelongsToModule(File rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public boolean apply(@Nullable MessageType input) {
        checkNotNull(input);
        // A path obtained from DescriptorSet file for which `src/proto` is the root.
        SourceFile sourceFile = input.sourceFile();
        File absoluteFile = new File(rootPath, sourceFile.toString());
        boolean belongsToModule = absoluteFile.exists();
        _debug("Source file {} tested if under {} with the result: {}",
               sourceFile, rootPath, belongsToModule);
        return belongsToModule;
    }
}
