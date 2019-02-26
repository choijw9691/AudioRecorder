package com.flk.epubviewersample.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.tts.OnHighlighterListener;
import com.ebook.tts.OnTTSDataInfoListener;
import com.ebook.tts.TTSDataInfo;
import com.flk.epubviewersample.R;
import com.flk.epubviewersample.preference.BookPreference;

import java.util.ArrayList;
import java.util.HashMap;

public class TTSControlPopup {

    private Context mContext;
    private PopupWindow mTTSPopup;
    private ViewerContainer mViewer;

    private TextToSpeech textToSpeech;
    private ArrayList<TTSDataInfo> ttsDataList;
    private int ttsCurrentIndex;

    private boolean isPlaying = false;
    private boolean isSelectionPlaying = false;
    public boolean isPaused=false;

    private LinearLayout lyStyle;

    public TTSControlPopup(Context context, ViewerContainer viewer, int width, int height) {
        mContext = context;
        mViewer = viewer;
        mViewer.setOnTTSDataInfoListener(new OnTTSDataInfoListener() {

            @Override
            public void onSpeechPositionChanged(int position) {

                Log.d("SSIN","onSpeechPositionChanged : "+position);

                if( position != -1 && position < ttsDataList.size() )
                    Toast.makeText(mContext, "onSpeechPositionChanged :" + ttsDataList.get(position).getText(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, "onSpeechPositionChanged invalid position" , Toast.LENGTH_SHORT).show();

                ttsCurrentIndex = position;
                if( !isPaused  && isPlaying)
                    speak();
            }

            @Override
            public void ttsDataFromSelection(TTSDataInfo selectedTTSDataInfo, int index) {
                if(selectedTTSDataInfo!=null) {
                    speakSelectionText(selectedTTSDataInfo.getText());
                    mViewer.addTTSHighlight(selectedTTSDataInfo);
                    ttsCurrentIndex = index;
                }
            }
        });

        mViewer.setOnTTSHighlighterListener(new OnHighlighterListener() {

            @Override
            public void moveToNextForHighlighter() {
                if(mTTSPopup!=null){
                    mViewer.scrollNext();
                }
            }
        });

        textToSpeech = new TextToSpeech(context, new OnInitListener() {

            @Override
            public void onInit(int arg0) {

                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                    @Override
                    public void onStart(String arg0) {
                        DebugSet.d("TAG", "text speech start");
                        if( !isSelectionPlaying )
                            mViewer.addTTSHighlight(ttsDataList.get(ttsCurrentIndex));

                    }

                    @Override
                    public void onError(String arg0) {

                    }

                    @Override
                    public void onDone(String arg0) {
                        if( isPlaying ) {
                            if( ttsCurrentIndex < ttsDataList.size()-1 ){
                                if( !isSelectionPlaying ) {
                                    ttsCurrentIndex++;
                                }
                                speak();
                            } else{
                                if(isPaused) {
                                    mViewer.clearAllTTSDataInfo();
                                    isChapterChanged=true;
                                    setTTSData(mViewer.makeTTSDataInfoInBackground());
                                } else {
                                    if(!isChapterChanged){
                                        mViewer.scrollNext();
                                    }
                                }
                            }

                            if( isSelectionPlaying )
                                isSelectionPlaying = false;

                        }
                    }
                });
//				setTTSData(mViewer.makeTTSDataInfo());

            }
        });

        setPopupView(width, height);
    }

    OnClickListener btnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_prev:
                    moveByTTSData();
                    break;
                case R.id.play_next:
                    ttsCurrentIndex++;
                    speak();
                    break;
                case R.id.play_pause:

                    if( isPlaying || isSelectionPlaying){
                        isPlaying = false;
                        isSelectionPlaying=false;
                        ((Button)v).setBackgroundResource(R.drawable.btn_play_nor);
                        textToSpeech.stop();
                    } else {
                        isSelectionPlaying = false;
                        mViewer.setPreventNoteref(true);
                        ((Button)v).setBackgroundResource(R.drawable.btn_puese_nor);
                        speak();
                        isPlaying = true;
                    }
                    break;
                case R.id.exit:
                    dismissPopup();
                    mViewer.setPreventNoteref(false);
                    break;

                case R.id.btn_tts_style :

                    if(lyStyle.getVisibility()==View.VISIBLE){
                        lyStyle.setVisibility(View.GONE);
                    }else {
                        lyStyle.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void setPopupView(int width, int height){
        LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = layoutInflater.inflate(R.layout.tts_control_popup, null);

        Button prevBtn = (Button)contentView.findViewById(R.id.play_prev);
        Button nextBtn = (Button)contentView.findViewById(R.id.play_next);
        Button playBtn = (Button)contentView.findViewById(R.id.play_pause);
        Button exitBtn = (Button)contentView.findViewById(R.id.exit);
        Button styleBtn = (Button)contentView.findViewById(R.id.btn_tts_style);

        prevBtn.setOnClickListener(btnClickListener);
        nextBtn.setOnClickListener(btnClickListener);
        playBtn.setOnClickListener(btnClickListener);
        exitBtn.setOnClickListener(btnClickListener);
        styleBtn.setOnClickListener(btnClickListener);

        SeekBar rateSeekbar = (SeekBar)contentView.findViewById(R.id.rate_seekbar);
        SeekBar pitchSeekbar = (SeekBar)contentView.findViewById(R.id.pitch_seekbar);
        SeekBar volumeSeekbar = (SeekBar)contentView.findViewById(R.id.volume_seekbar);

        final AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);


        rateSeekbar.setMax(50);
        pitchSeekbar.setMax(50);
        volumeSeekbar.setMax(maxVolume);

        rateSeekbar.setProgress((int) (BookPreference.getTTSRate(mContext)*10));
        pitchSeekbar.setProgress((int) (BookPreference.getTTSPitch(mContext)*10));
        volumeSeekbar.setProgress(BookPreference.getTTSVolume(mContext));

        lyStyle = (LinearLayout)contentView.findViewById(R.id.ly_tts_style);

        textToSpeech.setSpeechRate(BookPreference.getTTSRate(mContext));
        textToSpeech.setPitch(BookPreference.getTTSPitch(mContext));

        rateSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            boolean isPlaying = false;
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float rate = seekBar.getProgress() / 10f;
                textToSpeech.setSpeechRate(rate);
                Log.d("TAG", "rateSeekbar onStopTrackingTouch :" + isPlaying);
                if( isPlaying )
                    speak();

                BookPreference.setTTSRate(mContext, rate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if( textToSpeech.isSpeaking() ){
                    isPlaying = true;
                    textToSpeech.stop();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });

        pitchSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            boolean isPlaying = false;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float pitch = seekBar.getProgress() / 10f;
                textToSpeech.setPitch(pitch);
                Log.d("TAG", "pitchSeekbar onStopTrackingTouch :" + isPlaying);
                if( isPlaying )
                    speak();

                BookPreference.setTTSPitch(mContext, pitch);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if( textToSpeech.isSpeaking() ){
                    isPlaying = true;
                    textToSpeech.stop();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });

        volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            boolean isPlaying = false;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int volume = seekBar.getProgress();
                BookPreference.setTTSVolume(mContext, volume);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                Log.d("TAG", "volumeSeekbar onStopTrackingTouch :" + isPlaying);
                if( isPlaying )
                    speak();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if( textToSpeech.isSpeaking() ){
                    isPlaying = true;
                    textToSpeech.stop();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });
//		String languageCode = BookPreference.getTTSLanguage(mContext);
//		ArrayList<Locale> language = mViewer.getAvailableLanguageList();
//		int selectionNum = 0;
//		for (int i=0; i<language.size(); i++) {
//			Locale locale = language.get(i);
//			if( languageCode.equals(locale.getDisplayName()) ) {
//				mViewer.setLanguageTTS(locale);
//				selectionNum = i;
//			}
//		}


//		Spinner languageSpinner = (Spinner)contentView.findViewById(R.id.language_selector);
//		final LanguageAdater adapter = new LanguageAdater(mContext,R.layout.item_fontsizeselector, language);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		languageSpinner.setAdapter(adapter);
//		languageSpinner.setSelection(selectionNum);
//		languageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> arg0, View view, int position, long arg3) {
//				Locale lo = (Locale)adapter.getItem(position);
//
//				mViewer.setLanguageTTS(lo);
//
//				BookPreference.setTTSLanguage(mContext, lo.getDisplayName());
//
//				Log.d("TAG", "languageSpinner onItemSelected :" + mViewer.isPlayingTTS());
//				if( mViewer.isPlayingTTS() ){
//					mViewer.stopTTS();
//					mViewer.playTTS();
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//
//			}
//		});


        mTTSPopup = new PopupWindow(contentView, width, LayoutParams.WRAP_CONTENT, true);
        mTTSPopup.setOutsideTouchable(false);
        mTTSPopup.setFocusable(false);
        mTTSPopup.setBackgroundDrawable(new BitmapDrawable()) ;
//		mTTSPopup.setTouchInterceptor(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//
//				if( event.getAction() == MotionEvent.ACTION_OUTSIDE ){
//					dismissPopup();
//				}
//				return false;
//			}
//		});
        mTTSPopup.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
//				mViewer.destroyTTS();
            }
        });
    }

    private void speak() {
        speak(ttsDataList.get(ttsCurrentIndex).getText());
    }

    private void speak(String text) {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    public void speakSelectionText(String text) {
        isPlaying = false;
        textToSpeech.stop();
        isSelectionPlaying = true;
        speak(text);
//		mViewer.addTTSHighlightFromSelection();
    }

    public void showPopup(ViewerContainer container) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        FrameLayout parentView = new FrameLayout(mContext);
        parentView.setLayoutParams(params);
//		parentView.addView(mTTSPopup.getContentView());

        mTTSPopup.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
    }

    public void dismissPopup() {
        mTTSPopup.dismiss();
//		textToSpeech.shutdown();
        textToSpeech.stop();
        mViewer.removeTTSHighlight();
        mViewer.preventPageMove(false);
        mViewer.setPreventMediaControl(false);
    }

    public boolean isShowing(){
        return mTTSPopup.isShowing();
    }

    public void moveByTTSData(){
        if( ttsCurrentIndex < ttsDataList.size()-1 ) {
            mViewer.moveByTTSData(ttsDataList.get(ttsCurrentIndex));
        }
    }

    public void setTTSData(ArrayList<TTSDataInfo> ttsData){
        ttsDataList = ttsData;
        if(isPaused) {
            ttsCurrentIndex = 0;
            if( isPlaying )
                speak();
        } else {
            mViewer.requestTTSStartPosition();
        }
    }

    public void setTTSDataFromSelection(ArrayList<TTSDataInfo> ttsData){
        ttsDataList = ttsData;
    }

    private boolean isChapterChanged = false;
    public boolean getChapterChanged() {
        return isChapterChanged;
    }

    public void setChapterChanged(boolean changed) {
        isChapterChanged=changed;
    }

}

