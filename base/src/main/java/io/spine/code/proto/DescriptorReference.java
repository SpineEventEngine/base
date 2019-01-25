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

package io.spine.code.proto;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.io.ByteStreams.toByteArray;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.APPEND;

public final class DescriptorReference {

    private static final Splitter LINE_SPLITTER = Splitter.on(lineSeparator())
                                                          .omitEmptyStrings()
                                                          .trimResults();

    private static final String FILE_NAME = "desc.ref";

    private final ImmutableSet<String> references;

    private DescriptorReference(ImmutableSet<String> references) {
        this.references = references;
    }

    public static DescriptorReference toOneFile(File file) {
        checkNotNull(file);

        return toFiles(ImmutableSet.of(file));
    }

    public static DescriptorReference toFiles(Collection<File> descriptorFiles) {
        checkNotNull(descriptorFiles);
        ImmutableSet<String> references = descriptorFiles.stream()
                                                         .map(File::getPath)
                                                         .collect(toImmutableSet());
        return new DescriptorReference(references);
    }

    public void writeTo(Path directory) {
        checkNotNull(directory);

        directory.toFile()
                 .mkdirs();
        Path targetFile = directory.resolve(FILE_NAME);
        try {
            targetFile.toFile().createNewFile();
            Files.write(targetFile, references, APPEND);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    public static Iterator<URL> loadAll() {
        try (InputStream catalog = readResource(FILE_NAME)) {
            byte[] catalogBytes = toByteArray(catalog);
            String calalog = new String(catalogBytes, UTF_8);
            return LINE_SPLITTER.splitToList(calalog)
                                .stream()
                                .map(DescriptorReference::toUrl)
                                .iterator();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static URL toUrl(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }

    private static InputStream readResource(String name) {
        InputStream result = DescriptorReference.class.getClassLoader()
                                                      .getResourceAsStream(name);
        checkNotNull(result, "`%s` not found.", name);
        return result;
    }
}
