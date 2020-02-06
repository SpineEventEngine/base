package io.spine.tools.protoc.columns;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.ColumnFactory;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.NestedMember;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.proto.ColumnOption.hasColumns;

public final class ColumnGenerator extends CodeGenerator {

    private final ColumnFactory factory = new ColumnFactory();
    private final boolean generate;

    private ColumnGenerator(boolean generate) {
        super();
        this.generate = generate;
    }

    public static ColumnGenerator instance(SpineProtocConfig config) {
        boolean generate = config.getAddColumns()
                                 .getGenerate();
        return new ColumnGenerator(generate);
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!generate || !isEntityStateWithColumns(type)) {
            return ImmutableList.of();
        }
        return generateFor((MessageType) type);
    }

    private ImmutableList<CompilerOutput> generateFor(MessageType type) {
        List<GeneratedNestedClass> generatedClasses = factory.createFor(type);
        ImmutableList<CompilerOutput> result =
                generatedClasses.stream()
                                .map(cls -> NestedMember.from(cls, type))
                                .collect(toImmutableList());
        return result;
    }

    private static boolean isEntityStateWithColumns(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return false;
        }
        MessageType messageType = (MessageType) type;
        return messageType.isEntityState() && hasColumns(messageType);
    }

}
