package com.ebook.epub.viewer;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ViewerBase extends WebView {
    
    protected Context mContext;

    protected boolean mTextSelectionMode = false;
    
    public static boolean __isIgnoreDrm = false;

    public ViewerBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ViewerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewerBase(Context context) {
        super(context);
    }
}
