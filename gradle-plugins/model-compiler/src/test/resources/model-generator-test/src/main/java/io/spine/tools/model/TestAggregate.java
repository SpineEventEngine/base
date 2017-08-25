package io.spine.tools.model;

import io.spine.server.aggregate.Aggregate;
import io.spine.server.command.Assign;
import io.spine.validate.AnyVBuilder;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.util.Collections;
import java.util.List;

class TestAggregate extends Aggregate<String, Any, AnyVBuilder> {

    public TestAggregate(String id) {
        super(id);
    }

    @Assign
    List<Message> handle(Any command) {
        return Collections.emptyList();
    }
}
