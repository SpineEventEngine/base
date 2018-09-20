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

package io.spine.base.generate;

import com.google.common.base.Strings;
import io.spine.code.js.FileName;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * The helper that generates imports for the JS code.
 *
 * <p>Currently all imports are generated in the CommonJS style.
 *
 * @author Dmytro Kuzmin
 */
public final class JsImportGenerator extends JsCodeGenerator {

    /**
     * The path to parent dir.
     */
    private static final String PARENT_DIR = "../";

    /**
     * The path to the current dir.
     */
    private static final String CURRENT_DIR = "./";

    /**
     * The import format.
     *
     * <p>The placeholder represents the file to be imported.
     */
    private static final String IMPORT_FORMAT = "require('%s');";

    private static final String NAMED_IMPORT_FORMAT = "let %s = " + IMPORT_FORMAT;

    /**
     * The value which is prepended to every import.
     *
     * <p>In case of JS proto definitions, this prefix represents the path from the current file
     * to the proto JS location root.
     */
    private final String importPrefix;
    private final Collection<FileName> imports;

    private JsImportGenerator(Builder builder) {
        super(builder.jsOutput);
        this.importPrefix = builder.fileName != null
                ? composePathToRoot(builder.fileName)
                : "";
        this.imports = builder.imports != null
                ? builder.imports
                : emptyList();
    }

    @Override
    public void generate() {
        for (FileName fileToImport : imports) {
            String importPath = importPrefix + fileToImport;
            String theImport = format(IMPORT_FORMAT, importPath);
            jsOutput().addLine(theImport);
        }
    }

    /**
     * Generates a named JS import with a stored {@code importPrefix}.
     *
     * <p>Named import is a statement of type {@code let a = require('./b')}.
     */
    public void importFile(FileName fileToImport, String importName) {
        String importPath = importPrefix + fileToImport;
        String namedImport = format(NAMED_IMPORT_FORMAT, importName, importPath);
        jsOutput().addLine(namedImport);
    }

    /**
     * Generates a named JS import for a specified lib.
     *
     * <p>Named import is a statement of type {@code let a = require('./b')}.
     */
    public void importLib(String libName, String importName) {
        String namedImport = format(NAMED_IMPORT_FORMAT, importName, libName);
        jsOutput().addLine(namedImport);
    }

    /**
     * Composes the path from the given file to its root.
     *
     * <p>Basically, the method replaces all preceding path elements by the {@link #PARENT_DIR}.
     */
    private static String composePathToRoot(FileName fileName) {
        String[] pathElements = fileName.pathElements();
        int fileLocationDepth = pathElements.length - 1;
        String pathToRoot = Strings.repeat(PARENT_DIR, fileLocationDepth);
        String result = pathToRoot.isEmpty() ? CURRENT_DIR : pathToRoot;
        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a {@code JsImportGenerator} to generate imports for the specified file.
     *
     * 1. If file path not specified, generates relative to root.
     * etc.
     *
     */
    public static class Builder {

        /**
         * The file for which the imports will be generated.
         *
         * <p>The provided file path should be relative to the desired import root (e.g. sources root,
         * generated protos root).
         */
        private FileName fileName;
        private Collection<FileName> imports;
        private JsOutput jsOutput;

        public Builder setFileName(FileName fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setImports(Collection<FileName> imports) {
            this.imports = copyOf(imports);
            return this;
        }

        public Builder setJsOutput(JsOutput jsOutput) {
            this.jsOutput = jsOutput;
            return this;
        }

        /**
         * @return the {@code JsImportGenerator} that generates imports relative to this file
         */
        public JsImportGenerator build() {
            return new JsImportGenerator(this);
        }
    }
}
