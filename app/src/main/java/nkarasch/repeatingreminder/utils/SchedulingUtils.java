package nkarasch.repeatingreminder.utils;

import java.util.Calendar;

import nkarasch.repeatingreminder.Alert;

/*
 * Copyright (C) 2015-2016 Nick Karasch <nkarasch@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SchedulingUtils {


    public enum DAY_POSITION {BEFORE_START, BETWEEN, AFTER_END}

    public static Calendar getNextAlertTime(Calendar calendar, Alert alert) {

        calendar.add(Calendar.SECOND, alert.getFrequency());
        if (!alert.isSchedule()) {
            return calendar;
        }

        if (isLegalDay(calendar, alert)) {
            switch (getTimePosition(calendar, alert)) {
                case BETWEEN:
                    return calendar;
                case BEFORE_START:
                    calendar.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
                    calendar.set(Calendar.MINUTE, alert.getStartMinute());
                    calendar.set(Calendar.SECOND, 0);
                    return calendar;
                case AFTER_END:
                    setCalendarToStartDay(calendar, alert);
                    calendar.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
                    calendar.set(Calendar.MINUTE, alert.getStartMinute());
                    calendar.set(Calendar.SECOND, 0);
                    return calendar;
            }

        }

        setCalendarToStartDay(calendar, alert);
        calendar.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
        calendar.set(Calendar.MINUTE, alert.getStartMinute());
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public static Calendar setCalendarToStartDay(Calendar calendar, Alert alert) {
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int nextDay;
        if (currentDay == Calendar.SATURDAY) {
            nextDay = Calendar.SUNDAY;
        } else {
            nextDay = currentDay + 1;
        }

        for (int i = nextDay; i != currentDay; ++i) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (alert.isDayEnabled(i - 1)) {
                return calendar;
            }

            if (i >= Calendar.SATURDAY) {
                i = 0;
            }
        }
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    public static DAY_POSITION getTimePosition(Calendar calendar, Alert alert) {

        Calendar startTime = (Calendar)calendar.clone();
        startTime.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
        startTime.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
        startTime.set(Calendar.MINUTE, alert.getStartMinute());
        startTime.set(Calendar.SECOND, 0);
        startTime.add(Calendar.SECOND, -1);

        Calendar endTime = (Calendar)calendar.clone();
        endTime.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
        endTime.set(Calendar.HOUR_OF_DAY, alert.getEndHour());
        endTime.set(Calendar.MINUTE, alert.getEndMinute());
        endTime.set(Calendar.SECOND, 0);
        endTime.add(Calendar.SECOND, 61);

        if (calendar.after(startTime) && calendar.before(endTime)) {
            return DAY_POSITION.BETWEEN;
        } else if (calendar.after(endTime)) {
            return DAY_POSITION.AFTER_END;
        } else if (calendar.before(startTime)) {
            return DAY_POSITION.BEFORE_START;
        } else {
            return DAY_POSITION.BETWEEN;
        }
    }

    private static boolean isLegalDay(Calendar calendar, Alert alert) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return (alert.isDayEnabled(day - 1));
    }
}
