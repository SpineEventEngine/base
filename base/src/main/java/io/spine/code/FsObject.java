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

package io.spine.code;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Abstract base for source code objects on a file system.
 */
public abstract class FsObject {

    private final Path path;

    protected FsObject(Path path) {
        this.path = checkNotNull(path);
    }

    /**
     * Obtains the path of the source code object.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Checks if the object is actually present in the file system.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FsObject)) {
            return false;
        }
        FsObject other = (FsObject) obj;
        return Objects.equals(this.path, other.path);
    }

    /**
     * Ensures that the passed file exists.
     *
     * @return the passed file if it exists
     * @throws IllegalStateException if the file is missing
     */
    @CanIgnoreReturnValue
    public static File checkExists(File file) {
        checkNotNull(file);
        checkState(file.exists(), "The file `%s` does not exist.", file);
        return file;
    }
}
