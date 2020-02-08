package io.spine.tools.protoc.column;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.ColumnFactory;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.NestedComponent;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.code.proto.ColumnOption.hasColumns;

/**
 * A code generator which adds the strongly-typed columns to a message type.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link io.spine.tools.protoc.InsertionPoint#class_scope class_scope} insertion point.
 *
 * <p>Generates output only for those message types that represent an
 * {@linkplain MessageType#isEntityState() entity state} with
 * {@linkplain io.spine.code.proto.ColumnOption columns}.
 *
 * @see io.spine.base.EntityColumn
 */
public final class ColumnGenerator extends CodeGenerator {

    /**
     * The factory which is used for code generation.
     */
    private final ColumnFactory factory = new ColumnFactory();
    private final boolean generate;

    private ColumnGenerator(boolean generate) {
        super();
        this.generate = generate;
    }

    public static ColumnGenerator instance(SpineProtocConfig config) {
        checkNotNull(config);
        boolean generate = config.getAddColumns()
                                 .getGenerate();
        return new ColumnGenerator(generate);
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        checkNotNull(type);
        if (!generate || !isEntityStateWithColumns(type)) {
            return ImmutableList.of();
        }
        return generateFor((MessageType) type);
    }

    private ImmutableList<CompilerOutput> generateFor(MessageType type) {
        List<GeneratedNestedClass> generatedClasses = factory.createFor(type);
        ImmutableList<CompilerOutput> result =
                generatedClasses.stream()
                                .map(cls -> NestedComponent.from(cls, type))
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
