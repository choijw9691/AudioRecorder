package com.flk.epubviewersample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class CustomImageView extends ImageView {

    Context mContext;
    
    public interface OnVisibilityChanged {
        public abstract void onChanged(int visibility);
    }
    
    OnVisibilityChanged mOnVisibilityChanged=null;
    public void setOnVisibilityChanged(OnVisibilityChanged l) {
        mOnVisibilityChanged = l;
    }
    
    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomImageView(Context context) {
        super(context);
        init(context);
    }
    
    void init(Context context) {
        mContext = context;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if( mOnVisibilityChanged != null ) {
            mOnVisibilityChanged.onChanged(visibility);
        }
        
    }
    
    

}
