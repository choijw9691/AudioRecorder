package com.ebook.epub.fixedlayoutviewer.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebook.epub.fixedlayoutviewer.data.FixedLayoutPageData;
import com.ebook.epub.viewer.ViewerContainer;

import java.util.ArrayList;

public class TextviewCustom extends androidx.appcompat.widget.AppCompatTextView {

    private String TAG = "FixedLayoutContainerView";

    private ArrayList<FixedLayoutWebview> mWebviewList;
    private ArrayList<ProgressBar> 			mProgressBarList;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private FixedLayoutPageData mPageData;

    private ViewerContainer.PageDirection mPageDirection;

    private boolean mIsLoadEmpty;

    private boolean isTwoPageMode = false;



    public TextviewCustom(Context context, int width, int height) {    // org
        super(context);

        viewWidth = width;
        viewHeight = height;



        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewWidth, viewHeight);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
        setBackgroundColor(Color.BLACK);


    }


}
