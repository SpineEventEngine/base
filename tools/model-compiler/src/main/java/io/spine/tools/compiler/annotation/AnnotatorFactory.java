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
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.annotation.Beta;
import io.spine.annotation.Experimental;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;

import java.io.File;
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
import static java.util.stream.Collectors.toSet;

/**
 * A factory for {@linkplain Annotator Annotators}.
 */
public final class AnnotatorFactory {

    /**
     * Protobuf file descriptors to process.
     */
    private final ImmutableList<FileDescriptor> fileDescriptors;

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

    private AnnotatorFactory(Collection<FileDescriptor> fileDescriptors,
                             String genProtoDir,
                             String genGrpcDir) {
        checkNotNull(fileDescriptors);
        checkArgument(!isNullOrEmpty(genProtoDir));
        checkArgument(!isNullOrEmpty(genGrpcDir));
        this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
        this.genProtoDir = genProtoDir;
        this.genGrpcDir = genGrpcDir;
    }

    public static void process(File descriptorSetFile,
                               String generatedProtoDir,
                               String generatedGrpcDir) {
        Collection<FileDescriptor> descriptors = FileSet
                .parse(descriptorSetFile)
                .files()
                .stream()
                .filter(FileDescriptors::isNotGoogle)
                .collect(toSet());
        AnnotatorFactory factory =
                new AnnotatorFactory(descriptors, generatedProtoDir, generatedGrpcDir);
        annotateExperimentalApi(factory);
        annotateBetaApi(factory);
        annotateSpi(factory);
        annotateInternalApi(factory);
    }

    private static void annotateInternalApi(AnnotatorFactory factory) {
        ClassName internalName = ClassName.of(Internal.class);
        factory.createFileAnnotator(internalName, internalAll)
               .annotate();
        factory.createMessageAnnotator(internalName, internalType)
               .annotate();
        factory.createFieldAnnotator(internalName, internal)
               .annotate();
    }

    private static void annotateSpi(AnnotatorFactory factory) {
        ClassName spiName = ClassName.of(SPI.class);
        factory.createFileAnnotator(spiName, sPIAll)
               .annotate();
        factory.createMessageAnnotator(spiName, sPIType)
               .annotate();
        factory.createServiceAnnotator(spiName, sPIService)
               .annotate();
        factory.createFieldAnnotator(spiName, sPI)
               .annotate();
    }

    private static void annotateBetaApi(AnnotatorFactory factory) {
        ClassName betaName = ClassName.of(Beta.class);
        factory.createFileAnnotator(betaName, betaAll)
               .annotate();
        factory.createMessageAnnotator(betaName, betaType)
               .annotate();
        factory.createFieldAnnotator(betaName, beta)
               .annotate();
    }

    private static void annotateExperimentalApi(AnnotatorFactory factory) {
        ClassName experimentalName = ClassName.of(Experimental.class);
        factory.createFileAnnotator(experimentalName, experimentalAll)
               .annotate();
        factory.createMessageAnnotator(experimentalName, experimentalType)
               .annotate();
        factory.createFieldAnnotator(experimentalName, experimental)
               .annotate();
    }

    private Annotator createFileAnnotator(ClassName annotation,
                                          GeneratedExtension<FileOptions, Boolean> option) {
        return new FileAnnotator(annotation, option, fileDescriptors, genProtoDir, genGrpcDir);
    }

    private Annotator createMessageAnnotator(ClassName annotation,
                                             GeneratedExtension<MessageOptions, Boolean> option) {
        return new MessageAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    private Annotator createFieldAnnotator(ClassName annotation,
                                           GeneratedExtension<FieldOptions, Boolean> option) {
        return new FieldAnnotator(annotation, option, fileDescriptors, genProtoDir);
    }

    private Annotator createServiceAnnotator(ClassName annotation,
                                             GeneratedExtension<ServiceOptions, Boolean> option) {
        return new ServiceAnnotator(annotation, option, fileDescriptors, genGrpcDir);
    }
}
