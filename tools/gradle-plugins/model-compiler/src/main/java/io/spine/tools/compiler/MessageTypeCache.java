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
package io.spine.tools.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import io.spine.tools.gradle.compiler.RejectionGenPlugin;
import io.spine.tools.java.SimpleClassName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A cache for the Protobuf message types parsed into appropriate Java types during
 * the Spine Rejection generation.
 *
 * @author Alexander Yevsyukov
 * @author Alex Tymchenko
 * @see RejectionGenPlugin
 */
public class MessageTypeCache {

    /** A map from Protobuf type name to Java class FQN. */
    private final Map<String, String> cachedMessageTypes = newHashMap();

    /**
     * Caches all the types declared in the file.
     *
     * @param fileDescriptor the descriptor for file to cache
     */
    //It's fine, as we are caching multiple message types per file.
    @SuppressWarnings("MethodWithMultipleLoops")
    public void cacheTypes(FileDescriptorProto fileDescriptor) {
        log().debug("Caching all the types declared in the file: {}", fileDescriptor.getName());

        final FileOptions options = fileDescriptor.getOptions();

        final String sourceProtoPackage = fileDescriptor.getPackage();
        final String protoPackage = !sourceProtoPackage.isEmpty()
                                    ? (sourceProtoPackage + '.')
                                    : "";
        final String sourceJavaPackage = options.getJavaPackage();
        final StringBuilder javaPackage =
                new StringBuilder(!sourceJavaPackage.isEmpty()
                                          ? sourceJavaPackage + '.'
                                          : "");

        if (!options.getJavaMultipleFiles()) {
            final String singleFileSuffix = SimpleClassName.outerOf(fileDescriptor)
                                                           .value();
            javaPackage.append(singleFileSuffix)
                       .append('.');
        }

        final String pkgValue = javaPackage.toString();
        cacheMessageTypes(fileDescriptor, protoPackage, pkgValue);
        cacheEnumTypes(fileDescriptor, protoPackage, pkgValue);
    }

    private void cacheMessageTypes(FileDescriptorProto fileDescriptor,
                                   String protoPackage,
                                   String javaPackage) {
        for (DescriptorProto msg : fileDescriptor.getMessageTypeList()) {
            cacheMessageType(msg, protoPackage, javaPackage);
        }
    }

    private void cacheEnumTypes(FileDescriptorProto fileDescriptor,
                                String protoPackage,
                                String javaPackage) {
        for (EnumDescriptorProto enumType : fileDescriptor.getEnumTypeList()) {
            cacheEnumType(enumType, protoPackage, javaPackage);
        }
    }

    /**
     * Obtain an immutable copy of the parsed Protobuf type names mapped to
     * FQN of related Java classes.
     *
     * @return current cache contents
     */
    public Map<String, String> getCachedTypes() {
        final ImmutableMap<String, String> immutable = ImmutableMap.copyOf(cachedMessageTypes);
        return immutable;
    }

    //It's fine, as we are caching multiple nested types per message descriptor.
    @SuppressWarnings("MethodWithMultipleLoops")
    private void cacheMessageType(DescriptorProto msg, String protoPrefix, String javaPrefix) {
        final String msgName = msg.getName();
        final String key = protoPrefix + msgName;
        final String value = javaPrefix + msgName;
        log().debug("Caching message type {}", msgName);
        cachedMessageTypes.put(key, value);
        if (msg.getNestedTypeCount() > 0 || msg.getEnumTypeCount() > 0) {
            final String nestedProtoPrefix = protoPrefix + msgName + '.';
            final String nestedJavaPrefix = javaPrefix + msgName + '.';
            for (DescriptorProto nestedMsg : msg.getNestedTypeList()) {
                cacheMessageType(nestedMsg, nestedProtoPrefix, nestedJavaPrefix);
            }
            for (EnumDescriptorProto enumType : msg.getEnumTypeList()) {
                cacheEnumType(enumType, nestedProtoPrefix, nestedJavaPrefix);
            }
        }
    }

    private void cacheEnumType(EnumDescriptorProto descriptor,
                               String protoPrefix,
                               String javaPrefix) {
        final String name = descriptor.getName();
        log().debug("Caching enum type {}", name);
        final String key = protoPrefix + name;
        final String value = javaPrefix + name;
        cachedMessageTypes.put(key, value);
    }

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(MessageTypeCache.class);
    }
}
