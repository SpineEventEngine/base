/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.AddMethods;
import io.spine.tools.protoc.UuidConfig;
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
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     useFactory "io.spine.code.CustomMethodFactory", filePattern().endsWith("events.proto")
     *     }
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
     *     In io/spine/code/CustomMethodFactory.java:
     *     {@code
     *     package io.spine.code;
     *
     *     public CustomMethodFactory implements io.spine.tools.protoc.method.MethodFactory {
     *
     *         public CustomMethodFactory(){
     *         }
     *
     *         List<MethodBody> createFor(MessageType messageType) {
     *             // ...
     *         }
     *     }
     *     }
     *
     *     In build.gradle:
     *     {@code
     *     // ...
     *
     *     modelCompiler {
     *         generateMethods {
     *             useFactory "io.spine.code.CustomMethodFactory", filePattern().endsWith("events.proto")
     *         }
     *     }
     *     }
     * </pre>
     *
     * <p>Another option for an method generation configuration is to turn it off completely:
     * <pre>
     *     {@code
     *     ignore filePattern().endsWith("events.proto")
     *     }
     * </pre>
     *
     * <p>In such case, no additional methods are added to the message classes matching the
     * pattern.
     */
    public final void useFactory(@FullyQualifiedName String factoryName,
                                 FileSelector fileSelector) {
        checkNotNull(factoryName);
        checkNotNull(fileSelector);
        addPattern(fileSelector, ClassName.of(factoryName));
    }

    /**
     * Configures method generation for messages with a single {@code string} field called
     * {@code uuid}.
     *
     * <p>This method functions similarly to the {@link #useFactory(String, FileSelector)} except
     * the file in which the message type is defined does not matter.
     *
     * <p>Sample usage is:
     * <pre>
     *      {@code
     *      useFactory "io.spine.code.CustomMethodFactory", uuidMessage()
     *      }
     * </pre>
     */
    public final void useFactory(@FullyQualifiedName String factoryName, UuidMessage uuidMessage) {
        checkNotNull(uuidMessage);
        uuidFactoryConfig = uuidConfig(factoryName);
    }

    @Override
    public void ignore(UuidMessage uuidMessage) {
        checkNotNull(uuidMessage);
        uuidFactoryConfig = UuidConfig.getDefaultInstance();
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
