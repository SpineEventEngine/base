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

package io.spine.tools.compiler;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.code.java.SimpleClassName;
import io.spine.logging.Logging;

/**
 * Loads types from the passed file descriptor.
 */
class TypeLoader implements Logging {

    private final TypeCache cache;

    private final FileDescriptorProto file;

    private final String protoPrefix;
    private final String javaPrefix;

    TypeLoader(TypeCache cache, FileDescriptorProto file) {
        this.cache = cache;
        this.file = file;
        this.protoPrefix = getProtoPackage(file);
        this.javaPrefix = getJavaPackage(file);
    }

    private static String getProtoPackage(FileDescriptorProto file) {
        String sourceProtoPackage = file.getPackage();
        return !sourceProtoPackage.isEmpty()
               ? (sourceProtoPackage + '.')
               : "";
    }

    private static String getJavaPackage(FileDescriptorProto file) {
        DescriptorProtos.FileOptions options = file.getOptions();
        String sourceJavaPackage = options.getJavaPackage();
        StringBuilder javaPackageBuilder =
                new StringBuilder(!sourceJavaPackage.isEmpty()
                                  ? sourceJavaPackage + '.'
                                  : "");

        if (!options.getJavaMultipleFiles()) {
            String singleFileSuffix = SimpleClassName.outerOf(file)
                                                     .value();
            javaPackageBuilder.append(singleFileSuffix)
                              .append('.');
        }

        return javaPackageBuilder.toString();
    }

    void load() {
        log().debug("Caching all the types declared in the file: {}", file.getName());
        addMessageTypes();
        cacheEnumTypes();
    }

    private void addMessageTypes() {
        file.getMessageTypeList()
            .forEach(msgType -> addMessageType(msgType, protoPrefix, javaPrefix));
    }

    private void cacheEnumTypes() {
        file.getEnumTypeList()
            .forEach(enumType -> addEnumType(enumType, protoPrefix, javaPrefix));
    }

    private void addMessageType(DescriptorProto msg, String protoPrefix, String javaPrefix) {
        String msgName = msg.getName();
        String key = protoPrefix + msgName;
        String value = javaPrefix + msgName;
        log().debug("Caching message type {}", msgName);
        cache.put(key, value);
        if (containsNestedTypes(msg)) {
            addNestedTypes(msg, protoPrefix, javaPrefix);
        }
    }

    //It's fine, as we are caching multiple nested types per message descriptor.
    @SuppressWarnings("MethodWithMultipleLoops")
    private void addNestedTypes(DescriptorProto msg, String protoPrefix, String javaPrefix) {
        String msgName = msg.getName();
        String nestedProtoPrefix = protoPrefix + msgName + '.';
        String nestedJavaPrefix = javaPrefix + msgName + '.';
        for (DescriptorProto nestedMsg : msg.getNestedTypeList()) {
            addMessageType(nestedMsg, nestedProtoPrefix, nestedJavaPrefix);
        }
        for (EnumDescriptorProto enumType : msg.getEnumTypeList()) {
            addEnumType(enumType, nestedProtoPrefix, nestedJavaPrefix);
        }
    }

    private static boolean containsNestedTypes(DescriptorProto msg) {
        return msg.getNestedTypeCount() > 0 || msg.getEnumTypeCount() > 0;
    }

    private void addEnumType(EnumDescriptorProto enumType,
                             String protoPrefix,
                             String javaPrefix) {
        String name = enumType.getName();
        log().debug("Caching enum type {}", name);
        String key = protoPrefix + name;
        String value = javaPrefix + name;
        cache.put(key, value);
    }
}
