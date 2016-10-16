package nkarasch.repeatingreminder.gui;
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

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import nkarasch.repeatingreminder.scheduling.AlarmHandler;
import nkarasch.repeatingreminder.Alert;
import nkarasch.repeatingreminder.R;
import nkarasch.repeatingreminder.utils.ColorUtils;

import static nkarasch.repeatingreminder.gui.ProgrammableStyleableRadialTimePickerDialogFragment.*;


public class AlertView extends LinearLayout {

    private final FragmentActivity mContext;
    private AlertListAdapter mAdapter;
    private AlarmHandler mAlarmHandler;
    private Alert mAlert;
    private int mPosition;
    private boolean mSettingState;

    //main display components
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_frequency)
    TextView textFrequency;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.switch_on_off)
    SwitchCompat switchOnOff;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_label_display)
    TextView textLabel;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_days_display)
    TextView textDays;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_times_display)
    TextView textTimes;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.iv_expand_down)
    ImageView imageDownArrow;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.rl_schedule_expansion)
    RelativeLayout layoutSchedule;

    //expansion components
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.checkbox_vibrate)
    CheckBox checkVibrate;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_ringtone)
    TextView textRingtone;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.checkbox_schedule)
    CheckBox checkSchedule;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.checkbox_schedule_text)
    TextView checkScheduleText;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_alarm_on)
    TextView textStartTime;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.text_alarm_off)
    TextView textEndTime;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.ll_schedule_days)
    LinearLayout layoutScheduleDays;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.rl_expansion)
    RelativeLayout layoutExpansion;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.checkbox_wake)
    CheckBox checkWake;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.checkbox_mute)
    CheckBox checkMute;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.sunday_toggle)
    CircleToggleButton sundayButton;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.monday_toggle)
    CircleToggleButton mondayButton;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.tuesday_toggle)
    CircleToggleButton tuesdayButton;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.wednesday_toggle)
    CircleToggleButton wednesdayButton;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.thursday_toggle)
    CircleToggleButton thursdayButton;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.friday_toggle)
    CircleToggleButton fridayButton;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.saturday_toggle)
    CircleToggleButton saturdayButton;

    private final CircleToggleButton[] mDayOfWeekButtons = new CircleToggleButton[7];

    public AlertView(final Context context) {
        super(context);
        this.mContext = (FragmentActivity) context;
    }

    public AlertView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (FragmentActivity) context;
    }

    public AlertView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = (FragmentActivity) context;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mAlert.isExpanded()) {
            setBackgroundColor(ColorUtils.getTintedBackgroundColor());
            layoutExpansion.setVisibility(View.VISIBLE);
            imageDownArrow.setVisibility(View.GONE);
        } else {
            setBackgroundColor(ColorUtils.getCurrentHourColor());
            layoutExpansion.setVisibility(View.GONE);
            imageDownArrow.setVisibility(View.VISIBLE);
        }
    }

    public void update(AlertListAdapter adapter, AlarmHandler alarmHandler, int position) {
        this.mAdapter = adapter;
        this.mAlarmHandler = alarmHandler;
        this.mAlert = adapter.getItem(position);
        this.mPosition = position;
        ButterKnife.bind(this);
        mSettingState = true;
        mDayOfWeekButtons[0] = sundayButton;
        mDayOfWeekButtons[1] = mondayButton;
        mDayOfWeekButtons[2] = tuesdayButton;
        mDayOfWeekButtons[3] = wednesdayButton;
        mDayOfWeekButtons[4] = thursdayButton;
        mDayOfWeekButtons[5] = fridayButton;
        mDayOfWeekButtons[6] = saturdayButton;

        createDayOfWeekButtons();
        setState();

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
        checkWake.setChecked(mAlert.isWake());
        checkMute.setChecked(mAlert.isMute());

        for (int i = 0; i < 7; i++) {
            mDayOfWeekButtons[i].setActivated(mAlert.isDayEnabled(i));
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
            final CircleToggleButton button = mDayOfWeekButtons[i];
            final int iterator = i;

            button.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.onTouchEvent(event);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            if (mAlert.isDayEnabled(iterator)) {
                                mAlert.setDayEnabled(false, iterator);
                            } else {
                                mAlert.setDayEnabled(true, iterator);
                            }
                            textDays.setText(mAlert.getDaysDisplay());
                            stopAlert();
                            button.setActivated(!button.isActivated());
                            button.actionUp();
                            break;

                        case MotionEvent.ACTION_DOWN:
                            button.actionDown(event);
                            break;

                        case MotionEvent.ACTION_CANCEL:
                            button.actionCancel();
                            break;
                    }
                    invalidate();
                    return true;
                }
            });
        }
    }


    @OnClick(R.id.text_label_display)
    public void labelOnClick() {
        final EditText input = new EditText(mContext);
        input.setSingleLine();
        final int accentColor = ContextCompat.getColor(mContext, R.color.accent);
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

    @SuppressWarnings("WeakerAccess")
    @OnClick(R.id.text_frequency)
    public void frequencyDisplayOnClick() {
        new HmsPickerBuilder()
                .setFragmentManager(mContext.getSupportFragmentManager())
                .setStyleResId(R.style.frequency_picker_dialog).addHmsPickerDialogHandler(new HmsPickerDialogFragment.HmsPickerDialogHandlerV2() {
            @Override
            public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
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

    @OnClick({R.id.rl_display, R.id.rl_expansion})
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

    @OnCheckedChanged(R.id.checkbox_vibrate)
    public void vibrateOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        mAlert.setVibrate(isChecked);
        stopAlert();
    }

    @OnClick(R.id.checkbox_vibrate_text)
    public void vibrateCheck() {
        checkVibrate.setChecked(!checkVibrate.isChecked());
    }

    @OnCheckedChanged(R.id.checkbox_wake)
    public void wakeOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        mAlert.setWake(isChecked);
        stopAlert();
    }

    @OnClick(R.id.checkbox_wake_text)
    public void ledCheck() {
        checkWake.setChecked(!checkWake.isChecked());
    }

    @OnCheckedChanged(R.id.checkbox_mute)
    public void muteOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            return;
        }
        mAlert.setMute(isChecked);
        stopAlert();
    }

    @OnClick(R.id.checkbox_mute_text)
    public void muteCheck() {
        checkMute.setChecked(!checkMute.isChecked());
    }

    @OnCheckedChanged(R.id.checkbox_schedule)
    public void scheduleOnCheckChanged(boolean isChecked) {
        if (mSettingState) {
            expandScheduleDays(isChecked);
            return;
        }
        mAlert.setSchedule(isChecked);
        expandScheduleDays(isChecked);

        textDays.setText(mAlert.getDaysDisplay());
        textStartTime.setText(mAlert.getStartTimeDisplay());
        textEndTime.setText(mAlert.getEndTimeDisplay());
        textTimes.setText(mAlert.getTimeDisplay());
        stopAlert();
    }

    private void expandScheduleDays(boolean isSchedule) {
        if (isSchedule) {
            layoutSchedule.setVisibility(View.VISIBLE);
            layoutScheduleDays.setVisibility(View.VISIBLE);

            checkSchedule.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.alarm_bottom_padding) / 2);
            checkScheduleText.setPadding((int) getResources().getDimension(R.dimen.alarm_text_left_padding), 0, 0, (int) getResources().getDimension(R.dimen.alarm_bottom_padding) / 2);
        } else {
            layoutSchedule.setVisibility(View.VISIBLE);
            layoutScheduleDays.setVisibility(View.VISIBLE);
            layoutSchedule.setVisibility(View.GONE);
            layoutScheduleDays.setVisibility(View.GONE);
            checkSchedule.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.alarm_bottom_padding));
            checkScheduleText.setPadding((int) getResources().getDimension(R.dimen.alarm_text_left_padding), 0, 0, (int) getResources().getDimension(R.dimen.alarm_bottom_padding));
        }
    }

    @OnClick(R.id.checkbox_schedule_text)
    public void checkSchedule() {
        checkSchedule.setChecked(!checkSchedule.isChecked());
    }

    @OnClick({R.id.img_alarm_on, R.id.text_alarm_on})
    public void startTimeOnClick() {
        final ProgrammableStyleableRadialTimePickerDialogFragment timePickerDialog = new ProgrammableStyleableRadialTimePickerDialogFragment();
        timePickerDialog.setOnTimeSetListener(new OnTimeSetListener() {
            @Override
            public void onTimeSet(ProgrammableStyleableRadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                if (isTimeBefore(hourOfDay, minute, mAlert.getEndHour(), mAlert.getEndMinute())) {
                    mAlert.setStartTime(hourOfDay, minute);
                    textStartTime.setText(mAlert.getStartTimeDisplay());
                    textTimes.setText(mAlert.getTimeDisplay());
                    stopAlert();
                    timePickerDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "Scheduled start time must be before the end time (" + mAlert.getEndTimeDisplay() + ").", Toast.LENGTH_SHORT).show();
                }
            }
        });
        timePickerDialog.setStartTime(mAlert.getStartHour(), mAlert.getStartMinute());

        ProgrammableStyleableRadialTimePickerDialogFragment.ProgrammableStyle style = timePickerDialog.new ProgrammableStyle();
        style.headerBgColor = ContextCompat.getColor(mContext, R.color.primary);
        style.bodyBgColor = ColorUtils.getTintedBackgroundColor();
        style.buttonBgColor = ColorUtils.getCurrentHourColor();
        style.buttonTextColor = ContextCompat.getColor(mContext, android.R.color.white);
        style.selectedColor = ContextCompat.getColor(mContext, android.R.color.white);
        style.unselectedColor = ContextCompat.getColor(mContext, android.R.color.white);

        timePickerDialog.setStyleProgramatically(style);
        timePickerDialog.show(mContext.getSupportFragmentManager(), null);
    }

    @OnClick({R.id.img_alarm_off, R.id.text_alarm_off})
    public void endTimeOnClick() {

        final ProgrammableStyleableRadialTimePickerDialogFragment timePickerDialog = new ProgrammableStyleableRadialTimePickerDialogFragment();
        timePickerDialog.setOnTimeSetListener(new OnTimeSetListener() {
            @Override
            public void onTimeSet(ProgrammableStyleableRadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                if (isTimeBefore(mAlert.getStartHour(), mAlert.getStartMinute(), hourOfDay, minute)) {
                    mAlert.setEndTime(hourOfDay, minute);
                    textEndTime.setText(mAlert.getEndTimeDisplay());
                    textTimes.setText(mAlert.getTimeDisplay());
                    stopAlert();
                    timePickerDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "Scheduled end time must be after the start time (" + mAlert.getStartTimeDisplay() + ").", Toast.LENGTH_SHORT).show();
                }
            }
        });
        timePickerDialog.setStartTime(mAlert.getEndHour(), mAlert.getEndMinute());

        ProgrammableStyleableRadialTimePickerDialogFragment.ProgrammableStyle style = timePickerDialog.new ProgrammableStyle();
        style.headerBgColor = ContextCompat.getColor(mContext, R.color.primary);
        style.bodyBgColor = ColorUtils.getTintedBackgroundColor();
        style.buttonBgColor = ColorUtils.getCurrentHourColor();
        style.buttonTextColor = ContextCompat.getColor(mContext, android.R.color.white);
        style.selectedColor = ContextCompat.getColor(mContext, android.R.color.white);
        style.unselectedColor = ContextCompat.getColor(mContext, android.R.color.white);

        timePickerDialog.setStyleProgramatically(style);
        timePickerDialog.show(mContext.getSupportFragmentManager(), null);
    }

    @OnClick({R.id.img_ringtone, R.id.text_ringtone})
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
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(mAlert.getId());

            Toast.makeText(mContext, "Stopped", Toast.LENGTH_SHORT).show();
        }
        mAdapter.saveData();
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
}
