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
import io.spine.tools.protoc.method.MethodFactory;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.ProtocTaskConfigs.uuidConfig;

/**
 * A configuration of methods to be generated for Java message classes.
 */
public final class GeneratedMethods extends GeneratedConfigurations<AddMethods> {

    private UuidConfig uuidFactoryConfig = UuidConfig.getDefaultInstance();

    public GeneratedMethods() {
        super();
    }

    /**
     * Configures method generation for messages declared in files matching a given pattern.
     *
     * <p>Sample usage is as follows:
     * <pre>
     * applyFactory "io.spine.code.CustomMethodFactory",messages().inFiles(suffix: "events.proto")
     * </pre>
     *
     * <p>The statement in the example above configures all message types declared in a file which
     * name ends with {@code events.proto} to use {@code io.spine.code.CustomMethodFactory} as
     * a custom method factory. It is expected that {@code io.spine.code.CustomMethodFactory} is
     * an implementation of the {@link MethodFactory} interface.
     *
     * <p>Caution. In order for the framework components to function properly, the implementation
     * of the {@code MethodFactory} should always be public and has a no-argument public
     * constructor.
     *
     * Example of a possible implementation:
     * <pre>
     * // In io/spine/code/CustomMethodFactory.java:
     *
     * package io.spine.code;
     *
     * public CustomMethodFactory implements io.spine.tools.protoc.method.MethodFactory {
     *
     *     public CustomMethodFactory(){
     *     }
     *
     *    {@literal List<MethodBody>} createFor(MessageType messageType) {
     *         // ...
     *     }
     * }
     *
     * // In build.gradle:
     *
     * // ...
     *
     * modelCompiler {
     *     methods {
     *         applyFactory "io.spine.code.CustomMethodFactory", messages().inFiles(suffix: "events.proto")
     *     }
     * }
     * </pre>
     */
    public final void applyFactory(@FullyQualifiedName String factory, PatternSelector selector) {
        checkNotNull(factory);
        checkNotNull(selector);
        addPattern(selector, ClassName.of(factory));
    }

    /**
     * Configures method generation for messages with a single {@code string} field called
     * {@code uuid}.
     *
     * <p>This method functions similarly to the {@link #applyFactory(String, PatternSelector)}
     * except
     * the file in which the message type is defined does not matter.
     *
     * <p>Example:
     * <pre>
     * applyFactory "io.spine.code.CustomMethodFactory", messages().uuid()
     * </pre>
     */
    public final void applyFactory(@FullyQualifiedName String factory, UuidMessage selector) {
        checkNotNull(selector);
        uuidFactoryConfig = uuidConfig(ClassName.of(factory));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") // `Builder` API is used in `forEach` lambda.
    @Internal
    @Override
    public AddMethods asProtocConfig() {
        AddMethods.Builder result = AddMethods
                .newBuilder()
                .setUuidFactory(uuidFactoryConfig);
        patternConfigurations()
                .stream()
                .map(GeneratedConfigurations::toPatternConfig)
                .forEach(result::addFactoryByPattern);
        return result.build();
    }
}
