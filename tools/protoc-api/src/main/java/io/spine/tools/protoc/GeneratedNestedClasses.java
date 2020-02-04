/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc;

import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A configuration of nested classes to be generated for Java message classes.
 */
public final class GeneratedNestedClasses extends GeneratedConfigurations<AddNestedClasses> {

    /**
     * Configures nested class generation for messages declared in files matching a given pattern.
     *
     * <p>Example:
     * <pre>
     * applyFactory "io.spine.code.CustomNestedClassFactory", messages().inFiles(suffix: "events.proto")
     * </pre>
     *
     * <p>The statement above configures all message types which are declared in proto files with
     * names ending with {@code events.proto} to use the {@code CustomNestedClassFactory} for
     * nested class code generation. It is expected that {@code CustomNestedClassFactory} is an
     * implementation of the {@link io.spine.tools.protoc.nested.NestedClassFactory} interface.
     *
     * @apiNote When loading the factory class passed by FQN, Spine class loader assumes it is
     *        already accessible and instantiable with no additional arguments. So, the provided
     *        implementation of {@code NestedClassFactory} should be {@code public} and have a
     *        {@code public} no-argument constructor.
     */
    public final void applyFactory(@FullyQualifiedName String factory, PatternSelector selector) {
        checkNotNull(factory);
        checkNotNull(selector);
        addPattern(selector, ClassName.of(factory));
    }

    @Internal
    @Override
    public AddNestedClasses asProtocConfig() {
        AddNestedClasses.Builder result = AddNestedClasses.newBuilder();
        patternConfigurations()
                .stream()
                .map(GeneratedConfigurations::toPatternConfig)
                .forEach(result::addFactoryByPattern);
        return result.build();
    }
}
