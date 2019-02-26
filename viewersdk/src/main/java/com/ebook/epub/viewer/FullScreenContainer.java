package com.ebook.epub.viewer;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.FrameLayout;

public class FullScreenContainer extends FrameLayout {
	
	public interface OnBackKeyListener {
        void onBackKeyPress(KeyEvent event);
	}
	
	private OnBackKeyListener backKeyListener;

	public FullScreenContainer(Context context) {
		super(context);
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		DebugSet.d("TAG", "onKeyPreIme : " + keyCode);
		if( backKeyListener != null )
			backKeyListener.onBackKeyPress(event);
		return true;
	}
}
