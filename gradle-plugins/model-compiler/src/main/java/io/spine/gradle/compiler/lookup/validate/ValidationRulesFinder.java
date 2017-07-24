package io.spine.gradle.compiler.lookup.validate;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

class ValidationRulesFinder {

    private final FileDescriptorProto fileDescriptor;

    ValidationRulesFinder(FileDescriptorProto fileDescriptor) {
        this.fileDescriptor = checkNotNull(fileDescriptor);
    }

    Map<String, String> findRules() {
        return Collections.emptyMap();
    }
}
