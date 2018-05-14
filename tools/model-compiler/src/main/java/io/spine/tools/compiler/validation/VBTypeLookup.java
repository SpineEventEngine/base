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
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Message;
import io.spine.tools.compiler.MessageTypeCache;
import io.spine.tools.proto.FileDescriptors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Collects types for which validating builders are generated.
 *
 * @author Illia Shepilov
 * @author Alex Tymchenko
 */
class VBTypeLookup {

    private static final String JAVA_CLASS_NAME_SUFFIX = "VBuilder";

    /** The path to descriptor set file. */
    private final String descriptorSetFile;

    /** A map from Protobuf type name to Java class FQN. */
    private final MessageTypeCache messageTypeCache = new MessageTypeCache();

    /** A map from Protobuf type name to Protobuf FileDescriptorProto. */
    private final Map<DescriptorProto, FileDescriptorProto> descriptorCache = newHashMap();


    /** Verifies if a message is not one provided by the Protobuf library. */
    private final Predicate<DescriptorProto> isNotStandardType =
            new Predicate<DescriptorProto>() {

                @Override
                public boolean apply(@Nullable DescriptorProto message) {
                    if (message == null) {
                        return false;
                    }
                    final String javaPackage = getJavaPackage(message);
                    final boolean isGoogleMsg = javaPackage.contains(Message.class.getPackage()
                                                                                  .getName());
                    return !isGoogleMsg;
                }
            };

    VBTypeLookup(String descriptorSetFile) {
        this.descriptorSetFile = descriptorSetFile;
    }

    /**
     * Assembles the {@code VBMetadata}s.
     *
     * @return the {@code Set} of the assembled metadata for the validating builders.
     */
    Set<VBType> collect() {
        final Logger log = log();
        log.debug("Collecting types for all validating builders.");
        final Set<FileDescriptorProto> fileDescriptors = parseAndCollectTypes(descriptorSetFile);
        final Set<VBType> result = newHashSet();
        final Set<VBType> allTypes = fromAllFiles(fileDescriptors);
        result.addAll(allTypes);
        if (result.size() == 1) {
            final VBType found = allTypes.iterator()
                                         .next();
            log.debug("One type found for generating validating builder: {}", found);
        } else {
            log.debug("Types collected, {} validating builders will be generated.", result.size());
        }
        return ImmutableSet.copyOf(result);
    }

    private Set<VBType> fromAllFiles(Iterable<FileDescriptorProto> files) {
        log().debug("Obtaining the file-level metadata for the validating builders.");
        final Set<VBType> result = newHashSet();
        for (FileDescriptorProto file : files) {
            final List<DescriptorProto> messageDescriptors = file.getMessageTypeList();
            final Set<VBType> metadataSet = createMetadata(messageDescriptors, file);
            result.addAll(metadataSet);

        }
        log().debug("The file-level metadata is obtained.");
        return result;
    }

    private Set<VBType> createMetadata(Iterable<DescriptorProto> descriptors,
                                       FileDescriptorProto file) {
        final Set<VBType> result = newHashSet();
        for (DescriptorProto message : descriptors) {
            if (isNotStandardType.apply(message)) {
                final VBType type = newType(message, file);
                result.add(type);
            }
        }
        return result;
    }

    private VBType newType(DescriptorProto message, FileDescriptorProto file) {
        final String className = message.getName() + JAVA_CLASS_NAME_SUFFIX;
        final String javaPackage = getJavaPackage(message);
        final VBType result = new VBType(javaPackage, className, message, file.getName());
        return result;
    }

    private String getJavaPackage(DescriptorProto msgDescriptor) {
        final String result = descriptorCache.get(msgDescriptor)
                                             .getOptions()
                                             .getJavaPackage();
        return result;
    }

    private Set<FileDescriptorProto> parseAndCollectTypes(String descFilePath) {
        final Logger log = log();
        log.debug("Obtaining the file descriptors by {} path.", descFilePath);
        final ImmutableSet.Builder<FileDescriptorProto> result = ImmutableSet.builder();
        final Collection<FileDescriptorProto> allDescriptors =
                FileDescriptors.parse(descFilePath);

        for (FileDescriptorProto file : allDescriptors) {
            cacheTypesFromFile(file);
            messageTypeCache.cacheTypes(file);

            log.debug("Found Protobuf file: {}", file.getName());
            result.add(file);
        }
        log.debug("Found Message in files: {}", result);
        return result.build();
    }

    private void cacheTypesFromFile(FileDescriptorProto file) {
        for (DescriptorProto msgDescriptor : file.getMessageTypeList()) {
            descriptorCache.put(msgDescriptor, file);
        }
    }

    MessageTypeCache getTypeCache() {
        return messageTypeCache;
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(VBTypeLookup.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
