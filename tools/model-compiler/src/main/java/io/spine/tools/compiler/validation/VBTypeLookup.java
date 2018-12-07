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

package io.spine.tools.compiler.validation;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.java.PackageName;
import io.spine.code.proto.FileName;
import io.spine.logging.Logging;
import io.spine.tools.compiler.TypeCache;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static io.spine.code.proto.FileDescriptors.parseSkipStandard;
import static java.util.stream.Collectors.toList;

/**
 * Collects types for which validating builders are generated.
 */
class VBTypeLookup implements Logging {

    private static final String JAVA_CLASS_NAME_SUFFIX = "VBuilder";

    /** The path to descriptor set file. */
    private final String descriptorSetFile;

    /** A map from Protobuf type name to Java class FQN. */
    private final TypeCache typeCache = new TypeCache();

    VBTypeLookup(String descriptorSetFile) {
        this.descriptorSetFile = descriptorSetFile;
    }

    /**
     * Assembles the {@code VBMetadata}s.
     *
     * @return the {@code Set} of the assembled metadata for the validating builders.
     */
    Set<VBType> collect() {
        Logger log = log();
        log.debug("Collecting types for all validating builders.");
        Set<FileDescriptorProto> fileDescriptors = parseAndCollectTypes(descriptorSetFile);
        Set<VBType> result = newHashSet();
        Set<VBType> allTypes = fromAllFiles(fileDescriptors);
        result.addAll(allTypes);
        if (result.size() == 1) {
            VBType found = allTypes.iterator()
                                   .next();
            log.debug("One type found for generating validating builder: {}", found);
        } else {
            log.debug("Types collected, {} validating builders will be generated.", result.size());
        }
        return ImmutableSet.copyOf(result);
    }

    private Set<VBType> fromAllFiles(Iterable<FileDescriptorProto> files) {
        log().debug("Obtaining the file-level metadata for the validating builders.");
        Set<VBType> result = newHashSet();
        for (FileDescriptorProto file : files) {
            List<DescriptorProto> messageDescriptors = file.getMessageTypeList();
            Set<VBType> metadataSet = createMetadata(messageDescriptors, file);
            result.addAll(metadataSet);

        }
        log().debug("The file-level metadata is obtained.");
        return result;
    }

    private static Set<VBType> createMetadata(Iterable<DescriptorProto> descriptors,
                                              FileDescriptorProto file) {
        Set<VBType> result = newHashSet();
        for (DescriptorProto message : descriptors) {
            VBType type = newType(message, file);
            result.add(type);
        }
        return result;
    }

    private static VBType newType(DescriptorProto message, FileDescriptorProto file) {
        String className = message.getName() + JAVA_CLASS_NAME_SUFFIX;
        String javaPackage = PackageName.resolve(file)
                                        .value();
        VBType result = new VBType(javaPackage, className, message, file.getName());
        return result;
    }

    private Set<FileDescriptorProto> parseAndCollectTypes(String descFilePath) {
        Logger log = log();
        log.debug("Obtaining the file descriptors by {} path.", descFilePath);
        ImmutableSet.Builder<FileDescriptorProto> result = ImmutableSet.builder();
        Collection<FileDescriptorProto> descriptors = fileDescriptors(descFilePath);
        for (FileDescriptorProto file : descriptors) {
            typeCache.loadFrom(file);
            log.debug("Found Protobuf file: {}", file.getName());
            result.add(file);
        }
        log.debug("Found Message in files: {}", result);
        return result.build();
    }

    TypeCache getTypeCache() {
        return typeCache;
    }

    /**
     * Obtains file descriptors for which validating builders should be generated.
     *
     * @param descFilePath
     *         the path to the descriptor set file
     * @return descriptors excluding Protobuf and rejections descriptors
     */
    private static Collection<FileDescriptorProto> fileDescriptors(String descFilePath) {
        List<FileDescriptorProto> files = parseSkipStandard(descFilePath);
        return files.stream()
                    .filter(descriptor -> !FileName.from(descriptor)
                                                    .isRejections())
                    .collect(toList());
    }
}
