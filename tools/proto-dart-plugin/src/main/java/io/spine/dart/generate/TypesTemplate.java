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

package io.spine.dart.generate;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.PackageName;
import io.spine.code.proto.TypeSet;
import io.spine.dart.code.Call;
import io.spine.dart.code.FieldAccess;
import io.spine.dart.code.GeneratedAlias;
import io.spine.dart.code.Import;
import io.spine.dart.code.MapEntry;
import io.spine.dart.code.Reference;
import io.spine.dart.code.StringLiteral;
import io.spine.io.Resource;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

public final class TypesTemplate {

    private final Template template;
    private final TypeSet protoTypes;

    private TypesTemplate(Template template, TypeSet protoTypes) {
        this.template = template;
        this.protoTypes = protoTypes;
    }

    static TypesTemplate instance(Resource fileTemplate, File descriptorsFile) {
        checkNotNull(fileTemplate);
        checkNotNull(descriptorsFile);

        Template template = new Template(fileTemplate);
        List<FileDescriptorProto> fileDescriptors = FileDescriptors.parse(descriptorsFile);
        TypeSet types = TypeSet.from(FileSet.ofFiles(ImmutableSet.copyOf(fileDescriptors)));
        return new TypesTemplate(template, types);
    }

    // TODO:2019-09-25:dmytro.dashenkov: Naming.
    void addimports(String packageName) {
        String imports = protoTypes
                .allTypes()
                .stream()
                .map(Type::declaringFileName)
                .map(file -> PackageName.of(file.value())
                                        .asFilePath())
                .map(path -> new Import(packageName, path))
                .map(Import::dartCode)
                .collect(joining(lineSeparator()));
        insert(InsertionPoint.IMPORT, imports);
    }

    void addmap1() {
        String typeToInfo = protoTypes.messageTypes()
                                      .stream()
                                      .map(TypesTemplate::mapTypeUrlToBuilderInfo)
                                      .map(MapEntry::dartCode)
                                      .collect(joining(lineSeparator()));
        insert(InsertionPoint.TYPE_TO_INFO, typeToInfo);
    }

    private static MapEntry mapTypeUrlToBuilderInfo(MessageType type) {
        StringLiteral typeUrlKey = new StringLiteral(type.url().value());
        Descriptors.Descriptor descriptor = type.descriptor();
        PackageName protoPackage = PackageName.of(descriptor);
        Call constructorCall = new Call(
                new GeneratedAlias(protoPackage.asFilePath()),
                descriptor.getName()
        );
        FieldAccess field = new FieldAccess(constructorCall, new Reference("info_"));
        return new MapEntry(typeUrlKey, field);
    }

    void addmap2() {
        String messageToType = protoTypes.messageTypes()
                                    .stream()
                                    .map(TypesTemplate::mapMessageToTypeUrl)
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

    void storeAsFile(File directory) {
        SourceFile file = template.compile();
        file.storeIn(directory);
    }

    private void insert(InsertionPoint point, String content) {
        template.insert(point.key, point.comment + content);
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
