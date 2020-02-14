package io.spine.tools.protoc.given;

import io.spine.base.Field;
import io.spine.base.SubscribableField;

/**
 * A test-only subscribable field which marks the generated strongly-typed fields of a message.
 *
 * <p>See {@code build.gradle} for usage.
 */
public final class ProjectNameField extends SubscribableField {

    public ProjectNameField(Field field) {
        super(field);
    }
}
