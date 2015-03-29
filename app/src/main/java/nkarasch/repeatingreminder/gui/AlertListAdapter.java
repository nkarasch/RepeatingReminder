package nkarasch.repeatingreminder.gui;
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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.List;

import nkarasch.repeatingreminder.scheduling.AlarmHandler;
import nkarasch.repeatingreminder.Alert;
import nkarasch.repeatingreminder.R;
import nkarasch.repeatingreminder.utils.DataUtils;

public class AlertListAdapter extends ArrayAdapter<Alert> {

    private final ActionBarActivity mContext;
    private final AlarmHandler mAlarmHandler;
    private final LayoutInflater mLayoutInflater;
    private final List<Alert> mAlertList;

    public AlertListAdapter(ActionBarActivity context, AlarmHandler alarmHandler, List<Alert> alertList) {
        super(context, R.layout.alert, alertList);
        mContext = context;
        mAlarmHandler = alarmHandler;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAlertList = alertList;

        ListView listView = (ListView) context.findViewById(R.id.listView);

        //add footer (plus button, for adding a new item to the ListView)
        View footerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.alert_list_footer, null, false);
        listView.addFooterView(footerView);
        listView.setAdapter(this);

        ImageButton addButton = (ImageButton) context.findViewById(R.id.ib_footer_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alert alert;
                add(alert = new Alert());

               /*Every item in the list needs a unique, consistent ID for starting and stopping the BroadcastReceiver.
                 The highest integer that has been used is stored in shared preferences, each item added
                 increments it by one.*/
                SharedPreferences preferences = DataUtils.getSharedPreferences(AlertListAdapter.this.mContext);
                int id = preferences.getInt("max_id", 0);
                id++;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("max_id", id);
                editor.apply();

                alert.setId(id);
                saveData();
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * Using a custom View instead of the traditional "Holder" pattern.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AlertView view;
        if (convertView == null) {
            view = (AlertView) mLayoutInflater.inflate(R.layout.alert, null);
            if (Build.VERSION.SDK_INT < 21) {
                //create divider line in old APIs where elevation is not available
                View line = new View(mContext);
                line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1));
                line.setBackgroundColor(Color.rgb(51, 51, 51));
                view.addView(line);
            }
        } else {
            view = (AlertView) convertView;
        }

        view.update(this, mAlarmHandler, position);
        return view;
    }

    /**
     * Backs up the list of {@link Alert} to SharedPreferences
     */
    public void saveData() {
        DataUtils.listToPreferences(mContext, mAlertList);
    }
}
