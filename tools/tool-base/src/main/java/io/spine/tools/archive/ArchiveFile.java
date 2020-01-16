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

package io.spine.tools.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * Representation of a archive file contents.
 *
 * The file represented by an instance of this type is of one of the {@link ArchiveExtension}s, i.e.
 * a ZIP or a JAR.
 */
public final class ArchiveFile {

    private final File file;

    private ArchiveFile(File file) {
        this.file = file;
    }

    /**
     * Wraps the given {@code File} into an {@code ArchiveFile}.
     *
     * <p>If the file does not exist or is not an archive, an {@code IllegalArgumentException}
     * is thrown.
     *
     * <p>A file may be considered an archive if it has one of {@link ArchiveExtension}s.
     *
     * @param file
     *         the archive file
     * @return new instance
     */
    public static ArchiveFile from(File file) {
        checkArchive(file);
        return new ArchiveFile(file);
    }

    private static void checkArchive(File file) {
        checkNotNull(file);
        checkArgument(file.exists(), "Archive file %s does not exist.", file);
        checkArgument(isArchive(file),
                      "File %s must be have one of extensions: %s.",
                      Arrays.toString(ArchiveExtension.values()));
    }

    /**
     * Finds a ZIP entry with the given name according to {@code ZipEntry.getName()}.
     *
     * @param fileExtension
     *         file extension to look for; must start with a dot ({@code .})
     * @return the found entry or {@code Optional.empty()} if there is no such entry in this archive
     */
    public Collection<ArchiveEntry> findByExtension(String fileExtension) {
        checkNotNull(fileExtension);
        try (EntryLookup open = EntryLookup.open(this)) {
            return open.findByExtension(fileExtension);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Opens an {@code InputStream} from this file.
     *
     * <p>It is a responsibility of the client to close the stream when it is no longer needed.
     *
     * @return a buffered stream of the archive content
     */
    InputStream open() {
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Defines if the given file is acknowledged as an archive or not.
     *
     * @param file
     *         file to check
     * @return {@code true} if the file is an archive, {@code false} otherwise
     */
    public static boolean isArchive(File file) {
        return ArchiveExtension.anyMatch(file);
    }
}
