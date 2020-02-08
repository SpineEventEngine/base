package io.spine.tools.protoc.field;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.FieldFactory;
import io.spine.tools.protoc.AddFields;
import io.spine.tools.protoc.CodeGenerationTask;
import io.spine.tools.protoc.CodeGenerationTasks;
import io.spine.tools.protoc.CodeGenerator;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.ConfigByType;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.type.MessageType;
import io.spine.type.Type;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.Messages.isNotDefault;

/**
 * A code generator which adds the strongly-typed fields to a message type.
 *
 * <p>The generator produces {@link CompilerOutput compiler output} that fits into the message's
 * {@link io.spine.tools.protoc.InsertionPoint#class_scope class_scope} insertion point.
 *
 * <p>Generates output based on the passed
 * {@linkplain io.spine.tools.protoc.GeneratedFields Protoc config}.
 *
 * @see io.spine.base.SubscribableField
 */
public final class FieldGenerator extends CodeGenerator {

    /**
     * The factory used for code generation.
     */
    private static final FieldFactory factory = new FieldFactory();

    private final CodeGenerationTasks codeGenerationTasks;

    private FieldGenerator(ImmutableList<CodeGenerationTask> tasks) {
        super();
        this.codeGenerationTasks = new CodeGenerationTasks(tasks);
    }

    /**
     * Creates a new instance based on the passed Protoc config.
     */
    @SuppressWarnings("MethodWithMultipleLoops") // Required to configure code generation tasks.
    public static FieldGenerator instance(SpineProtocConfig spineProtocConfig) {
        checkNotNull(spineProtocConfig);
        AddFields config = spineProtocConfig.getAddFields();

        ImmutableList.Builder<CodeGenerationTask> tasks = ImmutableList.builder();
        EntityStateConfig entityStateConfig = config.getEntityStateSupertype();
        if (isNotDefault(entityStateConfig)) {
            tasks.add(new GenerateEntityStateFields(entityStateConfig, factory));
        }
        for (ConfigByPattern byPattern : config.getSupertypeByPatternList()) {
            tasks.add(new GenerateFieldsByPattern(byPattern, factory));
        }
        for (ConfigByType byType : config.getSupertypeByTypeList()) {
            tasks.add(new GenerateFieldsByType(byType, factory));
        }
        return new FieldGenerator(tasks.build());
    }

    @Override
    protected Collection<CompilerOutput> generate(Type<?, ?> type) {
        if (!(type instanceof MessageType)) {
            return ImmutableList.of();
        }
        MessageType messageType = (MessageType) type;
        ImmutableList<CompilerOutput> result = codeGenerationTasks.generateFor(messageType);
        return result;
    }
}
