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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.annotation.Internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.createParentDirs;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * A view on a {@link FileDescriptorSet} after merging.
 */
@Internal
public final class MergedDescriptorSet {

    private final FileDescriptorSet descriptorSet;

    private MergedDescriptorSet(FileDescriptorSet descriptorSet) {
        this.descriptorSet = descriptorSet;
    }

    /**
     * Merges the contents of the given files into a single descriptor set.
     *
     * <p>This method assumes that all the given files exist and contain instances of
     * {@link FileDescriptorSet} Protobuf message.
     *
     * @param files
     *         the files to merge
     * @return merged set
     */
    public static MergedDescriptorSet create(Collection<File> files) {
        FileDescriptorSet merged = files
                .stream()
                .map(FileDescriptors::parse)
                .flatMap(Collection::stream)
                .distinct()
                .reduce(FileDescriptorSet.newBuilder(),
                        FileDescriptorSet.Builder::addFile,
                        (right, left) -> right.addAllFile(left.getFileList()))
                .build();
        MergedDescriptorSet result = new MergedDescriptorSet(merged);
        return result;
    }

    /**
     * Writes this descriptor set into the given file.
     *
     * <p>If the file exists, it will be overridden. Otherwise, the file (and all its parent
     * directories if necessary) will be created.
     *
     * @param destination
     *         the file to write this descriptor set into
     */
    public void writeTo(File destination) {
        checkNotNull(destination);
        prepareFile(destination);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
            descriptorSet.writeTo(out);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static void prepareFile(File destination) {
        try {
            destination.delete();
            createParentDirs(destination);
            destination.createNewFile();
        } catch (IOException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }
}
