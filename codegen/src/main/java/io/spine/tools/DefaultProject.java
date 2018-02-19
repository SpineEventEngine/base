/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools;

import io.spine.tools.proto.FileDescriptors;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default project paths.
 *
 * @author Alexander Yevsyukov
 */
public final class DefaultProject extends AbstractDirectory {

    private DefaultProject(Path path) {
        super(path);
    }

    public static DefaultProject at(Path root) {
        checkNotNull(root);
        final DefaultProject result = new DefaultProject(root);
        return result;
    }

    public static DefaultProject at(File rootDir) {
        checkNotNull(rootDir);
        return at(rootDir.toPath());
    }

    public GeneratedRoot generated() {
        return new GeneratedRoot(this);
    }

    public File mainDescriptors() {
        final BuildRoot build = BuildRoot.of(this);
        final File result = build.descriptors()
                                 .getPath()
                                 .resolve(FileDescriptors.MAIN_FILE)
                                 .toFile();
        return result;
    }

    public HandmadeCodeRoot src() {
        return new HandmadeCodeRoot(this, "src");
    }

    public HandmadeCodeRoot test() {
        return new HandmadeCodeRoot(this, "test");
    }

    public File testDescriptors() {
        final BuildRoot build = BuildRoot.of(this);
        final File result = build.descriptors()
                                 .getPath()
                                 .resolve(FileDescriptors.TEST_FILE)
                                 .toFile();
        return result;
    }

    static class SourceDir extends SourceCodeDirectory {

        SourceDir(AbstractDirectory parent, String name) {
            super(parent.getPath()
                        .resolve(name));
        }
    }

    /**
     * A root source code directory in a project or a module.
     */
    public static class SourceRoot extends SourceDir {

        SourceRoot(DefaultProject parent, String name) {
            super(parent, name);
        }

        protected SourceDir getMain() {
            return new SourceDir(this, "main");
        }

        protected SourceDir getTest() {
            return new SourceDir(this, "test");
        }

        /**
         * A root directory for main Java code.
         */
        public io.spine.tools.java.Directory mainJava() {
            return io.spine.tools.java.Directory.rootIn(getMain());
        }

        /**
         * A root directory for test Java code.
         */
        public io.spine.tools.java.Directory testJava() {
            return io.spine.tools.java.Directory.rootIn(getTest());
        }
    }

    /**
     * A root source code directory for manually written code.
     *
     * <p>Adds a root directory for the proto code in addition to those exposed
     * by {@link SourceRoot}.
     */
    public static class HandmadeCodeRoot extends SourceRoot {

        private HandmadeCodeRoot(DefaultProject parent, String name) {
            super(parent, name);
        }

        public io.spine.tools.proto.Directory proto() {
            return io.spine.tools.proto.Directory.rootIn(this);
        }
    }

    /**
     * A root directory for the generated code.
     */
    public static final class GeneratedRoot extends SourceRoot {

        private static final String SPINE_DIR = "spine";
        private static final String GRPC_DIR = "grpc";
        private static final String RESOURCES_DIR = "resources";

        private GeneratedRoot(DefaultProject parent) {
            super(parent, "generated");
        }

        /**
         * Spine-generated source code directory.
         */
        public SourceCodeDirectory mainSpine() {
            return new SourceDir(getMain(), SPINE_DIR);
        }

        /**
         * Spine-generated source code directory for tests.
         */
        public SourceCodeDirectory testSpine() {
            return new SourceDir(getTest(), SPINE_DIR);
        }

        /**
         * The directory for the source code generated by gRPC.
         */
        public SourceCodeDirectory mainGrpc() {
            return new SourceDir(getMain(), GRPC_DIR);
        }

        /**
         * The directory for the test source code generated by gRPC.
         */
        public SourceCodeDirectory testGrpc() {
            return new SourceDir(getMain(), GRPC_DIR);
        }

        /**
         * The directory for generated resources.
         */
        public SourceCodeDirectory mainResources() {
            return new SourceDir(getMain(), RESOURCES_DIR);
        }

        /**
         * The directory for generated test resources.
         */
        @SuppressWarnings("unused") // reserved for future use.
        public SourceCodeDirectory testResources() {
            return new SourceDir(getTest(), RESOURCES_DIR);
        }
    }

    /**
     * The root directory for build output.
     */
    static final class BuildRoot extends AbstractDirectory {

        @SuppressWarnings("DuplicateStringLiteralInspection") // different meanings around the code.
        private static final String DIR_NAME = "build";

        private BuildRoot(DefaultProject module) {
            super(module.getPath()
                        .resolve(DIR_NAME));
        }

        static BuildRoot of(DefaultProject project) {
            return new BuildRoot(project);
        }

        private BuildDir descriptors() {
            return new BuildDir(this, "descriptors");
        }
    }

    static final class BuildDir extends AbstractDirectory {

        BuildDir(BuildRoot parent, String name) {
            super(parent.getPath()
                        .resolve(name));
        }
    }
}
