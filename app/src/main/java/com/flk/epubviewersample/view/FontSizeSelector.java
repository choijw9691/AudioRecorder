package com.flk.epubviewersample.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.flk.epubviewersample.R;


/**
 * @author  syw
 */
public class FontSizeSelector extends AlertDialog {

    /**
     */
    static FontSizeSelector mThis = null;
    
    Context mContext;

    View mRootView;
    TextView mPercent;
    
    int __percent = 100;
    int step = 10;
    
    public static interface OnChangeFontSize {
        public abstract void onChange(int percent);
    }
    
    /**
     */
    OnChangeFontSize mOnChangeFontSize = null;
    public void setOnChangeFontSize(OnChangeFontSize l) {
        mOnChangeFontSize = l;
    }
    
    public FontSizeSelector(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public FontSizeSelector(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public FontSizeSelector(Context context) {
        super(context);
        init(context);
    }

    void init(Context context) {
        mContext = context;

        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fontsize_dialog, null);
        this.setView(mRootView);
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mThis = this;
        mPercent = (TextView)mRootView.findViewById(R.id.sizePercent);
        mPercent.setText("" + __percent + "%");
        
        Button smaller = (Button)mRootView.findViewById(R.id.smaller);
        Button greater = (Button)mRootView.findViewById(R.id.greater);
        
        smaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( __percent - step < 100 ) return;
                    
                __percent -= step;
                mPercent.setText("" + __percent + "%");
                
                if( mOnChangeFontSize != null ) {
                    mOnChangeFontSize.onChange(__percent);
                }
                
                dismiss();
            }
        });
        greater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( __percent + step > 500 ) return;
                
                __percent += step;
                mPercent.setText("" + __percent + "%");
                
                if( mOnChangeFontSize != null ) {
                    mOnChangeFontSize.onChange(__percent);
                }
                
                dismiss();
            }
        });
        
    }

    public void setDefaultSize(int size) {
        __percent = size;
    }

}
