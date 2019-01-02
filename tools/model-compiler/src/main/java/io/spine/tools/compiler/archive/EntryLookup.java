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

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.readFully;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A process of an entry lookup inside of an archive.
 */
final class EntryLookup implements Closeable, Logging {

    private final ZipInputStream stream;

    private EntryLookup(ZipInputStream stream) {
        this.stream = stream;
    }

    /**
     * Opens the given archive for a lookup.
     *
     * @return new instance of {@code EntryLookup}
     */
    static EntryLookup open(ArchiveFile archiveFile) {
        checkNotNull(archiveFile);
        ZipInputStream stream = new ZipInputStream(archiveFile.open());
        return new EntryLookup(stream);
    }

    /**
     * Finds an entry with the given name in the archive.
     *
     * <p>This method should only be called once in the lifetime of an {@code EntryLookup}.
     * All subsequent calls will always return an empty result.
     *
     * @param name
     *         the name of the entry in terms of {@code ZipEntry.getName()}
     * @return a snapshot of the found entry or {@code Optional.empty()} if there is no such entry
     */
    Optional<ArchiveEntry> findEntry(String name) {
        try {
            return doFindEntry(name);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Optional<ArchiveEntry> doFindEntry(String name)
            throws IOException {
        for (ZipEntry entry = stream.getNextEntry();
             entry != null;
             entry = stream.getNextEntry()) {
            if (name.equals(entry.getName())) {
                _debug("Reading ZIP entry `{}` of size: {}.", entry, entry.getSize());
                ArchiveEntry read = readEntry(entry);
                return Optional.of(read);
            }
        }
        return Optional.empty();
    }

    private ArchiveEntry readEntry(ZipEntry entry) {
        long size = entry.getSize();
        if (size < 0 || size > Integer.MAX_VALUE) {
            throw newIllegalStateException("Unable to read ZIP entry {}.", entry.getName());
        } else {
            int entrySize = (int) size;
            byte[] buffer = new byte[entrySize];
            readEntryTo(buffer);
            return ArchiveEntry.of(buffer);
        }
    }

    private void readEntryTo(byte[] buffer) {
        try {
            readFully(stream, buffer);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the underlying file stream.
     *
     * @throws IOException
     *         if {@code InputStream.close()} throws an {@code IOException}
     */
    @Override
    public void close() throws IOException {
        stream.close();
    }
}
