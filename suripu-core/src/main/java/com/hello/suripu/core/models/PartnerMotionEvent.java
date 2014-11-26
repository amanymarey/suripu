package com.hello.suripu.core.models;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by pangwu on 11/22/14.
 */
public class PartnerMotionEvent extends SleepMotionEvent {
    public PartnerMotionEvent(long startTimestamp, long endTimestamp, int offsetMillis, int sleepDepth) {
        super(startTimestamp, endTimestamp, offsetMillis, sleepDepth);
        this.setType(Type.PARTNER_MOTION);
    }

    @Override
    public String getDescription(){
        return String.format("Your partner kicked you at %s",
                new DateTime(this.getStartTimestamp(), DateTimeZone.forOffsetMillis(this.getTimezoneOffset()))
                        .toString(DateTimeFormat.forPattern("HH:mm a")));
    }
}
