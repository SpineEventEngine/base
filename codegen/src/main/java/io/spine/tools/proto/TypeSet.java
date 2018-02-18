/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.proto;

import com.google.common.collect.Sets;
import com.google.protobuf.Descriptors.FileDescriptor;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * A set of proto types.
 *
 * @author Alexander Yevsyukov
 */
public class TypeSet {

    private final Set<Type> types;

    /** Creates a new empty set. */
    private TypeSet() {
        this.types = newHashSet();
    }

    private TypeSet(Iterable<Type> types) {
        this.types = newHashSet(types);
    }

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
        final int result = types.size();
        return result;
    }

    /**
     * Verifies if the set is empty.
     */
    public boolean isEmpty() {
        return types.isEmpty();
    }

    boolean add(Type type) {
        final boolean result = types.add(type);
        return result;
    }

    /**
     * Creates a new set which is a union of this and the passed one.
     */
    TypeSet union(TypeSet another) {
        if (another.isEmpty()) {
            return this;
        }

        if (this.isEmpty()) {
            return another;
        }

        final TypeSet result = new TypeSet(Sets.union(this.types, another.types));
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TypeSet other = (TypeSet) obj;
        return Objects.equals(this.types, other.types);
    }
}
