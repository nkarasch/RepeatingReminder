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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import nkarasch.repeatingreminder.Alert;

public class AlarmHandler {

    private final Context mContext;

    /**
     * Handles stopping and starting alerts. AlarmManager is used for starting the initial
     * BroadcastReceiver run, it is NOT repeating. Every subsequent arming of the BroadcastReceiver
     * is done manually from within when it is fired.
     *
     * Called by {@link BootBroadcastReceiver} if there are {@link Alert}'s set to an "On" state at
     * Boot.
     *
     * Most often called by {@link nkarasch.repeatingreminder.gui.AlertView} for starting or stopping
     * them when the on-off switch changes state.
     *
     * @param context any context
     */
    public AlarmHandler(Context context) {
        this.mContext = context;
    }

    public void startAlert(Alert alert) {
        Intent intent = new Intent(mContext, AlertBroadcastReceiver.class);
        intent.putExtra("alert", alert);
        intent.putExtra("initial", true);
        stopAlert(alert);

        PendingIntent sender = PendingIntent.getBroadcast(mContext, alert.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), sender);
    }

    public void stopAlert(Alert alert) {
        Intent intent = new Intent(mContext, AlertBroadcastReceiver.class);
        PendingIntent.getBroadcast(mContext, alert.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }
}

