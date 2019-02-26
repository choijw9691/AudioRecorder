package com.flk.epubviewersample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.ViewerContainer;
import com.flk.epubviewersample.R;

public class MemoWriteActivity extends Activity {

    private ViewerContainer mEPubViewer;

    private Highlight mHighlight;

    private String mPressedMemoText="";

    private boolean mEditMode = false;

    private EditText mEditText;

    private InputMethodManager inputMethodManager;

    private boolean mIsHighlight;
    private boolean isHighlightToMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewer_comment_memo_write);

        if(getIntent().getExtras() != null) {
//            mIsHighlight = getIntent().getExtras().getBoolean("mIsHighlight");
//            isHighlightToMemo = getIntent().getExtras().getBoolean("isHighlightToMemo");
            mPressedMemoText = getIntent().getExtras().getString("MEMO_CONTENT");
        }

        mEPubViewer = BaseBookActivity.mViewer;
//
//        mHighlight = ViewerEpubMainActivity.mMemoHighlight;

//        mPressedMemoText = mHighlight.memo;

        mEditMode = !(mPressedMemoText==null || mPressedMemoText.isEmpty());

//		if(mPressedMemoText==null || mPressedMemoText.length() == 0)
//			mEditMode = false;
//		else
//			mEditMode = true;

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }

    private void initView() {

        //EditText
        mEditText = (EditText) findViewById(R.id.viewer_comment_memo_text);
        if(mEditMode) {
            mEditText.setText(Uri.decode(mPressedMemoText));
        }

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = ((EditText)v);
                if(editText.length() != 0) {
                    editText.setSelection(editText.getText().toString().length());
                }
            }
        });

        mEditText.addTextChangedListener( new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.length() >= 2000) {
//                    Toast.makeText(getApplicationContext(), getString(R.string.viewer_memo_length_over) , Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Go back to viewer
        findViewById(R.id.viewer_write_memo_backtoViewer).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                memoExit();
            }
        });

        //Save memo
        findViewById(R.id.viewer_write_memo_save).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(memoTypingCheck()){
                    memoSave();
                    memoFinish();
                }
            }
        });


        mEditText.requestFocus();
    }


    /** 메모 종료요청 */
    private void memoExit() {
        try {
            String typingMessage = mEditText.getText().toString();

            if(mPressedMemoText!=null){
                //변경된 데이터가 없는 경우
                if(mPressedMemoText.equals(typingMessage) ){
                    memoFinish();
                }
                //변경된 데이터가 있는경우
                else{
                    //데이터 없는경우
                    if(typingMessage.trim().length() == 0){
                        memoExitDeleteAlert();
                    }
                    //데이터 있는경우
                    else{
                        memoExitSaveAlert();
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /** 메모 종료저장알럿 */
    private void memoExitSaveAlert() {
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(this);

        if(mEditMode)
            exitAlert.setMessage("수정중인 메모가 있습니다.\\n저장하시겠습니까?");
        else
            exitAlert.setMessage("작성중인 메모가 있습니다.\\n저장하시겠습니까?");


        exitAlert.setCancelable(false);
        exitAlert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                memoSave();
                memoFinish();
            }
        });
        exitAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    dialog.dismiss();
                }catch(Exception e){
                    //catch block
                }

                memoFinish();
            }
        });
        exitAlert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    try{
                        dialog.dismiss();
                    }catch(Exception e){
                        //catch block
                    }


                    memoFinish();
                }
                return false;
            }
        });
        exitAlert.show();
    }

    /** 메모 종료삭제알럿 */
    private void memoExitDeleteAlert() {
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(this);
        exitAlert.setMessage("메모를 삭제하시겠습니까?");
        exitAlert.setCancelable(false);
        exitAlert.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //메모삭제
                mEPubViewer.deleteHighlight(mHighlight);
                memoFinish();
                Toast.makeText(getApplicationContext(),"메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        exitAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //메모기존대로 남겨둠/
                try{
                    dialog.dismiss();
                }catch(Exception e){
                    //catch block
                }

                memoFinish();
            }
        });
        exitAlert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    try{
                        dialog.dismiss();
                    }catch(Exception e){
                    }
                    memoFinish();
                }
                return false;
            }
        });
        exitAlert.show();
    }

    /** 메모 입력값 체크 - 저장버튼시에만 사용 */
    private boolean memoTypingCheck() {
        if(mEditText.getText().toString().length() <= 0) {
            Toast.makeText(getApplicationContext(), "메모를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }else
            return true;
    }

    /** 메모저장 */
    private void memoSave() {
//        mHighlight.memo = mEditText.getText().toString();
//        mPressedMemoText = mEditText.getText().toString();
        String currentMemo = mEditText.getText().toString();
//        if(mIsHighlight){
//            mEPubViewer.changeIntoMemo(mHighlight, mPressedMemoText);
//        }else {
//            mEPubViewer.requestShowMemo(mHighlight, mEditMode);
//        }


        // TODO :: new custom selection modified
        // - 범위가 바뀌고 메모는 동일할 수 있으므로 무조건 태워야함
//        mEpubVIewer.doMemo();
        mEPubViewer.addAnnotationWithMemo(currentMemo, mEditMode);

        String text  = mEditMode ? "메모가 수정되었습니다." : "메모가 저장되었습니다";
        inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        mEditText.clearFocus();
    }

    /** 메모창 종료 */
    private void memoFinish() {
        if(mEditText!=null)
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

//        if(mHighlight.memo.trim().length() <= 0) {
//            if(!isHighlightToMemo){
//                mEPubViewer.deleteHighlight(mHighlight);
//            }
//        }
        onbackViewer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onbackViewer();
    }

    @Override
    public void onBackPressed() {
        memoExit();
    }

    private void onbackViewer(){
        this.finish();
    }
}
