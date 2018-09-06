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

package io.spine.tools.fromjson;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.NullValue;
import com.google.protobuf.Value;
import io.spine.code.proto.FieldName;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.tools.fromjson.js.KnownTypeParsersCode;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static io.spine.tools.gradle.TaskName.ADD_FROM_JSON;
import static io.spine.tools.gradle.TaskName.COMPILE_PROTO_TO_JS;
import static io.spine.tools.gradle.TaskName.COPY_MODULE_SOURCES;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.gradle.internal.impldep.org.apache.commons.lang.CharEncoding.UTF_8;

@SuppressWarnings({"Duplicates", "DuplicateStringLiteralInspection"})
public class GenerateFromJsonPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        System.out.println("Applying Js Model Compiler");
        Action<Task> testTask = newActionTest(project);
        newTask(ADD_FROM_JSON, testTask)
                .insertAfterTask(COMPILE_PROTO_TO_JS)
                .insertBeforeTask(COPY_MODULE_SOURCES)
                .applyNowTo(project);
    }

    private Action<Task> newActionTest(Project project) {
        return task -> {
            System.out.println("In Js Model Compiler test action");

            File projectDir = project.getProjectDir();
            String absolutePath = projectDir.getAbsolutePath();
            Path path = Paths.get(absolutePath, "build", "descriptors", "test", "known_types.desc");
            generateKnownTypesJs(path, project);
            generateKnownTypeParsers(path, project);
            insertIntoFiles(path, project);
        };
    }

    private static void generateKnownTypesJs(Path descriptorPath, Project project) {
        if (!Files.exists(descriptorPath)) {
            return;
        }

        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path knownTypesJsPath = Paths.get(absolutePath, "proto", "test", "js", "known_types.js");

        StringBuilder content = new StringBuilder();

        File setFile = new File(descriptorPath.toUri()
                                              .getPath());
        Collection<FileDescriptorProto> descriptors =
                FileDescriptors.parseSkipStandard(setFile.getPath());

        List<String> mapEntries = new ArrayList<>();

        for (FileDescriptorProto fileDescriptor : descriptors) {
            FileName fileName = FileName.from(fileDescriptor);
            String nameWithoutExtension = fileName.nameWithoutExtension() + "_pb.js";
            String importName =
                    fileName.nameWithoutExtension()
                            .replace(FileName.PATH_SEPARATOR, '_') + "_pb";
            if (fileDescriptor.getMessageTypeCount() > 0) {
                content.append(
                        "let " + importName + " = require('./" + nameWithoutExtension + "');")
                       .append(lineSeparator());
            }
            for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
                TypeUrl typeUrl = TypeUrl.ofMessage(fileDescriptor, messageDescriptor);

                String mapEntry = "['" + typeUrl.toString() + "', " + importName + '.' +
                        messageDescriptor.getName() + ']';
                mapEntries.add(mapEntry);
            }
        }

        content.append(lineSeparator());

        content.append("export const types = new Map([")
               .append(lineSeparator());
        for (Iterator<String> iter = mapEntries.iterator(); iter.hasNext(); ) {
            String mapEntry = iter.next();
            content.append(mapEntry);
            if (iter.hasNext()) {
                content.append(',');
            }
            content.append(lineSeparator());
        }
        content.append("]);")
               .append(lineSeparator());

        try {
            Files.write(knownTypesJsPath, content.toString()
                                                 .getBytes(UTF_8), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("IO Exception when writing known_types.js");
        }
    }

    private static final List<String> knownParserTypes = knownParserTypes();

    private static List<String> knownParserTypes() {
        List<String> types = new ArrayList<>();
        types.add("google.protobuf.Duration");
        return types;
    }

    private void generateKnownTypeParsers(Path descriptorPath, Project project) {
        if (!Files.exists(descriptorPath)) {
            // No proto messages defined.
            return;
        }

        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path knownTypeParsersPath = Paths.get(absolutePath, "proto", "test", "js",
                                              "known_type_parsers.js");
        try {
            Files.write(knownTypeParsersPath, KnownTypeParsersCode.get()
                                                                  .getBytes(UTF_8), CREATE,
                        TRUNCATE_EXISTING);
        } catch (IOException ex) {
            System.out.println("IO Exception when writing known_type_parsers.js: " + ex);
        }
    }

    // todo try to use full type names instead of getting them from imports
    // todo check if we need generate fromJson for Google Protobuf classes
    private static void insertIntoFiles(Path descriptorPath, Project project) {
        if (!Files.exists(descriptorPath)) {
            return;
        }

        FileSet fileSet = FileSet.parse(descriptorPath.toFile());
        TypeSet types = TypeSet.messagesAndEnums(fileSet);

        File setFile = new File(descriptorPath.toUri()
                                              .getPath());
        Collection<FileDescriptorProto> descriptors =
                FileDescriptors.parseSkipStandard(setFile.getPath());
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path protoPath = Paths.get(absolutePath, "proto", "test", "js");
        for (FileDescriptorProto fileDescriptor : descriptors) {
            FileName fileName = FileName.from(fileDescriptor);
            String nameWithoutExtension = fileName.nameWithoutExtension();
            Path fullPath = Paths.get(protoPath.toString(), nameWithoutExtension);
            String fullJsPathString = fullPath + "_pb.js";
            Path fullJsPath = Paths.get(fullJsPathString);

            String descriptorPackage = fileDescriptor.getPackage();

            if (!descriptorPackage.isEmpty()) {
                descriptorPackage += ".";
            }

            String knownTypeParsersImport = lineSeparator() + "let known_type_parsers = require('";

            String filePath = fileDescriptor.getName();
            String[] split = filePath.split("/");

            // One of the words is the file name itself, and not the folder we should skip with "../".
            for (int i = 0; i < split.length - 1; i++) {
                knownTypeParsersImport += "../";
            }

            knownTypeParsersImport += "known_type_parsers.js');" + lineSeparator();

            String knownTypesImport = lineSeparator() + "let known_types = require('";

            String filePath1 = fileDescriptor.getName();
            String[] split1 = filePath1.split("/");

            // One of the words is the file name itself, and not the folder we should skip with "../".
            for (int i = 0; i < split1.length - 1; i++) {
                knownTypesImport += "../";
            }

            knownTypesImport += "known_types.js');" + lineSeparator();
            try {
                Files.write(fullJsPath, knownTypeParsersImport.getBytes(UTF_8), APPEND);
                Files.write(fullJsPath, knownTypesImport.getBytes(UTF_8), APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
                String protoName = messageDescriptor.getName();
                String fullTypeName = "proto." + descriptorPackage + protoName;

                String parseJsonObject = "  let jsonObject = JSON.parse(json);";
                String returnFromObject = "  return " + fullTypeName + ".fromObject(jsonObject);";
                String fromJsonFunction =
                        ".fromJson = function(json) {" + lineSeparator()
                                + parseJsonObject + lineSeparator()
                                + returnFromObject + lineSeparator()
                                + "};" + lineSeparator();
                String fromJson = lineSeparator() + fullTypeName + fromJsonFunction;

                StringBuilder fromObjectFunction = new StringBuilder(
                        ".fromObject = function(obj) {" + lineSeparator());
                fromObjectFunction.append("  let message = new " + fullTypeName + "();")
                                  .append(lineSeparator());

                TypeUrl typeUrl = TypeUrl.ofMessage(fileDescriptor, messageDescriptor);
                TypeName from = TypeName.from(typeUrl);
                Optional<io.spine.code.proto.Type<?, ?>> messageTypeOptional = types.find(from);
                io.spine.code.proto.Type<?, ?> messageType = messageTypeOptional.get();
                GenericDescriptor genericDescriptor = messageType.descriptor();
                Descriptor descriptor = (Descriptor) genericDescriptor;
                List<FieldDescriptor> fields = descriptor.getFields();
                for (FieldDescriptor fieldDescriptor : fields) {
                    if ("proto.spine.web.test.CreateTask".equals(fullTypeName)) {
                        // todo check it works okay if we set original field name usage for JSON in Proto compiler.
                        fromObjectFunction.append(lineSeparator());
                        String jsonName = fieldDescriptor.getJsonName();
                        String jsonObject = "obj." + jsonName;

                        Label label = fieldDescriptor.toProto()
                                                     .getLabel();
                        boolean isNullValue = fieldDescriptor.getType() == ENUM &&
                                fieldDescriptor.getEnumType()
                                               .getFullName()
                                               .equals(NullValue.getDescriptor()
                                                                .getFullName());
                        // Null is allowed for repeated, but when it's null we just do nothing
                        if ((fieldDescriptor.getType() == MESSAGE && label != LABEL_REPEATED) ||
                                isNullValue) {
                            // Null is allowed for messages and repeated/map values.
                            fromObjectFunction.append("  if (" + jsonObject + " !== undefined) {")
                                              .append(lineSeparator());
                        } else {
                            fromObjectFunction.append(
                                    "  if (" + jsonObject + " !== undefined && " + jsonObject +
                                            " !== null) {")
                                              .append(lineSeparator());
                        }
                        String name = fieldDescriptor.getName();
                        FieldName fieldName = FieldName.of(name);
                        String camelCaseName = fieldName.toCamelCase();
                        String capitalizedName =
                                camelCaseName.substring(0, 1)
                                             .toUpperCase() + camelCaseName.substring(1);
                        if (label == LABEL_REPEATED) {
                            if (fieldDescriptor.getType() == MESSAGE) {
                                Descriptor fieldType = fieldDescriptor.getMessageType();
                                String aPackage = fileDescriptor.getPackage();
                                if (!aPackage.isEmpty()) {
                                    aPackage += '.';
                                }
                                String typeName = aPackage + messageDescriptor.getName();
                                String supposedTypeName =
                                        typeName + '.' + capitalizedName + "Entry";
                                boolean isMapType = fieldType.getFullName()
                                                             .equals(supposedTypeName);
                                if (isMapType) {
                                    fromObjectFunction.append(
                                            "    for (let attrname in " + jsonObject + ") {")
                                                      .append(lineSeparator());
                                    fromObjectFunction.append("      if (" + jsonObject +
                                                                      ".hasOwnProperty(attrname)) {")
                                                      .append(lineSeparator());
                                    FieldDescriptor valueDescriptor =
                                            fieldDescriptor.getMessageType()
                                                           .findFieldByName("value");

                                    if (valueDescriptor.getType() == MESSAGE) {
                                        fromObjectFunction.append("        if (" + jsonObject +
                                                                          "[attrname] === null) {")
                                                          .append(lineSeparator());
                                        String setStatement =
                                                "          message.get" + capitalizedName +
                                                        "Map().set(attrname, null);";
                                        fromObjectFunction.append(setStatement)
                                                          .append(lineSeparator());
                                        fromObjectFunction.append("        } else {")
                                                          .append(lineSeparator());
                                    } else {
                                        // Just check it's not null.
                                        fromObjectFunction.append("        if (" + jsonObject +
                                                                          "[attrname] !== null) {")
                                                          .append(lineSeparator());
                                    }

                                    setMapField(fromObjectFunction, fieldDescriptor,
                                                valueDescriptor, jsonObject);

                                    fromObjectFunction.append("        }")
                                                      .append(lineSeparator());

                                    fromObjectFunction.append("      }")
                                                      .append(lineSeparator());
                                    fromObjectFunction.append("    }")
                                                      .append(lineSeparator());
                                } else {
                                    // Is a list.
                                    fromObjectFunction.append("    " + jsonObject + ".forEach(")
                                                      .append(lineSeparator());
                                    fromObjectFunction.append("      (listItem, index, array) => {")
                                                      .append(lineSeparator());
                                    if (fieldDescriptor.getType() == MESSAGE) {
                                        fromObjectFunction.append(
                                                "        if (listItem === null) {")
                                                          .append(lineSeparator());
                                        String setterName = "add" + capitalizedName;
                                        fromObjectFunction.append(
                                                "          message." + setterName + "(null);")
                                                          .append(lineSeparator());
                                        fromObjectFunction.append("        } else {")
                                                          .append(lineSeparator());
                                    } else {
                                        // Just check it's not null.
                                        fromObjectFunction.append(
                                                "        if (listItem !== null) {")
                                                          .append(lineSeparator());

                                    }
                                    setField(fromObjectFunction, fieldDescriptor, "listItem",
                                             "add");
                                    fromObjectFunction.append("        }")
                                                      .append(lineSeparator());
                                    fromObjectFunction.append("      }")
                                                      .append(lineSeparator());
                                    fromObjectFunction.append("    );")
                                                      .append(lineSeparator());
                                }
                            } else {
                                // Is a list.
                                fromObjectFunction.append("    " + jsonObject + ".forEach(")
                                                  .append(lineSeparator());
                                fromObjectFunction.append("      (listItem, index, array) => {")
                                                  .append(lineSeparator());
                                if (fieldDescriptor.getType() == MESSAGE) {
                                    fromObjectFunction.append(
                                            "        if (listItem === null) {")
                                                      .append(lineSeparator());
                                    String setterName = "add" + capitalizedName;
                                    fromObjectFunction.append(
                                            "          message." + setterName + "(null);")
                                                      .append(lineSeparator());
                                    fromObjectFunction.append("        } else {")
                                                      .append(lineSeparator());
                                } else {
                                    // Just check it's not null.
                                    fromObjectFunction.append(
                                            "        if (listItem !== null) {")
                                                      .append(lineSeparator());

                                }
                                setField(fromObjectFunction, fieldDescriptor, "listItem",
                                         "add");
                                fromObjectFunction.append("        }")
                                                  .append(lineSeparator());
                                fromObjectFunction.append("      }")
                                                  .append(lineSeparator());
                                fromObjectFunction.append("    );")
                                                  .append(lineSeparator());
                            }
                        } else {
                            //todo
                            // 1. For Value and ListValue and NullValue don't set null directly and allow it to reach parser.
                            if (fieldDescriptor.getType() == MESSAGE &&
                                    !fieldDescriptor.getMessageType()
                                                    .getFullName()
                                                    .equals(Value.getDescriptor()
                                                                 .getFullName())) {
                                fromObjectFunction.append("    if (" + jsonObject + " === null) {")
                                                  .append(lineSeparator());
                                // Just call setter.
                                String setterName = "set" + capitalizedName;
                                fromObjectFunction.append("      message." + setterName + "(null);")
                                                  .append(lineSeparator());
                                fromObjectFunction.append("    } else {")
                                                  .append(lineSeparator());
                            }
                            if (isNullValue) {
                                EnumDescriptor enumType = fieldDescriptor.getEnumType();
                                TypeUrl url = TypeUrl.from(enumType);
                                fromObjectFunction.append(
                                        "    let type = known_types.types.get('" + url.toString() +
                                                "');")
                                                  .append(lineSeparator());
                                fromObjectFunction.append(
                                        "    let parser = known_type_parsers.parsers.get(type);")
                                                  .append(lineSeparator());
                                fromObjectFunction.append(
                                        "    let value = parser.parse(" + jsonObject + ");")
                                                  .append(lineSeparator());
                                String setterName = "set" + capitalizedName;
                                fromObjectFunction.append("    message." + setterName + "(value);")
                                                  .append(lineSeparator());
                            } else {
                                setField(fromObjectFunction, fieldDescriptor, jsonObject, "set");
                            }
                            if (fieldDescriptor.getType() == MESSAGE &&
                                    !fieldDescriptor.getMessageType()
                                                    .getFullName()
                                                    .equals(Value.getDescriptor()
                                                                 .getFullName())) {
                                fromObjectFunction.append("    }")
                                                  .append(lineSeparator());
                            }
                        }
                        fromObjectFunction.append("  }")
                                          .append(lineSeparator());
                    }
                }
                fromObjectFunction.append(lineSeparator());
                fromObjectFunction.append("  return message;")
                                  .append(lineSeparator());

                fromObjectFunction.append("};")
                                  .append(lineSeparator());

                String fromObject = lineSeparator() + fullTypeName + fromObjectFunction;

                String content = fromJson + fromObject;

                try {
                    Files.write(fullJsPath, content.getBytes(UTF_8), APPEND);
                } catch (IOException e) {
                    System.out.println("IO Exception");
                }
            }
        }
    }

    private static void setField(StringBuilder fromObjectFunction,
                                 FieldDescriptor fieldDescriptor,
                                 String object,
                                 String setFunction) {
        Type type = fieldDescriptor.getType();
        String indent = "";
        if ("add".equals(setFunction)) {
            indent = "      ";
        }
        if (type == MESSAGE) {
            boolean isValue = fieldDescriptor.getMessageType()
                                             .getFullName()
                                             .equals(Value.getDescriptor()
                                                          .getFullName());
            if ("set".equals(setFunction) && !isValue) {
                indent += "  ";
            }
            Descriptor fieldType = fieldDescriptor.getMessageType();
            TypeUrl url = TypeUrl.from(fieldType);
            if (knownParserTypes.contains(url.getTypeName())) {
                fromObjectFunction.append(
                        indent + "    let type = known_types.types.get('" + url.toString() + "');")
                                  .append(lineSeparator());
                fromObjectFunction.append(
                        indent + "    let parser = known_type_parsers.parsers.get(type);")
                                  .append(lineSeparator());
                fromObjectFunction.append(indent + "    let value = parser.parse(" + object + ");")
                                  .append(lineSeparator());
                String name = fieldDescriptor.getName();
                FieldName fieldName = FieldName.of(name);
                String camelCaseName = fieldName.toCamelCase();
                String capitalizedName =
                        camelCaseName.substring(0, 1)
                                     .toUpperCase() +
                                camelCaseName.substring(1);
                String setterName = setFunction + capitalizedName;
                fromObjectFunction.append(
                        indent + "    message." + setterName + "(value);")
                                  .append(lineSeparator());
            } else {
                fromObjectFunction.append(
                        indent + "    let type = known_types.types.get('" +
                                url.toString() + "');")
                                  .append(lineSeparator());
                fromObjectFunction.append(
                        indent + "    let value = type.fromObject(" +
                                object + ");")
                                  .append(lineSeparator());
                String name = fieldDescriptor.getName();
                FieldName fieldName = FieldName.of(name);
                String camelCaseName = fieldName.toCamelCase();
                String capitalizedName =
                        camelCaseName.substring(0, 1)
                                     .toUpperCase() +
                                camelCaseName.substring(1);
                String setterName = setFunction + capitalizedName;
                fromObjectFunction.append(
                        indent + "    message." + setterName + "(value);")
                                  .append(lineSeparator());
            }
        } else {
            String name = fieldDescriptor.getName();
            FieldName fieldName = FieldName.of(name);
            String camelCaseName = fieldName.toCamelCase();
            String capitalizedName = camelCaseName.substring(0, 1)
                                                  .toUpperCase() +
                    camelCaseName.substring(1);
            String setterName = setFunction + capitalizedName;
            fromObjectFunction.append(
                    indent + "    message." + setterName + '(' + object + ");")
                              .append(lineSeparator());
        }
    }

    private static void setMapField(StringBuilder fromObjectFunction,
                                    FieldDescriptor fieldDescriptor,
                                    FieldDescriptor valueDescriptor,
                                    String jsonObject) {
        Type type = valueDescriptor.getType();
        String indent = "          ";
        if (type == MESSAGE) {
            Descriptor valueType = valueDescriptor.getMessageType();
            TypeUrl url = TypeUrl.from(valueType);
            if (knownParserTypes.contains(url.getTypeName())) {
                fromObjectFunction.append(
                        indent + "let type = known_types.types.get('" + url.toString() + "');")
                                  .append(lineSeparator());
                fromObjectFunction.append(
                        indent + "let parser = known_type_parsers.parsers.get(type);")
                                  .append(lineSeparator());
                fromObjectFunction.append(
                        indent + "let value = parser.parse(" + jsonObject + "[attrname]);")
                                  .append(lineSeparator());
                String name = fieldDescriptor.getName();
                FieldName fieldName = FieldName.of(name);
                String camelCaseName = fieldName.toCamelCase();
                String capitalizedName =
                        camelCaseName.substring(0, 1)
                                     .toUpperCase() +
                                camelCaseName.substring(1);
                String setStatement =
                        "message.get" + capitalizedName + "Map().set(attrname, value);";
                fromObjectFunction.append(indent + setStatement)
                                  .append(lineSeparator());
            } else {
                fromObjectFunction.append(
                        indent + "let type = known_types.types.get('" + url.toString() + "');")
                                  .append(lineSeparator());
                fromObjectFunction.append(
                        indent + "let value = type.fromObject(" + jsonObject + "[attrname]);")
                                  .append(lineSeparator());
                String name = fieldDescriptor.getName();
                FieldName fieldName = FieldName.of(name);
                String camelCaseName = fieldName.toCamelCase();
                String capitalizedName =
                        camelCaseName.substring(0, 1)
                                     .toUpperCase() +
                                camelCaseName.substring(1);
                String setStatement =
                        "message.get" + capitalizedName + "Map().set(attrname, value);";
                fromObjectFunction.append(indent + setStatement)
                                  .append(lineSeparator());
            }
        } else {
            String name = fieldDescriptor.getName();
            FieldName fieldName = FieldName.of(name);
            String camelCaseName = fieldName.toCamelCase();
            String capitalizedName = camelCaseName.substring(0, 1)
                                                  .toUpperCase() +
                    camelCaseName.substring(1);
            String setStatement =
                    "message.get" + capitalizedName + "Map().set(attrname, " + jsonObject +
                            "[attrname]);";
            fromObjectFunction.append(indent + setStatement)
                              .append(lineSeparator());
        }
    }
}
