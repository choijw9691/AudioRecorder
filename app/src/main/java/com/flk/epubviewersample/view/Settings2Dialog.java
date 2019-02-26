package com.flk.epubviewersample.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.ebook.epub.viewer.BookHelper;
import com.flk.epubviewersample.R;

public class Settings2Dialog extends AlertDialog {
    
    Context mContext;
    
    View mRootView;
   
    int mHandMode = 0;
    int mAsideBackground = 0;
    int mAsideBorderColor = 0;
    int mAsideFontColor = 0;
    int mAsideBorderSize = 1;
    boolean mVolumeUse = false;
    
    public interface OnSettings2Listener {
        public abstract void onBrightnessChanged(int value); 
        public abstract void onViewModeChanged(int mode);
        public abstract void onViewHandMade(int mode);
        public abstract void onAsideBackroundColor(int color);
        public abstract void onAsideBorderColor(int color);
        public abstract void onAsideFontColor(int color);
        public abstract void onAsideBorderSize(int size);
        public abstract void onVolumeUse(boolean chkUse);
    }

    OnSettings2Listener mOnSettings2Listener=null;
    
    public void setOnSettings2Listener(OnSettings2Listener l) {
        mOnSettings2Listener = l;
    }
    
    public Settings2Dialog(Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public Settings2Dialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }
    
    public Settings2Dialog(Context context, int handmode, boolean volumeUse) {
        super(context);
        mHandMode = handmode;
        mVolumeUse = volumeUse;
        init(context);
    }

    void init(Context context) {
        mContext = context;
        
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.settings_dialog, null);
        this.setView(mRootView);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        SeekBar sb = (SeekBar) mRootView.findViewById(R.id.seekBar1);
        sb.setMax(255);
        sb.setProgress(125);
        
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                
                if( mOnSettings2Listener != null ) 
                    mOnSettings2Listener.onBrightnessChanged(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                
            }
            
        });
        
        
        
        RadioGroup rg = (RadioGroup) mRootView.findViewById(R.id.rgViewMode);
        
        RadioButton[] rdo = new RadioButton[3];
        
        rdo[0] = (RadioButton) mRootView.findViewById(R.id.rdoDefault);
        rdo[1] = (RadioButton) mRootView.findViewById(R.id.rdoSlide);
        rdo[2] = (RadioButton) mRootView.findViewById(R.id.rdoCurling);

        if( BookHelper.animationType >= 0 && BookHelper.animationType < 3)
            rdo[BookHelper.animationType].setChecked(true);
        
        
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                
                switch(id) {
                    case R.id.rdoDefault : {
                        if( mOnSettings2Listener != null ) {
                            BookHelper.animationType = 0;
                            mOnSettings2Listener.onViewModeChanged(0);
                        }
                        break;
                    }
                        
                    case R.id.rdoSlide: {
                        if( mOnSettings2Listener != null ) {
                            BookHelper.animationType = 1;
                            BookHelper.animationDuration = 100;
                            mOnSettings2Listener.onViewModeChanged(1);
                        }
                        break;
                    }
                        
                    case R.id.rdoCurling: {
                        if( mOnSettings2Listener != null ) {
                            BookHelper.animationType = 2;
                            mOnSettings2Listener.onViewModeChanged(2);
                        }
                        break;
                    }
                }
            }
            
        });
        
        RadioGroup handmode = (RadioGroup) mRootView.findViewById(R.id.handMode);
        
        RadioButton[] hmode = new RadioButton[3];
        
        hmode[0] = (RadioButton) mRootView.findViewById(R.id.righthand);
        hmode[1] = (RadioButton) mRootView.findViewById(R.id.lefthand);
      
        hmode[mHandMode].setChecked(true);
        
        handmode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                
                switch(id) {
                    case R.id.righthand : {
                        if( mOnSettings2Listener != null ) {
                        	mHandMode = 0;
                            mOnSettings2Listener.onViewHandMade(0);
                        }
                        break;
                    }
                        
                    case R.id.lefthand: {
                        if( mOnSettings2Listener != null ) {
                        	mHandMode = 1;
                            mOnSettings2Listener.onViewHandMade(1);
                        }
                        break;
                    }

                }
            }
            
        });
        
        RadioGroup asideBackground = (RadioGroup) mRootView.findViewById(R.id.asidePopupBackground);
        
        RadioButton[] backgroundColor = new RadioButton[3];
        
        backgroundColor[0] = (RadioButton) mRootView.findViewById(R.id.asidePopupBackGray);
        backgroundColor[1] = (RadioButton) mRootView.findViewById(R.id.asidePopupBackYellow);
        backgroundColor[2] = (RadioButton) mRootView.findViewById(R.id.asidePopupBackGreen);
      
        backgroundColor[mAsideBackground].setChecked(true);
        
        asideBackground.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                
                switch(id) {
                    case R.id.asidePopupBackGray : {
                        if( mOnSettings2Listener != null ) {
                        	mAsideBackground = 0;
                            mOnSettings2Listener.onAsideBackroundColor(0);
                        }
                        break;
                    }
                        
                    case R.id.asidePopupBackYellow: {
                        if( mOnSettings2Listener != null ) {
                        	mAsideBackground = 1;
                        	mOnSettings2Listener.onAsideBackroundColor(1);
                        }
                        break;
                    }
                    case R.id.asidePopupBackGreen: {
                        if( mOnSettings2Listener != null ) {
                        	mAsideBackground = 2;
                        	mOnSettings2Listener.onAsideBackroundColor(2);
                        }
                        break;
                    }

                }
            }
            
        });
        
        
        CheckBox chkVolumeUse = (CheckBox)mRootView.findViewById(R.id.chkvolumeuse);
        chkVolumeUse.setChecked(mVolumeUse);
        
        chkVolumeUse.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CheckBox chk = (CheckBox)v;
                
				mVolumeUse = chk.isChecked();
                
                if( mOnSettings2Listener != null ) 
                	mOnSettings2Listener.onVolumeUse(mVolumeUse);
			}
		});
    }

}
