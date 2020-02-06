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

package io.spine.code.proto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.TypeRegistry;
import io.spine.type.EnumType;
import io.spine.type.MessageType;
import io.spine.type.ServiceType;
import io.spine.type.Type;
import io.spine.type.TypeName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * A set of Protobuf types.
 */
@Immutable
public final class TypeSet {

    private final ImmutableMap<TypeName, MessageType> messageTypes;
    private final ImmutableMap<TypeName, EnumType> enumTypes;
    private final ImmutableMap<TypeName, ServiceType> serviceTypes;

    /** Creates a new empty set. */
    private TypeSet() {
        this(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    }

    private TypeSet(ImmutableMap<TypeName, MessageType> messageTypes,
                    ImmutableMap<TypeName, EnumType> enumTypes,
                    ImmutableMap<TypeName, ServiceType> serviceTypes) {
        this.messageTypes = messageTypes;
        this.enumTypes = enumTypes;
        this.serviceTypes = serviceTypes;
    }

    private TypeSet(Builder builder) {
        this(ImmutableMap.copyOf(builder.messageTypes),
             ImmutableMap.copyOf(builder.enumTypes),
             ImmutableMap.copyOf(builder.serviceTypes));
    }

    /**
     * Obtains message and enum types declared in the passed file.
     */
    public static TypeSet from(FileDescriptor file) {
        TypeSet messages = MessageType.allFrom(file);
        TypeSet enums = EnumType.allFrom(file);
        TypeSet services = ServiceType.allFrom(file);
        return new TypeSet(messages.messageTypes, enums.enumTypes, services.serviceTypes);
    }

    /**
     * Obtains message and enum types declared in the files represented by the passed set.
     */
    public static TypeSet from(FileSet fileSet) {
        TypeSet result = new TypeSet();
        for (FileDescriptor file : fileSet.files()) {
            result = result.union(from(file));
        }
        return result;
    }

    /**
     * Obtains message types declared in the passed file set.
     */
    public static ImmutableCollection<MessageType> onlyMessages(FileSet fileSet) {
        TypeSet result = new TypeSet();
        for (FileDescriptor file : fileSet.files()) {
            TypeSet messageTypes = MessageType.allFrom(file);
            result = result.union(messageTypes);
        }
        return result.messageTypes.values();
    }

    /**
     * Obtains message types declared in the passed file.
     */
    public static ImmutableCollection<MessageType> onlyMessages(FileDescriptor file) {
        TypeSet typeSet = MessageType.allFrom(file);
        return typeSet.messageTypes.values();
    }

    /**
     * Obtains the size of the set.
     */
    public int size() {
        int messagesCount = messageTypes.size();
        int enumsCount = enumTypes.size();
        int servicesCount = serviceTypes.size();
        int result = messagesCount + enumsCount + servicesCount;
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
        }
        Type<?, ?> enumType = enumTypes.get(name);
        if (enumType != null) {
            return Optional.of(enumType);
        }
        Type<?, ?> serviceType = serviceTypes.get(name);
        return Optional.ofNullable(serviceType);
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
     * Writes all the types in this set into a {@link TypeRegistry}.
     */
    public TypeRegistry toTypeRegistry() {
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
    public TypeSet union(TypeSet another) {
        if (another.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return another;
        }
        ImmutableMap<TypeName, MessageType> messages =
                unite(this.messageTypes, another.messageTypes);
        ImmutableMap<TypeName, EnumType> enums =
                unite(this.enumTypes, another.enumTypes);
        ImmutableMap<TypeName, ServiceType> services =
                unite(this.serviceTypes, another.serviceTypes);
        TypeSet result = new TypeSet(messages, enums, services);
        return result;
    }

    private static <T extends Type<?, ?>> ImmutableMap<TypeName, T>
    unite(Map<TypeName, T> left, Map<TypeName, T> right) {
        // Use HashMap instead of ImmutableMap.Builder to deal with duplicates.
        Map<TypeName, T> union = newHashMapWithExpectedSize(left.size() + right.size());
        union.putAll(left);
        union.putAll(right);
        return ImmutableMap.copyOf(union);
    }

    /**
     * Obtains all the types contained in this set.
     */
    public ImmutableSet<Type<?, ?>> allTypes() {
        ImmutableSet<Type<?, ?>> types = ImmutableSet
                .<Type<?, ?>>builder()
                .addAll(messagesAndEnums())
                .addAll(serviceTypes.values())
                .build();
        return types;
    }

    /**
     * Obtains message and enum types contained in this set.
     */
    public Set<Type<?, ?>> messagesAndEnums() {
        ImmutableSet<Type<?, ?>> types = ImmutableSet
                .<Type<?, ?>>builder()
                .addAll(messageTypes.values())
                .addAll(enumTypes.values())
                .build();
        return types;
    }

    /**
     * Obtains message types from this set.
     */
    public ImmutableSet<MessageType> messageTypes() {
        return ImmutableSet.copyOf(messageTypes.values());
    }

    /**
     * Obtains enum types from this set.
     */
    public ImmutableSet<EnumType> enumTypes() {
        return ImmutableSet.copyOf(enumTypes.values());
    }

    /**
     * Obtains service types from this set.
     */
    public ImmutableSet<ServiceType> serviceTypes() {
        return ImmutableSet.copyOf(serviceTypes.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeSet)) {
            return false;
        }
        TypeSet typeSet = (TypeSet) o;
        return Objects.equal(messageTypes, typeSet.messageTypes) &&
                Objects.equal(enumTypes, typeSet.enumTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageTypes, enumTypes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("messageTypes", namesForDisplay(messageTypes))
                          .add("enumTypes", namesForDisplay(enumTypes))
                          .add("serviceTypes", namesForDisplay(serviceTypes))
                          .toString();
    }

    private static String namesForDisplay(Map<TypeName, ?> types) {
        return types.keySet()
                    .stream()
                    .map(TypeName::value)
                    .sorted()
                    .collect(joining(lineSeparator()));
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
        private final Map<TypeName, ServiceType> serviceTypes = newHashMap();

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

        @CanIgnoreReturnValue
        public Builder add(ServiceType type) {
            TypeName name = type.name();
            serviceTypes.put(name, type);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder addAll(Iterable<MessageType> types) {
            checkNotNull(types);
            ImmutableMap<TypeName, MessageType> map = uniqueIndex(types, MessageType::name);
            messageTypes.putAll(map);
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
