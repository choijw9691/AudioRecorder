package com.flk.epubviewersample.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.ViewerContainer;
import com.flk.epubviewersample.R;

public class MemoDialog extends Dialog {

    public static MemoDialog mThis = null;
    
    Context mContext;
    View mRootView;
    
    Highlight mMemo;
    
    EditText mEdit;
    
    private ViewerContainer mEPubViewer;
    
    public interface OnMemoClose {
        public abstract void onClose(Highlight high, String memo, boolean isEdit);
    }
    
    OnMemoClose mOnMemoClose = null;
    
    public void setOnMemoClose(OnMemoClose l) {
        mOnMemoClose = l;
    }
    
    public MemoDialog(Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);    
    }

    public MemoDialog(Context context, int theme) {
        super(context, theme);
        init(context);    
    }

    public MemoDialog(Context context) {
        super(context);
        init(context);    
    }

    public MemoDialog(Context context, Highlight high, int theme, ViewerContainer mViewer) {
        super(context, theme);
        init(context);
        mEPubViewer = mViewer;
        mMemo = high;
    }
    public MemoDialog(Context context, Highlight high) {
        super(context);
        init(context);
        
        mMemo = high;
    }
    
    void init(Context context) {
        mContext = context;

        mRootView = LayoutInflater.from(mContext).inflate(R.layout.memo_dialog, null);
        this.setContentView(mRootView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThis = this;
        
        this.getWindow().setLayout(-1, LayoutParams.WRAP_CONTENT);

        TextView snippet = (TextView)mRootView.findViewById(R.id.tvSnippet);
        snippet.setText("\"" + mMemo.text + "\"");

        mEdit = (EditText)mRootView.findViewById(R.id.editMemo);
//        mEdit.setText( Uri.decode(mMemo.memo) );
        mEdit.setText( mMemo.memo );
        
        Button test = (Button)mRootView.findViewById(R.id.btn_etc);
        test.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mOnMemoClose.onClose(mMemo, "", false);
			}
        });
        
        Button close = (Button)mRootView.findViewById(R.id.btnClose);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               
            	String memoText = mEdit.getText().toString();
                String highMemoText = Uri.decode(mMemo.memo).trim().toLowerCase();
                boolean isEdit = false;

                if( mOnMemoClose != null ) {
                	if( !memoText.equals("") && !highMemoText.equals(memoText.trim().toLowerCase()) ){
                		isEdit = true;
                		mMemo.memo = memoText;
                        mOnMemoClose.onClose(mMemo, memoText, isEdit);
                        mThis.dismiss();
                	} else if(memoText.equals("")){
                		Toast.makeText(mContext, "메모 입력하세요.", Toast.LENGTH_SHORT).show();
                	} else if(highMemoText.equals(memoText.trim().toLowerCase())){
                		mOnMemoClose.onClose(mMemo, memoText, isEdit);
                		mThis.dismiss();
                	}
                }
//            	mEPubViewer.doHighlight();
            }
        });
        
        setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keycode, KeyEvent event) {

				if(keycode == KeyEvent.KEYCODE_BACK){
					mOnMemoClose.onClose(mMemo, "", false);
				}
				return false;
			}
		});
    }
    
}
