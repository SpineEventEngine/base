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
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.ImplementInterface;
import io.spine.tools.protoc.InterfacesGeneration;
import io.spine.tools.protoc.UuidImplementInterface;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.MessageFile.COMMANDS;
import static io.spine.base.MessageFile.EVENTS;
import static io.spine.base.MessageFile.REJECTIONS;

/**
 * A configuration of interfaces to be generated for Java message classes.
 */
public final class GeneratedInterfaces extends GeneratedConfigurations<InterfacesGeneration> {

    private UuidImplementInterface uuidInterface = UuidImplementInterface.getDefaultInstance();

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
     *     <li>{@link UuidValue} interface for {@linkplain #uuidMessage() UUID messages}.
     * </ul>
     *
     * @return new config
     */
    @VisibleForTesting
    public static GeneratedInterfaces withDefaults() {
        GeneratedInterfaces config = new GeneratedInterfaces();
        FileSelectorFactory filePattern = config.filePattern();
        config.mark(filePattern.endsWith(COMMANDS.suffix()), CommandMessage.class.getName());
        config.mark(filePattern.endsWith(EVENTS.suffix()), EventMessage.class.getName());
        config.mark(filePattern.endsWith(REJECTIONS.suffix()), RejectionMessage.class.getName());
        config.mark(config.uuidMessage(), UuidValue.class.getName());
        return config;
    }

    /**
     * Configures an interface generation for messages declared in files matching a given pattern.
     *
     * <p>Sample usage is:
     * <pre>
     *     {@code
     *     mark filePattern().endsWith("events.proto"), "my.custom.EventMessage"
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
     *             mark filePattern().endsWith("events.proto"), "my.custom.EventMessage"
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
     *     ignore filePattern().endsWith("events.proto")
     *     }
     * </pre>
     *
     * <p>In such case, no additional interface is added to the top-level message classes matching
     * the pattern. However, the interfaces defined via {@code (is)} and {@code (every_is)} options
     * are generated regardless the configuration.
     */
    public final void mark(FileSelector fileSelector, @FullyQualifiedName String interfaceName) {
        checkNotNull(fileSelector);
        checkNotNull(interfaceName);
        addPattern(fileSelector, ClassName.of(interfaceName));
    }

    /**
     * Configures an interface generation for messages with a single {@code string} field called
     * {@code uuid}.
     *
     * <p>This method functions similarly to the {@link #mark(FileSelector, String)} except for
     * several differences:
     * <ul>
     *     <li>the file in which the message type is defined does not matter;
     *     <li>nested definitions are affected as well as top-level ones.
     * </ul>
     *
     * <p>Sample usage is:
     * <pre>
     *      {@code
     *      mark uuidMessage(), "my.custom.Identifier"
     *      }
     * </pre>
     */
    public final void mark(UuidMessage uuidMessage, @FullyQualifiedName String interfaceName) {
        checkNotNull(uuidMessage);
        checkNotNull(interfaceName);
        uuidInterface = uuidInterface(interfaceName);
    }

    @Override
    public final void ignore(UuidMessage uuidMessage) {
        checkNotNull(uuidMessage);
        uuidInterface = UuidImplementInterface.getDefaultInstance();
    }

    // GeneratedInterfacesConfig.Builder usage in `forEach`.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    @Internal
    public InterfacesGeneration asProtocConfig() {
        InterfacesGeneration.Builder result = InterfacesGeneration
                .newBuilder()
                .setUuidInterface(uuidInterface);
        patternConfigurations()
                .stream()
                .map(GeneratedInterfaces::toCommand)
                .forEach(result::addImplementInterface);
        return result.build();
    }

    private static UuidImplementInterface uuidInterface(@FullyQualifiedName String interfaceName) {
        return UuidImplementInterface
                .newBuilder()
                .setInterfaceName(interfaceName)
                .build();
    }

    private static ImplementInterface toCommand(Map.Entry<FileSelector, ClassName> e) {
        FileSelector fileSelector = e.getKey();
        ClassName className = e.getValue();
        return ImplementInterface
                .newBuilder()
                .setPattern(fileSelector.toProto())
                .setInterfaceName(className.value())
                .build();
    }
}
