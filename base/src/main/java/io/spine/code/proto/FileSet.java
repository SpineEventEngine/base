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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.annotation.Internal;
import io.spine.type.KnownTypes;
import io.spine.type.MessageType;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.flogger.LazyArgs.lazy;
import static io.spine.code.proto.Linker.link;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * A set of proto files represented by their {@linkplain FileDescriptor descriptors}.
 */
@Internal
public final class FileSet {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final FileDescriptor[] EMPTY = {};

    /**
     * All the files of this set.
     *
     * <p>Each file is identified by its {@linkplain FileDescriptor#getFullName() full name}.
     * Otherwise, the Protobuf imports will not work.
     */
    private final Map<FileName, FileDescriptor> files;

    private FileSet(Map<FileName, FileDescriptor> files) {
        this.files = newHashMap(files);
    }

    private FileSet() {
        this.files = newHashMap();
    }

    /**
     * Creates an empty set.
     */
    static FileSet newInstance() {
        return new FileSet();
    }

    /**
     * Creates a new file set by parsing the passed descriptor set file.
     */
    public static FileSet parse(File descriptorSet) {
        checkNotNull(descriptorSet);
        checkState(descriptorSet.exists(), "File %s does not exist.", descriptorSet);
        return doParse(descriptorSet);
    }

    /**
     * Parses the given descriptor set file and resolves the descriptors from {@link KnownTypes}.
     *
     * <p>The read descriptors are intentionally not linked. Instead, the descriptors with such
     * names are found in the {@link KnownTypes} and returned as a file set. This way, all
     * the dependencies get resolved.
     *
     * <p>If some of the parsed files are not found in the known types,
     * an {@code IllegalStateException} is thrown.
     *
     * @param descriptorSet the descriptor set file to parse
     * @return new file set
     */
    public static FileSet parseAsKnownFiles(File descriptorSet) {
        Set<FileName> fileNames = FileDescriptors.parse(descriptorSet)
                                                 .stream()
                                                 .map(FileName::from)
                                                 .collect(toSet());
        Map<FileName, FileDescriptor> knownFiles = KnownTypes.instance()
                .asTypeSet()
                .allTypes()
                .stream()
                .map(type -> type.descriptor().getFile())
                .filter(descriptor -> fileNames.contains(FileName.from(descriptor.getFile())))
                .collect(toMap(FileName::from,       // File name as the key.
                               file -> file,         // File descriptor as the value.
                               (left, right) -> left // On duplicates, take the first option.
                ));
        if (knownFiles.size() != fileNames.size()) {
            return onUnknownFile(knownFiles.keySet(), fileNames);
        }
        FileSet result = new FileSet(knownFiles);
        return result;
    }

    private static FileSet onUnknownFile(Set<FileName> knownFiles, Set<FileName> requestedFiles) {
        FluentLogger.Api debug = logger.atFine();
        debug.log("Failed to find files in the known types set. Looked for %s%s.",
                  lineSeparator(),
                  requestedFiles);
        debug.log("Could not find files: %s.", lazy(() ->
                  requestedFiles
                          .stream()
                          .filter(fileName -> !knownFiles.contains(fileName))
                          .map(FileName::toString)
                          .collect(joining(", "))));
        throw newIllegalStateException("Some files are not known.");
    }

    /**
     * Creates a new file set by parsing the passed descriptor set file.
     */
    private static FileSet doParse(File descriptorSetFile) {
        Collection<FileDescriptorProto> files = FileDescriptors.parse(descriptorSetFile);
        return link(files);
    }

    /**
     * Loads main file set from resources.
     */
    public static FileSet load() {
        Collection<FileDescriptorProto> files = FileDescriptors.load();
        return link(files);
    }

    /**
     * Constructs a new {@code FileSet} out of the given file descriptors.
     *
     * <p>The file descriptors are linked to obtain normal {@code FileDescriptor}s out
     * of {@code FileDescriptorProto}s.
     *
     * @param protoDescriptors
     *         file descriptors to include in the set
     * @return new file set
     */
    public static FileSet ofFiles(ImmutableSet<FileDescriptorProto> protoDescriptors) {
        checkNotNull(protoDescriptors);
        return link(protoDescriptors);
    }

    /**
     * Obtains message declarations that match the specified {@link java.util.function.Predicate}.
     *
     * @param predicate the predicate to test a message
     * @return the message declarations
     */
    public List<MessageType> findMessageTypes(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageType> result = ImmutableList.builder();
        for (FileDescriptor file : files()) {
            SourceFile sourceFile = SourceFile.from(file);
            Collection<MessageType> declarations = sourceFile.allThat(predicate);
            result.addAll(declarations);
        }
        return result.build();
    }

    /**
     * Creates a new set which is a union of this and the passed one.
     */
    public FileSet union(FileSet another) {
        if (another.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return another;
        }
        int expectedSize = this.files.size() + another.files.size();
        Map<FileName, FileDescriptor> files = newHashMapWithExpectedSize(expectedSize);
        files.putAll(this.files);
        files.putAll(another.files);
        FileSet result = new FileSet(files);
        return result;
    }

    /**
     * Creates a new set without filtered out files.
     *
     * @param predicate
     *         the predicate to filter files
     * @return a new file set
     */
    public FileSet filter(Predicate<FileDescriptor> predicate) {
        Collection<FileDescriptor> filteredFiles = files.values()
                                                        .stream()
                                                        .filter(predicate)
                                                        .collect(toList());
        FileSet newFileSet = newInstance();
        for (FileDescriptor file : filteredFiles) {
            newFileSet.add(file);
        }
        return newFileSet;
    }

    /**
     * Obtains immutable view of the files in this set.
     */
    public ImmutableSet<FileDescriptor> files() {
        return ImmutableSet.copyOf(files.values());
    }

    /**
     * Obtains array with the files of this set.
     */
    FileDescriptor[] toArray() {
        return files.values()
                    .toArray(EMPTY);
    }

    /**
     * Returns {@code true} if the set contains a file with the passed name,
     * {@code false} otherwise.
     */
    public boolean contains(FileName fileName) {
        Optional<FileDescriptor> found = tryFind(fileName);
        return found.isPresent();
    }

    /**
     * Returns {@code true} if the set contains all the files with the passed names,
     * {@code false} otherwise.
     */
    public boolean containsAll(Collection<FileName> fileNames) {
        FileSet found = find(fileNames);
        boolean result = found.size() == fileNames.size();
        return result;
    }

    /**
     * Obtains the set of the files that match passed names.
     */
    public FileSet find(Collection<FileName> fileNames) {
        Map<FileName, FileDescriptor> found = newHashMapWithExpectedSize(fileNames.size());
        for (FileName name : fileNames) {
            Optional<FileDescriptor> file = tryFind(name);
            file.ifPresent(descriptor -> found.put(name, descriptor));
        }
        return new FileSet(found);
    }

    /**
     * Returns an Optional containing the first file that matches the name, if such file exists.
     */
    public Optional<FileDescriptor> tryFind(FileName fileName) {
        if (files.containsKey(fileName)) {
            return Optional.of(files.get(fileName));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds file to the set.
     */
    @CanIgnoreReturnValue
    public boolean add(FileDescriptor file) {
        FileName name = FileName.from(file);
        Object previous = files.put(name, file);
        boolean isNew = previous == null;
        return isNew;
    }

    /**
     * Obtains the size of the set.
     */
    public int size() {
        int result = files.size();
        return result;
    }

    /**
     * Verifies if the set is empty.
     */
    public boolean isEmpty() {
        boolean result = files.isEmpty();
        return result;
    }

    /**
     * Returns a string with alphabetically sorted list of files of this set.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("files", files.keySet()
                                             .stream()
                                             .sorted()
                                             .collect(toList()))
                          .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FileSet other = (FileSet) obj;
        return Objects.equals(this.files, other.files);
    }
}
