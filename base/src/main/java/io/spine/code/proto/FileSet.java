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
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.annotation.Internal;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static io.spine.code.proto.Linker.link;
import static java.util.stream.Collectors.toList;

/**
 * A set of proto files represented by their {@linkplain FileDescriptor descriptors}.
 */
@Internal
public final class FileSet {

    private static final FileDescriptor[] EMPTY = {};

    /**
     * All the files of this set.
     *
     * <p>Each file is identified by its {@linkplain FileDescriptor#getFullName() full name}
     * (otherwise, the Protobuf imports would not work).
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
     * Creates a new file set by parsing the passed file if it exists.
     *
     * <p>If the file does not exist, returns empty set.
     */
    public static FileSet parseOrEmpty(File descriptorSet) {
        FileSet result = descriptorSet.exists()
                ? parse(descriptorSet)
                : newInstance();
        return result;
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
     * Creates file set by parsing the descriptor set file with the passed name.
     */
    public static FileSet parse(String descriptorSetFile) {
        checkNotNull(descriptorSetFile);
        File file = new File(descriptorSetFile);
        return parse(file);
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
     * <p>The file descriptors are {@linkplain Linker#link linked} in order to obtain normal
     * {@code FileDescriptor}s out of {@code FileDescriptorProto}s.
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
     * Obtains message declarations, that match the specified {@link java.util.function.Predicate}.
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
     * Obtains immutable view of the files in this set.
     */
    public Collection<FileDescriptor> files() {
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
     * Returns an Optional containing the first file that matches the name, if such an file exists.
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
     * Obtains alphabetically sorted list of names of files of this set.
     */
    public List<FileName> getFileNames() {
        List<FileName> fileNames =
                files.keySet()
                     .stream()
                     .sorted()
                     .collect(toList());
        return fileNames;
    }

    /**
     * Returns a string with alphabetically sorted list of files of this set.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("files", getFileNames())
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
