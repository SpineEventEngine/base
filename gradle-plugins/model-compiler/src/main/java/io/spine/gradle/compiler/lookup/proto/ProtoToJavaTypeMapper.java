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
package io.spine.gradle.compiler.lookup.proto;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.gradle.compiler.util.JavaCode.getOuterClassName;
import static io.spine.option.OptionsProto.TYPE_URL_PREFIX_FIELD_NUMBER;
import static io.spine.protobuf.UnknownOptions.getUnknownOptionValue;

/**
 * Maps Protobuf message types from a {@code .proto} file to the corresponding Java classes.
 *
 * @author Alexander Litus
 * @author Alex Tymchenko
 */
public class ProtoToJavaTypeMapper {

    private static final String JAVA_INNER_CLASS_SEPARATOR = "$";

    /** A separator used in Protobuf type names and Java packages. */
    private static final String DOT = ".";

    private static final String GOOGLE_TYPE_URL_PREFIX = "type.googleapis.com";
    private static final String PROTO_TYPE_URL_SEPARATOR = "/";

    private final FileDescriptorProto file;

    private final String protoPackagePrefix;
    private final String javaPackagePrefix;
    private final String typeUrlPrefix;
    private final String commonOuterClassPrefix;

    ProtoToJavaTypeMapper(FileDescriptorProto file) {
        this.file = file;
        this.protoPackagePrefix = getProtoPackagePrefix(file);
        this.javaPackagePrefix = getJavaPackagePrefix(file);
        this.typeUrlPrefix = getTypeUrlPrefix(file);
        this.commonOuterClassPrefix = getCommonOuterJavaClassPrefix(file);
    }

    /**
     * Returns a map from Protobuf type url to the corresponding
     * fully-qualified Java class name.
     */
    public Map<String, String> mapTypes() {
        log().debug("Mapping file {}", file.getName());
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        putMessageEntries(file.getMessageTypeList(), builder, new LinkedList<String>());
        putEnumEntries(file.getEnumTypeList(), builder, new LinkedList<String>());
        return builder.build();
    }

    private void putMessageEntries(Iterable<DescriptorProto> messages,
                                   ImmutableMap.Builder<String, String> builder,
                                   Collection<String> parentMsgNames) {
        log().debug("Obtaining the messages");
        for (DescriptorProto msg : messages) {
            if (!isGeneratedMapEntryMsg(msg, parentMsgNames)) {
                log().debug("Found message {}", msg.getName());
                putMessageEntry(builder, msg, parentMsgNames);
            }
        }
    }

    /**
     * Returns true if the message is generated map entry type, and thus is not user-defined type.
     *
     * <p>This happens if a field of type {@code map} is used in a message. For example:
     *
     * <pre>
     * message Outer {
     *     map&lt;string, int32&gt; my_map = 1;
     * }
     * </pre>
     *
     * In this case, the descriptor of the {@code Outer} message will contain an inner type
     * {@code MyMap}, which will contain two fields: {@code string} {@code key} and
     * {@code int32} {@code value}.
     *
     * @param message        a message to check
     * @param parentMsgNames names of the parent messages
     * @return true if the message is generated map entry type
     */
    private static boolean isGeneratedMapEntryMsg(DescriptorProto message,
                                                  Collection<String> parentMsgNames) {
        final List<FieldDescriptorProto> fields = message.getFieldList();
        final boolean endWithEntry = message.getName()
                                            .endsWith("Entry");
        final boolean hasTwoFieldsKeyAndValue =
                (fields.size() == 2) &&
                firstIsKey(fields) &&
                secondIsValue(fields);
        final boolean result = endWithEntry && hasTwoFieldsKeyAndValue && !parentMsgNames.isEmpty();
        return result;
    }

    private static boolean secondIsValue(List<FieldDescriptorProto> fields) {
        return "value".equals(fields.get(1)
                                    .getName());
    }

    private static boolean firstIsKey(List<FieldDescriptorProto> fields) {
        return "key".equals(fields.get(0)
                                  .getName());
    }

    /**
     * Puts an entry for this message to the map builder.
     * Then puts entries for all inner messages and enums of this message.
     */
    private void putMessageEntry(ImmutableMap.Builder<String, String> builder,
                                 DescriptorProto msg,
                                 Collection<String> parentMsgNames) {
        final List<String> parentMsgNamesCopy = newLinkedList(parentMsgNames);
        putEntry(msg.getName(), builder, parentMsgNames);

        parentMsgNamesCopy.add(msg.getName());

        final List<DescriptorProto> messagesNested = msg.getNestedTypeList();
        if (!messagesNested.isEmpty()) {
            putMessageEntries(messagesNested, builder, parentMsgNamesCopy);
        }
        final List<EnumDescriptorProto> enumsNested = msg.getEnumTypeList();
        if (!enumsNested.isEmpty()) {
            putEnumEntries(enumsNested, builder, parentMsgNamesCopy);
        }
    }

    private void putEnumEntries(Iterable<EnumDescriptorProto> enums,
                                ImmutableMap.Builder<String, String> builder,
                                Collection<String> parentMsgNames) {
        log().debug("Obtaining the enums");
        for (EnumDescriptorProto msg : enums) {
            log().debug("Found enum {}", msg.getName());
            putEntry(msg.getName(), builder, parentMsgNames);
        }
    }

    private void putEntry(String typeName,
                          ImmutableMap.Builder<String, String> builder,
                          Collection<String> parentMsgNames) {
        final String typeUrl = getTypeUrl(typeName, parentMsgNames);
        final String javaClassName = getJavaClassName(typeName, parentMsgNames);
        builder.put(typeUrl, javaClassName);
    }

    private String getTypeUrl(String typeName, Collection<String> parentMsgNames) {
        final String parentMessagesPrefix = getParentTypesPrefix(parentMsgNames, DOT);
        final String result = typeUrlPrefix + protoPackagePrefix + parentMessagesPrefix + typeName;
        return result;
    }

    private String getJavaClassName(String typeName, Collection<String> parentTypeNames) {
        final String parentClassesPrefix = getParentTypesPrefix(parentTypeNames,
                                                                JAVA_INNER_CLASS_SEPARATOR);
        final String result = javaPackagePrefix + commonOuterClassPrefix + parentClassesPrefix +
                        typeName;
        return result;
    }

    private static String getProtoPackagePrefix(FileDescriptorProto file) {
        final String trimmedPackage = file.getPackage()
                                          .trim();
        final String result = trimmedPackage.isEmpty()
                              ? ""
                              : (file.getPackage() + DOT);
        return result;
    }

    private static String getJavaPackagePrefix(FileDescriptorProto file) {
        final String trimmedPackage = file.getOptions()
                                          .getJavaPackage()
                                          .trim();
        final String result = trimmedPackage.isEmpty()
                              ? ""
                              : (trimmedPackage + DOT);
        return result;
    }

    private static String getTypeUrlPrefix(FileDescriptorProto file) {
        final String typeUrlPrefix = getUnknownOptionValue(file, TYPE_URL_PREFIX_FIELD_NUMBER);
        final String prefix = isNullOrEmpty(typeUrlPrefix) ? GOOGLE_TYPE_URL_PREFIX : typeUrlPrefix;
        final String result = (prefix + PROTO_TYPE_URL_SEPARATOR);
        return result;
    }

    private static String getCommonOuterJavaClassPrefix(FileDescriptorProto file) {
        final boolean inSeparateFiles = file.getOptions()
                                            .getJavaMultipleFiles();
        if (inSeparateFiles) {
            return "";
        }

        final String outerClassName = getOuterClassName(file);
        return outerClassName.isEmpty()
                ? ""
                : (outerClassName + JAVA_INNER_CLASS_SEPARATOR);
    }

    private static String getParentTypesPrefix(Collection<String> parentTypeNames,
                                               String separator) {
        if (parentTypeNames.isEmpty()) {
            return "";
        }
        final String result = Joiner.on(separator)
                                    .join(parentTypeNames) + separator;
        return result;
    }

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(ProtoToJavaTypeMapper.class);
    }

}
