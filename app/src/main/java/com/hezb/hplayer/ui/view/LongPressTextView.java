package com.hezb.hplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 支持长按复制TextView
 * Created by hezb on 2016/1/20.
 */
public class LongPressTextView extends EditText {

    public LongPressTextView(Context context) {
        super(context);
    }

    public LongPressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongPressTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean getDefaultEditable() {
        return false;
    }
}
