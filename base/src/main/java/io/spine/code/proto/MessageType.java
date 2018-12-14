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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.annotation.Internal;
import io.spine.code.java.SimpleClassName;
import io.spine.type.ClassName;
import io.spine.type.TypeUrl;

import java.util.Deque;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * A message type as declared in a proto file.
 */
@Internal
public class MessageType extends Type<Descriptor, DescriptorProto> {

    /**
     * Standard suffix for a Validating Builder class name.
     */
    public static final String VBUILDER_SUFFIX = "VBuilder";

    protected MessageType(Descriptor descriptor) {
        super(descriptor);
    }

    static MessageType create(Descriptor descriptor) {
        return new MessageType(descriptor);
    }

    /**
     * Collects all message types, including nested, declared in the passed file.
     */
    static TypeSet allFrom(FileDescriptor file) {
        checkNotNull(file);
        TypeSet.Builder result = TypeSet.newBuilder();
        for (Descriptor messageType : file.getMessageTypes()) {
            addType(messageType, result);
        }
        return result.build();
    }

    private static void addType(Descriptor type, TypeSet.Builder set) {
        if (type.getOptions()
                .getMapEntry()) {
            return;
        }
        set.add(create(type));
        for (Descriptor nestedType : type.getNestedTypes()) {
            addType(nestedType, set);
        }
    }

    @Override
    public final DescriptorProto toProto() {
        return descriptor().toProto();
    }

    @Override
    public final TypeUrl url() {
        return TypeUrl.from(descriptor());
    }

    @Override
    public final ClassName javaClassName() {
        return ClassName.from(descriptor());
    }

    @Override
    public SimpleClassName simpleJavaClassName() {
        return SimpleClassName.ofMessage(descriptor());
    }

    /**
     * Verifies if this message is under the "google" package.
     */
    public boolean isGoogle() {
        FileDescriptor file = descriptor().getFile();
        boolean result = FileDescriptors.isGoogle(file);
        return result;
    }

    public ClassName builderClass() {

        ClassName result = javaClassName().nestedClass(SimpleClassName.ofBuilder());
        return result;
    }

    /**
     * Obtains the name of a Validating Builder class that corresponds to this message type.
     *
     * @return the class name of the builder, or empty optional if this message type is
     *         from the "google" package
     */
    public Optional<ClassName> validatingBuilderClass() {
        if (isGoogle()) {
            return Optional.empty();
        }
        return Optional.of(javaClassName().with(VBUILDER_SUFFIX));
    }

    /**
     * Obtains all nested declarations that match the passed predicate.
     */
    ImmutableList<MessageType> getAllNested(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageType> result = ImmutableList.builder();
        Iterable<MessageType> nestedDeclarations = getImmediateNested();
        Deque<MessageType> deque = newLinkedList(nestedDeclarations);

        while (!deque.isEmpty()) {
            MessageType nestedDeclaration = deque.pollFirst();

            assert nestedDeclaration != null; // Cannot be null since the queue is not empty.
            DescriptorProto nestedDescriptor = nestedDeclaration.descriptor()
                                                                .toProto();

            if (predicate.test(nestedDescriptor)) {
                result.add(nestedDeclaration);
            }

            deque.addAll(nestedDeclaration.getImmediateNested());
        }
        return result.build();
    }

    /**
     * Obtains immediate declarations of nested types of this declaration, or
     * empty list if no nested types are declared.
     */
    private ImmutableList<MessageType> getImmediateNested() {
        ImmutableList<MessageType> result =
                descriptor().getNestedTypes()
                            .stream()
                            .map(MessageType::create)
                            .collect(toImmutableList());
        return result;
    }

    /**
     * Obtains message documentation.
     */
    public MessageDocumentation documentation() {
        return new MessageDocumentation(this);
    }

    /**
     * Obtains fields declared in the message type.
     */
    public ImmutableList<FieldDeclaration> fields() {
        ImmutableList<FieldDeclaration> result =
                descriptor().getFields()
                            .stream()
                            .map(d -> new FieldDeclaration(d, this))
                            .collect(toImmutableList());
        return result;
    }
}
