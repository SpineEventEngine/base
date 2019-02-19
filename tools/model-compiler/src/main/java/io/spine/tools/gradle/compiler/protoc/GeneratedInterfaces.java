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

import com.google.common.annotations.VisibleForTesting;
import io.spine.annotation.Internal;
import io.spine.base.CommandMessage;
import io.spine.base.EnrichmentMessage;
import io.spine.base.EventMessage;
import io.spine.base.MessageFile;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.EnrichmentInterface;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.UuidInterface;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

/**
 * A configuration of interfaces to be generated for Java message classes.
 */
public final class GeneratedInterfaces extends GeneratedConfigurations<PatternInterfaceConfig> {

    private final UuidInterfaceConfig uuidInterfaceConfig = new UuidInterfaceConfig();
    private final EnrichmentInterfaceConfig enrichmentConfig = new EnrichmentInterfaceConfig();

    private GeneratedInterfaces() {
        super();
    }

    /**
     * Creates a new instance of {@code GeneratedInterfaces} with the default values.
     *
     * <p>The default values are:
     * <ul>
     *     <li>{@link CommandMessage} interface for Proto files ending with {@code commands.proto};
     *     <li>{@link EventMessage} interface for Proto files ending with {@code events.proto};
     *     <li>{@link RejectionMessage} interface for Proto files ending with
     *         {@code rejections.proto};
     *     <li>{@link UuidValue} interface for {@linkplain #uuidMessage() UUID messages};
     *     <li>{@link EnrichmentMessage} interface
     *         {@linkplain #enrichmentMessage() enrichment messages}.
     * </ul>
     *
     * @return new config
     */
    @VisibleForTesting
    public static GeneratedInterfaces withDefaults() {
        GeneratedInterfaces config = new GeneratedInterfaces();
        config.filePattern(config.endsWith(MessageFile.COMMANDS.suffix()))
              .markWith(CommandMessage.class.getName());
        config.filePattern(config.endsWith(MessageFile.EVENTS.suffix()))
              .markWith(EventMessage.class.getName());
        config.filePattern(config.endsWith(MessageFile.REJECTIONS.suffix()))
              .markWith(RejectionMessage.class.getName());
        config.uuidMessage()
              .markWith(UuidValue.class.getName());
        config.enrichmentMessage()
              .markWith(EnrichmentMessage.class.getName());
        return config;
    }

    /**
     * Configures an interface generation for messages declared in files matching a given pattern.
     *
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     filePattern(endsWith("events.proto")).markWith("my.custom.EventMessage")
     *     }
     * </pre>
     *
     * <p>The statement in the example above configures all message types declared in a file which
     * name ends with {@code events.proto} to implement the {@code my.custom.EventMessage}. It is
     * expected that {@code my.custom.EventMessage} is an interface defined by the user.
     *
     * <p>Note that only the top-level messages declarations are affected by this configuration.
     * Nested messages defined in the same file do not implement the interface.
     *
     * <p>Caution. In order for the framework components to function properly, one should not
     * entirely override the {@linkplain #withDefaults() default} message interfaces. Instead,
     * provide a custom interface which {@code extends} the standard one.
     *
     * Example of a safe way to override standard interfaces:
     * <pre>
     *     In my/custom/EventMessage.java:
     *     {@code
     *     package my.custom;
     *
     *     public interface EventMessage extends io.spine.base.EventMessage {
     *         // ...
     *     }
     *     }
     *
     *     In build.gradle:
     *     {@code
     *     // ...
     *
     *     modelCompiler {
     *         generateInterfaces {
     *             filePattern(endsWith("events.proto")).markWith("my.custom.EventMessage")
     *         }
     *     }
     *     }
     * </pre>
     *
     * <p>In the example above, {@code my.custom.EventMessage} extends
     * {@link EventMessage io.spine.base.EventMessage} and thus it is safe to mark all events with
     * this interface instead of the default one.
     *
     * <p>Another option for an interface generation configuration is to turn it off completely:
     * <pre>
     *     {@code
     *     filePattern(endsWith("events.proto")).ignore()
     *     }
     * </pre>
     *
     * <p>In such case, no additional interface is added to the top-level message classes matching
     * the pattern. However, the interfaces defined via {@code (is)} and {@code (every_is)} options
     * are generated regardless the configuration.
     *
     * @param pattern
     *         the file pattern
     * @return a configuration object for Proto files matching the pattern
     */
    @SuppressWarnings("RedundantMethodOverride") // do override to extend javadoc
    @Override
    public PatternInterfaceConfig filePattern(FilePattern pattern) {
        return super.filePattern(pattern);
    }

    @Override
    PatternInterfaceConfig patternConfiguration(@NonNull FilePattern pattern) {
        return PatternInterfaceConfig.fromPattern(pattern);
    }

    /**
     * Configures an interface generation for messages with a single {@code string} field called
     * {@code uuid}.
     *
     * <p>This method functions similarly to the {@link #filePattern(FilePattern)} except for
     * several differences:
     * <ul>
     *     <li>the file in which the message type is defined does not matter;
     *     <li>nested definitions are affected as well as top-level ones.
     * </ul>
     *
     * @return a configuration object for Proto messages matching UUID message pattern
     */
    @SuppressWarnings("WeakerAccess") // Gradle DSL public API
    public GeneratedInterfaceConfig uuidMessage() {
        return uuidInterfaceConfig;
    }

    /**
     * Configures an interface generation for messages with {@code (enrichment_for)} option.
     *
     * <p>This method functions are similar to the {@link #filePattern(FilePattern)} except for
     * several differences:
     * <ul>
     *     <li>the file in which the message type is defined does not matter;
     * </ul>
     *
     * @return a configuration object for Proto messages matching enrichment message pattern
     */
    @SuppressWarnings("WeakerAccess") // Gradle DSL public API
    public GeneratedInterfaceConfig enrichmentMessage() {
        return enrichmentConfig;
    }

    /**
     * Converts this config into a {@link SpineProtocConfig}.
     */
    @Internal
    @VisibleForTesting
    public SpineProtocConfig asProtocConfig() {
        UuidInterface uuidInterface = uuidInterface();
        EnrichmentInterface enrichmentInterface = enrichmentConfig();
        SpineProtocConfig.Builder result = SpineProtocConfig
                .newBuilder()
                .setUuidInterface(uuidInterface)
                .setEnrichmentInterface(enrichmentInterface);
        patternConfigurations()
                .stream()
                .map(PatternInterfaceConfig::generatedInterface)
                .forEach(result::addGeneratedInterface);
        return result.build();
    }

    private EnrichmentInterface enrichmentConfig() {
        Optional<ClassName> name = enrichmentConfig.interfaceName();
        EnrichmentInterface enrichmentInterface = name
                .map(GeneratedInterfaces::newEnrichmentInterface)
                .orElse(EnrichmentInterface.getDefaultInstance());
        return enrichmentInterface;
    }

    private static EnrichmentInterface newEnrichmentInterface(ClassName className) {
        return EnrichmentInterface
                .newBuilder()
                .setInterfaceName(className.value())
                .build();
    }

    private UuidInterface uuidInterface() {
        Optional<ClassName> name = uuidInterfaceConfig.interfaceName();
        UuidInterface uuidInterface = name
                .map(GeneratedInterfaces::newUuidInterface)
                .orElse(UuidInterface.getDefaultInstance());
        return uuidInterface;
    }

    private static UuidInterface newUuidInterface(ClassName className) {
        return UuidInterface
                .newBuilder()
                .setInterfaceName(className.value())
                .build();
    }

}
