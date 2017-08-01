package io.spine.protobuf;

import com.google.protobuf.Message;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Signals an error working with a protobuf message field.
 *
 * @author Alexander Yevsyukov
 */
public class MessageFieldException extends RuntimeException {

    private final Message protobufMessage;

    /**
     * Constructs a {@code MessageFieldException} with the formatted message text.
     *
     * @param protobufMessage    a Protobuf message object working with a field of which caused
     *                           an error
     * @param errorMessageFormat a formatted string for error message
     * @param params             error message parameters
     */
    public MessageFieldException(Message protobufMessage,
                                 String errorMessageFormat,
                                 @Nullable Object... params) {
        super(formatMessage(checkNotNull(errorMessageFormat), params));
        this.protobufMessage = checkNotNull(protobufMessage);
    }

    /**
     * Constructs a {@code MessageFieldException} without no message text.
     *
     * @param protobufMessage a Protobuf message object working with a field of which caused
     *                        an error
     */
    public MessageFieldException(Message protobufMessage) {
        this(protobufMessage, "");
    }

    /**
     * Obtains a Protobuf message working with a field of which caused an error
     */
    public Message getProtobufMessage() {
        return protobufMessage;
    }

    private static String formatMessage(String format, @Nullable Object... params) {
        if (params == null) {
            return format;
        }
        return String.format(format, params);
    }
}
