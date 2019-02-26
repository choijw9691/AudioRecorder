package com.ebook.epub.viewer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

public class ChromeClient extends WebChromeClient {

    private Context mContext;
    private CustomViewCallback mCallBack;
    private FullScreenContainer mFullScreenContainer;
    private FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    
    public ChromeClient(Context context) {
        mContext = context;
    }
    
	@Override
	public void onHideCustomView() {
		DebugSet.d("TAG", "onHideCustomView >>>>>>>>>>>>>>>>>> ");
        FrameLayout decor = (FrameLayout)((Activity)mContext).getWindow().getDecorView();
        decor.removeView(mFullScreenContainer);
        mFullScreenContainer = null;
        if( mCallBack != null )
            mCallBack.onCustomViewHidden();
		super.onHideCustomView();
	}
	
	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
		super.onShowCustomView(view, callback);
		DebugSet.d("TAG", "onShowCustomView >>>>>>>>>>>>>>>>>> ");

		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			return;

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ){
			final FrameLayout decor = (FrameLayout)((Activity)mContext).getWindow().getDecorView();
			mFullScreenContainer = new FullScreenContainer(mContext);
			mFullScreenContainer.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
			mFullScreenContainer.addView(view, COVER_SCREEN_PARAMS);
			decor.addView(mFullScreenContainer, COVER_SCREEN_PARAMS);
			mCallBack = callback;
		}
	}
}
