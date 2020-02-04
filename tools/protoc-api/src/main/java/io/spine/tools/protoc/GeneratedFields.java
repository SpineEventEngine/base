package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;

import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Maps.newHashMap;

public final class GeneratedFields extends GeneratedConfigurations<AddFields> {

    private final Map<String, ClassName> byType = newHashMap();

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
        AddFields result = AddFields
                .newBuilder()
                .addAllByType(generatedTypes())
                .build();
        return result;
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
        ConfigByType result = ConfigByType
                .newBuilder()
                .setType(type)
                .setMarkAs(markAs.value())
                .build();
        return result;
    }
}
