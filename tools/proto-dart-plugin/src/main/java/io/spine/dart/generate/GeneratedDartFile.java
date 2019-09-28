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

package io.spine.dart.generate;

import com.google.common.io.Files;
import com.google.common.io.MoreFiles;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Files.createParentDirs;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A generated Dart source code file.
 */
public final class GeneratedDartFile {

    private final String content;

    GeneratedDartFile(String content) {
        this.content = checkNotNull(content);
    }

    /**
     * Writes this source file to the given point on file system.
     */
    public void writeTo(File target) {
        checkNotNull(target);
        try {
            if (!target.exists()) {
                createParentDirs(target);
                target.createNewFile();
            }
            Files.asCharSink(target, UTF_8)
                 .write(content);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
