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

package io.spine.code.structure;

import io.spine.code.AbstractDirectory;
import io.spine.code.SourceCodeDirectory;

import java.io.File;
import java.nio.file.Path;

/**
 * This class represents a default directory structure for a Spine-based project of any language.
 *
 * <p>The descendants of the class contain the language-specific project structures.
 *
 * <p>The {@code DefaultProject} helps resolving names of the directories and files under the
 * project directory. It is expected that for most projects, the default values of paths remain
 * unchanged.
 *
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
// Only stores common elements of subclasses.
public abstract class DefaultProject extends AbstractDirectory {

    /**
     * The Spine internal directory name for storing temporary build artifacts.
     *
     * @see #tempArtifacts()
     */
    private static final String TEMP_ARTIFACT_DIR = ".spine";

    protected DefaultProject(Path path) {
        super(path);
    }

    public BuildRoot buildRoot() {
        return BuildRoot.of(this);
    }

    /**
     * Obtains the directory for temporary Spine build artifacts.
     *
     * <p>Spine Gradle tasks may write some temporary files into this directory.
     *
     * <p>The directory is deleted on {@code :pre-clean"}.
     */
    public File tempArtifacts() {
        File result = new File(getPath().toFile(), TEMP_ARTIFACT_DIR);
        return result;
    }

    protected static class SourceDir extends SourceCodeDirectory {

        public SourceDir(AbstractDirectory parent, String name) {
            super(parent.getPath()
                        .resolve(name));
        }
    }

    /**
     * A root source code directory in a project or a module.
     */
    public static class SourceRoot extends SourceDir {

        protected SourceRoot(DefaultProject parent, String name) {
            super(parent, name);
        }

        protected SourceDir getMain() {
            return new SourceDir(this, "main");
        }

        protected SourceDir getTest() {
            return new SourceDir(this, "test");
        }
    }

    /**
     * The root directory for build output.
     */
    public static final class BuildRoot extends AbstractDirectory {

        @SuppressWarnings("DuplicateStringLiteralInspection") // different meanings around the code.
        private static final String DIR_NAME = "build";

        private BuildRoot(DefaultProject module) {
            super(module.getPath()
                        .resolve(DIR_NAME));
        }

        static BuildRoot of(DefaultProject project) {
            return new BuildRoot(project);
        }

        public DescriptorsDir descriptors() {
            return new DescriptorsDir(this, "descriptors");
        }
    }

    public static final class DescriptorsDir extends AbstractDirectory {

        DescriptorsDir(BuildRoot parent, String name) {
            super(parent.getPath()
                        .resolve(name));
        }

        public Path mainDescriptors() {
            return getPath().resolve("main");
        }

        public Path testDescriptors() {
            return getPath().resolve("test");
        }
    }
}
