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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Alert implements Parcelable {

    private boolean mOn;
    private String mLabel = "Add Label";
    private int mId;
    private int mFrequency = 0;

    // Scheduling components
    private boolean mSchedule;
    private boolean[] mDaysEnabled = new boolean[]{true, true, true, true, true, true, true};
    private int mStartHour = 0;
    private int mStartMinute = 0;
    private int mEndHour = 23;
    private int mEndMinute = 59;

    // Notification style options
    private boolean mVibrate;
    private boolean mLight;
    private boolean mMute;
    private String mToneURI;
    private String mToneTitle;

    // RecycleView consistency flags
    private boolean mExpanded = true;
    private boolean mNewlyCreated = true;

    public Alert() {
    }

    /**
     * @param toneTitle name of the selected ringtone to be displayed to the user
     */
    public void setToneDisplay(String toneTitle) {
        this.mToneTitle = toneTitle;
    }

    /**
     * @return the name of the chosen notification sound. If none are chosen, {@link #getToneURI()}
     * return the devices default notification sound and this returns the generic name "Default" to
     * match.
     */
    public String getToneDisplay() {
        if (mToneURI != null) {
            return mToneTitle;
        } else {
            return "Default";
        }
    }

    /**
     * @return the URI of the ringtone the user has chosen. If none have been set, returns the
     * devices default notification sound.
     */
    public Uri getToneURI() {
        if (mToneURI != null) {
            return Uri.parse(mToneURI);
        } else {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    /**
     * @return a string in (hour)h (minute)' (second)" format to display the chosen frequency to the
     * user
     */
    public String getFrequencyDisplay() {
        int hours = mFrequency / 3600;
        int minutes = (mFrequency / 60) % 60;
        int seconds = mFrequency % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "' " + seconds + "\"";
        }

        if (minutes > 0) {
            return minutes + "' " + seconds + "\"";
        }

        if (seconds > 0) {
            return seconds + "\"";
        } else {
            return "Set Frequency";
        }
    }

    /**
     * @return a combined start and end time time range to be displayed to the user, represents the
     * hours of the day they have chosen for the alert to be enabled. example: 12:00am - 1:45pm
     */
    public String getTimeDisplay() {
        if (mSchedule) {
            return getStartTimeDisplay() + "-" + getEndTimeDisplay();
        } else {
            return "Always";
        }
    }

    /**
     * @return a readable form of the users chosen time to start the alert on each scheduled day.
     * example: 12:00am
     */
    public String getStartTimeDisplay() {
        if (mSchedule) {
            String amPM;
            int displayHour = mStartHour;
            if (mStartHour > 11) {
                amPM = "pm";
                displayHour -= 12;
            } else {
                amPM = "am";
            }

            if (displayHour == 0) {
                displayHour = 12;
            }

            return String.format("%1$2s", displayHour) + ":" + String.format("%02d", mStartMinute) + amPM;
        } else {
            return "";
        }
    }

    /**
     * @return a readable form of the users chosen time to end the alert on each scheduled day.
     * example: 11:00pm
     */
    public String getEndTimeDisplay() {
        if (mSchedule) {
            String amPM;
            int displayHour = mEndHour;
            if (mEndHour > 11) {
                amPM = "pm";
                displayHour -= 12;
            } else {
                amPM = "am";
            }

            if (displayHour == 0) {
                displayHour = 12;
            }

            return String.format("%1$2s", displayHour) + ":" + String.format("%02d", mEndMinute) + amPM;
        } else {
            return "";
        }
    }

    /**
     * @return string representation of all days the user has enabled for scheduling. example: Mon,
     * Tue, Thu, Sat
     */
    public String getDaysDisplay() {

        if(mSchedule) {
            StringBuilder daysDisplay = new StringBuilder();

            for (int i = 0; i < mDaysEnabled.length; i++) {
                if (mDaysEnabled[i]) {
                    switch (i) {
                        case 0:
                            daysDisplay.append(" Sun,");
                            break;
                        case 1:
                            daysDisplay.append(" Mon,");
                            break;
                        case 2:
                            daysDisplay.append(" Tue,");
                            break;
                        case 3:
                            daysDisplay.append(" Wed,");
                            break;
                        case 4:
                            daysDisplay.append(" Thu,");
                            break;
                        case 5:
                            daysDisplay.append(" Fri,");
                            break;
                        case 6:
                            daysDisplay.append(" Sat,");
                            break;
                        default:
                            break;
                    }
                }
            }

            if (daysDisplay.length() > 0) {
                daysDisplay.deleteCharAt(daysDisplay.length() - 1);
                daysDisplay.deleteCharAt(0);
            }

            return daysDisplay.toString();
        } else {
            return "Every day";
        }
    }

    /**
     * Name/note set to an alert to will be displayed to the user in the GUI and in notifications.
     * i.e. "Do 10 pushups" or "Take medicine"
     *
     * @return the user defined label or "Set Label" if they have not defined one.
     */
    public String getLabel() {
        if (mLabel == null || mLabel.equals("")) {
            return "Set Label";
        }

        return mLabel;
    }

    /**
     * @param label Name/note set to an alert to will be displayed to the user in the GUI and in
     *              notifications. i.e. "Do 10 pushups" or "Take medicine"
     */
    public void setLabel(String label) {
        if (label.length() > 20) {
            label = label.substring(0, 20);
        }
        this.mLabel = label;
    }

    /**
     * @return how often the Alert will be fired in seconds
     */
    public int getFrequency() {
        return mFrequency;
    }

    /**
     * @param frequency how often the Alert will be fired in seconds
     */
    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }

    /**
     * @return does the user want the notification to vibrate the device
     */
    public boolean isVibrate() {
        return mVibrate;
    }

    /**
     * @param vibrate does the user want the notification to vibrate the device
     */
    public void setVibrate(boolean vibrate) {
        this.mVibrate = vibrate;
    }

    /**
     * @param dayOfWeek day of the week to check whether the alert is scheduled to fire on (0-6 for
     *                  Sunday-Saturday)
     * @return whether the alert is scheduled to fire on the given day of the week
     */
    public boolean isDayEnabled(int dayOfWeek) {
        return mDaysEnabled[dayOfWeek];
    }

    /**
     * @param isScheduled is the given day enabled
     * @param dayOfWeek   day of the week to enable or disable scheduling on (0-6 for
     *                    Sunday-Saturday)
     */
    public void setDayEnabled(boolean isScheduled, int dayOfWeek) {
        mDaysEnabled[dayOfWeek] = isScheduled;
    }

    /**
     * @param hour   hour of the day to start the alert firing on scheduled days if scheduling is
     *               enabled
     * @param minute minute of the starting hour to start the alert on scheduled days if scheduling
     *               is enabled
     */
    public void setStartTime(int hour, int minute) {
        this.mStartHour = hour;
        this.mStartMinute = minute;
    }

    /**
     * @param hour   hour of the day to stop the alert firing on scheduled days if scheduling is
     *               enabled
     * @param minute minute of the starting hour to stop the alert on scheduled days if scheduling
     *               is enabled
     */
    public void setEndTime(int hour, int minute) {
        this.mEndHour = hour;
        this.mEndMinute = minute;
    }

    /**
     * If scheduling is chosen, this represents the hour of the day the repeating notifications will
     * begin.
     *
     * @return hour of the day to start firing notifications, 0-23 (24-hour time)
     */
    public int getStartHour() {
        return mStartHour;
    }

    /**
     * If schedule is chosen, this represents the minute of the hour the repeating notifications
     * will begin.
     *
     * @return minute of the starting hour to begin firing notifications if scheduling is enabled.
     */
    public int getStartMinute() {
        return mStartMinute;
    }

    /**
     * If scheduling is chosen, this represents the hour of the day the repeating notifications will
     * end.
     *
     * @return hour of the day to stop firing notifications, 0-23 (24-hour time)
     */
    public int getEndHour() {
        return mEndHour;
    }

    /**
     * If schedule is chosen, this represents the minute of the hour the repeating notifications
     * will end.
     *
     * @return minute of the end hour to begin firing notifications if scheduling is enabled.
     */
    public int getEndMinute() {
        return mEndMinute;
    }

    /**
     * @param toneURI the Uri of the sound clip the notification will play
     */
    public void setToneURI(Uri toneURI) {
        if (toneURI != null) {
            this.mToneURI = toneURI.toString();
        } else {
            this.mToneURI = null;
        }
    }

    /**
     * This is used only for view recycling.
     *
     * @return has the user expanded the AlertView to display extra options
     */
    public boolean isExpanded() {
        return mExpanded;
    }

    /**
     * This is used only for view recycling.
     *
     * @param expanded has the user expanded the AlertView to display extra options
     */
    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
    }

    /**
     * @return has the user requested scheduling
     */
    public boolean isSchedule() {
        return mSchedule;
    }

    /**
     * @param schedule has the user requested scheduling
     */
    public void setSchedule(boolean schedule) {
        this.mSchedule = schedule;
    }

    /**
     * Used as the sole decider for whether an alert should be triggered. Called on boot through
     * {@link nkarasch.repeatingreminder.scheduling.BootBroadcastReceiver}, and {@link
     * nkarasch.repeatingreminder.gui.AlertView} for managing the on-off switch.
     *
     * @return is the alert enabled
     */
    public boolean isOn() {
        return mOn;
    }

    /**
     * @param on is the alert enabled
     */
    public void setOn(boolean on) {
        this.mOn = on;
    }

    /**
     * Unique id for each Alert. Used for starting and stopping the {@link
     * nkarasch.repeatingreminder.scheduling.AlertBroadcastReceiver}
     *
     * @return value that is unique to this Alert
     */
    public int getId() {
        return mId;
    }

    /**
     * @param id unique id, will be used for starting and stopping the {@link
     *           nkarasch.repeatingreminder.scheduling.AlertBroadcastReceiver}
     */
    public void setId(int id) {
        this.mId = id;
    }

    /**
     * @return has the user requested the devices light flash when the notification is fired
     */
    public boolean isLight() {
        return mLight;
    }

    /**
     * @param light has the user requested the devices light flash when the notification is fired
     */
    public void setLight(boolean light) {
        this.mLight = light;
    }

    /**
     * @return has the user requested the notification be silent
     */
    public boolean isMute() {
        return mMute;
    }

    /**
     * @param mute has the user requested the notification be silent
     */
    public void setMute(boolean mute) {
        this.mMute = mute;
    }

    /**
     * When a new Alert is created by the user the frequency chooser dialog needs to pop up. This
     * boolean is used to determine whether it is needed.
     *
     * @return does the frequency chooser need to appear on {@link nkarasch.repeatingreminder.gui.AlertView}
     * load?
     */
    public boolean isNewlyCreated() {
        return mNewlyCreated;
    }

    /**
     * Sets mNewlyCreated to false, the frequency chooser has already automatically popped up and
     * does not need to again.
     */
    public void disableNewlyCreated() {
        mNewlyCreated = false;
    }

    protected Alert(Parcel in) {
        mOn = in.readByte() != 0x00;
        mLabel = in.readString();
        mId = in.readInt();
        mFrequency = in.readInt();
        mSchedule = in.readByte() != 0x00;
        in.readBooleanArray(mDaysEnabled);
        mStartHour = in.readInt();
        mStartMinute = in.readInt();
        mEndHour = in.readInt();
        mEndMinute = in.readInt();
        mVibrate = in.readByte() != 0x00;
        mLight = in.readByte() != 0x00;
        mMute = in.readByte() != 0x00;
        mToneURI = in.readString();
        mToneTitle = in.readString();
        mExpanded = in.readByte() != 0x00;
        mNewlyCreated = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mOn ? 0x01 : 0x00));
        dest.writeString(mLabel);
        dest.writeInt(mId);
        dest.writeInt(mFrequency);
        dest.writeByte((byte) (mSchedule ? 0x01 : 0x00));
        dest.writeBooleanArray(mDaysEnabled);
        dest.writeInt(mStartHour);
        dest.writeInt(mStartMinute);
        dest.writeInt(mEndHour);
        dest.writeInt(mEndMinute);
        dest.writeByte((byte) (mVibrate ? 0x01 : 0x00));
        dest.writeByte((byte) (mLight ? 0x01 : 0x00));
        dest.writeByte((byte) (mMute ? 0x01 : 0x00));
        dest.writeString(mToneURI);
        dest.writeString(mToneTitle);
        dest.writeByte((byte) (mExpanded ? 0x01 : 0x00));
        dest.writeByte((byte) (mNewlyCreated ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Alert> CREATOR = new Parcelable.Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel in) {
            return new Alert(in);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };
}