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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import nkarasch.repeatingreminder.Alert;
import nkarasch.repeatingreminder.utils.DataUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {

    /**
     * Called on system boot to start any alerts the user has set to "On".
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        AlarmHandler handler = new AlarmHandler(context);
        List<Alert> alertList = DataUtils.listFromPreferences(context);
        if (alertList != null) {
            for (Alert alert : alertList) {
                if (alert.isOn()) {
                    handler.startAlert(alert);
                }
            }
        }
    }
}
