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

package io.spine.codegen.proto;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

/**
 * A set of proto types.
 *
 * @author Alexander Yevsyukov
 * @author Dmytro Dashenkov
 */
public class TypeSet {

    private final Set<MessageType> messageTypes;
    private final Set<EnumType> enumTypes;

    /** Creates a new empty set. */
    private TypeSet() {
        this(newHashSet(), newHashSet());
    }

    private TypeSet(Iterable<MessageType> messageTypes, Iterable<EnumType> enumTypes) {
        this.messageTypes = newHashSet(messageTypes);
        this.enumTypes = newHashSet(enumTypes);
    }

    /**
     * Creates a new empty instance.
     */
    static TypeSet newInstance() {
        return new TypeSet();
    }

    /**
     * Obtains message and enum types declared in the passed file.
     */
    public static TypeSet messagesAndEnums(FileDescriptor file) {
        final TypeSet messages = MessageType.allFrom(file);
        final TypeSet enums = EnumType.allFrom(file);
        final TypeSet result = messages.union(enums);
        return result;
    }

    /**
     * Obtains message and enum types declared in the files represented by the passed set.
     */
    public static TypeSet messagesAndEnums(FileSet fileSet) {
        TypeSet result = newInstance();
        for (FileDescriptor file : fileSet.files()) {
            result = result.union(messagesAndEnums(file));
        }
        return result;
    }

    /**
     * Obtains the size of the set.
     */
    public int size() {
        final int messagesCount = messageTypes.size();
        final int enumsCount = enumTypes.size();
        final int result = messagesCount + enumsCount;
        return result;
    }

    /**
     * Verifies if the set is empty.
     */
    public boolean isEmpty() {
        final boolean empty = size() == 0;
        return empty;
    }

    /**
     * Writes all the types in this set into
     * a {@link TypeRegistry JsonFormat.TypeRegistry}.
     *
     * <p>Retrieves an instance of {@link TypeRegistry.Builder} which can be appended with more
     * types if necessary.
     */
    public TypeRegistry.Builder toJsonPrinterRegistry() {
        final Iterable<Descriptor> messageTypes = getMessageTypes();
        final TypeRegistry.Builder registry = TypeRegistry.newBuilder()
                                                          .add(messageTypes);
        return registry;
    }

    /**
     * Adds the passed message type to the set.
     */
    @CanIgnoreReturnValue
    boolean add(MessageType type) {
        final boolean result = messageTypes.add(type);
        return result;
    }

    /**
     * Adds the passed enum type to the set.
     */
    @CanIgnoreReturnValue
    boolean add(EnumType type) {
        final boolean result = enumTypes.add(type);
        return result;
    }

    /**
     * Creates a new set which is a union of this and the passed one.
     */
    private TypeSet union(TypeSet another) {
        if (another.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return another;
        }
        final Set<MessageType> messages = Sets.union(this.messageTypes, another.messageTypes);
        final Set<EnumType> enums = Sets.union(this.enumTypes, another.enumTypes);
        final TypeSet result = new TypeSet(messages, enums);
        return result;
    }

    private Iterable<Descriptor> getMessageTypes() {
        final Iterable<Descriptor> descriptors = messageTypes.stream()
                                                             .map(MessageType::getType)
                                                             .collect(toList());
        return descriptors;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeSet typeSet = (TypeSet) o;
        return Objects.equal(messageTypes, typeSet.messageTypes) &&
                Objects.equal(enumTypes, typeSet.enumTypes);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hashCode(messageTypes, enumTypes);
    }
}
