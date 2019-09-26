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

package io.spine.dart.knowntypes;

import com.google.protobuf.Descriptors;
import io.spine.code.proto.FileName;
import io.spine.code.proto.PackageName;
import io.spine.code.proto.TypeSet;
import io.spine.dart.code.Call;
import io.spine.dart.code.FieldAccess;
import io.spine.dart.code.GeneratedAlias;
import io.spine.dart.code.Import;
import io.spine.dart.code.MapEntry;
import io.spine.dart.code.StringLiteral;
import io.spine.dart.generate.CodeTemplate;
import io.spine.dart.generate.GeneratedDartFile;
import io.spine.type.MessageType;
import io.spine.type.Type;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 *  Builds the Dart source code which registers known Protobuf types.
 */
public final class KnownTypesBuilder {

    private @MonotonicNonNull CodeTemplate template;
    private @MonotonicNonNull TypeSet knownTypes;
    private @MonotonicNonNull String packageName;
    private @MonotonicNonNull Path generatedProtoDir;

    private KnownTypesBuilder() {
    }

    /**
     * Creates a new instance of this builder.
     */
    public static KnownTypesBuilder newBuilder() {
        return new KnownTypesBuilder();
    }

    /**
     * Specifies the {@link CodeTemplate}.
     *
     * <p>The generated Dart code is based on this template.
     */
    public KnownTypesBuilder setTemplate(CodeTemplate template) {
        this.template = checkNotNull(template);
        return this;
    }

    /**
     * Specifies the types to register.
     *
     * <p>Typically, this set should only contain types declared in current project.
     */
    public KnownTypesBuilder setKnownTypes(TypeSet knownTypes) {
        this.knownTypes = checkNotNull(knownTypes);
        return this;
    }

    /**
     * Specifies the name of the Dart package to which these types belong.
     */
    public KnownTypesBuilder setPackageName(String packageName) {
        this.packageName = checkNotNull(packageName);
        return this;
    }

    /**
     * Specifies the base directory which contains Dart code generated for these Protobuf types.
     */
    public KnownTypesBuilder setGeneratedProtoDir(Path generatedProtoDir) {
        this.generatedProtoDir = checkNotNull(generatedProtoDir);
        return this;
    }

    public GeneratedDartFile buildAsSourceFile() {
        checkNotNull(template);
        checkNotNull(knownTypes);
        checkNotNull(packageName);
        checkNotNull(generatedProtoDir);

        fillInImports();
        fillInTypeUrlMap();
        fillInBuilderInfoMap();

        return template.compile();
    }

    private void fillInImports() {
        String imports = knownTypes
                .allTypes()
                .stream()
                .map(Type::declaringFileName)
                .map(this::protoFilePath)
                .map(path -> new Import(packageName, path))
                .map(Import::dartCode)
                .collect(joining(lineSeparator()));
        insert(InsertionPoint.IMPORT, imports);
    }

    private String protoFilePath(FileName file) {
        PackageName packageName = PackageName.of(file.value());
        String packagePath = packageName.asFilePath();
        return generatedProtoDir.resolve(packagePath)
                                .toString();
    }

    private void fillInBuilderInfoMap() {
        String typeToInfo = knownTypes.messageTypes()
                                      .stream()
                                      .map(KnownTypesBuilder::mapTypeUrlToBuilderInfo)
                                      .map(MapEntry::dartCode)
                                      .collect(joining(lineSeparator()));
        insert(InsertionPoint.TYPE_TO_INFO, typeToInfo);
    }

    private static MapEntry mapTypeUrlToBuilderInfo(MessageType type) {
        StringLiteral typeUrlKey = new StringLiteral(type.url()
                                                         .value());
        Descriptors.Descriptor descriptor = type.descriptor();
        PackageName protoPackage = PackageName.of(descriptor);
        Call constructorCall = new Call(
                new GeneratedAlias(protoPackage.asFilePath()),
                descriptor.getName()
        );
        FieldAccess field = new FieldAccess(constructorCall, "info_");
        return new MapEntry(typeUrlKey, field);
    }

    private void fillInTypeUrlMap() {
        String messageToType = knownTypes.messageTypes()
                                         .stream()
                                         .map(KnownTypesBuilder::mapMessageToTypeUrl)
                                         .map(MapEntry::dartCode)
                                         .collect(joining(lineSeparator()));
        insert(InsertionPoint.MESSAGE_TO_TYPE, messageToType);
    }

    private static MapEntry mapMessageToTypeUrl(MessageType type) {
        Descriptors.Descriptor descriptor = type.descriptor();
        PackageName protoPackage = PackageName.of(descriptor);
        Call constructorCall = new Call(
                new GeneratedAlias(protoPackage.asFilePath()),
                descriptor.getName()
        );
        StringLiteral typeUrl = new StringLiteral(type.url()
                                                      .value());
        return new MapEntry(constructorCall, typeUrl);
    }

    private void insert(InsertionPoint point, String content) {
        template.replace(point.key, point.comment + content);
    }

    private enum InsertionPoint {

        IMPORT("import", "Protobuf message imports:"),
        TYPE_TO_INFO("type-to-info", "Map of type URLs to BuilderInfos:"),
        MESSAGE_TO_TYPE("message-to-type", "Map of default message instances to type URLs:");

        private final String key;
        private final String comment;

        InsertionPoint(String key, String comment) {
            this.key = key;
            this.comment = comment + lineSeparator();
        }
    }
}
