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

package io.spine.js.generate.output.snippet;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.spine.code.js.FileName;
import io.spine.js.generate.JsCodeGenerator;
import io.spine.js.generate.output.CodeLines;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * The helper that generates imports for the JS code.
 *
 * <p>The class has several usages: firstly, it can generate imports in a batch using
 * {@link Builder#setImports(Collection)} and {@link JsImportGenerator#generate()}.
 *
 * <p>Secondly, it can generate single imports via the dedicated methods.
 *
 * <p>Currently all imports are generated in the CommonJS style.
 */
public final class JsImportGenerator extends JsCodeGenerator {

    /**
     * The path to parent directory.
     */
    private static final String PARENT_DIR = "../";

    /**
     * The path to the current directory.
     */
    private static final String CURRENT_DIR = "./";

    /**
     * The import format.
     *
     * <p>The placeholder represents the file to be imported.
     */
    private static final String IMPORT_FORMAT = "require('%s');";

    /**
     * The named import format.
     *
     * <p>The first placeholder is the import name and the second one represents the file to be
     * imported.
     */
    static final String NAMED_IMPORT_FORMAT = "let %s = " + IMPORT_FORMAT;

    /**
     * The value which is prepended to every import.
     *
     * <p>In case of generated messages, this prefix represents the path from the current file
     * to the proto location root.
     */
    private final String importPrefix;

    /**
     * The predefined imports which will be stored to the {@link io.spine.js.generate.output.CodeLines} when calling
     * {@link #generate()}.
     */
    private final Collection<FileName> imports;

    private JsImportGenerator(Builder builder) {
        super(checkNotNull(builder.jsOutput));
        this.importPrefix = builder.fileName != null
                ? composePathToRoot(builder.fileName)
                : CURRENT_DIR;
        this.imports = builder.imports != null
                ? builder.imports
                : emptyList();
    }

    /**
     * Put all the predefined imports specified on creation into the {@link io.spine.js.generate.output.CodeLines}.
     */
    @Override
    public void generate() {
        for (FileName fileToImport : imports) {
            String importPath = importPath(fileToImport);
            String theImport = format(IMPORT_FORMAT, importPath);
            jsOutput().append(theImport);
        }
    }

    /**
     * Generates a named JS import with a stored {@code importPrefix}.
     *
     * <p>Named file import is a statement of type {@code let a = require('./file.js')}.
     */
    public void importFile(FileName fileToImport, String importName) {
        checkNotNull(fileToImport);
        checkNotNull(importName);
        String importPath = importPath(fileToImport);
        String namedImport = format(NAMED_IMPORT_FORMAT, importName, importPath);
        jsOutput().append(namedImport);
    }

    /**
     * Generates a named JS import for a specified lib.
     *
     * <p>Named lib import is a statement of type {@code let a = require('lib')}.
     */
    public void importLib(String libToImport, String importName) {
        checkNotNull(libToImport);
        checkNotNull(importName);
        String namedImport = format(NAMED_IMPORT_FORMAT, importName, libToImport);
        jsOutput().append(namedImport);
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

    private String importPath(FileName fileToImport) {
        return importPrefix + fileToImport;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a {@code JsImportGenerator} to generate imports for the specified file.
     *
     * <p>The {@link #fileName} and {@link #imports} parameters are optional and the
     * {@link #jsOutput} is required to be set.
     *
     * <ol>
     *     <li>If the {@link #fileName} is specified, the {@code JsImportGenerator} will generate
     *         all its imports relative to the specified file.
     *     <li>If the {@link #imports} are specified then the {@code JsImportGenerator} will be
     *         able to generate all specified imports in a batch via the {@link #generate()} method.
     * </ol>
     */
    public static class Builder {

        private FileName fileName;
        private Collection<FileName> imports;
        private CodeLines jsOutput;

        public Builder setFileName(FileName fileName) {
            checkNotNull(fileName);
            this.fileName = fileName;
            return this;
        }

        public Builder setImports(Collection<FileName> imports) {
            checkNotNull(imports);
            this.imports = ImmutableList.copyOf(imports);
            return this;
        }

        public Builder setJsOutput(CodeLines jsOutput) {
            checkNotNull(jsOutput);
            this.jsOutput = jsOutput;
            return this;
        }

        public JsImportGenerator build() {
            return new JsImportGenerator(this);
        }
    }
}
