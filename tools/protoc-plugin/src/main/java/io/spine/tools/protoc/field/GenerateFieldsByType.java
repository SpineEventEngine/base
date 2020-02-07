package io.spine.tools.protoc.field;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.FieldFactory;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ConfigByType;
import io.spine.type.MessageType;
import io.spine.type.TypeName;

import static com.google.common.base.Preconditions.checkNotNull;

final class GenerateFieldsByType extends FieldGenerationTask {

    private final TypeName expectedType;

    GenerateFieldsByType(ConfigByType config, FieldFactory factory) {
        super(fieldSupertype(checkNotNull(config)), factory);
        this.expectedType = expectedType(config);
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        boolean isExpectedType = expectedType.equals(type.name());
        if (!isExpectedType) {
            return ImmutableList.of();
        }
        return generateFieldsFor(type);
    }

    private static ClassName fieldSupertype(ConfigByType config) {
        String typeName = config.getValue();
        return ClassName.of(typeName);
    }

    private static TypeName expectedType(ConfigByType config) {
        return TypeName.of(config.getPattern().getExpectedType());
    }
}
