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

package io.spine.js.generate;

import io.spine.code.js.Directory;
import io.spine.code.proto.FileSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An enhancement of generated Protobuf sources.
 */
public abstract class FileSetEnhancement {

    private final Directory generatedRoot;
    private final FileSet fileSet;

    protected FileSetEnhancement(Directory generatedRoot, FileSet fileSet) {
        this.generatedRoot = checkNotNull(generatedRoot);
        this.fileSet = checkNotNull(fileSet);
    }

    /**
     * {@linkplain #processSources() Enhances} generated sources.
     *
     * <p>Does nothing if there are no Protobuf files to process.
     */
    public final void perform() {
        if (hasFilesToProcess()) {
            processSources();
        }
    }

    /**
     * Processes generated Protobuf {@link #fileSet() files}.
     */
    protected abstract void processSources();

    /**
     * Obtains files to be enhanced.
     */
    protected FileSet fileSet() {
        return fileSet;
    }

    /**
     * Obtains the root of the {@linkplain #fileSet() sources}.
     */
    protected Directory generatedRoot() {
        return generatedRoot;
    }

    /**
     * Checks if the {@code JsonParsersWriter} has any files to process.
     *
     * <p>Will return {@code false} either if there are no known types to process or the generated
     * files for them cannot be found.
     *
     * @return {@code true} if there are files to process and {@code false} otherwise
     */
    private boolean hasFilesToProcess() {
        boolean hasFilesToProcess = !fileSet.isEmpty() && generatedRoot.exists();
        return hasFilesToProcess;
    }
}
