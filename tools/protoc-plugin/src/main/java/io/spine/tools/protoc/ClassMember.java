package io.spine.tools.protoc;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.type.MessageType;

/**
 * A compiler output which alters a generated message with an additional method or nested type.
 *
 * <p>The output is added on the {@link InsertionPoint#class_scope class_scope} insertion point.
 */
public final class ClassMember extends AbstractCompilerOutput {

    private ClassMember(CodeGeneratorResponse.File file) {
        super(file);
    }

    public static ClassMember method(GeneratedMethod generatedMethod, MessageType messageType) {
        CodeGeneratorResponse.File response =
                codeGeneratorResponse(generatedMethod.toString(), messageType);
        return new ClassMember(response);
    }

    public static ClassMember nestedClass(GeneratedNestedClass generatedNestedClass,
                                          MessageType messageType) {
        CodeGeneratorResponse.File response =
                codeGeneratorResponse(generatedNestedClass.toString(), messageType);
        return new ClassMember(response);
    }

    private static CodeGeneratorResponse.File
    codeGeneratorResponse(String content, MessageType messageType) {
        String insertionPoint = InsertionPoint.class_scope.forType(messageType);
        CodeGeneratorResponse.File.Builder file = ProtocPluginFiles.prepareFile(messageType);
        CodeGeneratorResponse.File result = file.setInsertionPoint(insertionPoint)
                                                .setContent(content)
                                                .build();
        return result;
    }
}
