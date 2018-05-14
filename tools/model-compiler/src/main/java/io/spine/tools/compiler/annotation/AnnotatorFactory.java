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

package io.spine.tools.compiler.annotation;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.annotation.Beta;
import io.spine.annotation.Experimental;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import io.spine.tools.proto.FileDescriptors;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.option.OptionsProto.beta;
import static io.spine.option.OptionsProto.betaAll;
import static io.spine.option.OptionsProto.betaType;
import static io.spine.option.OptionsProto.experimental;
import static io.spine.option.OptionsProto.experimentalAll;
import static io.spine.option.OptionsProto.experimentalType;
import static io.spine.option.OptionsProto.internal;
import static io.spine.option.OptionsProto.internalAll;
import static io.spine.option.OptionsProto.internalType;
import static io.spine.option.OptionsProto.sPI;
import static io.spine.option.OptionsProto.sPIAll;
import static io.spine.option.OptionsProto.sPIService;
import static io.spine.option.OptionsProto.sPIType;

/**
 * A factory for {@linkplain Annotator Annotators}.
 *
 * @author Alex Tymchenko
 */
public class AnnotatorFactory {

    /**
     * Protobuf file descriptors to process.
     */
    private final ImmutableList<FileDescriptorProto> fileDescriptors;

    /**
     * An absolute path to the Java sources directory,
     * generated basing on {@link #fileDescriptors}.
     */
    private final String genProtoDir;

    /**
     * An absolute path to the {@code gRPC} services directory,
     * generated basing on {@link #fileDescriptors}.
     */
    private final String genGrpcDir;

    private AnnotatorFactory(Collection<FileDescriptorProto> fileDescriptors,
                             String genProtoDir,
                             String genGrpcDir) {
        checkNotNull(fileDescriptors);
        checkArgument(!isNullOrEmpty(genProtoDir));
        checkArgument(!isNullOrEmpty(genGrpcDir));
        this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
        this.genProtoDir = genProtoDir;
        this.genGrpcDir = genGrpcDir;
    }

    public static void processDescriptorSetFile(File setFile,
                                                String generatedProtoDir,
                                                String generatedGrpcDir) {
        final Collection<FileDescriptorProto> descriptors =
                FileDescriptors.parseSkipStandard(setFile.getPath());
        final AnnotatorFactory factory =
                new AnnotatorFactory(descriptors, generatedProtoDir, generatedGrpcDir);

        factory.createFileAnnotator(Experimental.class, experimentalAll)
               .annotate();
        factory.createMessageAnnotator(Experimental.class, experimentalType)
               .annotate();
        factory.createFieldAnnotator(Experimental.class, experimental)
               .annotate();

        factory.createFileAnnotator(Beta.class, betaAll)
               .annotate();
        factory.createMessageAnnotator(Beta.class, betaType)
               .annotate();
        factory.createFieldAnnotator(Beta.class, beta)
               .annotate();

        factory.createFileAnnotator(SPI.class, sPIAll)
               .annotate();
        factory.createMessageAnnotator(SPI.class, sPIType)
               .annotate();
        factory.createServiceAnnotator(SPI.class, sPIService)
               .annotate();
        factory.createFieldAnnotator(SPI.class, sPI)
               .annotate();

        factory.createFileAnnotator(Internal.class, internalAll)
               .annotate();
        factory.createMessageAnnotator(Internal.class, internalType)
               .annotate();
        factory.createFieldAnnotator(Internal.class, internal)
               .annotate();
    }

    private Annotator createFileAnnotator(Class<? extends Annotation> annotation,
                                          GeneratedExtension<FileOptions, Boolean> option) {
        return new FileAnnotator(annotation, option, fileDescriptors, genProtoDir, genGrpcDir);
    }

    private Annotator createMessageAnnotator(Class<? extends Annotation> annotation,
                                             GeneratedExtension<MessageOptions, Boolean> option) {
        return new MessageAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    private Annotator createFieldAnnotator(Class<? extends Annotation> annotation,
                                           GeneratedExtension<FieldOptions, Boolean> option) {
        return new FieldAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    private Annotator createServiceAnnotator(Class<? extends Annotation> annotation,
                                             GeneratedExtension<ServiceOptions, Boolean> option) {
        return new ServiceAnnotator(annotation, option, fileDescriptors, genGrpcDir);
    }
}
