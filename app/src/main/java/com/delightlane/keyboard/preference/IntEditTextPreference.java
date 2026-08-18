package com.delightlane.keyboard.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class IntEditTextPreference extends EditTextPreference {

    public IntEditTextPreference(Context context) {
        super(context);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        int value = getPersistedInt(-1);
        if(value == -1) return defaultReturnValue;
        return String.valueOf(value);
    }

    @Override
    protected boolean persistString(String value) {
        try {
            return persistInt(Integer.parseInt(value));
        } catch(NumberFormatException e) {
            return false;
        }
    }
}