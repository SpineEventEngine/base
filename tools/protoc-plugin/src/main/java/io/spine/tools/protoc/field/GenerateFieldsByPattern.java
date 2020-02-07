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

final class GenerateFieldsByPattern extends FieldGenerationTask {

    private final FilePatternMatcher patternMatcher;

    GenerateFieldsByPattern(ConfigByPattern config, FieldFactory factory) {
        super(fieldSupertype(checkNotNull(config)), checkNotNull(factory));
        checkNotDefaultArg(config.getPattern());
        this.patternMatcher = new FilePatternMatcher(config.getPattern());
    }

    /**
     * Generates fields for the given type.
     *
     * <p>No code is generated if the type file name does not match the supplied
     * {@link io.spine.tools.protoc.FilePattern pattern}.
     */
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
        return ClassName.of(typeName);
    }
}
