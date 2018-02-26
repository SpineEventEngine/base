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

package io.spine.tools.compiler.validation;

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.tools.compiler.MessageTypeCache;
import io.spine.tools.proto.FileDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Assembles the {@code VBMetadata}s from the Protobuf.
 *
 * @author Illia Shepilov
 * @author Alex Tymchenko
 */
class MetadataAssembler {

    private static final String JAVA_CLASS_NAME_SUFFIX = "VBuilder";

    @SuppressWarnings("DuplicateStringLiteralInspection") // The same string has different semantics
    private static final String PROTOBUF_PACKAGE_NAME = "com.google.protobuf";

    /** A map from Protobuf type name to Java class FQN. */
    private final MessageTypeCache messageTypeCache = new MessageTypeCache();

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
        final Logger log = log();
        log.debug("Assembling the metadata for all validating builders.");
        final Set<VBMetadata> result = newHashSet();
        final Set<FileDescriptorProto> fileDescriptors = getProtoFileDescriptors(descriptorPath);
        final Set<VBMetadata> metadataItems = obtainFileMetadata(fileDescriptors);
        result.addAll(metadataItems);
        log.debug("The metadata is obtained, {} validating builder(s) will be generated.",
                    result.size());
        return result;
    }

    private Set<VBMetadata> obtainFileMetadata(Iterable<FileDescriptorProto> fileDescriptors) {
        log().trace("Obtaining the file-level metadata for the validating builders.");
        final Set<VBMetadata> result = newHashSet();
        for (FileDescriptorProto fileDescriptor : fileDescriptors) {
            final List<DescriptorProto> messageDescriptors = fileDescriptor.getMessageTypeList();
            final Set<VBMetadata> metadataSet = createMetadata(messageDescriptors, fileDescriptor);
            result.addAll(metadataSet);

        }
        log().trace("The file-level metadata is obtained.");
        return result;
    }

    private Set<VBMetadata> createMetadata(Iterable<DescriptorProto> descriptors,
                                           FileDescriptorProto fileDescriptor) {
        final Set<VBMetadata> result = newHashSet();
        for (DescriptorProto descriptorMsg : descriptors) {
            if (isNotProtoLangMessage.apply(descriptorMsg)) {
                final VBMetadata metadata = createMetadata(descriptorMsg, fileDescriptor);
                result.add(metadata);
            }
        }
        return result;
    }

    private VBMetadata createMetadata(DescriptorProto msgDescriptor,
                                      FileDescriptorProto fileDescriptor) {
        final String className = msgDescriptor.getName() + JAVA_CLASS_NAME_SUFFIX;
        final String javaPackage = getJavaPackage(msgDescriptor);
        final VBMetadata result = new VBMetadata(javaPackage,
                                                 className,
                                                 msgDescriptor,
                                                 fileDescriptor.getName());
        return result;
    }

    private String getJavaPackage(DescriptorProto msgDescriptor) {
        final String result = descriptorCache.get(msgDescriptor)
                                             .getOptions()
                                             .getJavaPackage();
        return result;
    }

    private Set<FileDescriptorProto> getProtoFileDescriptors(String descFilePath) {
        final Logger log = log();
        log.debug("Obtaining the file descriptors by {} path.", descFilePath);
        final Set<FileDescriptorProto> result = newHashSet();
        final Collection<FileDescriptorProto> allDescriptors =
                FileDescriptors.parse(descFilePath);

        for (FileDescriptorProto fileDescriptor : allDescriptors) {
            cacheFileDescriptors(fileDescriptor);
            messageTypeCache.cacheTypes(fileDescriptor);

            log.debug("Found Protobuf file: {}", fileDescriptor.getName());
            result.add(fileDescriptor);
        }
        log.debug("Found Message in files: {}", result);
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
}
