package com.example.evm.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for handling date/time with Vietnam timezone (UTC+7)
 */
public class DateTimeUtils {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Get current date and time in Vietnam timezone (UTC+7)
     * @return LocalDateTime in Vietnam timezone
     */
    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    /**
     * Convert a LocalDateTime to Vietnam timezone
     * @param dateTime the LocalDateTime to convert
     * @return LocalDateTime in Vietnam timezone
     */
    public static LocalDateTime toVietnamTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        // If the dateTime is already in system default timezone, convert it
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.withZoneSameInstant(VIETNAM_ZONE).toLocalDateTime();
    }
}

