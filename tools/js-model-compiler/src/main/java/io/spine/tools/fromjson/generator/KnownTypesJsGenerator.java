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

package io.spine.tools.fromjson.generator;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.type.TypeUrl;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static java.lang.System.lineSeparator;

public class KnownTypesJsGenerator {

    // todo create common class with file names
    static final String FILE_NAME = "known_types";
    static final String MAP_NAME = "types";

    private static final String JS_FILE_NAME = FILE_NAME + ".js";
    private final Project project;
    private final FileSet protoJsFiles;

    public KnownTypesJsGenerator(Project project, FileSet protoJsFiles) {
        this.protoJsFiles = protoJsFiles;
        this.project = project;
    }

    public Path composeFilePath() {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path path = Paths.get(absolutePath, "proto", "test", "js", JS_FILE_NAME);
        return path;
    }

    // todo divide into smaller methods.
    public String createFileContent() {
        StringBuilder content = new StringBuilder();

        List<String> imports = new ArrayList<>();
        List<String> mapEntries = new ArrayList<>();
        generateImportsAndMapEntries(imports, mapEntries);

        String importSection = createImportSection(imports);
        content.append(importSection);
        content.append(lineSeparator());
        String map = createKnownTypesMap(mapEntries);
        content.append(map);

        String result = content.toString();
        return result;
    }

    // todo divide into smaller methods.
    @SuppressWarnings("MethodWithMultipleLoops")
    private void generateImportsAndMapEntries(Collection<String> imports, List<String> mapEntries) {
        Set<Entry<FileName, FileDescriptor>> entries = protoJsFiles.getEntries();
        for (Entry<FileName, FileDescriptor> entry : entries) {
            FileName fileName = entry.getKey();
            FileDescriptor fileDescriptor = entry.getValue();

            // Generate JS file name and import name.
            String nameWithoutExtension = fileName.nameWithoutExtension();
            String jsFileName = nameWithoutExtension + "_pb.js";
            String importName = nameWithoutExtension.replace(FileName.PATH_SEPARATOR, '_') + "_pb";

            // Generate import statements.
            List<Descriptor> declaredMessages = fileDescriptor.getMessageTypes();
            int messagesDeclaredCount = declaredMessages.size();
            if (messagesDeclaredCount > 0) {
                String importStatement = importStatement(importName, jsFileName);
                imports.add(importStatement);
            }

            // Generate map entries.
            for (Descriptor messageDescriptor : declaredMessages) {
                TypeUrl typeUrl = TypeUrl.from(messageDescriptor);
                String messageName = messageDescriptor.getName();
                String mapEntry = mapEntry(messageName, typeUrl, importName);
                mapEntries.add(mapEntry);
            }
        }
    }

    private static String createImportSection(Iterable<String> imports) {
        String importSection = String.join(lineSeparator(), imports);
        return importSection;
    }

    private static String createKnownTypesMap(Iterable<String> mapEntries) {
        StringBuilder typesMap = new StringBuilder();
        typesMap.append("export const " + MAP_NAME + " = new Map([")
                .append(lineSeparator());
        String mapEntriesCollected = String.join(',' + lineSeparator(), mapEntries);
        typesMap.append(mapEntriesCollected)
                .append(lineSeparator());
        typesMap.append("]);")
                .append(lineSeparator());
        String result = typesMap.toString();
        return result;
    }

    private static String importStatement(String importName, String fileName) {
        String importStatement = "var " + importName + " = require('./" + fileName + "');";
        return importStatement;
    }

    private static String mapEntry(String messageName, TypeUrl typeUrl, String importName) {
        String mapEntry = "['" + typeUrl + "', " + importName + '.' + messageName + ']';
        return mapEntry;
    }
}
