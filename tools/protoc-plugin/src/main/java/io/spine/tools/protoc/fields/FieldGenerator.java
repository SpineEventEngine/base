package io.spine.tools.protoc.fields;

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.Type;

import java.util.Collection;

public final class FieldGenerator extends CodeGenerator {


    private FieldGenerator() {
    }

    public static FieldGenerator instance(SpineProtocConfig config) {
        return new FieldGenerator();
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        return ImmutableList.of();
    }
}
