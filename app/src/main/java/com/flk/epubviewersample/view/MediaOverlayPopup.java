package com.flk.epubviewersample.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.ebook.epub.viewer.ViewerContainer;
import com.flk.epubviewersample.R;

public class MediaOverlayPopup {
	
	private Context mContext;
	private PopupWindow mMediaOverlayPopup;
	private ViewerContainer mViewer;
	
	public boolean isPlayingMediaOverlay=false;
	
	public MediaOverlayPopup(Context context, ViewerContainer viewer, int width, int height) {
		mContext = context;
		mViewer = viewer;
		setPopupView(width, height);
	}
	
	public void setPopupView(int width, int height){
		LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = layoutInflater.inflate(R.layout.media_overlay_control_popup, null);
		Button playBtn = (Button)contentView.findViewById(R.id.play);
		Button pauseBtn = (Button)contentView.findViewById(R.id.pause);
		Button stopBtn = (Button)contentView.findViewById(R.id.stop);
		
		playBtn.setOnClickListener(btnClickListener);
		pauseBtn.setOnClickListener(btnClickListener);
		stopBtn.setOnClickListener(btnClickListener);
		
		
		mMediaOverlayPopup = new PopupWindow(contentView, width, LayoutParams.WRAP_CONTENT, true);
		mMediaOverlayPopup.setOutsideTouchable(false);
		mMediaOverlayPopup.setFocusable(false);
	}
	
	public void showPopup(ViewerContainer container) {
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		FrameLayout parentView = new FrameLayout(mContext);
		parentView.setLayoutParams(params);
		mMediaOverlayPopup.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
	}
	
	public void dismissPopup() {
//		mViewer.finishMediaOverlay();
//		mViewer.removeTTSHighlight();
		mViewer.clearAllMediaOverlayInfo();
		mMediaOverlayPopup.dismiss();
//		mViewer.preventPageMove(false);
	}
	
	public boolean isShowing(){
		return mMediaOverlayPopup.isShowing();
	}
	
	OnClickListener btnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play:
				isPlayingMediaOverlay = true;
				
//				mViewer.preventPageMove(true);
				mViewer.playMediaOverlay();
				break;
			case R.id.pause:
				isPlayingMediaOverlay = false;
				
				mViewer.pauseMediaOverlay();
				break;
			case R.id.stop:
				isPlayingMediaOverlay = false;

				mViewer.stopMediaOverlay();
//				mViewer.preventPageMove(false);
				break;
			default:
				break;
			}
		}
	};
}
