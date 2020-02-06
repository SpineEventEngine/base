package io.spine.tools.protoc.fields;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.FieldFactory;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;

final class GenerateEntityStateFields extends FieldGenerationTask {

    GenerateEntityStateFields(EntityStateConfig config, FieldFactory factory) {
        super(fieldSupertype(checkNotNull(config)), factory);
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        if (!type.isEntityState()) {
            return ImmutableList.of();
        }
        return generateFieldsFor(type);
    }

    private static ClassName fieldSupertype(EntityStateConfig config) {
        String typeName = config.getValue();
        return ClassName.of(typeName);
    }
}
