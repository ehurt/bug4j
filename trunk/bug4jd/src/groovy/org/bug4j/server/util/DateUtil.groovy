/*
 * Copyright 2012 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.bug4j.server.util

import java.text.DateFormat
import java.text.SimpleDateFormat

class DateUtil {
    public static enum TimeAdjustType {
        BEGINNING_OF_DAY, END_OF_DAY
    }
    private static final DATE_PATTERN_MM_DD_YYYY = ~/(\d{1,2})\/(\d{1,2})\/(\d{2,4})/
    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)

    public static Date interpretDate(def s, TimeAdjustType adjustType) {
        Date ret = null
        s = s.toString().trim()
        if ("today".equals(s)) {
            ret = new Date()
        } else if ("yesterday".equals(s)) {
            final calendar = new GregorianCalendar()
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            ret = calendar.getTime()
        } else {
            final matcher = DATE_PATTERN_MM_DD_YYYY.matcher(s)
            if (matcher.matches()) {
                int month = matcher.group(1) as int
                int day = matcher.group(2) as int
                int year = matcher.group(3) as int
                if (year < 2000) {
                    year += 2000
                }
                def calendar = new GregorianCalendar(year, month - 1, day)
                ret = calendar.getTime()
            }
        }
        if (ret != null) {
            ret = adjustToDayBoundary(ret, adjustType)
        }
        return ret
    }

    public static Date adjustToDayBoundary(Date date, TimeAdjustType adjustType) {
        final calendarSince = date.toCalendar()
        calendarSince.set(Calendar.HOUR_OF_DAY, 0)
        calendarSince.set(Calendar.MINUTE, 0)
        calendarSince.set(Calendar.SECOND, 0)
        calendarSince.set(Calendar.MILLISECOND, 0)
        if (adjustType == TimeAdjustType.END_OF_DAY) {
            calendarSince.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendarSince.getTime()
    }

    public static int toDays(long timeInMillis) {
        timeInMillis / 1000 / 60 / 60 / 24
    }

    public static String toHumanString(Date date) {
        final tonight = adjustToDayBoundary(new Date(), TimeAdjustType.END_OF_DAY)
        final millisBack = tonight.time - date.time
        final int daysBack = millisBack / (24 * 60 * 60 * 1000)
        switch (daysBack) {
            case 0: return 'today';
            case 1: return 'yesterday';
            case 2: return 'two days ago';
        }
        return null
    }

    public static String formatDate(Date date) {
        final ret = ""
        if (date) {
            synchronized (DATE_FORMAT) {
                ret = DATE_FORMAT.format(date)
            }
            final humanString = toHumanString(date)
            if (humanString) {
                ret += " (" + humanString + ")"
            }
        }
        return ret
    }
}
