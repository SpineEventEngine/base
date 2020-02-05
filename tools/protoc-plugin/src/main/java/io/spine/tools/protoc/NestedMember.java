package io.spine.tools.protoc;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import io.spine.type.MessageType;
import io.spine.value.StringTypeValue;

public final class NestedMember extends AbstractCompilerOutput {

    private NestedMember(CodeGeneratorResponse.File file) {
        super(file);
    }

    /**
     * Creates a new instance of {@code NestedClass}.
     */
    public static NestedMember from(StringTypeValue generatedCode, MessageType messageType) {
        String insertionPoint = InsertionPoint.class_scope.forType(messageType);
        String content = generatedCode.value();
        CodeGeneratorResponse.File.Builder file = ProtocPluginFiles.prepareFile(messageType);
        CodeGeneratorResponse.File result = file.setInsertionPoint(insertionPoint)
                                                .setContent(content)
                                                .build();
        return new NestedMember(result);
    }
}
