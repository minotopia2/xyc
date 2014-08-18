/*
 * Copyright (c) 2014 xxyy (Philipp Nowak; devnull@nowak-at.net). All rights reserved.
 *
 * Any usage, including, but not limited to, compiling, running, redistributing, printing, copying and reverse-engineering is strictly prohibited without permission from the original author and may result in legal steps being taken.
 */

package io.github.xxyy.common.misc;

import java.util.Calendar;

/**
 * Simple class to manage unix timestamps.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 */
public class UnixTimestamp {
    private long seconds = 0;

    private UnixTimestamp(long seconds) {
        this.seconds = seconds;
    }

    /**
     * @return Amount of milliseconds that passed since 1.1.1970 for this object.
     */
    public long getMillis() {
        return this.seconds * 1000;
    }

    /**
     * @return Amount of seconds that passed since 1.1.1970 for this object.
     */
    public long getSeconds() {
        return this.seconds;
    }

    /**
     * Gets a new {@link UnixTimestamp} from a amount of milliseconds that passed since 1.1.1970.
     *
     * @param millis the amount of milliseconds since Jan 1 1970 for this timestamp
     * @return a unix timestamp object with the specified parameters
     */
    public static UnixTimestamp fromMillis(long millis) {
        return new UnixTimestamp(millis / 1000);
    }

    /**
     * Gets a new {@link UnixTimestamp} from a amount of seconds that passed since 1.1.1970.
     * @param seconds the amount of seconds since Jan 1 1970 for this timestamp
     * @return a unix timestamp object with the specified parameters
     */
    public static UnixTimestamp fromSeconds(long seconds) {
        return new UnixTimestamp(seconds);
    }

    /**
     * @return the current {@link UnixTimestamp}.
     */
    public static UnixTimestamp now() {
        return new UnixTimestamp(Calendar.getInstance().getTimeInMillis() / 1000);
    }
}
