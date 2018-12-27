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

package io.spine.js.generate.parse;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.code.js.MethodReference;
import io.spine.code.js.TypeName;
import io.spine.js.generate.Snippet;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A parser of a generated Protobuf message.
 *
 * <p>This parser should be generated for all messages except standard ones
 * like {@code Any}, {@code int32}, {@code Timestamp}. The parsers for these
 * standard types are manually created and require no code generation.
 *
 * <p>Code provided by the class is in {@code ES5} standard. It is not generated
 * in {@code ES6} since Protobuf compiler generates Javascript in {@code ES5}
 * and we want to be fully compatible with standard generated code.
 */
final class Parser implements Snippet {

    /** The name of the abstract parser to extend from. */
    private static final String ABSTRACT_PARSER = "ObjectParser";

    /** The message to generate the parser for. */
    private final Descriptor message;

    Parser(Descriptor message) {
        checkNotNull(message);
        this.message = message;
    }

    @Override
    public CodeLines value() {
        CodeLines lines = new CodeLines();
        lines.append(constructor());
        lines.append(initPrototype());
        lines.append(initConstructor());
        lines.append(fromObjectMethod());
        return lines;
    }

    /**
     * Obtains the name of the parser to be generated.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection" /* Intersects with handcrafted parsers. */)
    private String parserName() {
        TypeName messageName = TypeName.from(message);
        return messageName + "Parser";
    }

    private Method constructor() {
        MethodReference reference = MethodReference.constructor(parserName());
        String callSuper = format("%s.call(this);", superClass());
        return Method
                .newBuilder(reference)
                .appendToBody(callSuper)
                .build();
    }

    private String initPrototype() {
        String result = format("%s = Object.create(%s.prototype);",
                               prototypeReference(), superClass());
        return result;
    }

    private String initConstructor() {
        String result = format("%s = %s;", constructorReference(), parserName());
        return result;
    }

    private CodeLines fromObjectMethod() {
        return new CodeLines();
    }

    /**
     * Obtains the reference to the prototype of the parser.
     */
    private String prototypeReference() {
        return parserName() + ".prototype";
    }

    /**
     * Obtains the reference to the constructor of the parser.
     */
    private String constructorReference() {
        return prototypeReference() + ".constructor";
    }

    private static String superClass() {
        return ABSTRACT_PARSER;
    }
}
