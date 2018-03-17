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

package io.spine.validate;

import com.google.protobuf.Timestamp;
import io.spine.test.validate.msg.builder.Attachment;
import io.spine.test.validate.msg.builder.Member;
import io.spine.test.validate.msg.builder.ProjectVBuilder;
import io.spine.test.validate.msg.builder.Task;
import org.junit.Before;
import org.junit.Test;

import static com.google.protobuf.ByteString.copyFrom;
import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.base.Identifier.newUuid;
import static io.spine.environment.Time.getCurrentTime;

/**
 * A test suite covering the {@link ValidatingBuilder} behavior.
 *
 * <p>Since most {@code ValidatingBuilders} are generated, the concrete test suits for each of them
 * are not required.
 *
 * <p>Any {@code ValidatingBuilder} implementation should pass these tests. When implementing your
 * own {@code ValidatingBuilder}, be sure to check if it fits the constraints stated below.
 *
 * @author Dmytro Dashenkov
 */
public class ValidatingBuilderShould {

    private ProjectVBuilder builder;

    @Before
    public void setUp() {
        builder = fill();
    }

    @Test(expected = ValidationException.class)
    public void check_required_validated_repeated_fields() {
        builder.addTask(Task.getDefaultInstance());
    }

    @Test(expected = ValidationException.class)
    public void ensure_required_validated_repeated_fields() {
        builder.clearTask();
        builder.build();
    }

    @Test(expected = ValidationException.class)
    public void check_required_validated_map_field_values() {
        builder.putRole("Co-owner", Member.getDefaultInstance());
    }

    @Test(expected = ValidationException.class)
    public void ensure_required_validated_map_fields() {
        builder.clearRole();
        builder.build();
    }

    @Test(expected = ValidationException.class)
    public void check_validated_repeated_fields() {
        builder.addSubscriberEmail("");
    }

    @Test
    public void dispense_with_validated_repeated_fields() {
        builder.clearSubscriberEmail();
        builder.build();
    }

    @Test(expected = ValidationException.class)
    public void check_validated_map_field_values() {
        builder.putAttachment(newUuid(), Attachment.getDefaultInstance());
    }

    @Test
    public void dispense_with_validated_map_fields() {
        builder.clearAttachment();
        builder.build();
    }

    @Test
    public void accept_any_required_repeated_fields() {
        builder.addMember(Member.getDefaultInstance());
    }

    @Test(expected = ValidationException.class)
    public void ensure_required_repeated_fields() {
        builder.clearMember();
        builder.build();
    }

    @Test
    public void accept_any_required_map_field_value() {
        builder.putDeletedTask(newUuid(), timeInFuture());
    }

    @Test(expected = ValidationException.class)
    public void ensure_required_map_fields() {
        builder.clearDeletedTask();
        builder.build();
    }

    @Test
    public void accept_any_unchecked_repeated_fields() {
        builder.addDescription("");
    }

    @Test
    public void dispense_with_unchecked_repeated_fields() {
        builder.clearDescription();
        builder.build();
    }

    @Test
    public void accept_any_unchecked_map_field_value() {
        builder.putLabel("empty", "none");
    }

    @Test
    public void dispense_with_unchecked_map_fields() {
        builder.clearLabel();
        builder.build();
    }

    /**
     * Creates a valid {@link ProjectVBuilder} instance.
     */
    private static ProjectVBuilder fill() {
        final ProjectVBuilder builder = ProjectVBuilder.newBuilder()
                                                       .addTask(task())
                                                       .addMember(member())
                                                       .putRole("Ownner", member())
                                                       .putDeletedTask(newUuid(), timeInPast());
        builder.build(); // Ensure no ValidationException is thrown.
        return builder;
    }

    private static Timestamp timeInPast() {
        return add(getCurrentTime(), fromSeconds(-1000L));
    }

    private static Timestamp timeInFuture() {
        return add(getCurrentTime(), fromSeconds(1000L));
    }

    private static Task task() {
        final Task.Builder task = Task.newBuilder()
                                      .setId(newUuid());
        task.setName("Task name" + task.getId());
        return task.build();
    }

    private static Member member() {
        final Member.Builder member = Member.newBuilder()
                                            .setId(newUuid())
                                            .setName("John Smith")
                                            .setAvatarImage(copyFrom(new byte[] {1, 2, 3}));
        return member.build();
    }
}
