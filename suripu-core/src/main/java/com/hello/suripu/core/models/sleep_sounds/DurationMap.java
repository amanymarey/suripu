package com.hello.suripu.core.models.sleep_sounds;

import com.google.common.base.Optional;

/**
 * Created by jakepiccolo on 4/4/16.
 */
public interface DurationMap {
    Optional<Duration> getDurationBySeconds(final Integer durationSeconds);
}
