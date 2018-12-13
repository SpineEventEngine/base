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

package io.spine.tools.compiler.validation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.spine.code.Indent;
import io.spine.logging.Logging;
import io.spine.tools.compiler.TypeCache;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Gradle {@code Action} for validating builder generation.
 *
 * <p>An instance-per-scope is usually created. E.g. test sources and main source are
 * generated with different instances of this class.
 */
public class VBuilderGenerator implements Logging {

    /** Code will be generated into this directory. */
    private final String targetDirPath;

    /** Indentation for the generated code. */
    private final Indent indent;

    /**
     * The predicate for filtering types by module, or accepting all types,
     * if the generation is requested for all types.
     */
    private final Predicate<VBType> predicate;

    /**
     * Creates new instance of the generator.
     *
     * @param protoSrcDirPath
     *        an absolute path to the folder, containing the {@code .proto} files for
     *        the given scope
     * @param allTypes
     *        If {@code true}, all message types from the classpath will be included.
     *        If {@code false}, only messages types declared in the current module will be included.
     * @param targetDirPath
     *        an absolute path to the folder, serving as a target for the generation for
     *        the given scope
     * @param indent
     *        indentation for the generated code
     */
    public VBuilderGenerator(String protoSrcDirPath,
                             boolean allTypes,
                             String targetDirPath,
                             Indent indent) {
        this.targetDirPath = targetDirPath;
        this.predicate = allTypes
                         ? (type -> true)
                         : new BelongsToModule(protoSrcDirPath);
        this.indent = indent;
    }

    public void process(File descriptorSetFile) {
        Logger log = log();
        log.debug("Generating validating builders for types from {}.", descriptorSetFile);

        VBTypeLookup lookup = new VBTypeLookup(descriptorSetFile.getPath());
        Set<VBType> allFound = lookup.collect();
        TypeCache typeCache = lookup.getTypeCache();

        Set<VBType> filtered = filter(allFound);
        if (filtered.isEmpty()) {
            log.warn("No validating builders will be generated.");
            return;
        }

        generate(filtered, typeCache);
    }

    private void generate(Set<VBType> builders, TypeCache cache) {
        ValidatingBuilderWriter writer =
                new ValidatingBuilderWriter(targetDirPath, indent, cache);

        for (VBType vb : builders) {
            try {
                writer.write(vb);
            } catch (RuntimeException e) {
                logError(vb, e);
            }
        }
        log().debug("Validating builder generation is finished.");
    }

    private Set<VBType> filter(Set<VBType> types) {
        Iterable<VBType> filtered = Iterables.filter(types, predicate::test);
        Set<VBType> result = ImmutableSet.copyOf(filtered);
        return result;
    }

    private void logError(VBType vb, RuntimeException e) {
        Logger log = log();
        String message =
                format("Cannot generate a validating builder for %s.%n" +
                               "Error: %s", vb, e.toString());
        // If debug level is enabled give it under this lever, otherwise WARN.
        if (log.isDebugEnabled()) {
            log.debug(message, e);
        } else {
            log.warn(message);
        }
    }

    /**
     * A predicate determining if the given {@linkplain VBType validating builder metadata}
     * has been collected from the source file in the specified module.
     */
    private static class BelongsToModule implements Predicate<VBType> {

        /**
         *  An absolute path to the root folder for the {@code .proto} files in the module.
         */
        private final String protoSrcDirPath;

        private BelongsToModule(String protoSrcDirPath) {
            this.protoSrcDirPath = protoSrcDirPath.endsWith(File.separator)
                             ? protoSrcDirPath
                             : protoSrcDirPath + File.separator;
        }

        @Override
        public boolean test(@Nullable VBType input) {
            checkNotNull(input);

            String path = input.getSourceProtoFile();
            File protoFile = new File(protoSrcDirPath + path);
            boolean belongsToModule = protoFile.exists();
            return belongsToModule;
        }
    }
}
