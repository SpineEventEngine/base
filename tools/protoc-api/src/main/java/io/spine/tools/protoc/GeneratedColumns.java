package io.spine.tools.protoc;

import io.spine.annotation.Internal;

/**
 * A configuration of strongly-typed entity columns to be generated for certain message types.
 *
 * @see io.spine.base.EntityColumn
 */
public final class GeneratedColumns extends GeneratedConfigurations<AddColumns> {

    private boolean generate = false;

    /**
     * Enables or disables the column generation.
     *
     * <p>A usage example:
     * <pre>
     * modelCompiler {
     *     columns {
     *         generate = true
     *     }
     * }
     * </pre>
     */
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
