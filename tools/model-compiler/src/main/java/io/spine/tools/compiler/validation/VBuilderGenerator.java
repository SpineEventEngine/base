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

    /** Source directory with proto files. */
    private final String protoSrcDirPath;

    /** Controls the scope of validating builder generation. */
    private final boolean allTypes;

    /** Indentation for the generated code. */
    private final Indent indent;

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
        this.protoSrcDirPath = protoSrcDirPath.endsWith(File.separator)
                               ? protoSrcDirPath
                               : protoSrcDirPath + File.separator;
        this.allTypes = allTypes;
        this.indent = indent;
    }

    public void process(File descriptorSetFile) {
        Logger log = log();
        log.debug("Generating the validating builders from {}.", descriptorSetFile);

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
        Logger log = log();
        ValidatingBuilderWriter writer =
                new ValidatingBuilderWriter(targetDirPath, indent, cache);

        for (VBType vb : builders) {
            try {
                writer.write(vb);
            } catch (RuntimeException e) {
                String message =
                        format("Cannot generate the validating builder for %s. %n" +
                               "Error: %s", vb, e.toString());
                // If debug level is enabled give it under this lever, otherwise WARN.
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                } else {
                    log.warn(message);
                }
            }
        }
        log.debug("The validating builder generation is finished.");
    }

    private Set<VBType> filter(Set<VBType> types) {
        Predicate<VBType> predicate = getPredicate();
        Iterable<VBType> filtered = Iterables.filter(types, predicate::test);
        Set<VBType> result = ImmutableSet.copyOf(filtered);
        return result;
    }

    private Predicate<VBType> getPredicate() {
        return allTypes
               ? (type -> true)
               : new SourceProtoBelongsToModule(protoSrcDirPath);
    }

    /**
     * A predicate determining if the given {@linkplain VBType validating builder metadata}
     * has been collected from the source file in the specified module.
     *
     * <p>Each predicate instance requires to specify the root folder of Protobuf definitions
     * for the module. This value is used to match the given {@code VBMetadata}.
     */
    private static class SourceProtoBelongsToModule implements Predicate<VBType> {

        /**
         *  An absolute path to the root folder for the {@code .proto} files in the module.
         */
        private final String protoRoot;

        private SourceProtoBelongsToModule(String protoRoot) {
            this.protoRoot = protoRoot;
        }

        @Override
        public boolean test(@Nullable VBType input) {
            checkNotNull(input);

            String path = input.getSourceProtoFile();
            File protoFile = new File(protoRoot + path);
            boolean belongsToModule = protoFile.exists();
            return belongsToModule;
        }
    }
}
