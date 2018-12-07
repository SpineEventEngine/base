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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import io.spine.code.java.SimpleClassName;
import io.spine.logging.Logging;

import java.util.List;

/**
 * Loads type names into {@code TypeCache}.
 */
abstract class TypeLoader implements Logging {

    /** The cache that we populate. */
    private final TypeCache cache;
    /** The prefix which would be added to a Proto type name. */
    private final String protoPrefix;
    /** The prefix which would be added to a Java type name. */
    private final String javaPrefix;

    /**
     * Loads types declared in the file into cache.
     */
    @SuppressWarnings("ClassReferencesSubclass") // we hide impl. details in simple hierarchy.
    static void load(TypeCache cache, FileDescriptorProto file) {
        FileLoader loader = new FileLoader(cache, file);
        loader.load();
    }

    private TypeLoader(TypeCache cache, String protoPrefix, String javaPrefix) {
        this.cache = cache;
        this.protoPrefix = protoPrefix;
        this.javaPrefix = javaPrefix;
    }

    /**
     * Puts the key/value pair into the cache.
     */
    private void put(String key, String value) {
        cache.put(key, value);
    }

    void load() {
        addMessageTypes();
        addEnumTypes();
    }

    private void addMessageTypes() {
        messageTypes().forEach(this::addMessageType);
    }

    private void addEnumTypes() {
        enumTypes().forEach(this::addEnumType);
    }

    abstract List<DescriptorProto> messageTypes();

    abstract List<EnumDescriptorProto> enumTypes();

    private void addMessageType(DescriptorProto msg) {
        String msgName = msg.getName();
        String key = protoPrefix + msgName;
        String value = javaPrefix + msgName;
        log().debug("Caching message type {}", msgName);
        put(key, value);
        if (containsNestedTypes(msg)) {
            addNestedTypes(msg);
        }
    }

    private void addEnumType(EnumDescriptorProto enumType) {
        String name = enumType.getName();
        log().debug("Caching enum type {}", name);
        String key = protoPrefix + name;
        String value = javaPrefix + name;
        put(key, value);
    }

    static boolean containsNestedTypes(DescriptorProto msg) {
        return msg.getNestedTypeCount() > 0 || msg.getEnumTypeCount() > 0;
    }

    @SuppressWarnings("ClassReferencesSubclass")
    private void addNestedTypes(DescriptorProto msg) {
        NestedTypeLoader nested = new NestedTypeLoader(this, msg);
        nested.load();
    }

    /**
     * Loads types from the passed file descriptor.
     */
    private static final class FileLoader extends TypeLoader {

        private final FileDescriptorProto file;

        private FileLoader(TypeCache cache, FileDescriptorProto file) {
            super(cache, protoPrefix(file), javaPrefix(file));
            this.file = file;
            log().debug("Caching all the types declared in the file: {}", file.getName());
        }

        private static String protoPrefix(FileDescriptorProto file) {
            String protoPackage = file.getPackage();
            return !protoPackage.isEmpty()
                   ? (protoPackage + '.')
                   : "";
        }

        private static String javaPrefix(FileDescriptorProto file) {
            FileOptions options = file.getOptions();
            String javaPackage = options.getJavaPackage();
            StringBuilder builder =
                    new StringBuilder(!javaPackage.isEmpty()
                                      ? javaPackage + '.'
                                      : "");

            if (!options.getJavaMultipleFiles()) {
                String singleFileSuffix = SimpleClassName.outerOf(file)
                                                         .value();
                builder.append(singleFileSuffix)
                       .append('.');
            }

            return builder.toString();
        }

        @Override
        List<DescriptorProto> messageTypes() {
            return file.getMessageTypeList();
        }

        @Override
        List<EnumDescriptorProto> enumTypes() {
            return file.getEnumTypeList();
        }
    }

    /**
     * Loads types nested under a message type declaration.
     */
    private static final class NestedTypeLoader extends TypeLoader {

        private final DescriptorProto messageType;

        private NestedTypeLoader(TypeLoader parent, DescriptorProto type) {
            super(parent.cache, protoPrefix(parent, type), javaPrefix(parent, type));
            this.messageType = type;
        }

        private static String protoPrefix(TypeLoader parent, DescriptorProto type) {
            return parent.protoPrefix + type.getName() + '.';
        }

        private static String javaPrefix(TypeLoader parent, DescriptorProto type) {
            return parent.javaPrefix + type.getName() + '.';
        }

        @Override
        List<DescriptorProto> messageTypes() {
            return messageType.getNestedTypeList();
        }

        @Override
        List<EnumDescriptorProto> enumTypes() {
            return messageType.getEnumTypeList();
        }
    }
}
