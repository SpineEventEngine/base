package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;

import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Maps.newHashMap;

public final class GeneratedFields extends GeneratedConfigurations<AddFields> {

    private boolean generate;
    private EntityStateConfig entityStateConfig = EntityStateConfig.getDefaultInstance();
    private final Map<String, ClassName> byType = newHashMap();

    public final void generate(boolean generate) {
        this.generate = generate;
    }

    public final void generateFor(EntityState entityState, ClassName markAs) {
        entityStateConfig = EntityStateConfig
                .newBuilder()
                .setValue(markAs.value())
                .build();
    }

    public final void generateFor(PatternSelector pattern, ClassName markAs) {
        addPattern(pattern, markAs);
    }

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
                .setGenerate(generate)
                .setEntityStateSupertype(entityStateConfig)
                .addAllSupertypeByType(generatedTypes());
        patternConfigurations()
                .stream()
                .map(GeneratedConfigurations::toPatternConfig)
                .forEach(result::addSupertypeByPattern);
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
