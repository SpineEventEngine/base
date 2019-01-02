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

package io.spine.tools.compiler.archive;

import io.spine.logging.Logging;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

public final class ArchiveFile implements Logging {

    private final File file;

    private ArchiveFile(File file) {
        this.file = file;
    }

    public static ArchiveFile from(File file) {
        checkArchive(file);
        return new ArchiveFile(file);
    }

    private static void checkArchive(File file) {
        checkNotNull(file);
        checkArgument(file.exists(), "Archive file %s does not exist.", file);
        checkArgument(isArchive(file),
                      "File %s must be have one of extensions: %s.",
                      Arrays.toString(ZipArchiveExtension.values()));
    }

    public static boolean isArchive(File file) {
        return ZipArchiveExtension.anyMatch(file);
    }

    public Optional<ArchiveEntry> findEntry(String path) {
        checkNotNull(path);
        try (EntryLookup open = EntryLookup.open(this)) {
            return open.findEntry(path);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    InputStream open() {
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
