package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;

import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * A configuration of strongly-typed message fields to be generated for certain message types.
 *
 * @see io.spine.base.SubscribableField
 */
public final class GeneratedFields extends GeneratedConfigurations<AddFields> {

    private EntityStateConfig entityStateConfig = EntityStateConfig.getDefaultInstance();
    private final Map<String, ClassName> byType = newHashMap();

    /**
     * Configures strongly-typed fields generation for messages that represent an entity state.
     *
     * <p>Example:
     * <pre>
     * generateFor messages().entityState(), markAs("com.some.custom.Field")
     * </pre>
     *
     * <p>The statement above enables the strongly-typed fields generation for all messages that
     * represent an entity state and marks the generated fields as {@code some.custom.Field}.
     *
     * <p>It is expected by the Spine routines that the type passed to the {@code markAs} method is
     * a descendant of {@link io.spine.base.EntityStateField} and has a c-tor accepting a single
     * {@link io.spine.base.Field Field} argument.
     */
    @SuppressWarnings("unused") // Gradle DSL.
    public final void generateFor(EntityState entityState, ClassName markAs) {
        entityStateConfig = EntityStateConfig
                .newBuilder()
                .setValue(markAs.value())
                .build();
    }

    /**
     * Configures the strongly-typed fields generation for messages declared in files matching
     * a given pattern.
     *
     * <p>Example:
     * <pre>
     * generateFor messages().inFiles(suffix: "messages.proto"), markAs("com.some.custom.Field")
     * </pre>
     *
     * <p>The statement above enables strongly-typed fields generation for all messages residing in
     * files with names ending with {@code messages.proto} and marks the generated fields as
     * {@code some.custom.Field}.
     *
     * <p>It is expected by the Spine routines that the type passed to the {@code markAs} method is
     * a descendant of {@link io.spine.base.SubscribableField} and has a c-tor accepting a single
     * {@link io.spine.base.Field Field} argument.
     *
     * <p>In case the field generation is configured for {@link io.spine.base.EventMessage event}
     * and {@link io.spine.base.RejectionMessage rejection} messages, it is expected that the
     * passed field type inherits from {@link io.spine.base.EventMessageField}.
     *
     * <p>The configuration may be applied multiple times to enable code generation for multiple
     * file patterns.
     */
    public final void generateFor(PatternSelector pattern, ClassName markAs) {
        addPattern(pattern, markAs);
    }

    /**
     * Configures the strongly-typed fields generation for a message type with the passed name.
     *
     * <p>Example:
     * <pre>
     * generateFor "custom.message.Type", markAs("com.some.custom.Field")
     * </pre>
     *
     * <p>The statement above enables strongly-typed fields generation for the message type with
     * Proto name {@code custom.message.Type} and marks the generated fields as
     * {@code some.custom.Field}.
     *
     * <p>It is expected by the Spine routines that the type passed to the {@code markAs} method is
     * a descendant of {@link io.spine.base.SubscribableField} and has a c-tor accepting a single
     * {@link io.spine.base.Field Field} argument.
     *
     * <p>The configuration may be applied multiple times to enable code generation for multiple
     * message types.
     */
    public final void generateFor(String type, ClassName markAs) {
        byType.put(type, markAs);
    }

    /**
     * A syntax sugar method used for a more natural Gradle DSL.
     */
    @SuppressWarnings({"MethodMayBeStatic", "unused"}) // Gradle DSL.
    public final ClassName markAs(String type) {
        return ClassName.of(type);
    }

    @Internal
    @Override
    public AddFields asProtocConfig() {
        AddFields.Builder result = AddFields
                .newBuilder()
                .setEntityStateConfig(entityStateConfig)
                .addAllConfigByType(generatedTypes());
        patternConfigurations()
                .stream()
                .map(GeneratedConfigurations::toPatternConfig)
                .forEach(result::addConfigByPattern);
        return result.build();
    }

    private Iterable<ConfigByType> generatedTypes() {
        ImmutableList<ConfigByType> result =
                byType.entrySet()
                      .stream()
                      .map(entry -> configByType(entry.getKey(), entry.getValue()))
                      .collect(toImmutableList());
        return result;
    }

    private static ConfigByType configByType(String type, ClassName markAs) {
        TypePattern pattern = TypePattern
                .newBuilder()
                .setExpectedType(type)
                .build();
        ConfigByType result = ConfigByType
                .newBuilder()
                .setValue(markAs.value())
                .setPattern(pattern)
                .build();
        return result;
    }
}
