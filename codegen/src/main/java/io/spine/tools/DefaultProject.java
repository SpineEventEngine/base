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

    public static GeneratedRoot generated(ModuleDir module) {
        return new GeneratedRoot(module);
    }

    public File mainDescriptors() {
        final BuildRoot build = BuildRoot.of(this);
        final File result = build.descriptors()
                                 .getPath()
                                 .resolve(FileDescriptors.MAIN_FILE)
                                 .toFile();
        return result;
    }

    /*
        DefaultProject.generated()
        DefaultProject.generated().resources()
        DefaultProject.generated().mainJava()
        DefaultProject.generated().mainGrpc()
        DefaultProject.generated().mainSpine()

        DefaultProject.mainProto()
        DefaultProject.testProto()

        DefaultProject.generated().testSpine()

     */

    public static class ModuleDir extends AbstractDirectory {

        ModuleDir(Path directory) {
            super(directory);
        }

        public static ModuleDir at(Path directory) {
            return new ModuleDir(directory);
        }
    }

    static class SourceDir extends AbstractDirectory {

        SourceDir(ModuleDir parent, String name) {
            super(parent.getPath()
                        .resolve(name));
        }
    }

    static class SourceRoot extends SourceDir {

        SourceRoot(ModuleDir parent, String name) {
            super(parent, name);
        }

        /**
         * A root directory for Java code.
         */
        public io.spine.tools.java.Directory java() {
            return io.spine.tools.java.Directory.rootIn(this);
        }
    }

    /**
     * A root source code directory for manually written code.
     */
    static class HandmadeCodeRoot extends SourceRoot {

        HandmadeCodeRoot(ModuleDir parent, String name) {
            super(parent, name);
        }

        public io.spine.tools.proto.Directory proto() {
            return io.spine.tools.proto.Directory.rootIn(this);
        }
    }

    public static final class MainRoot extends HandmadeCodeRoot {

        private MainRoot(ModuleDir parent) {
            super(parent, "main");
        }
    }

    public static final class TestRoot extends HandmadeCodeRoot {

        private TestRoot(ModuleDir parent) {
            super(parent, "test");
        }
    }

    public static final class GeneratedRoot extends SourceDir {

        private GeneratedRoot(ModuleDir parent) {
            super(parent, "generated");
        }
    }

    public static final class BuildRoot extends AbstractDirectory {

        BuildRoot(DefaultProject module) {
            super(module.getPath()
                        .resolve("build"));
        }

        public static BuildRoot of(DefaultProject project) {
            return new BuildRoot(project);
        }

        public BuildDir descriptors() {
            return new BuildDir(this, "descriptors");
        }
    }

    public static final class BuildDir extends AbstractDirectory {

        BuildDir(BuildRoot parent, String name) {
            super(parent.getPath()
                        .resolve(name));
        }
    }
}
