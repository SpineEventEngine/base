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
package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.annotation.Internal;
import io.spine.io.ResourceFiles;
import io.spine.logging.Logging;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Streams.stream;
import static com.google.common.io.Files.createParentDirs;
import static io.spine.option.Options.registry;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A utility class which allows to obtain Protobuf file descriptors.
 */
@Internal
public final class FileDescriptors {

    /**
     * The name of the descriptor set file.
     *
     * <p>The file contains a {@link FileDescriptorSet} describing the Protobuf of a given module.
     *
     * <p>The file is generated by the Protobuf compiler in the build process.
     */
    public static final String KNOWN_TYPES = "known_types.desc";

    /** Prevents instantiation of this utility class. */
    private FileDescriptors() {
    }

    /**
     * Returns descriptors of all {@code .proto} files described in the descriptor set file.
     *
     * @param descriptorSetFile
     *         the path to the file generated by the Protobuf compiler which
     *         contains descriptors of the project {@code .proto} files
     * @return a list of descriptors
     */
    public static List<FileDescriptorProto> parse(String descriptorSetFile) {
        return parseAndFilter(descriptorSetFile, descriptor -> true);
    }

    /**
     * Obtains the list of files from the passed descriptor set file, skipping files provided
     * by Google Protobuf.
     *
     * @see #parse(String)
     */
    public static List<FileDescriptorProto> parseSkipStandard(String descriptorSetFile) {
        return parseAndFilter(descriptorSetFile, IsNotGoogleProto.PREDICATE);
    }

    /**
     * Returns descriptors of `.proto` files described in the descriptor set file
     * which match the filter predicate.
     *
     * @param descriptorSetFile
     *         the path to the file generated by the Protobuf compiler which
     *         contains descriptors of the project {@code .proto} files
     * @param filter
     *         a filter predicate to apply to the files
     * @return a list of descriptors
     */
    private static List<FileDescriptorProto> parseAndFilter(String descriptorSetFile,
                                                            Predicate<FileDescriptorProto> filter) {
        File descriptorsFile = new File(descriptorSetFile);
        checkArgument(descriptorsFile.exists(), "File %s does not exist", descriptorSetFile);

        Logger log = log();
        if (log.isDebugEnabled()) {
            log.debug("Looking up for the proto files matching predicate {} under {}",
                      filter,
                      descriptorSetFile);
        }

        List<FileDescriptorProto> files;
        try (final FileInputStream fis = new FileInputStream(descriptorsFile)) {
            FileDescriptorSet fileSet = FileDescriptorSet.parseFrom(fis, registry());
            files = fileSet.getFileList()
                           .stream()
                           .filter(filter)
                           .collect(toList());
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Cannot get proto file descriptors. Path: %s", descriptorSetFile
            );
        }
        log.debug("Found {} files.", files.size());
        return files;
    }

    /**
     * Loads the {@code known_types.desc} descriptor file from the classpath.
     *
     * <p>If several files found, all of them are loaded.
     *
     * @return the set of {@linkplain FileDescriptorProto file descriptors}
     *         contained in the loaded files
     */
    public static Set<FileDescriptorProto> load() {
        Iterator<URL> resources = ResourceFiles.loadAll(KNOWN_TYPES);
        Set<FileDescriptorProto> files = stream(resources)
                .map(FileDescriptors::loadFrom)
                .flatMap(set -> set.getFileList()
                                   .stream())
                .filter(distinctBy(FileDescriptorProto::getName))
                .collect(toSet());
        return files;
    }

    /**
     * Merges the contents of the given files into a single descriptor set.
     *
     * <p>This method assumes that all the given files exist and contain instances of
     * {@link FileDescriptorSet} Protobuf message.
     *
     * @param files
     *         the file to merge
     * @return the {@link MergedDescriptorSet}
     */
    public static MergedDescriptorSet merge(Collection<File> files) {
        FileDescriptorSet merged = files
                .stream()
                .map(File::getPath)
                .map(FileDescriptors::parse)
                .flatMap(Collection::stream)
                .distinct()
                .reduce(FileDescriptorSet.newBuilder(),
                        FileDescriptorSet.Builder::addFile,
                        (right, left) -> right.addAllFile(left.getFileList()))
                .build();
        MergedDescriptorSet result = new MergedDescriptorSet(merged);
        return result;
    }

    /**
     * Retrieves a {@link Predicate} on a given type {@code T}.
     *
     * <p>The predicate is satisfied (returns {@code true}) iff the result of applying the given
     * {@code selector} function to the predicate argument is not seen before by this function.
     * Therefore, the predicate is stateful and should not be used in parallel streams.
     *
     * @param selector
     *         the key selector function; takes the predicate parameter as an argument and
     *         returns the property to distinct by
     * @param <T>
     *         the predicate type
     * @param <K>
     *         the type of the key
     * @return a predicate on {@code T}
     */
    private static <T, K> Predicate<T> distinctBy(Function<T, K> selector) {
        Set<? super K> seen = newHashSet();
        return element -> {
            K key = selector.apply(element);
            boolean newKey = seen.add(key);
            return newKey;
        };
    }

    /**
     * Reads an instance of {@link FileDescriptorSet} from the given {@link URL}.
     */
    private static FileDescriptorSet loadFrom(URL file) {
        checkNotNull(file);
        try (InputStream stream = file.openStream()) {
            FileDescriptorSet parsed = FileDescriptorSet.parseFrom(stream, registry());
            return parsed;
        } catch (IOException e) {
            throw newIllegalStateException(
                    e,
                    "Unable to load file descriptor set from %s.",
                    file
            );
        }
    }

    /**
     * A view on a {@link FileDescriptorSet} after merging.
     */
    public static final class MergedDescriptorSet {

        private final FileDescriptorSet descriptorSet;

        private MergedDescriptorSet(FileDescriptorSet descriptorSet) {
            this.descriptorSet = descriptorSet;
        }

        /**
         * Writes this descriptor set into the given file.
         *
         * <p>If the file exists, it will be overridden. Otherwise, the file (and all its parent
         * directories if necessary) will be created.
         *
         * @param destination
         *         the file to write this descriptor set into
         */
        public void writeTo(File destination) {
            checkNotNull(destination);
            prepareFile(destination);
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
                descriptorSet.writeTo(out);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private static void prepareFile(File destination) {
            try {
                destination.delete();
                createParentDirs(destination);
                destination.createNewFile();
            } catch (IOException e) {
                throw illegalArgumentWithCauseOf(e);
            }
        }
    }

    /**
     * Verifies if a package of a file does not start with {@code "google"}.
     */
    private enum IsNotGoogleProto implements Predicate<FileDescriptorProto> {

        PREDICATE;

        /** The constant of the company name prefix used in Google proto types. */
        @SuppressWarnings("DuplicateStringLiteralInspection") // Encapsulated package name.
        private static final String GOOGLE_PACKAGE = "google";

        @Override
        public boolean test(FileDescriptorProto file) {
            checkNotNull(file);
            boolean result = !file.getPackage()
                                  .startsWith(GOOGLE_PACKAGE);
            return result;
        }

        @Override
        public String toString() {
            return IsNotGoogleProto.class.getSimpleName();
        }
    }

    private static Logger log() {
        return Logging.get(FileDescriptors.class);
    }
}
