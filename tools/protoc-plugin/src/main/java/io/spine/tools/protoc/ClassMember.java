package io.spine.tools.protoc;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.type.MessageType;
import io.spine.value.StringTypeValue;

/**
 * Alters a generated message class with an additional method or nested type.
 *
 * <p>The output is added on the {@link InsertionPoint#class_scope class_scope} insertion point.
 */
public final class ClassMember extends AbstractCompilerOutput {

    private ClassMember(CodeGeneratorResponse.File file) {
        super(file);
    }

    public static ClassMember from(StringTypeValue generatedCode, MessageType messageType) {
        String insertionPoint = InsertionPoint.class_scope.forType(messageType);
        String content = generatedCode.value();
        CodeGeneratorResponse.File.Builder file = ProtocPluginFiles.prepareFile(messageType);
        CodeGeneratorResponse.File result = file.setInsertionPoint(insertionPoint)
                                                .setContent(content)
                                                .build();
        return new ClassMember(result);
    }
}
