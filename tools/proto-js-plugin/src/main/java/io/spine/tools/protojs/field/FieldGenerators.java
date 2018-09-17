/*
 * Copyright 2018, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.protojs.field;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.field.precondition.FieldPrecondition;
import io.spine.tools.protojs.generate.JsOutput;
import io.spine.tools.protojs.field.parser.FieldParser;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.field.Fields.isMap;
import static io.spine.tools.protojs.field.Fields.isRepeated;
import static io.spine.tools.protojs.field.Fields.keyDescriptor;
import static io.spine.tools.protojs.field.Fields.valueDescriptor;
import static io.spine.tools.protojs.field.precondition.FieldPreconditions.checkerFor;
import static io.spine.tools.protojs.field.parser.FieldParsers.parserFor;

/**
 * The helper class which provides the {@link FieldHandler} implementation for the given
 * {@linkplain FieldDescriptor field}.
 *
 * @author Dmytro Kuzmin
 */
public final class FieldGenerators {

    /** Prevents instantiation of this utility class. */
    private FieldGenerators() {
    }

    /**
     * Creates a {@code FieldHandler} for the given field.
     *
     * @param field
     *         the descriptor of the field to be handled
     * @param jsOutput
     *         the {@code JsOutput} to accumulate all the generated code
     * @return the new {@code FieldHandler} of the appropriate type
     */
    public static FieldHandler createFor(FieldDescriptor field, JsOutput jsOutput) {
        checkNotNull(field);
        checkNotNull(jsOutput);
        if (isMap(field)) {
            return mapHandler(field, jsOutput);
        }
        if (isRepeated(field)) {
            return repeatedHandler(field, jsOutput);
        }
        return singularHandler(field, jsOutput);
    }

    /**
     * Creates a {@linkplain MapFieldGenerator handler} for the {@code map} field.
     *
     * @implNote
     * The creation logic is different from all other handlers.
     *
     * <p>As the {@code map} field is always a {@code message} of type {@code ...Entry}, we create
     * {@link FieldPrecondition} and {@link FieldParser} for it's field with name {@code "value"}
     * (whose type corresponds to the {@code map} value type).
     *
     * <p>The key also has to be parsed via the separate {@code FieldParser}, as in JSON it is
     * always converted to a {@code string}. So we create additional {@code FieldParser} for
     * the {@code ...Entry} {@code "key"} field.
     */
    private static FieldHandler mapHandler(FieldDescriptor field, JsOutput jsOutput) {
        FieldParser keyParser = mapKeyParser(field, jsOutput);
        FieldParser valueParser = mapValueParser(field, jsOutput);
        FieldPrecondition valueChecker = mapValueChecker(field, jsOutput);

        FieldHandler handler = MapFieldGenerator
                .newBuilder()
                .setField(field)
                .setChecker(valueChecker)
                .setKeyParser(keyParser)
                .setParser(valueParser)
                .setJsOutput(jsOutput)
                .build();
        return handler;
    }

    /**
     * Creates a {@linkplain RepeatedFieldGenerator handler} for the {@code repeated} proto field.
     */
    private static FieldHandler repeatedHandler(FieldDescriptor field, JsOutput jsOutput) {
        FieldPrecondition checker = checkerFor(field, jsOutput);
        FieldParser parser = parserFor(field, jsOutput);

        FieldHandler handler = RepeatedFieldGenerator
                .newBuilder()
                .setField(field)
                .setChecker(checker)
                .setParser(parser)
                .setJsOutput(jsOutput)
                .build();
        return handler;
    }

    /**
     * Creates a {@linkplain SingularFieldGenerator handler} for the ordinary proto field.
     */
    private static FieldHandler singularHandler(FieldDescriptor field, JsOutput jsOutput) {
        FieldPrecondition checker = checkerFor(field, jsOutput);
        FieldParser parser = parserFor(field, jsOutput);

        FieldHandler handler = SingularFieldGenerator
                .newBuilder()
                .setField(field)
                .setChecker(checker)
                .setParser(parser)
                .setJsOutput(jsOutput)
                .build();
        return handler;
    }

    /**
     * Creates a {@code FieldPrecondition} for the value of the map field.
     */
    private static FieldPrecondition mapValueChecker(FieldDescriptor field, JsOutput jsOutput) {
        FieldDescriptor valueDescriptor = valueDescriptor(field);
        FieldPrecondition checker = checkerFor(valueDescriptor, jsOutput);
        return checker;
    }

    /**
     * Creates a {@code FieldParser} for the key of the map field.
     */
    private static FieldParser mapKeyParser(FieldDescriptor field, JsOutput jsOutput) {
        FieldDescriptor keyDescriptor = keyDescriptor(field);
        FieldParser parser = parserFor(keyDescriptor, jsOutput);
        return parser;
    }

    /**
     * Creates a {@code FieldParser} for the value of the map field.
     */
    private static FieldParser mapValueParser(FieldDescriptor field, JsOutput jsOutput) {
        FieldDescriptor valueDescriptor = valueDescriptor(field);
        FieldParser parser = parserFor(valueDescriptor, jsOutput);
        return parser;
    }
}
