package nkarasch.repeatingreminder.scheduling;
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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;
import java.util.Date;

import nkarasch.repeatingreminder.Alert;
import nkarasch.repeatingreminder.MainActivity;
import nkarasch.repeatingreminder.R;
import nkarasch.repeatingreminder.utils.DataUtils;
import nkarasch.repeatingreminder.utils.SchedulingUtils;


public class AlertBroadcastReceiver extends BroadcastReceiver {

    /**
     * Schedules the next firing of this BroadcastReceiver based on the data in the Alert and fires the
     * Alert if appropriate
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        byte[] serialized = intent.getByteArrayExtra("alert");
        boolean initial = intent.getBooleanExtra("initial", false);
        intent.putExtra("initial", false);
        Alert alert = (Alert) DataUtils.deserialize(serialized);

        Calendar nextTime = SchedulingUtils.getNextAlertTime(Calendar.getInstance(), alert);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, alert.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTime.getTimeInMillis(), sender);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime.getTimeInMillis(), sender);
        }

        fireNotification(context, alert, nextTime, initial);

    }

    private void fireNotification(final Context context, Alert alert, Calendar nextAlertTime, boolean initial) {
         final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(alert.getLabel());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Every: " + alert.getLongFormFrequencyDisplay() + "\n" + "Next alert at: " + java.text.DateFormat.getDateTimeInstance().format(new Date(nextAlertTime.getTimeInMillis()))));


        if (!initial) {
            if (alert.isVibrate()) {
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
            }

            if (alert.isWake()) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, "RepeatingReminder" + String.valueOf(alert.getId()));
                wakeLock.acquire(100);
            }

            if (!alert.isMute()) {
                builder.setSound(alert.getToneURI());
            }

            builder.setUsesChronometer(true);
        }

        builder.setAutoCancel(false);
        builder.setOngoing(true);
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
        mNotificationManager.cancel(alert.getId());
        mNotificationManager.notify(alert.getId(), builder.build());
    }
}
