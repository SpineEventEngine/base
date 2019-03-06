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

package io.spine.tools.compiler.validation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import io.spine.code.generate.Indent;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.ProtoBelongsToModule;
import io.spine.code.proto.SourceProtoBelongsToModule;
import io.spine.code.proto.TypeSet;
import io.spine.logging.Logging;
import io.spine.type.MessageType;
import org.slf4j.Logger;

import java.io.File;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;

/**
 * Gradle {@code Action} for validating builder generation.
 *
 * <p>An instance-per-scope is usually created. E.g. test sources and main source are
 * generated with different instances of this class.
 */
public final class VBuilderGenerator implements Logging {

    private final File protoSrcDir;

    /** Code will be generated into this directory. */
    private final File targetDir;

    /** Indentation for the generated code. */
    private final Indent indent;

    /**
     * Creates new instance of the generator.
     *
     * @param protoSrcDir
     *         the directory with proto source files
     * @param targetDir
     *         an absolute path to the folder, serving as a target for the code generation
     * @param indent
     *         the indentation for generated code
     */
    public VBuilderGenerator(File protoSrcDir, File targetDir, Indent indent) {
        this.protoSrcDir = protoSrcDir;
        this.targetDir = targetDir;
        this.indent = indent;
        _debug("Initiating generation of validating builders. " +
                       "Proto src dir: {} Target dir: {}", protoSrcDir, targetDir);
    }

    public void process(FileSet files) {
        FileSet fileSet = moduleFiles(files);
        ImmutableCollection<MessageType> messageTypes = TypeSet.onlyMessages(fileSet);
        @SuppressWarnings("Guava") // it's more neat Guava way here.
        ImmutableList<MessageType> customTypes =
                messageTypes.stream()
                            .filter(MessageType::isCustom)
                            .filter(not(MessageType::isRejection))
                            .collect(toImmutableList());
        generate(customTypes);
    }

    private FileSet moduleFiles(FileSet allFiles) {
        ProtoBelongsToModule predicate = new SourceProtoBelongsToModule(protoSrcDir);
        return allFiles.filter(predicate.forDescriptor());
    }

    private void generate(ImmutableCollection<MessageType> messages) {
        _debug("Generating validating builders for {} types.", messages.size());
        for (MessageType messageType : messages) {
            try {
                VBuilderCode code = new VBuilderCode(targetDir, indent, messageType);
                code.write();
            } catch (RuntimeException e) {
                logError(messageType, e);
            }
        }
        _debug("Validating builder generation is finished.");
    }

    private void logError(MessageType type, RuntimeException e) {
        Logger log = log();
        String message =
                format("Cannot generate a validating builder for `%s`.%n" +
                               "Error: %s", type, e.toString());
        // If debug level is enabled give it under this lever, otherwise WARN.
        if (log.isDebugEnabled()) {
            log.debug(message, e);
        } else {
            log.warn(message);
        }
    }
}
