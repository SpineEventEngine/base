package io.spine.tools.protoc;

import io.spine.annotation.Internal;

public final class GeneratedColumns extends GeneratedConfigurations<AddColumns> {

    private boolean generate = false;

    public final void generate(boolean generate) {
        this.generate = generate;
    }

    @Internal
    @Override
    public AddColumns asProtocConfig() {
        AddColumns result = AddColumns
                .newBuilder()
                .setGenerate(generate)
                .build();
        return result;
    }
}
