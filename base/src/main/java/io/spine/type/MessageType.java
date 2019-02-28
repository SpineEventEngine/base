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

package io.spine.type;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.java.VBuilderClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileName;
import io.spine.code.proto.LocationPath;
import io.spine.code.proto.SourceFile;
import io.spine.code.proto.TypeSet;
import io.spine.logging.Logging;
import io.spine.option.IsOption;

import java.util.Comparator;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.code.proto.FileDescriptors.sameFiles;
import static io.spine.option.OptionsProto.enrichmentFor;

/**
 * A message type as declared in a proto file.
 */
@SuppressWarnings("ClassWithTooManyMethods")
public class MessageType extends Type<Descriptor, DescriptorProto> implements Logging {

    /**
     * Standard suffix for a Validating Builder class name.
     */
    public static final String VBUILDER_SUFFIX = "VBuilder";

    /**
     * Creates a new instance by the given message descriptor.
     */
    public MessageType(Descriptor descriptor) {
        super(descriptor, true);
    }

    /**
     * Collects all message types, including nested, declared in the passed file.
     */
    public static TypeSet allFrom(FileDescriptor file) {
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
        MessageType messageType = new MessageType(type);
        set.add(messageType);
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
    @SuppressWarnings("unchecked") // It is safe since we work with a message descriptors
    public Class<? extends Message> javaClass() {
        return (Class<? extends Message>) super.javaClass();
    }

    /**
     * Obtains source file with the declaration of this message type.
     */
    public SourceFile sourceFile() {
        return SourceFile.from(descriptor().getFile());
    }

    /**
     * Tells if this message is under the "google" package.
     */
    public boolean isGoogle() {
        FileDescriptor file = descriptor().getFile();
        boolean result = FileDescriptors.isGoogle(file);
        return result;
    }

    /**
     * Tells if this message type is not from "google" package, and is not an extension
     * defined in "options.proto".
     */
    public boolean isCustom() {
        if (isGoogle()) {
            return false;
        }
        FileDescriptor optionsProto = IsOption.getDescriptor()
                                              .getFile();
        FileDescriptor file = descriptor().getFile();
        return !sameFiles(optionsProto, file);
    }

    /**
     * Tells if this message is top-level in its file.
     */
    public boolean isTopLevel() {
        Descriptor descriptor = descriptor();
        return isTopLevel(descriptor);
    }

    /**
     * Verifies if the message is top-level (rather than nested).
     */
    public static boolean isTopLevel(Descriptor descriptor) {
        Descriptor parent = descriptor.getContainingType();
        return parent == null;
    }

    /**
     * Tells if this message is nested inside another message declaration.
     */
    public boolean isNested() {
        return !isTopLevel();
    }

    /**
     * Tells if this message is a rejection.
     */
    public boolean isRejection() {
        boolean result = isTopLevel() && declaringFileName().isRejections();
        return result;
    }

    /**
     * Tells if this message is a command.
     */
    public boolean isCommand() {
        boolean result = isTopLevel() && declaringFileName().isCommands();
        return result;
    }

    /**
     * Tells if this message is an event.
     *
     * <p>Returns {@code false} if this type is a {@linkplain #isRejection() rejection}.
     */
    public boolean isEvent() {
        boolean result = isTopLevel() && declaringFileName().isEvents();
        return result;
    }

    private FileName declaringFileName() {
        return FileName.from(descriptor().getFile());
    }

    /**
     * Tells if the message is an enrichment.
     */
    public boolean isEnrichment() {
        MessageOptions options = descriptor().getOptions();
        boolean result = isTopLevel() && options.hasExtension(enrichmentFor);
        return result;
    }

    /**
     * Obtains the name of a Validating Builder class for the type.
     *
     * @throws java.lang.IllegalStateException
     *         if the message type does not have a corresponding
     *         a Validating Builder class, for example, because it's a Google Protobuf message
     */
    public SimpleClassName validatingBuilderClass() {
        checkState(isCustom(), "No validating builder class available for the type `%s`.", this);
        SimpleClassName result = VBuilderClassName.of(this);
        return result;
    }

    /**
     * Obtains all nested declarations that match the passed predicate.
     */
    public ImmutableList<MessageType> nestedTypesThat(Predicate<DescriptorProto> predicate) {
        ImmutableList.Builder<MessageType> result = ImmutableList.builder();
        Iterable<MessageType> nestedDeclarations = immediateNested();
        Deque<MessageType> deque = newLinkedList(nestedDeclarations);

        while (!deque.isEmpty()) {
            MessageType nestedDeclaration = deque.pollFirst();

            assert nestedDeclaration != null; // Cannot be null since the queue is not empty.
            DescriptorProto nestedDescriptor = nestedDeclaration.descriptor()
                                                                .toProto();

            if (predicate.test(nestedDescriptor)) {
                result.add(nestedDeclaration);
            }

            deque.addAll(nestedDeclaration.immediateNested());
        }
        return result.build();
    }

    /**
     * Obtains immediate declarations of nested types of this declaration, or
     * empty list if no nested types are declared.
     */
    private ImmutableList<MessageType> immediateNested() {
        ImmutableList<MessageType> result =
                descriptor().getNestedTypes()
                            .stream()
                            .map(MessageType::new)
                            .collect(toImmutableList());
        return result;
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

    /**
     * Returns the message location path for a top-level message definition.
     *
     * @return the message location path
     */
    public LocationPath path() {
        LocationPath result = LocationPath.fromMessage(descriptor());
        return result;
    }

    /**
     * Obtains the comments going before a rejection declaration.
     *
     * <p>Requires the following Protobuf plugin configuration:
     * <pre> {@code
     * generateProtoTasks {
     *     all().each { final task ->
     *         // If true, the descriptor set will contain line number information
     *         // and comments. Default is false.
     *         task.descriptorSetOptions.includeSourceInfo = true
     *         // ...
     *     }
     * }
     * }</pre>
     *
     * @return the comments text or {@code Optional.empty()} if there are no comments
     * @see <a href="https://github.com/google/protobuf-gradle-plugin/blob/master/README.md#generate-descriptor-set-files">
     *         Protobuf plugin configuration</a>
     */
    public Optional<String> leadingComments() {
        LocationPath messagePath = path();
        return leadingComments(messagePath);
    }

    /**
     * Obtains a leading comments by the {@link LocationPath}.
     *
     * @param locationPath
     *         the location path to get leading comments
     * @return the leading comments or empty {@code Optional} if there are no such comments or
     *         a descriptor was generated without source code information
     */
    public Optional<String> leadingComments(LocationPath locationPath) {
        FileDescriptorProto file = descriptor()
                .getFile()
                .toProto();
        if (!file.hasSourceCodeInfo()) {
            _warn("Unable to obtain proto source code info. " +
                          "Please configure the Gradle Protobuf plugin as follows:%n%s",
                  "`task.descriptorSetOptions.includeSourceInfo = true`.");
            return Optional.empty();
        }

        DescriptorProtos.SourceCodeInfo.Location location = locationPath.toLocation(file);
        return location.hasLeadingComments()
               ? Optional.of(location.getLeadingComments())
               : Optional.empty();
    }

    /**
     * Obtains a lexicographical comparator of full type names.
     */
    public static Comparator<MessageType> fullNameComparator() {
        return (o1, o2) -> {
            String name1 = o1.descriptor().getFullName();
            String name2 = o2.descriptor().getFullName();
            return name1.compareTo(name2);
        };
    }
}
