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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.util.JsonFormat.TypeRegistry;
import io.spine.annotation.Internal;
import io.spine.type.TypeName;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;

/**
 * A set of proto types.
 *
 * @author Alexander Yevsyukov
 * @author Dmytro Dashenkov
 */
@Internal
public class TypeSet {

    private final Map<TypeName, MessageType> messageTypes;
    private final Map<TypeName, EnumType> enumTypes;

    /** Creates a new empty set. */
    private TypeSet() {
        this(of(), of());
    }

    private TypeSet(Map<TypeName, MessageType> messageTypes, Map<TypeName, EnumType> enumTypes) {
        this.messageTypes = copyOf(messageTypes);
        this.enumTypes = copyOf(enumTypes);
    }

    private TypeSet(Builder builder) {
        this(copyOf(builder.messageTypes),
             copyOf(builder.enumTypes));
    }

    /**
     * Obtains message and enum types declared in the passed file.
     */
    public static TypeSet messagesAndEnums(FileDescriptor file) {
        TypeSet messages = MessageType.allFrom(file);
        TypeSet enums = EnumType.allFrom(file);
        TypeSet result = messages.union(enums);
        return result;
    }

    /**
     * Obtains message and enum types declared in the files represented by the passed set.
     */
    public static TypeSet messagesAndEnums(FileSet fileSet) {
        TypeSet result = new TypeSet();
        for (FileDescriptor file : fileSet.files()) {
            result = result.union(messagesAndEnums(file));
        }
        return result;
    }

    /**
     * Obtains the size of the set.
     */
    public int size() {
        int messagesCount = messageTypes.size();
        int enumsCount = enumTypes.size();
        int result = messagesCount + enumsCount;
        return result;
    }

    /**
     * Obtains a type by its name.
     *
     * @param name the name of the type to find
     * @return the type with the given name or {@code Optional.empty()} if there is no such type in
     *         this set
     * @see #contains(TypeName)
     */
    public Optional<Type<?, ?>> find(TypeName name) {
        checkNotNull(name);
        Type<?, ?> messageType = messageTypes.get(name);
        if (messageType != null) {
            return Optional.of(messageType);
        } else {
            Type<?, ?> enumType = enumTypes.get(name);
            return Optional.ofNullable(enumType);
        }
    }

    /**
     * Checks if a type with the given name is present in this set.
     *
     * <p>It is guaranteed that if this method returns {@code true}, then {@link #find(TypeName)}
     * will successfully find the type by the same name, and otherwise, if
     * {@code contains(TypeName)} returns {@code false}, then {@code find(TypeName)} will return
     * an empty value.
     *
     * @param typeName the name to look by
     * @return {@code true} if the set contains a type with the given name, {@code false} otherwise
     * @see #find(TypeName)
     */
    public boolean contains(TypeName typeName) {
        boolean result = find(typeName).isPresent();
        return result;
    }

    /**
     * Verifies if the set is empty.
     */
    public boolean isEmpty() {
        boolean empty = size() == 0;
        return empty;
    }

    /**
     * Writes all the types in this set into a {@link TypeRegistry JsonFormat.TypeRegistry}.
     */
    public TypeRegistry toJsonPrinterRegistry() {
        TypeRegistry.Builder registry = TypeRegistry.newBuilder();
        messageTypes.values()
                    .stream()
                    .map(Type::descriptor)
                    .forEach(registry::add);
        return registry.build();
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
        Map<TypeName, MessageType> messages = ImmutableMap
                .<TypeName, MessageType>builder()
                .putAll(this.messageTypes)
                .putAll(another.messageTypes)
                .build();
        Map<TypeName, EnumType> enums = ImmutableMap
                .<TypeName, EnumType>builder()
                .putAll(this.enumTypes)
                .putAll(another.enumTypes)
                .build();
        TypeSet result = new TypeSet(messages, enums);
        return result;
    }

    /**
     * Obtains all the types contained in this set.
     */
    public Collection<Type<?, ?>> types() {
        ImmutableList<Type<?, ?>> types = ImmutableList
                .<Type<?, ?>>builder()
                .addAll(messageTypes.values())
                .addAll(enumTypes.values())
                .build();
        return types;
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

    /**
     * Creates a new instance of {@code Builder} for {@code TypeSet} instances.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code TypeSet} instances.
     */
    public static final class Builder {

        private final Map<TypeName, MessageType> messageTypes = newHashMap();
        private final Map<TypeName, EnumType> enumTypes = newHashMap();

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        @CanIgnoreReturnValue
        public Builder add(MessageType type) {
            TypeName name = type.name();
            messageTypes.put(name, type);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder add(EnumType type) {
            TypeName name = type.name();
            enumTypes.put(name, type);
            return this;
        }

        /**
         * Creates a new instance of {@code TypeSet}.
         *
         * @return new instance of {@code TypeSet}
         */
        public TypeSet build() {
            return new TypeSet(this);
        }
    }

}
