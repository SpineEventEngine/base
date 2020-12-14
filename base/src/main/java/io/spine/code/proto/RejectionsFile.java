/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.java.SimpleClassName;
import io.spine.type.RejectionType;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.type.RejectionType.isValidOuterClassName;

/**
 * A proto file with declarations of {@linkplain io.spine.base.RejectionMessage rejections}.
 *
 * <p>A valid rejections file must:
 * <ul>
 *     <li>be named ending on {@link io.spine.base.MessageFile#REJECTIONS “rejections.proto”};
 *     <li>have the {@code java_multiple_files} option set to {@code false};
 *     <li>either have a {@code java_outer_classname} value which ends with
 *         {@linkplain RejectionType#isValidOuterClassName(SimpleClassName)} “Rejections”},
 *         or not have the {@code java_outer_classname} option set at all.
 *
 * </ul>
 */
public final class RejectionsFile extends SourceFile {

    private RejectionsFile(FileDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Creates an instance by the passed rejections file.
     */
    public static RejectionsFile from(SourceFile file) {
        checkNotNull(file);
        checkMatchesConvention(file);

        RejectionsFile result = new RejectionsFile(file.descriptor());
        return result;
    }

    private static void checkMatchesConvention(SourceFile file) {
        FileDescriptor descriptor = file.descriptor();
        checkArgument(isRejections(descriptor),
                      "`%s`. A rejection file must have a name ending in `rejections.proto`.",
                      file);
        checkArgument(!descriptor.getOptions().getJavaMultipleFiles(),
                      "`%s`. A rejection file should generate Java classes into a single file. " +
                              "Please set `java_multiple_files` to `false`.",
                      file);
        Optional<SimpleClassName> outerClass = SimpleClassName.declaredOuterClassName(descriptor);
        outerClass.ifPresent(name -> checkArgument(
                isValidOuterClassName(name),
                "%s. A rejection file must have the `java_outer_classname` ending in " +
                        "`Rejections` or not have a `java_outer_classname` option at all.",
                file)
        );
    }

    /**
     * Obtains rejection messages declared in the file.
     */
    public List<RejectionType> rejectionDeclarations() {
        ImmutableList.Builder<RejectionType> result = ImmutableList.builder();
        FileDescriptor file = descriptor();
        for (Descriptor type : file.getMessageTypes()) {
            RejectionType declaration = new RejectionType(type);
            result.add(declaration);
        }
        return result.build();
    }

    private static boolean isRejections(FileDescriptor file) {
        return FileName.from(file)
                       .isRejections();
    }

    /**
     * Obtains rejection files from the passed set of files.
     */
    public static ImmutableSet<RejectionsFile> findAll(FileSet fileSet) {
        ImmutableSet<RejectionsFile> result =
                fileSet.files()
                       .stream()
                       .filter(RejectionsFile::isRejections)
                       .map(file -> {
                           SourceFile sourceFile = SourceFile.from(file);
                           return from(sourceFile);
                       })
                       .collect(toImmutableSet());
        return result;
    }
}
