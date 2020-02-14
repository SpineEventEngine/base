package io.spine.tools.protoc.field;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.FieldFactory;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.FilePatternMatcher;
import io.spine.type.MessageType;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotDefaultArg;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Generates the strongly-typed fields for the passed {@link MessageType} if the type is declared
 * in a file that matches the provided {@linkplain io.spine.tools.protoc.FilePattern pattern}.
 */
final class GenerateFieldsByPattern extends FieldGenerationTask {

    private final FilePatternMatcher patternMatcher;

    GenerateFieldsByPattern(ConfigByPattern config, FieldFactory factory) {
        super(fieldSupertype(checkNotNull(config)), checkNotNull(factory));
        checkNotDefaultArg(config.getPattern());
        this.patternMatcher = new FilePatternMatcher(config.getPattern());
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        if (!patternMatcher.test(type)) {
            return ImmutableList.of();
        }
        return generateFieldsFor(type);
    }

    private static ClassName fieldSupertype(ConfigByPattern config) {
        String typeName = config.getValue();
        checkNotEmptyOrBlank(typeName);
        return ClassName.of(typeName);
    }
}
