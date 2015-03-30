package nkarasch.repeatingreminder.scheduling;
/*
 * Copyright (C) 2015 Nick Karasch <nkarasch@gmail.com>
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
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Calendar;

import nkarasch.repeatingreminder.Alert;
import nkarasch.repeatingreminder.MainActivity;
import nkarasch.repeatingreminder.R;


public class AlertBroadcastReceiver extends BroadcastReceiver {

    public enum DAY_POSITION {BEFORE_START, BETWEEN, AFTER_END}

    /**
     * Schedules the next firing of this BroadcastReceiver based on the data in the Alert and fires the
     * Alert if appropriate
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Alert alert = intent.getParcelableExtra("alert");
        boolean initial = intent.getBooleanExtra("initial", false);

        intent.putExtra("initial", false);
        Calendar nextTime = getNextTime(Calendar.getInstance(), alert);
        if (nextTime != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(context, alert.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime.getTimeInMillis(), sender);
        }

        if(initial){
            return;
        }

        Calendar calendar = Calendar.getInstance();
        if (!alert.isSchedule()) {
            fireNotification(context, alert);
        } else {
            if (isLegalDay(calendar, alert) && isLegalTime(calendar, alert) == DAY_POSITION.BETWEEN) {
                fireNotification(context, alert);
            }
        }
    }

    private void fireNotification(Context context, Alert alert) {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(alert.getLabel());

        if (alert.isVibrate()) {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }

        if (alert.isLight()) {
            builder.setLights(0xffff00ff, 1000, 300);
        }

        if (!alert.isMute()) {
            builder.setSound(alert.getToneURI());
        }
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent homeIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(homeIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(alert.getId(), builder.build());
    }

    private Calendar getNextTime(Calendar calendar, Alert alert) {

        if (alert.isSchedule()) {
            if (isLegalDay(calendar, alert)) {
                calendar.add(Calendar.SECOND, alert.getFrequency());
                DAY_POSITION position = isLegalTime(calendar, alert);

                switch (position) {
                    case BETWEEN:
                        return calendar;
                    case BEFORE_START:
                        calendar.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
                        calendar.set(Calendar.MINUTE, alert.getStartMinute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.add(Calendar.SECOND, alert.getFrequency());
                        return calendar;
                    case AFTER_END:
                        getDayToStart(calendar, alert);
                        break;
                    default:
                        return null;
                }

                return calendar;
            } else {
                return getDayToStart(calendar, alert);
            }

        } else {
            //not scheduled
            calendar.add(Calendar.SECOND, alert.getFrequency());
            return calendar;
        }
    }

    private Calendar getDayToStart(Calendar calendar, Alert alert) {
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (currentDay == 6) {
            currentDay = 0;
        } else {
            currentDay += 1;
        }

        boolean nextWeek = false;
        for (int i = currentDay; !(i == currentDay && nextWeek); i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (alert.isDayEnabled(i)) {
                calendar.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
                calendar.set(Calendar.MINUTE, alert.getStartMinute());
                calendar.add(Calendar.SECOND, alert.getFrequency());
                return calendar;
            }

            if (i >= 6) {
                i = -1;
                nextWeek = true;
            }
        }
        return null;
    }

    private DAY_POSITION isLegalTime(Calendar calendar, Alert alert) {

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
        startTime.set(Calendar.HOUR_OF_DAY, alert.getStartHour());
        startTime.set(Calendar.MINUTE, alert.getStartMinute());
        startTime.set(Calendar.SECOND, 0);
        startTime.add(Calendar.SECOND, -1);

        Calendar endTime = Calendar.getInstance();
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

    private boolean isLegalDay(Calendar calendar, Alert alert) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        return (alert.isDayEnabled(day - 1));
    }
}
