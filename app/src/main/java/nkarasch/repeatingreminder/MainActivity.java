package nkarasch.repeatingreminder;
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
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;

import nkarasch.repeatingreminder.gui.AlertListAdapter;
import nkarasch.repeatingreminder.scheduling.AlarmHandler;
import nkarasch.repeatingreminder.utils.ColorUtils;
import nkarasch.repeatingreminder.utils.DataUtils;

public class MainActivity extends ActionBarActivity {

    private AlertListAdapter mArrayAdapter;
    private List<Alert> mAlertList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((mAlertList = DataUtils.listFromPreferences(this)) == null) {
            mAlertList = new ArrayList<>();
        }

        AlarmHandler alarmHandler = new AlarmHandler(this);
        mArrayAdapter = new AlertListAdapter(this, alarmHandler, mAlertList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.rl_main_activity).setBackgroundColor(ColorUtils.getCurrentHourColor());
    }


    /**
     * For the ringtone chooser
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Alert alert = mAlertList.get(getIntent().getIntExtra("array_index", 0));
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                if (ringtone != null) {
                    String title = ringtone.getTitle(this);
                    alert.setToneURI(uri);
                    alert.setToneDisplay(title);
                }
            }

            mArrayAdapter.notifyDataSetChanged();
            mArrayAdapter.saveData();
        }
    }
}