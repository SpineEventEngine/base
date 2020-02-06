package io.spine.tools.protoc.given;

import io.spine.base.Field;
import io.spine.base.SubscribableField;

/**
 * A test-only subscribable field to mark the generated strongly-typed fields with.
 *
 * <p>See {@code build.gradle} for usage.
 */
public final class ProjectNameField extends SubscribableField {

    public ProjectNameField(Field field) {
        super(field);
    }
}
