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

package io.spine.code;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract base for source code files.
 */
public abstract class AbstractSourceFile extends FsObject {

    protected AbstractSourceFile(Path path) {
        super(path);
    }

    /**
     * Tells if this source file belongs to the passed directory.
     *
     * @param directory
     *         the passed instance must be a {@linkplain java.io.File#isDirectory() directory}
     * @return {@code true} if the file belongs to the directory,
     *         {@code false} if the directory does not exist, or the file is in another directory
     */
    public boolean isUnder(File directory) {
        checkNotNull(directory);

        if(!directory.exists()) {
            return false;
        }

        checkArgument(directory.isDirectory(), "%s is not a directory.", directory);

        String thisFile = getPath().toAbsolutePath()
                                   .toString();
        String dir = directory.getAbsolutePath();
        boolean result = thisFile.startsWith(dir);
        return result;
    }
}
