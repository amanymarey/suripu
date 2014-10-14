package com.hello.suripu.core.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum QueueName {

    PILL_DATA ("pill_data"),
    MORPHEUS_DATA ("morpheus_data"),
    AUDIO_FEATURES("audio_features"),
    ACTIVITY_STREAM("activity_stream");

    private String value;

    private QueueName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QueueName fromString(final String val) {
        final QueueName[] queueNames = QueueName.values();

        for (final QueueName queueName: queueNames) {
            if (queueName.value.equals(val)) {
                return queueName;
            }
        }

        throw new IllegalArgumentException(String.format("%s is not a valid KinesisStreamName", val));
    }
}
