package nkarasch.repeatingreminder.utils;
/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.graphics.Color;

import java.util.Calendar;

public class ColorUtils {

    /**
     * Provides varying background colors based on the time of day.
     * These functions are taken from the DeskClock of the Android Open Source Project (Lollipop).
     * https://android.googlesource.com/platform/packages/apps/DeskClock/+/lollipop-release
     */

    private static final String[] BACKGROUND_SPECTRUM = {"#212121", "#27232e", "#2d253a",
            "#332847", "#382a53", "#3e2c5f", "#442e6c", "#393a7a", "#2e4687", "#235395", "#185fa2",
            "#0d6baf", "#0277bd", "#0d6cb1", "#1861a6", "#23569b", "#2d4a8f", "#383f84", "#433478",
            "#3d3169", "#382e5b", "#322b4d", "#2c273e", "#272430"};

    private static final float TINTED_LEVEL = 0.09f;

    public static int getCurrentHourColor() {
        final int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return Color.parseColor(BACKGROUND_SPECTRUM[hourOfDay]);
    }

    public static int getTintedBackgroundColor() {
        final int c = getCurrentHourColor();
        final int red = Color.red(c) + (int) (TINTED_LEVEL * (255 - Color.red(c)));
        final int green = Color.green(c) + (int) (TINTED_LEVEL * (255 - Color.green(c)));
        final int blue = Color.blue(c) + (int) (TINTED_LEVEL * (255 - Color.blue(c)));
        return Color.rgb(red, green, blue);
    }
}
