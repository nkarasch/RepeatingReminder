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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.hmspicker.HmsPickerBuilder;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import nkarasch.repeatingreminder.scheduling.AlarmHandler;
import nkarasch.repeatingreminder.Alert;
import nkarasch.repeatingreminder.R;
import nkarasch.repeatingreminder.utils.ColorUtils;


public class AlertView extends LinearLayout {

    private static final String[] longDaysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] shortDaysOfWeek = {"S", "M", "T", "W", "T", "F", "S"};

    private static Typeface frequencyFont;

    private final FragmentActivity mContext;
    private final LayoutInflater mLayoutInflater;
    private AlertListAdapter mAdapter;
    private AlarmHandler mAlarmHandler;
    private Alert mAlert;
    private int mPosition;
    private boolean mSettingState;

    //main display components
    @InjectView(R.id.text_frequency) TextView textFrequency;
    @InjectView(R.id.switch_on_off) SwitchCompat switchOnOff;
    @InjectView(R.id.text_label_display) TextView textLabel;
    @InjectView(R.id.text_days_display) TextView textDays;
    @InjectView(R.id.text_times_display) TextView textTimes;
    @InjectView(R.id.iv_expand_down) ImageView imageDownArrow;
    @InjectView(R.id.ll_schedule_expansion) LinearLayout layoutSchedule;

    //expansion components
    @InjectView(R.id.check_vibrate) CheckBox checkVibrate;
    @InjectView(R.id.text_tone) TextView textRingtone;
    @InjectView(R.id.check_schedule) CheckBox checkSchedule;
    @InjectView(R.id.text_time_start) TextView textStartTime;
    @InjectView(R.id.text_time_end) TextView textEndTime;
    @InjectView(R.id.ll_schedule_days) LinearLayout layoutScheduleDays;
    @InjectView(R.id.ll_expansion) LinearLayout layoutExpansion;
    @InjectView(R.id.check_light) CheckBox checkNotification;
    @InjectView(R.id.check_mute) CheckBox checkMute;

    private final Button[] mDayOfWeekButtons = new Button[7];

    public AlertView(Context context) {
        super(context);
        this.mContext = (FragmentActivity) context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public AlertView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (FragmentActivity) context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public AlertView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = (FragmentActivity) context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void update(AlertListAdapter adapter, AlarmHandler alarmHandler, int position) {
        this.mAdapter = adapter;
        this.mAlarmHandler = alarmHandler;
        this.mAlert = adapter.getItem(position);
        this.mPosition = position;
        ButterKnife.inject(this);

        mSettingState = true;
        createDayOfWeekButtons();
        setState();

        if (Build.VERSION.SDK_INT < 16) {
            textFrequency.setTypeface(getFrequencyFont());
        }

        if (mAlert.getFrequency() < 5 && mAlert.isNewlyCreated()) {
            frequencyDisplayOnClick();
            mAlert.disableNewlyCreated();
        }

        mSettingState = false;
    }

    private void setState() {

        if (mAlert.isExpanded()) {
            setBackgroundColor(ColorUtils.getTintedBackgroundColor());
            layoutExpansion.setVisibility(View.VISIBLE);
            imageDownArrow.setVisibility(View.GONE);
        } else {
            setBackgroundColor(ColorUtils.getCurrentHourColor());
            layoutExpansion.setVisibility(View.GONE);
            imageDownArrow.setVisibility(View.VISIBLE);
        }

        textFrequency.setText(mAlert.getFrequencyDisplay());
        switchOnOff.setChecked(mAlert.isOn());
        textLabel.bringToFront();
        textLabel.setText(mAlert.getLabel());
        textDays.setText(mAlert.getDaysDisplay());
        textTimes.setText(mAlert.getTimeDisplay());
        checkNotification.setChecked(mAlert.isLight());
        checkMute.setChecked(mAlert.isMute());

        for (int i = 0; i < 7; i++) {
            if (mAlert.isDayEnabled(i)) {
                turnOnDayOfWeek(mDayOfWeekButtons[i]);
            } else {
                turnOffDayOfWeek(mDayOfWeekButtons[i]);
            }
        }

        checkVibrate.setChecked(mAlert.isVibrate());
        textRingtone.setText(mAlert.getToneDisplay());
        checkSchedule.setChecked(mAlert.isSchedule());
        if (mAlert.isSchedule()) {
            layoutScheduleDays.setVisibility(View.VISIBLE);
            layoutSchedule.setVisibility(View.VISIBLE);
        } else {
            layoutScheduleDays.setVisibility(View.GONE);
            layoutSchedule.setVisibility(View.GONE);
        }

        textDays.setText(mAlert.getDaysDisplay());
        textStartTime.setText(mAlert.getStartTimeDisplay());
        textEndTime.setText(mAlert.getEndTimeDisplay());

    }

    private void createDayOfWeekButtons() {
        for (int i = 0; i < 7; i++) {
            if (mDayOfWeekButtons[i] == null) {
                final Button dayButton = (Button) mLayoutInflater.inflate(
                        R.layout.day_button, layoutScheduleDays, false);
                dayButton.setText(shortDaysOfWeek[i]);
                dayButton.setContentDescription(longDaysOfWeek[i]);
                layoutScheduleDays.addView(dayButton);
                mDayOfWeekButtons[i] = dayButton;
            }
        }

        for (int i = 0; i < 7; i++) {
            final Button button = mDayOfWeekButtons[i];
            final int iterator = i;
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAlert.isDayEnabled(iterator)) {
                        mAlert.setDayEnabled(false, iterator);
                        turnOffDayOfWeek(button);
                    } else {
                        mAlert.setDayEnabled(true, iterator);
                        turnOnDayOfWeek(button);
                    }
                    textDays.setText(mAlert.getDaysDisplay());
                    stopAlert();
                }
            });
        }
    }


    @OnClick(R.id.text_label_display)
    public void labelOnClick() {
        final EditText input = new EditText(mContext);
        input.setSingleLine();
        final int accentColor = getResources().getColor(R.color.accent);
        final int textColor = Color.WHITE;

        final AlertDialog labelDialog = new DialogBuilder(mContext)
                .setTitle("Set Label")
                .setTitleColor(accentColor)
                .setDividerColor(accentColor)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mAlert.setLabel(input.getText().toString());
                        textLabel.setText(mAlert.getLabel());
                        stopAlert();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();

        labelDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                labelDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
                labelDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
            }
        });

        labelDialog.show();
    }

    @OnClick(R.id.text_frequency)
    public void frequencyDisplayOnClick() {
        new HmsPickerBuilder()
                .setFragmentManager(mContext.getSupportFragmentManager())
                .setStyleResId(R.style.frequency_picker_dialog).addHmsPickerDialogHandler(new HmsPickerDialogFragment.HmsPickerDialogHandler() {
            @Override
            public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
                int frequency = hours * 3600 + minutes * 60 + seconds;
                if (frequency >= 5) {
                    mAlert.setFrequency(frequency);
                    if (mAlert.getFrequencyDisplay() != null) {
                        textFrequency.setText(mAlert.getFrequencyDisplay());
                    }
                    stopAlert();
                } else {
                    Toast.makeText(mContext, "Repeating actions at intervals under 5 seconds is disabled.", Toast.LENGTH_LONG).show();
                }
            }
        }).show();
    }

    @OnClick({R.id.rl_display, R.id.ll_expansion})
    public void onDisplayLayoutClicked() {
        if (mAlert.isExpanded()) {
            setBackgroundColor(ColorUtils.getCurrentHourColor());
            mAlert.setExpanded(false);
            collapseView(layoutExpansion);
            imageDownArrow.setVisibility(View.VISIBLE);
        } else {
            setBackgroundColor(ColorUtils.getTintedBackgroundColor());
            mAlert.setExpanded(true);
            expandView(layoutExpansion);
            imageDownArrow.setVisibility(View.GONE);
        }
        mAdapter.saveData();
    }

    @OnCheckedChanged(R.id.switch_on_off)
    public void onOffSwitchChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        if (isChecked) {
            startAlert();
        } else {
            stopAlert();
        }
    }

    @OnCheckedChanged(R.id.check_vibrate)
    public void vibrateOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        mAlert.setVibrate(isChecked);
        stopAlert();
    }

    @OnCheckedChanged(R.id.check_light)
    public void lightOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        mAlert.setLight(isChecked);
        stopAlert();
    }

    @OnCheckedChanged(R.id.check_mute)
    public void muteOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        mAlert.setMute(isChecked);
        stopAlert();
    }

    @OnCheckedChanged(R.id.check_schedule)
    public void scheduleOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        if (isChecked) {
            mAlert.setSchedule(true);
            layoutSchedule.setVisibility(View.VISIBLE);
            layoutScheduleDays.setVisibility(View.VISIBLE);
        } else {
            mAlert.setSchedule(false);
            layoutSchedule.setVisibility(View.GONE);
            layoutScheduleDays.setVisibility(View.GONE);
        }

        textDays.setText(mAlert.getDaysDisplay());
        textStartTime.setText(mAlert.getStartTimeDisplay());
        textEndTime.setText(mAlert.getEndTimeDisplay());
        textTimes.setText(mAlert.getTimeDisplay());
        stopAlert();
    }

    @OnClick(R.id.text_time_start)
    public void startTimeOnClick() {
        final RadialTimePickerDialog timePickerDialog1 = new RadialTimePickerDialog();
        timePickerDialog1.initialize(new RadialTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialTimePickerDialog dialog, int hour, int minute) {
                if (isTimeBefore(hour, minute, mAlert.getEndHour(), mAlert.getEndMinute())) {
                    mAlert.setStartTime(hour, minute);
                    textStartTime.setText(mAlert.getStartTimeDisplay());
                    textTimes.setText(mAlert.getTimeDisplay());
                    stopAlert();
                    timePickerDialog1.dismiss();
                } else {
                    Toast.makeText(mContext, "Scheduled start time must be before the end time (" + mAlert.getEndTimeDisplay() + ").", Toast.LENGTH_SHORT).show();
                }
            }
        }, mAlert.getStartHour(), mAlert.getStartMinute(), false);
        timePickerDialog1.setThemeDark(true);
        timePickerDialog1.show(mContext.getSupportFragmentManager(), null);
    }

    @OnClick(R.id.text_time_end)
    public void endTimeOnClick() {
        final RadialTimePickerDialog timePickerDialog = new RadialTimePickerDialog();
        timePickerDialog.initialize(new RadialTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialTimePickerDialog dialog, int hour, int minute) {
                if (isTimeBefore(mAlert.getStartHour(), mAlert.getStartMinute(), hour, minute)) {
                    mAlert.setEndTime(hour, minute);
                    textEndTime.setText(mAlert.getEndTimeDisplay());
                    textTimes.setText(mAlert.getTimeDisplay());
                    stopAlert();
                    timePickerDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "Scheduled end time must be after the start time (" + mAlert.getStartTimeDisplay() + ").", Toast.LENGTH_SHORT).show();
                }
            }
        }, mAlert.getEndHour(), mAlert.getEndMinute(), false);
        timePickerDialog.setThemeDark(true);
        timePickerDialog.show(mContext.getSupportFragmentManager(), null);
    }

    @OnClick(R.id.text_tone)
    public void toneOnClick() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        mContext.getIntent().putExtra("array_index", mPosition);
        mContext.startActivityForResult(intent, 5);
        stopAlert();
    }

    @OnClick(R.id.ib_delete)
    public void deleteOnClick() {
        mAdapter.remove(mAlert);
        stopAlert();
    }

    private void startAlert() {
        if (!mAlert.isOn()) {
            if (mAlert.getFrequency() >= 5) {
                switchOnOff.setChecked(true);
                mAlert.setOn(true);
                mAlarmHandler.startAlert(mAlert);
                Toast.makeText(mContext, "Started", Toast.LENGTH_SHORT).show();
            } else {
                switchOnOff.setChecked(false);
                stopAlert();
            }
        }
        mAdapter.saveData();
    }

    private void stopAlert() {

        if (mAlert.isOn()) {
            switchOnOff.setChecked(false);
            mAlert.setOn(false);
            mAlarmHandler.stopAlert(mAlert);
            if (mAlert.getFrequencyDisplay() != null) {
                textFrequency.setText(mAlert.getFrequencyDisplay());
            }
            Toast.makeText(mContext, "Stopped", Toast.LENGTH_SHORT).show();
        }
        mAdapter.saveData();
    }

    private void turnOnDayOfWeek(Button button) {
        button.setActivated(true);
        button.setTextColor(getResources().getColor(R.color.black));
        if (Build.VERSION.SDK_INT < 21) {
            button.setBackgroundResource(R.drawable.bg_day_selected);
        }
    }

    private void turnOffDayOfWeek(Button button) {
        button.setActivated(false);
        button.setTextColor(getResources().getColor(R.color.text_primary));
        if (Build.VERSION.SDK_INT < 21) {
            button.setBackgroundResource(0);
        }
    }

    private static void expandView(final View view) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();
        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    private static void collapseView(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation transformation) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    private static boolean isTimeBefore(int isThisHour, int isThisMinute, int beforeThisHour, int beforeThisMinute) {
        return (isThisHour < beforeThisHour) || (isThisHour == beforeThisHour && isThisMinute < beforeThisMinute);
    }

    private static Typeface getFrequencyFont() {
        if (frequencyFont == null) {
            frequencyFont = Typeface.create("sans-serif-light", Typeface.NORMAL);
        }
        return frequencyFont;
    }
}
