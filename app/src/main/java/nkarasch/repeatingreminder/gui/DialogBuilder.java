package nkarasch.repeatingreminder.gui;
/*
 * Copyright (C) 2014 Jared Rummler <jared.rummler@gmail.com>
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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Class which extends {@link AlertDialog.Builder}.</p>
 * <p/>
 * This adds the ability to set the color for the title, icon, divider, and progressbar.</p>
 *
 * @author Jared Rummler <jared.rummler@gmail.com>
 * @since Dec 11, 2014
 */
public class DialogBuilder extends AlertDialog.Builder {

    // ===========================================================
    // STATIC FIELDS
    // ===========================================================

    private static final String DIALOG_COLOR_ERROR = "The dialog must be shown before any color changes can be made. Call dialog.show() beforehand.";

    // ===========================================================
    // STATIC METHODS
    // ===========================================================


    private static void setIconColor(Dialog dialog, final int color) {
        if (!dialog.isShowing()) {
            throw new RuntimeException(DIALOG_COLOR_ERROR);
        }
        if (color != -1) {
            final int iconId = dialog.getContext().getResources()
                    .getIdentifier("android:id/icon", null, null);
            if (iconId != 0) {
                final ImageView icon = (ImageView) dialog.findViewById(iconId);
                if (icon != null) {
                    icon.setColorFilter(color);
                }
            }
        }
    }

    private static void setTitleColor(Dialog dialog, final int color) {
        if (!dialog.isShowing()) {
            throw new RuntimeException(DIALOG_COLOR_ERROR);
        }
        if (color != -1) {
            final int textViewId = dialog.getContext().getResources()
                    .getIdentifier("android:id/alertTitle", null, null);
            if (textViewId != 0) {
                final TextView tv = (TextView) dialog.findViewById(textViewId);
                if (tv != null) {
                    tv.setTextColor(color);
                }
            }
        }
    }

    private static void setDividerColor(Dialog dialog, final int color) {
        if (!dialog.isShowing()) {
            throw new RuntimeException(DIALOG_COLOR_ERROR);
        }
        if (color != -1) {
            final int dividerId = dialog.getContext().getResources()
                    .getIdentifier("android:id/titleDivider", null, null);
            if (dividerId != 0) {
                final View divider = dialog.findViewById(dividerId);
                if (divider != null) {
                    divider.setBackgroundColor(color);
                }
            }
        }
    }

    private static void setProgressBarColor(Dialog dialog, final int color) {
        if (!dialog.isShowing()) {
            throw new RuntimeException(DIALOG_COLOR_ERROR);
        }
        if (color != -1) {
            try {
                final ProgressBar bar = (ProgressBar) dialog.findViewById(android.R.id.progress);
                final LayerDrawable ld = (LayerDrawable) bar.getProgressDrawable();
                final Drawable progress = ld.getDrawable(2);
                progress.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP);
                ld.setDrawableByLayerId(android.R.id.progress, progress);
                bar.setProgressDrawable(ld);
            } catch (Exception ignore) {
            }
        }
    }

    // ===========================================================
    // FIELDS
    // ===========================================================

    private int mIconColor = -1;

    private int mTitleColor = -1;

    private int mDividerColor = -1;

    private int mProgressBarColor = -1;

    // ===========================================================
    // CONSTRUCTORS
    // ===========================================================

    public DialogBuilder(Context context) {
        super(context);
    }


    // ===========================================================
    // METHODS FOR/FROM SUPERCLASS/INTERFACES
    // ===========================================================

    @Override
    public DialogBuilder setTitle(int titleId) {
        super.setTitle(titleId);
        return this;
    }

    @Override
    public DialogBuilder setTitle(CharSequence title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public DialogBuilder setCustomTitle(View customTitleView) {
        super.setCustomTitle(customTitleView);
        return this;
    }

    @Override
    public DialogBuilder setMessage(int messageId) {
        super.setMessage(messageId);
        return this;
    }

    @Override
    public DialogBuilder setMessage(CharSequence message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public DialogBuilder setIcon(int iconId) {
        super.setIcon(iconId);
        return this;
    }

    @Override
    public DialogBuilder setIcon(Drawable icon) {
        super.setIcon(icon);
        return this;
    }

    @Override
    public DialogBuilder setIconAttribute(int attrId) {
        super.setIconAttribute(attrId);
        return this;
    }

    @Override
    public DialogBuilder setPositiveButton(int textId, final OnClickListener listener) {
        super.setPositiveButton(textId, listener);
        return this;
    }

    @Override
    public DialogBuilder setPositiveButton(CharSequence text, final OnClickListener listener) {
        super.setPositiveButton(text, listener);
        return this;
    }

    @Override
    public DialogBuilder setNegativeButton(int textId, final OnClickListener listener) {
        super.setNegativeButton(textId, listener);
        return this;
    }

    @Override
    public DialogBuilder setNegativeButton(CharSequence text, final OnClickListener listener) {
        super.setNegativeButton(text, listener);
        return this;
    }

    @Override
    public DialogBuilder setNeutralButton(int textId, final OnClickListener listener) {
        super.setNeutralButton(textId, listener);
        return this;
    }

    @Override
    public DialogBuilder setNeutralButton(CharSequence text, final OnClickListener listener) {
        super.setNeutralButton(text, listener);
        return this;
    }

    @Override
    public DialogBuilder setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    @Override
    public DialogBuilder setOnCancelListener(OnCancelListener onCancelListener) {
        super.setOnCancelListener(onCancelListener);
        return this;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public DialogBuilder setOnDismissListener(OnDismissListener onDismissListener) {
        super.setOnDismissListener(onDismissListener);
        return this;
    }

    @Override
    public DialogBuilder setOnKeyListener(OnKeyListener onKeyListener) {
        super.setOnKeyListener(onKeyListener);
        return this;
    }

    @Override
    public DialogBuilder setItems(int itemsId, final OnClickListener listener) {
        super.setItems(itemsId, listener);
        return this;
    }

    @Override
    public DialogBuilder setItems(CharSequence[] items, final OnClickListener listener) {
        super.setItems(items, listener);
        return this;
    }

    @Override
    public DialogBuilder setAdapter(ListAdapter adapter, final OnClickListener listener) {
        super.setAdapter(adapter, listener);
        return this;
    }

    @Override
    public DialogBuilder setCursor(Cursor cursor, final OnClickListener listener,
                                   final String labelColumn) {
        super.setCursor(cursor, listener, labelColumn);
        return this;
    }

    @Override
    public DialogBuilder setMultiChoiceItems(int itemsId, final boolean[] checkedItems,
                                             final OnMultiChoiceClickListener listener) {
        super.setMultiChoiceItems(itemsId, checkedItems, listener);
        return this;
    }

    @Override
    public DialogBuilder setMultiChoiceItems(CharSequence[] items,
                                             final boolean[] checkedItems, final OnMultiChoiceClickListener listener) {
        super.setMultiChoiceItems(items, checkedItems, listener);
        return this;
    }

    @Override
    public DialogBuilder setMultiChoiceItems(Cursor cursor, final String isCheckedColumn,
                                             final String labelColumn, final OnMultiChoiceClickListener listener) {
        super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
        return this;
    }

    @Override
    public DialogBuilder setSingleChoiceItems(int itemsId, final int checkedItem,
                                              final OnClickListener listener) {
        super.setSingleChoiceItems(itemsId, checkedItem, listener);
        return this;
    }

    @Override
    public DialogBuilder setSingleChoiceItems(Cursor cursor, final int checkedItem,
                                              final String labelColumn, final OnClickListener listener) {
        super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
        return this;
    }

    @Override
    public DialogBuilder setSingleChoiceItems(CharSequence[] items, final int checkedItem,
                                              final OnClickListener listener) {
        super.setSingleChoiceItems(items, checkedItem, listener);
        return this;
    }

    @Override
    public DialogBuilder setSingleChoiceItems(ListAdapter adapter, final int checkedItem,
                                              final OnClickListener listener) {
        super.setSingleChoiceItems(adapter, checkedItem, listener);
        return this;
    }

    @Override
    public DialogBuilder
    setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(listener);
        return this;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DialogBuilder setView(int layoutResId) {
        super.setView(layoutResId);
        return this;
    }

    @Override
    public DialogBuilder setView(View view) {
        super.setView(view);
        return this;
    }

    @Override
    public AlertDialog show() {
        final AlertDialog dialog = super.show();
        setIconColor(dialog, mIconColor);
        setTitleColor(dialog, mTitleColor);
        setDividerColor(dialog, mDividerColor);
        setProgressBarColor(dialog, mProgressBarColor);
        return dialog;
    }

    // ===========================================================
    // METHODS
    // ===========================================================

    /**
     * @param color The color for the title icon, text, divider, and progressbar.
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public DialogBuilder setColor(int color) {
        mIconColor = color;
        mTitleColor = color;
        mDividerColor = color;
        mProgressBarColor = color;
        return this;
    }

    /**
     * @param titleColor The color for the title text.
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public DialogBuilder setTitleColor(int titleColor) {
        mTitleColor = titleColor;
        return this;
    }

    /**
     * @param dividerColor The color for the title divider.
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public DialogBuilder setDividerColor(int dividerColor) {
        mDividerColor = dividerColor;
        return this;
    }
}