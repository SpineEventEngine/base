/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.validate;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.compiler.message.MessageTypeCache;
import io.spine.gradle.compiler.util.DescriptorSetUtil;
import io.spine.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.gradle.compiler.util.FieldTypes.isMap;
import static io.spine.gradle.compiler.util.FieldTypes.isMessage;
import static io.spine.gradle.compiler.util.FieldTypes.trimTypeName;

/**
 * Assembles the {@code VBMetadata}s from the Protobuf.
 *
 * @author Illia Shepilov
 * @author Alex Tymchenko
 */
class MetadataAssembler {

    private static final String JAVA_CLASS_NAME_SUFFIX = "VBuilder";
    private static final String PROTOBUF_PACKAGE_NAME = "com.google.protobuf";

    /** A map from Protobuf type name to Java class FQN. */
    private final MessageTypeCache messageTypeCache = new MessageTypeCache();

    /** A map from Protobuf type name to Protobuf DescriptorProto. */
    private final Map<String, DescriptorProto> allMessageDescriptors = newHashMap();

    /** A map from Protobuf type name to Protobuf FileDescriptorProto. */
    private final Map<DescriptorProto, FileDescriptorProto> descriptorCache = newHashMap();

    private final String descriptorPath;

    private final Predicate<DescriptorProto> isNotProtoLangMessage =
            new Predicate<DescriptorProto>() {

                @Override
                public boolean apply(@Nullable DescriptorProto msgDescriptor) {
                    if (msgDescriptor == null) {
                        return false;
                    }
                    final String javaPackage = getJavaPackage(msgDescriptor);
                    final boolean isGoogleMsg = javaPackage.contains(PROTOBUF_PACKAGE_NAME);
                    return !isGoogleMsg;
                }
            };

    MetadataAssembler(String descriptorPath) {
        this.descriptorPath = descriptorPath;
    }

    /**
     * Assembles the {@code VBMetadata}s.
     *
     * @return the {@code Set} of the assembled metadata for the validating builders.
     */
    Set<VBMetadata> assemble() {
        log().debug("Assembling the metadata for all validating builders.");
        final Set<VBMetadata> result = newHashSet();
        final Set<FileDescriptorProto> fileDescriptors = getProtoFileDescriptors(descriptorPath);
        final Set<VBMetadata> metadataItems = obtainFileMetadata(fileDescriptors);
        final Set<VBMetadata> fieldMetadata = obtainFieldMetadata(metadataItems);
        result.addAll(fieldMetadata);
        result.addAll(metadataItems);
        log().debug("The metadata is obtained, {} validating builder(s) will be generated.",
                    fieldMetadata.size());
        return result;
    }

    @SuppressWarnings("MethodWithMultipleLoops")    // It's fine, as for the utility plugin.
    private Set<VBMetadata> obtainFileMetadata(Iterable<FileDescriptorProto> fileDescriptors) {
        log().trace("Obtaining the file-level metadata for the validating builders.");
        final Set<VBMetadata> result = newHashSet();
        for (FileDescriptorProto fileDescriptor : fileDescriptors) {
            final List<DescriptorProto> messageDescriptors = fileDescriptor.getMessageTypeList();
            final Set<VBMetadata> metadataSet =
                    constructMessageFieldMetadata(messageDescriptors);
            for (VBMetadata metadata : metadataSet) {
                metadata.setSourceProtoFilePath(fileDescriptor.getName());
            }
            result.addAll(metadataSet);

        }
        log().trace("The file-level metadata is obtained.");
        return result;
    }

    @SuppressWarnings("MethodWithMultipleLoops")    // It's fine, as for the utility plugin.
    private Set<VBMetadata> obtainFieldMetadata(Iterable<VBMetadata> metadataSet) {
        log().trace("Obtaining the metadata for the validating builders, " +
                            "which will be generated for the Message fields.");
        final Set<VBMetadata> result = newHashSet();

        for (VBMetadata metadata : metadataSet) {

            if (metadata.getJavaPackage()
                        .contains(PROTOBUF_PACKAGE_NAME)) {
                continue;
            }
            final DescriptorProto msgDescriptor = metadata.getMsgDescriptor();
            log().trace("Analyzing the descriptors for {}", metadata);
            final Set<DescriptorProto> unfiltered = getDescriptorsRecursively(msgDescriptor);
            final Set<DescriptorProto> collectedDescriptors =
                    Sets.filter(unfiltered, isNotProtoLangMessage);

            final Set<VBMetadata> fieldMetadataSet =
                    constructMessageFieldMetadata(collectedDescriptors);

            if(metadata.getSourceProtoFilePath().isPresent()) {
                for (VBMetadata fieldItem : fieldMetadataSet) {
                    fieldItem.setSourceProtoFilePath(metadata.getSourceProtoFilePath()
                                                             .get());
                }
            }
            result.addAll(fieldMetadataSet);
        }
        log().trace("The metadata for the field validating builders is obtained.");
        return result;
    }

    private Set<VBMetadata> constructMessageFieldMetadata(Iterable<DescriptorProto> descriptors) {
        final Set<VBMetadata> result = newHashSet();
        for (DescriptorProto descriptorMsg : descriptors) {
            if (isNotProtoLangMessage.apply(descriptorMsg)) {
                final VBMetadata metadata = createMetadata(descriptorMsg);
                result.add(metadata);
            }
        }
        return result;
    }

    private VBMetadata createMetadata(DescriptorProto msgDescriptor) {
        final String className = msgDescriptor.getName() + JAVA_CLASS_NAME_SUFFIX;
        final String javaPackage = getJavaPackage(msgDescriptor);
        final VBMetadata result =
                new VBMetadata(javaPackage, className, msgDescriptor);
        return result;
    }

    private Set<DescriptorProto> getDescriptorsRecursively(DescriptorProto msgDescriptor) {
        log().trace("    Obtaining descriptors for {}", msgDescriptor.getName());

        final Set<DescriptorProto> result = newHashSet();
        final Set<String> processedTypeNames = newHashSet();
        final Deque<FieldAndType> deque = newLinkedList();

        addMembersToDeque(deque, msgDescriptor);

        while(!deque.isEmpty()) {
            final FieldAndType fieldAndType = deque.pollFirst();
            final FieldDescriptorProto fieldDescriptor = fieldAndType.fieldDescriptor;

            log().trace("        - Analyzing the field {}", fieldDescriptor.getName());

            if (isMap(fieldDescriptor)) {
                log().trace("        - It is a Map.");
                final FieldDescriptorProto keyDescriptor = fieldAndType.fieldType.getField(0);
                addToDeque(deque, keyDescriptor);

                final FieldDescriptorProto valueDescriptor = fieldAndType.fieldType.getField(1);
                addToDeque(deque, valueDescriptor);

            } else {
                final String fieldTypeName = trimTypeName(fieldDescriptor);
                if(!processedTypeNames.contains(fieldTypeName)) {
                    final DescriptorProto type = allMessageDescriptors.get(fieldTypeName);
                    result.add(type);
                    processedTypeNames.add(fieldTypeName);

                    addMembersToDeque(deque, type);

                    log().trace("        - It is not map. Adding {} to results.", type);
                }
            }
        }
        log().trace("    COMPLETED: obtaining descriptors for {}", msgDescriptor.getName());
        return result;
    }

    private void addMembersToDeque(Deque<FieldAndType> deque,
                                   @Nullable DescriptorProto msgDescriptor) {
        if(msgDescriptor == null) {
            return;
        }

        final List<FieldDescriptorProto> fieldList = msgDescriptor.getFieldList();
        int nestedTypeIndex = 0;
        for (FieldDescriptorProto fieldDescriptor : fieldList) {

            if (!isMessage(fieldDescriptor)) {
                continue;
            }

            try {
                final DescriptorProto fieldType;
                if (isMap(fieldDescriptor)) {
                    fieldType = msgDescriptor.getNestedType(nestedTypeIndex);
                    nestedTypeIndex++;
                } else {
                    final String typeName = trimTypeName(fieldDescriptor);
                    fieldType = allMessageDescriptors.get(typeName);
                }

                final FieldAndType wrapper = new FieldAndType(fieldDescriptor, fieldType);

                deque.add(wrapper);
            } catch (RuntimeException e) {
                log().error("Cannot process the field {} of message descriptor {}",
                            fieldDescriptor.getTypeName(),
                            msgDescriptor.getName());
                throw Exceptions.illegalArgumentWithCauseOf(e);
            }
        }
    }

    private void addToDeque(Deque<FieldAndType> deque, FieldDescriptorProto fieldDescriptor) {

        if (!isMessage(fieldDescriptor)) {
            return;
        }

        final String typeName = trimTypeName(fieldDescriptor);
        final DescriptorProto type = allMessageDescriptors.get(typeName);
        deque.addLast(new FieldAndType(fieldDescriptor, type));
    }

    private String getJavaPackage(DescriptorProto msgDescriptor) {
        final String result = descriptorCache.get(msgDescriptor)
                                             .getOptions()
                                             .getJavaPackage();
        return result;
    }

    private Set<FileDescriptorProto> getProtoFileDescriptors(String descFilePath) {
        log().trace("Obtaining the file descriptors by {} path.", descFilePath);
        final Set<FileDescriptorProto> result = newHashSet();
        final Collection<FileDescriptorProto> allDescriptors =
                DescriptorSetUtil.getProtoFileDescriptors(descFilePath);
        for (FileDescriptorProto fileDescriptor : allDescriptors) {

            cacheAllMessageDescriptors(fileDescriptor);
            cacheFileDescriptors(fileDescriptor);
            messageTypeCache.cacheTypes(fileDescriptor);

            log().info("Found Protobuf file: {}", fileDescriptor.getName());
            result.add(fileDescriptor);
        }
        log().trace("Found Message in files: {}", result);
        return result;
    }

    private void cacheAllMessageDescriptors(FileDescriptorProto fileDescriptor) {
        List<DescriptorProto> descriptors = fileDescriptor.getMessageTypeList();
        for (DescriptorProto msgDescriptor : descriptors) {
            final String messageFullName = getMessageFullName(fileDescriptor.getPackage(),
                                                              msgDescriptor.getName());
            allMessageDescriptors.put(messageFullName, msgDescriptor);
        }
    }

    private static String getMessageFullName(String packageName, String className) {
        final String result = packageName + '.' + className;
        return result;
    }

    private void cacheFileDescriptors(FileDescriptorProto fileDescriptor) {
        for (DescriptorProto msgDescriptor : fileDescriptor.getMessageTypeList()) {
            descriptorCache.put(msgDescriptor, fileDescriptor);
        }
    }

    MessageTypeCache getAssembledMessageTypeCache() {
        return messageTypeCache;
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(MetadataAssembler.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    /**
     * Utility class which holds a description of the field and its type.
     */
    private static class FieldAndType {
        private final FieldDescriptorProto fieldDescriptor;
        private final DescriptorProto fieldType;

        private FieldAndType(FieldDescriptorProto fieldDescriptorProto, DescriptorProto fieldType) {
            this.fieldDescriptor = fieldDescriptorProto;
            this.fieldType = fieldType;
        }
    }
}
