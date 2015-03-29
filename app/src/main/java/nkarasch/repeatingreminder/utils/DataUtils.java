package nkarasch.repeatingreminder.utils;
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
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import nkarasch.repeatingreminder.Alert;

public class DataUtils {

    private static final String ALERT_LIST = "alert_list";
    private static final String PREF_NAME = "nkarasch.metronomelife";
    private static Gson gson;

    public static void listToPreferences(Context context, List<Alert> alertList) {

        Type alertListType = new TypeToken<List<Alert>>() {
        }.getType();
        String json = getGSON().toJson(alertList, alertListType);

        SharedPreferences preferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(ALERT_LIST, json);
        editor.apply();
    }

    public static List<Alert> listFromPreferences(Context context) {

        Type alertListType = new TypeToken<List<Alert>>() {
        }.getType();

        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences.contains(ALERT_LIST)) {
            String json = preferences.getString(ALERT_LIST, null);
            return getGSON().fromJson(json, alertListType);
        } else {
            return null;
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static Gson getGSON() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
