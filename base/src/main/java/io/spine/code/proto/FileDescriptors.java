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

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.annotation.Internal;
import io.spine.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Streams.stream;
import static io.spine.code.GooglePackage.notInGooglePackage;
import static io.spine.io.Files2.checkExists;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A utility class which allows to obtain Protobuf file descriptors.
 */
@Internal
public final class FileDescriptors {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * The name of the descriptor set file.
     *
     * <p>The file contains a {@link FileDescriptorSet} describing the Protobuf of a given module.
     *
     * <p>The file is generated by the Protobuf compiler in the build process.
     */
    public static final String KNOWN_TYPES = "known_types.desc";

    public static final String DESC_EXTENSION = ".desc";

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
    public static List<FileDescriptorProto> parse(File descriptorSetFile) {
        return parseAndFilter(descriptorSetFile, descriptor -> true);
    }

    @SuppressWarnings("PMD.MethodNamingConventions") // to make it more visible.
    private static FluentLogger.Api _debug() {
        return logger.atFine();
    }

    /**
     * Returns descriptors of `.proto` files described in the descriptor set file
     * which match the filter predicate.
     *
     * @param descriptorSet
     *         the file generated by the Protobuf compiler which
     *         contains descriptors of the project {@code .proto} files
     * @param filter
     *         a filter predicate to apply to the files
     * @return a list of descriptors
     */
    private static List<FileDescriptorProto>
    parseAndFilter(File descriptorSet, Predicate<FileDescriptorProto> filter) {
        checkExists(descriptorSet);
        _debug().log("Looking up for the proto files matching predicate `%s` under `%s`.",
                     filter,
                     descriptorSet);

        List<FileDescriptorProto> files;
        try (FileInputStream fis = new FileInputStream(descriptorSet)) {
            FileDescriptorSet fileSet = FileDescriptorSets.parse(fis);
            files = fileSet.getFileList()
                           .stream()
                           .filter(filter)
                           .collect(toList());
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Cannot get proto file descriptors. Path: %s", descriptorSet
            );
        }
        _debug().log("Found %d files.", files.size());
        return files;
    }

    /**
     * Loads the descriptor files from the classpath.
     *
     * @return the set of {@linkplain FileDescriptorProto file descriptors}
     *         contained in the loaded files
     */
    static Set<FileDescriptorProto> load() {
        Iterator<Resource> resources = DescriptorReference.loadAll();
        Set<FileDescriptorProto> files = stream(resources)
                .map(FileDescriptors::loadFrom)
                .flatMap(set -> set.getFileList().stream())
                .filter(distinctBy(FileDescriptorProto::getName))
                .collect(toSet());
        return files;
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
    private static FileDescriptorSet loadFrom(Resource resource) {
        checkNotNull(resource);
        return resource.exists()
               ? doLoadFrom(resource)
               : FileDescriptorSet.getDefaultInstance();
    }

    private static FileDescriptorSet doLoadFrom(Resource resource) {
        try (InputStream stream = resource.open()) {
            FileDescriptorSet parsed = FileDescriptorSets.parse(stream);
            return parsed;
        } catch (IOException e) {
            throw newIllegalStateException(
                    e,
                    "Unable to load file descriptor set from %s.",
                    resource
            );
        }
    }

    /**
     * Verifies if the passed file declares types under the "google" package.
     */
    public static boolean isGoogle(FileDescriptor file) {
        return !IsNotGoogleProto.PREDICATE.test(file.toProto());
    }

    /**
     * Tells if two descriptors represent the same file.
     */
    public static boolean sameFiles(FileDescriptor f1, FileDescriptor f2) {
        boolean sameName = f2.getFullName()
                             .equals(f1.getFullName());
        boolean samePackage = f2.getPackage()
                                .equals(f1.getPackage());
        return sameName && samePackage;
    }

    /**
     * Verifies if the passed file declares types NOT under the "google" package.
     */
    public static boolean isNotGoogle(FileDescriptor file) {
        return !isGoogle(file);
    }

    /**
     * Verifies if a package of a file does not start with {@code "google"}.
     */
    private enum IsNotGoogleProto implements Predicate<FileDescriptorProto> {

        PREDICATE;

        @Override
        public boolean test(FileDescriptorProto file) {
            checkNotNull(file);
            boolean result = notInGooglePackage(file);
            _debug().log("[IsNotGoogleProto] Tested %s with package %s. The result is %b.",
                       file.getName(), file.getPackage(), result);
            return result;
        }

        @Override
        public String toString() {
            return IsNotGoogleProto.class.getSimpleName();
        }
    }
}
