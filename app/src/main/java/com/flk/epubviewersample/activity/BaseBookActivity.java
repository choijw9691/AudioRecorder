package com.flk.epubviewersample.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ebook.epub.parser.common.UnModifiableArrayList;
import com.ebook.epub.parser.ocf.EpubFileSystemException;
import com.ebook.epub.parser.ocf.XmlContainerException;
import com.ebook.epub.parser.opf.XmlPackageException;
import com.ebook.epub.parser.ops.XmlNavigationException;
import com.ebook.epub.viewer.BookHelper;
import com.ebook.epub.viewer.Bookmark;
import com.ebook.epub.viewer.DebugSet;
import com.ebook.epub.viewer.Highlight;
import com.ebook.epub.viewer.MyZip;
import com.ebook.epub.viewer.ReadingStyle;
import com.ebook.epub.viewer.ReadpositionData;
import com.ebook.epub.viewer.SearchResult;
import com.ebook.epub.viewer.ViewerActionListener;
import com.ebook.epub.viewer.ViewerContainer;
import com.ebook.epub.viewer.ViewerContainer.OnTextSelection;
import com.ebook.epub.viewer.data.AudioNavigationInfo;
import com.ebook.epub.viewer.data.ChapterInfo;
import com.ebook.epub.viewer.data.ReadingOrderInfo;
import com.ebook.media.AudioContent;
import com.ebook.tts.TTSDataInfo;
import com.flk.epubviewersample.R;
import com.flk.epubviewersample.view.AnnotationDialog;
import com.flk.epubviewersample.view.AudioSelector;
import com.flk.epubviewersample.view.BookmarkSelector;
import com.flk.epubviewersample.view.ChapterSelector;
import com.flk.epubviewersample.view.MediaOverlayPopup;
import com.flk.epubviewersample.view.SearchDialog;
import com.flk.epubviewersample.view.SettingsDialog;
import com.flk.epubviewersample.view.TTSControlPopup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class BaseBookActivity extends Activity implements Runnable {

    String TAG = "BaseBookActivity";

    public final static int LOAD_BOOK_EPUB = 103;
    public final static int LOAD_FORCE_CHANGING = 104;
    public final static int EPUB_LOAD_ERROR = 105;
    public final static int MSG_SCREEN_HIDE = 107;
    public final static int EPUB_PARSE_FINISH = 108;

    public static BaseBookActivity mThis = null;

    @SuppressLint("StaticFieldLeak")
    public static ViewerContainer mViewer;

    String mPath;

    String mBookName;

    boolean layoutVisible = false;

    int mHandMode = 0;

    public boolean mVolumeUse = false;

    ImageView veilView;

    ProgressBar loadProgress;

    RelativeLayout viewerContainer;

    LinearLayout topNavLinear;

    LinearLayout bottomNavLinear;

    LinearLayout controlbox;

    TextView mTitle;

    TextView mIndicator;

    ImageView mBookmark;

    ImageView mBookmarkDefault;

    Button mNotes;

    SeekBar mSeekbar;
    SeekBar mLightSeekBar;
    int __currentProgress=0;
    Toast mToast;

    Handler mMainHandler;

    TTSControlPopup ttsControlPopup = null;

    ArrayList<View> mViewList = new ArrayList<View>();

    SearchDialog mSearchDialog = null;

    PopupWindow mContextMenu=null;

    SettingsDialog mSettingsDialog = null;

    //	[ssin-audio] s
    HashMap<String, AudioContent> audioContents = new HashMap<String, AudioContent>();
    ArrayList<String> audioContentsOnCurrentPage = new ArrayList<String>();
    AudioContent currentPlayAudioContent;
    boolean isPlaying=false;
    boolean isVeilViewShow=false;
    //	[ssin-audio] e

    //	[ssin-mediaoverlay] s
    MediaOverlayPopup mediaOverlayControlPopup = null;
    private boolean hasMediaOverlayOnChapter = true;
    private boolean isMediaOverlayPlaying=false;
    //	[ssin-mediaoverlay] e

    private boolean isMoveSeekbar = false;

    private RelativeLayout lyScrollNext;
    private RelativeLayout lyScrollPrev;
    private Button btnScrollNext;
    private Button bntScrollPrev;

    private Highlight mCurrentHighlight;

    private ArrayList<TTSDataInfo> ttsDataInfoArrayList = new ArrayList<>();

    private static final int REQUEST_CODE_SEARCH = 990;

    private void createAudioListUI(UnModifiableArrayList<AudioNavigationInfo> audioNavigations) {

        if(audioNavigations.size()<=0){
            Toast.makeText(getApplicationContext(), "NO AUDIO", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<AudioNavigationInfo> fullList = new ArrayList<>();

        for (int idx = 0; idx < audioNavigations.size(); idx++) {
            if (audioNavigations.get(idx).audioNavigationInfos.size() > 0) {
                // child exist
                fullList.addAll(audioNavigations.get(idx).audioNavigationInfos);
            }
        }

        // test
        AudioSelector audio = new AudioSelector(mThis, fullList);
        audio.show();

        audio.setOnSelectAudio(new AudioSelector.OnSelectAudio() {

            @Override
            public void onSelect(int position, AudioNavigationInfo audioInfo) {
                isVeilViewShow = true;
                Toast.makeText(getApplicationContext(), "PLAY : "+audioInfo.audioPath, Toast.LENGTH_SHORT).show();
                mViewer.moveByAudioNavigation(audioInfo);
            }
        });
    }
    /**
     * bottom Menu class
     * @author syw
     */
    int selectedChapterPosition = 0;
    class OnClickCallback implements OnClickListener {
        @Override
        public void onClick(View v) {

            int id = v.getId();
            switch (id) {
                case R.id.selectAudio: {
                    createAudioListUI(mViewer.getAudioNavigations());
                    break;
                }
                case R.id.selectChapter: {

                    UnModifiableArrayList<ChapterInfo> chapters = mViewer.getChapterInfoList();

                    for(int idx=0; idx<chapters.size(); idx++){
                        int page = mViewer.getChapterStartPage(chapters.get(idx).getChapterFilePath());
                    }

                    if (chapters.size() <= 0)
                        return;

                    ChapterSelector cs = new ChapterSelector(mThis, chapters, mViewer.getCurrentChapterInfo());
                    cs.show();

                    cs.setOnSelectChapter(new ChapterSelector.OnSelectChapter() {

                        @Override
                        public void onSelect(ChapterSelector sender, int position, ChapterInfo chapter) {
                            selectedChapterPosition = position;
                            isVeilViewShow=true;
                            Toast.makeText(mThis,"" + chapter.getChapterName(), Toast.LENGTH_SHORT).show();
                            mViewer.goPage(chapter);
                        }
                    });
                    break;
                }

                case R.id.bookMark: {

                    ArrayList<Bookmark> bms = mViewer.getBookMarks();

                    BookmarkSelector bs = new BookmarkSelector(mThis, bms, mViewer.getDocumentVersion());
                    bs.show();
                    bs.setOnSelectBookmark(new BookmarkSelector.OnSelectBookmark() {
                        @Override
                        public void onSelect(BookmarkSelector sender, Bookmark bmd) {
                            isVeilViewShow = true;
                            Toast.makeText(mThis,"" + bmd.chapterName,Toast.LENGTH_SHORT).show();
                            mViewer.goPage(bmd);
                        }
                    });
                    break;
                }

                case R.id.btn_search: {

                    //	[ssin-search] s
                    mViewer.goPageByJump();
                    Intent searchIntent = new Intent(getApplicationContext(), SearchListActivity.class);
                    startActivityForResult(searchIntent, REQUEST_CODE_SEARCH);
                    //	[ssin-search] e

//                    if (mSearchDialog == null)
//                        mSearchDialog = new SearchDialog(mThis, mViewer);
//
//                    mSearchDialog.show();

//                    mSearchDialog.setOnSelectKeyword(new SearchDialog.OnSelectKeyword() {
//
//                        @Override
//                        public void onSelect(SearchDialog sender, int position, SearchResult result) {
////                            mViewer.goPageByJump();
//                            isVeilViewShow=true;
//                            if (result != null) {
//                                mViewer.focusText(result);
//                            }
//                        }
//                    });
                    break;
                }

                case R.id.btn_bookmark: {
                    mViewer.doBookmark();
                    break;
                }

            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP){
            if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
                if (mContextMenu != null && mContextMenu.isShowing()) {
                }
                else {
                    toggleLayoutVisible();
                }

            } else if( event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN ) {
                if(mVolumeUse)
                    mViewer.scrollPrior();
                return true;
            }
            else if( event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP ){
                if(mVolumeUse)
                    mViewer.scrollNext();
                return true;
            }
        } else if ( event.getAction() == KeyEvent.ACTION_DOWN ){
        }

        return super.dispatchKeyEvent(event);
    }

//	void do_copy_clipboard() {
//
//		String text = mViewer.getSelectedText();
//		((ClipboardManager) getSystemService("clipboard")).setText(text);
//
//		Toast.makeText(mThis, "복사 : " + text, Toast.LENGTH_SHORT).show();
//	}

//    void do_highlight() {
//        mViewer.doHighlight();
//    }

//    void do_memo() {
//        mViewer.doMemo();
//    }

//	void do_underLine() {
//		mViewer.doUnderline();
//	}

    void lockScreenOrientation() {

        switch (getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    if(rotation == android.view.Surface.ROTATION_90|| rotation == android.view.Surface.ROTATION_180){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }
                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    if(rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                }
                break;
        }
    }

    void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mViewer.stopMediaOverlay();

        lockScreenOrientation();

        if( mSettingsDialog != null && mSettingsDialog.isShowing() ) {
            mSettingsDialog.hide();
        }

        super.onConfigurationChanged(newConfig);
    }

    long startTime = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 뷰어 디버그 로그 설정
        DebugSet.DEBUGABLE = true;

        Log.d("DEBUG","CURRENT VIEWER VERSION : "+ BookHelper.VIEWER_VERSION);

        printMemory("onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.basebookactivity);

        mThis = this;

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        int info = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        DebugSet.d("TAG", "Process Memory limit : " + info);
        DebugSet.d("TAG", "Nativa Heap limit : " + Long.toString(mi.availMem));

        initExtras(true);

        initViews();

        toggleProgress(true);

        Thread startThread = new Thread(this);
        startThread.start();

//		registerReceiver(new BroadcastsHandler(), new IntentFilter(Intent.ACTION_HEADSET_PLUG));

//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        final Handler h = new Handler();
        final int delay = 3000; //milliseconds

//		h.postDelayed(new Runnable(){
//			public void run(){
//
//				List<ActivityManager.RunningServiceInfo> rs=am.getRunningServices(200);
//
//				for(ActivityManager.RunningServiceInfo ar:rs){
//					if(ar.process.equals("com.android.systemui:screenshot")){
//						Bitmap bitmap = viewerScreenShot(viewerContainer);
//						saveBitmapToFile(addWaterMark(bitmap), Environment.getExternalStorageDirectory().getAbsolutePath()+"/testpic.png");
//					}
//				}
//				h.postDelayed(this, delay);
//			}
//		}, delay);
    }

    @Override
    public void onBackPressed() {
        if( mViewer.getViewerLayoutMode() == ViewerContainer.LayoutMode.Reflowable ) {
            if( mSettingsDialog != null && mSettingsDialog.isShowing() ) {
                mSettingsDialog.hide();

                mViewer.setPreviewMode(false);

                if( mSettingsDialog.checkSettingsChanged() ) {
                    mViewer.reLoadBook();
                }

            } else {
                // 뷰어 종료 프로세스 시작
                mViewer.onClose();
            }
        } else {
            if( mSettingsDialog != null && mSettingsDialog.isShowing() ) {
                mSettingsDialog.hide();
            } else {
                mViewer.onClose();
            }
        }
    }

    void initExtras(boolean bInit) {

        mMainHandler = new MainHandler();
        Intent intent = getIntent();

        if (intent != null) {

            String path = intent.getStringExtra("BOOK_PATH");
            String file = intent.getStringExtra("BOOK_FILE");

            if (path == null || file == null) {
                Toast.makeText(this, "invalid book filename or path",
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            mPath = path;
            mBookName = file;
        }
    }

    void settingViewer(){

        if(mViewer.getViewerPageDirection() == ViewerContainer.PageDirection.RTL)
            mSeekbar.setRotation(180);

        mViewer.setPageColumOnLandscape(2);
        mViewer.setPageEffect(1);

        mViewer.setOnNoterefListener(new ViewerContainer.OnNoterefListener() {

            @Override
            public void didShowNoterefPopup() {
                Log.d("DEBUG","show noteref ");
            }

            @Override
            public void didHideNoterefPopup() {
                Log.d("DEBUG","hide noteref ");
            }
        });

        mViewer.setMoveToLinearNoChapter(new ViewerContainer.OnMoveToLinearNoChapterListener() {

            @Override
            public void moveToLinearNoChapter(String chapterFilePath) {
            }
        });

        mViewer.setOnVideoInfoListener(new ViewerContainer.OnVideoInfoListener() {

            @Override
            public void videoInfo(String onVideoInfo) {
                Toast.makeText(BaseBookActivity.this, "video src  : " + onVideoInfo, Toast.LENGTH_LONG).show();
//                if (onVideoInfo.substring(0, 7).matches("file://")) {
//                    onVideoInfo =  onVideoInfo.substring(7);
//                }
//                File file = new File( onVideoInfo );
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setDataAndType(FileProvider.getUriForFile( mThis, mThis.getPackageName() + ".fileprovider", file ), "video/*");
//                startActivity(intent);
            }
        });

//        mViewer.setOnTTSStateChangeListener(new ViewerContainer.OnTTSStateChangeListener() {
//
//            @Override
//            public void onStop() {
//            }
//
//            @Override
//            public void onStart() {
//            }
//
//            @Override
//            public void onReady() {
//                Log.d(TAG, "OnTTSStateChangeListener onReady");
//                ttsControlPopup = new TTSControlPopup(BaseBookActivity.this, mViewer, mViewer.getWidth(), mViewer.getHeight());
//                ttsControlPopup.showPopup(mViewer);
//                toggleLayoutVisible();
//                toggleProgress(false);
//            }
//
//            @Override
//            public void onError() {
//                Toast.makeText(BaseBookActivity.this, "TTS 오류 입니다.", Toast.LENGTH_SHORT).show();
//                toggleLayoutVisible();
//                toggleProgress(false);
//            }
//        });

//		[ssin-mediacontrol] s
        mViewer.setOnMediaControlListener(new ViewerContainer.OnMediaControlListener() {

            @Override
            public void didPlayPreventMedia(final String xPath, String mediaType) {
//				TTS 실행 중 미디어 제어 불가 통지 리스너

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showDialog(xPath);
                    }
                });


            }
        });
//		[ssin-mediacontrol] e

        // [ssin - actionmode]
//        mViewer.setOnActionModeListener(new ViewerContainer.OnActionModeListener() {  // TODO :: new custom selection - deleted
//
//            @Override
//            public void setClickItemId(int id) {
//                if (id == R.id.highlight) {
//                    mViewer.doHighlight();
//                } else if (id == R.id.memo) {
//                    mViewer.doMemo();
//                } else if(id == R.id.share){
//                    Toast.makeText(BaseBookActivity.this, mViewer.getSelectedText(), Toast.LENGTH_SHORT).show();
//                    mViewer.resetTextSelection();
//                } else if(id == R.id.tts){
//
//                    if(ttsDataInfoArrayList.isEmpty())
//                        ttsDataInfoArrayList = mViewer.makeTTSDataInfo();
//
//                    if(ttsControlPopup!=null){
//                        ttsControlPopup.setTTSDataFromSelection(ttsDataInfoArrayList);
//                        mViewer.requestTTSDataFromSelection();
//                    }
//
//                    ttsControlPopup.showPopup(mViewer);
//                    mViewer.preventPageMove(true);
//                }
//            }
//        });

        mViewer.setOnPageBookmark(new ViewerContainer.OnPageBookmark() {

            @Override
            public void onMark(boolean bShow) {
                Log.d(TAG, "Page Bookmark::onMark() >> " + bShow);
                if(mBookmark.getVisibility() == View.VISIBLE && bShow) {
                } else {
                    toggleBookmarkVisible(bShow);
                }
            }

            @Override
            public void onAdd(Bookmark bm, boolean bShow) {
                Log.d(TAG, "Page Bookmark::onAdd() >> " + bShow);
                onMark(bShow);
            }
        });

//		[ssin-audio] s
//		audio 관련 리스너 세팅
        mViewer.setOnAudioListener(new ViewerContainer.OnAudioListener() {

            @Override
            public void finishAudioList(HashMap<String, AudioContent> list) {
//				챕터 로딩 후 뷰어에서 현재 챕터에 있는 audio 정보 리스트 프론트에 전달
//				autoplay 속성에 대한 제어는 autoplay 값 보고 front에서 시나리오대로 알아서 진행
                audioContents.clear();
                audioContents = list;

                Iterator<String> iterator = audioContents.keySet().iterator();

                while (iterator.hasNext()) {
                    String xpath = (String) iterator.next();
                }
            }

            @Override
            public void existAudioContentsOncurrentPage(ArrayList<String> audioList) {
                audioContentsOnCurrentPage.clear();
                audioContentsOnCurrentPage = audioList;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(audioContentsOnCurrentPage.size()>0)
                            controlbox.setVisibility(View.VISIBLE);
                        else
                            controlbox.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void didPlayAudio(String xPath, double startTime) {
//				audio 재생 후 재생한 해당 audio xPath 및 재생 시작 시간 프론트에 전달
                Log.d("DEBUG","didPlayAudio");
                currentPlayAudioContent = audioContents.get(xPath);
            }

            @Override
            public void didFinishAudio(String xPath) {
//				audio 재생 완료한 후 해당 audio xPath 프론트에 전달
                Log.d("DEBUG","didFinishAudio");
            }

            @Override
            public void didPauseAudio(String xPath, double pauseTime) {
//				audio 일시정지 후 해당  audio xPath 및 일시정지 시 시간 프론트에 전달
                Log.d("DEBUG","didPauseAudio");
            }

            @Override
            public void updateCurrentPlayingPosition(String xPath, double currentTime) {
                Log.d("DEBUG","[FRONT] updateCurrentPlayingPosition() xPath : "+xPath+" / currentTime : "+currentTime);
//				audio 재생 시 현재 재생중인 audio xPath 및 현재 재생 시간 프론트에 전달
            }

            @Override
            public void didStopAudio(String xPath) {
//				audio 재생 중 사용자가 멈추면 해당 audio xPath 프론트에 전달
                Log.d("DEBUG","didStopAudio");
            }
        });
//		[ssin-audio] e

//		[ssin-BG] s
        mViewer.setOnBGMControlListener(new ViewerContainer.OnBGMControlListener() {

            @Override
            public void didBGMStopListener() {
                Log.d("DEBUG","didBGMStopListener()");
            }

            @Override
            public void didBGMPlayListener() {
                Log.d("DEBUG","didBGMPlayListener()");
            }

            @Override
            public void didBGMPauseListener() {
                Log.d("DEBUG","didBGMPauseListener()");
            }
        });
//		[ssin-BG] e

//		[ssin-mediaoverlay] s
        mViewer.setOnMediaOverlayStateListerner(new ViewerContainer.OnMediaOverlayStateListener() {

            @Override
            public void existMediaOverlay(boolean hasMediaOverlay) {
//				Reflowable인 경우에 현재 페이지 미디어오버레이 존재여부 callback

                if(hasMediaOverlay){
//					미디어오버레이 존재 시 재생 index 설정 함수 호출
                    mViewer.setPositionOfMediaOverlay();

                } else{
                    if(mViewer.getViewerLayoutMode() == ViewerContainer.LayoutMode.Reflowable && mediaOverlayControlPopup!=null && mediaOverlayControlPopup.isPlayingMediaOverlay){		// TODO :reflowable인 경우만 체크하여 콜
                        mViewer.scrollNext();
                    } else {
//						미디어오버레이 없을경우 UI 처리
                        if(mediaOverlayControlPopup!=null && mediaOverlayControlPopup.isShowing()){

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mediaOverlayControlPopup.dismissPopup();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void setPositoinOfMediaOverlayDone() {
//				재생 index 설정 callback

//				미디어오버레이 존재 할 경우 UI 처리
                if(mediaOverlayControlPopup==null){
                    mediaOverlayControlPopup = new MediaOverlayPopup(BaseBookActivity.this, mViewer, mViewer.getWidth(), mViewer.getHeight());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mediaOverlayControlPopup.showPopup(mViewer);
                        }
                    });

                } else if(!mediaOverlayControlPopup.isShowing()){

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mediaOverlayControlPopup.showPopup(mViewer);
                        }
                    });
                }

//				미디어오버레이 기존 재생 상태에 따른 컨트롤
                if(mediaOverlayControlPopup.isPlayingMediaOverlay){
                    mViewer.playMediaOverlay();
                }
            }

            @Override
            public void finishMediaOverlayPlay() {
//				미디어오버레이 재생 완료 콜백
                Log.d("DEBUG","finishPlay() ");
                mViewer.scrollNext();
            }

            @Override
            public void selectedMediaOverlayPlaying() {
//				미디어오버레이 선택재생 시 콜백 - 프론트 메뉴 띄우지 않음
                layoutVisible = true;
                toggleLayoutVisible();
            }

            @Override
            public void didMediaOverlayPlayListener() {
                Log.d("DEBUG","mediaOverlay PLAY");
                isMediaOverlayPlaying=true;
            }

            @Override
            public void didMediaOverlayPauseListener() {
                Log.d("DEBUG","mediaOverlay PAUSE");
                isMediaOverlayPlaying=false;
            }

            @Override
            public void didMediaOverlayStopListener() {
                Log.d("DEBUG","mediaOverlay STOP");
                isMediaOverlayPlaying=false;
            }
        });
//		[ssin-mediaoverlay] e

        mViewer.setOnPageScroll(new ViewerContainer.OnPageScroll() {

            @Override
            public void onScrollBefore(int currentPage) {
            }

            @Override
            public void onScrollAfter(int currentPage, int readingPage, int pageCount, double currentPercent) {
                Log.d("SSIN", "onScrollAfter ################### " + currentPage + " | " + readingPage + " | " + pageCount + " | " + currentPercent);

//				[ssin-mediaoverlay] s
//				Reflowable 컨텐츠인 경우 챕터에 미디어오버레이 존재 시 현재 페이지에 미디어오버레이 존재여부 함수 call
                if(hasMediaOverlayOnChapter)
                    mViewer.existMediaOverlayOnPage();
//				[ssin-mediaoverlay] e

                mIndicator.setText("" + Math.round(currentPercent) + "% - "+currentPage);
                mViewer.hasBookmark();
                mSeekbar.setProgress((int)Math.round(currentPercent));
                toggleProgress(false);
                veilView.setVisibility(View.INVISIBLE);

                if(ttsControlPopup!=null && ttsControlPopup.isShowing()) {
                    ttsControlPopup.setChapterChanged(false);
                }

                if(isSearching){
                    mViewer.goPageByJump();
                }

                mViewer.getCurrentChapterInfo();
            }

            @Override
            public void onScrollInScrollMode(int direction) {
                if(direction==1){	//next
                    lyScrollNext.setVisibility(View.VISIBLE);
                    lyScrollPrev.setVisibility(View.INVISIBLE);
                } else if (direction==-1){	//prev
                    lyScrollPrev.setVisibility(View.VISIBLE);
                    lyScrollNext.setVisibility(View.INVISIBLE);
                } else if (direction==0){	//none
                    lyScrollPrev.setVisibility(View.INVISIBLE);
                    lyScrollNext.setVisibility(View.INVISIBLE);
                }
            }
        });

//		mViewer.setOnSearchResult(new ViewerContainer.OnSearchResult() {
//			@Override
//			public void onStart(EPubViewer sender) {
//				if (mSearchDialog != null) {
//					mSearchDialog.showProgress(true);
//				}
//			}
//
//			@Override
//			public void onEnd(EPubViewer sender) {
//				if (mSearchDialog != null) {
//					mSearchDialog.showProgress(false);
//				}
//			}
//
//			@Override
//			public void onFound(EPubViewer sender, SearchResult sr) {
//				if (mSearchDialog != null) {
//					mSearchDialog.addResult(sr);
//				}
//			}
//		});


        mViewer.setOnTouchEventListener(new ViewerContainer.OnTouchEventListener() {

            @Override
            public void onDown(int x, int y) {}

            @Override
            public void onUp(BookHelper.ClickArea ca) {
                if (ca == BookHelper.ClickArea.Middle) {
                    toggleLayoutVisible();
                } else {
                    if (ca == BookHelper.ClickArea.Left_Corner) {
                        mViewer.doBookmark();
                    }

                    if (layoutVisible)
                        toggleLayoutVisible();
                }
            }

            @Override
            public void onFling() {
                Log.d("SSIN","onFling in");
                if (layoutVisible)
                    toggleLayoutVisible();
            }

            @Override
            public void onTwoFingerMove(int moveDirection) {
                Log.d("DEBUG","moveDirection : "+moveDirection);
                if(moveDirection == 0 )
                    mViewer.reLoadBook();
            }
        });

        mViewer.setOnViewerState(new ViewerContainer.OnViewerState() {
            @Override
            public void onStart() {
            }

            @Override
            public void onEnd() {
                DebugSet.e(TAG, "Viewer State : " + "onEnd() ");
                // viewer all parameters save after
                hideSelectionMenu(mContextMenu);
                finish();
            }

        });

        ViewerActionListener inputListener = new ViewerActionListener(BaseBookActivity.this, mViewer);      // TODO :: new custom selection - modified
        mViewer.setOnTouchListener(inputListener);
        mViewer.setOnKeyListener(inputListener);

//		mViewer.setBookmarkIcon(getResources().getDrawable(R.drawable.icon_bookmark_on));
        mViewer.setSelectionIcon(
                getResources().getDrawable(R.drawable.btn_longpre_l),
                getResources().getDrawable(R.drawable.btn_longpre_r));    // TODO :: new custom selection - modified

        mViewer.setContextMenuSize(36, 10, 5);     // TODO :: new custom selection added - dp

        mViewer.setMemoIconPath("file:///android_asset/btn_body_memo_nor.png");

        mViewer.setSlideResource(true, R.anim.reader_ani_push_left_in, R.anim.reader_ani_push_left_out);
        mViewer.setSlideResource(false, R.anim.reader_ani_push_right_in, R.anim.reader_ani_push_right_out);
//        mViewer.setActionModeItemResourceWithDisableItem(R.menu.annotation, 5,false, 4,true);   //[ssin - actionmode] // TODO :: new custom selection - deleted

        mViewer.setTextSelectionColor(0x4D0087ff);

        mViewer.setOnMemoSelection(new ViewerContainer.OnMemoSelection() {

//            @Override
//            public void onStart(EPubViewer sender, Highlight high) {        // TODO :: new custom selection - deleted
//                MemoDialog md = new MemoDialog(mThis, high, R.style.myDialog, mViewer);
//                md.show();
//
//                md.setOnMemoClose(new MemoDialog.OnMemoClose() {
//                    @Override
//                    public void onClose(Highlight high, String memo, boolean isEdit) {
//
//                        if( memo.trim().length() <= 0 && !isExisHighlight) {
//                            mViewer.deleteHighlight(high);
//                        } else if( memo.trim().length()>0 && isExisHighlight){
//                            mViewer.changeIntoMemo(high, memo);
//                        } else {
//                            mViewer.requestShowMemo(high, isEdit);
//                        }
//                    }
//                });
//
//                md.setOnDismissListener(new OnDismissListener() {
//
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        isExisHighlight = false;
//                        mCurrentHighlight = null;
//                    }
//                });
//            }
//            @Override
//            public void onEnd(EPubViewer sender) {
//                Log.d(TAG, "****************************** memo end");
//            }

            @Override
            public void onGetAllMemoText(String allMemoText) {
                Intent intent = new Intent(getApplicationContext(), MemoWriteActivity.class);
                intent.putExtra("MEMO_CONTENT",allMemoText);
                startActivityForResult(intent, REQUEST_MEMO_ACTIVITY);
            }
        });

        mViewer.setOnReportError(new ViewerContainer.OnReportError() {
            @Override
            public void onError(int errCode) {
                switch (errCode) {
                    case 0: {
                        // setup
                        finish();
                        break;
                    }

                    case 1: {
                        // text selection
                        Toast.makeText(mThis, "선택할 수 없는 택스트 입니다.", Toast.LENGTH_LONG).show();
                        break;
                    }

                    case 2: {
                        // bookmark error
                        Toast.makeText(mThis, "북마크 정보를 얻을 수 없습니다.", Toast.LENGTH_LONG).show();
                        break;
                    }

                    case 3: {
                        Toast.makeText(mThis, "선택한 텍스트 블럭이 너무 큽니다.", Toast.LENGTH_LONG).show();
                        break;
                    }

                    case 4: {
                        // 컨텐츠 링크 에러
                        break;
                    }

                    case 5: {
                        // text highlighting error
                        Toast.makeText(mThis, "주석 표시를 할 수 없습니다.", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        });

        mViewer.setOnBookStartEnd(new ViewerContainer.OnBookStartEnd() {

            @Override
            public void onStart() {
                Toast.makeText(mThis, "처음", Toast.LENGTH_SHORT).show();
                toggleProgress(false);
                veilView.setVisibility(View.GONE);
            }

            @Override
            public void onEnd() {
                Toast.makeText(mThis, "마지막", Toast.LENGTH_SHORT).show();
                toggleProgress(false);
                veilView.setVisibility(View.GONE);
            }
        });

        mViewer.setOnTextSelection(new OnTextSelection() {

            @Override
            public void onStartTextSelection() {
                if(layoutVisible){
                    toggleLayoutVisible();
                }
            }

            @Override
            public void onEndTextSelection() {

            }

            @Override
            public void onOverflowTextSelection() {
                Toast.makeText(BaseBookActivity.this, "1000자 넘음", Toast.LENGTH_SHORT).show();
            }

//            @Override
//            public void onStart() {
//                Log.d(TAG, "onTextSelection start ................. ");
//                if(layoutVisible){
//                    toggleLayoutVisible();
//                }
//            }
        });

        mViewer.setOnSelectionMenu(new ViewerContainer.OnContextMenu() {

            @Override
            public void onShowContextMenu(String highlightID, BookHelper.ContextMenuType contextMenuType, int x, int y) {
                // TODO :: new custom selection call back
                // TODO :: FRONT - contextMenuType 바라보도록 수정 필요
                if(layoutVisible){
                    toggleLayoutVisible();
                }
                boolean hasMemo=false;

                mContextMenu = createPopup(R.layout.context_popup, contextMenuType);

                if(contextMenuType == BookHelper.ContextMenuType.TYPE_MODIFY || contextMenuType == BookHelper.ContextMenuType.TYPE_MODIFY_CONTINUE ){
                    ReadingOrderInfo current = mViewer.getCurrentSpineInfo();
                    if(highlightID != null){
                        for( Highlight h: mViewer.getHighlights() ) {
                            if(highlightID.equals(h.highlightID)) { // h.chapterFile.toLowerCase().equals(fileName) &&
                                mCurrentHighlight = h;
                                if(h.isMemo()){
                                    hasMemo = true;
                                    mContextMenu = showMemoPopup(R.layout.memo_popup, h.memo, h.colorIndex );
                                } else {
                                    mContextMenu = createPopup2(R.layout.context_popup2, contextMenuType);
                                }
                                break;
                            }
                        }
                    } else {
                        mContextMenu = createPopup2(R.layout.context_popup2, contextMenuType);
                        mCurrentHighlight=null;
                    }
                }
                mViewer.registSelectionMenu(mContextMenu);
                showSelectionMenu(mContextMenu, x, y, hasMemo);
            }

            @Override
            public void onHideContextMenu() {
                // TODO :: new custom selection call back
                hideSelectionMenu(mContextMenu);
            }

//            @Override
//            public void onShow(boolean isHighlight, String highlightID, int top, int bottom) {
//                // 구버전 메뉴 콜백
//                boolean hasMemo=false;
//
//                if(isHighlight) {
//
//                    ReadingOrderInfo current = mViewer.getCurrentSpineInfo();
//                    String fileName = current.getSpinePath().toLowerCase();
//
//                    for( Highlight h: mViewer.getHighlights() ) {
//                        if(highlightID.equals(h.highlightID)) {
//                            mCurrentHighlight = h;
//                            if(h.isMemo()){
//                                hasMemo = true;
//                                mContextMenu = showMemoPopup(R.layout.memo_popup, h.memo, h.colorIndex );
//                            } else {
////                                mContextMenu = createPopup2(R.layout.context_popup2);
//                            }
//                            break;
//                        }
//                    }
//                } else {
////                    mContextMenu = createPopup(R.layout.context_popup);
//                }
//
//                mViewer.registSelectionMenu(mContextMenu);
//                showSelectionMenu(mContextMenu, top, bottom, hasMemo);
//            }

        });


        mViewer.setOnChapterChange(new ViewerContainer.OnChapterChange() {

            @Override
            public void onChangeBefore() {
                if(BookHelper.animationType!=3 || isVeilViewShow){
                    isVeilViewShow=false;
                    veilView.setImageResource(R.drawable.blank_white_shape);
                    veilView.setVisibility(View.VISIBLE);
                }
                toggleProgress(true);
            }

            @Override
            public void onChangeAfter(int pageCount) {
                Log.d("DEBUG", "################## changeAfter ");

                mViewer.clearAllTTSDataInfo();

                if( ttsControlPopup != null && ttsControlPopup.isShowing() ) {
                    mViewer.setPreventMediaControl(true);
                    ttsDataInfoArrayList = mViewer.makeTTSDataInfo();
                    ttsControlPopup.setTTSData(ttsDataInfoArrayList);
                }

//				[ssin-mediaoverlay] s
                hasMediaOverlayOnChapter = false;

//				챕터 변경 시 기존 미디어오버레이 관련 정보 reset
                mViewer.clearAllMediaOverlayInfo();

//				현재 챕터에 미디어오버레이 존재 여부 체크 함수 호출
                if(mViewer.existMediaOverlayOnChapter()){
                    hasMediaOverlayOnChapter = true;

//					현재 챕터에 미디어오버레이 존재 시 미디어오버레이 초기화 함수 호출
                    if(mViewer.initMediaOverlay()){
//						미디어오버레이 초기화 함수 후 재생 index 설정 함수 호출
                        if(mViewer.getViewerLayoutMode() == ViewerContainer.LayoutMode.FixedLayout){
                            mViewer.setPositionOfMediaOverlay();	// TODO : fixed인 경우만 체크하여 콜
                        }
                    }
                } else {
                    if(mViewer.getViewerLayoutMode() == ViewerContainer.LayoutMode.FixedLayout && mediaOverlayControlPopup!=null && mediaOverlayControlPopup.isPlayingMediaOverlay){ // 조건 어떻게 할건가?

//						new Handler().postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								mViewer.scrollNext();
//							}
//						}, 3000);

                    } else {
//						현재 챕터에 미디어오버레이 없을 경우 UI 처리
                        if(mediaOverlayControlPopup!=null && mediaOverlayControlPopup.isShowing()){
                            mediaOverlayControlPopup.dismissPopup();
                            mediaOverlayControlPopup = null;
                        }
                    }
                }
//				[ssin-mediaoverlay] e
            }

            @Override
            public void onPageReady(int pageCount) {
                Log.d(TAG, "################## onPageReady : "+pageCount);
            }

        });

        mViewer.setOnTagClick(new ViewerContainer.OnTagClick() {

            @Override
            public void onImage(String url) {
                try {
                    UnModifiableArrayList<String> image = mViewer.getImageList();
                    ArrayList<String> il = new ArrayList<>();
                    for (int i = 0; i < image.size(); i++) {
                        il.add(image.get(i));
                    }

                    Intent i = new Intent(BaseBookActivity.this, ImageViewer.class);
                    i.putExtra("IMAGE_FILENAME", url);
                    i.putStringArrayListExtra("IMAGE_LIST", il);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLink(String url){

                String hrefValue = URLDecoder.decode(url).replaceAll("file://", "");

                if( hrefValue.startsWith("http") || hrefValue.startsWith("mailto") || hrefValue.startsWith("tel")) {
                    if (hrefValue.startsWith("http")) {
                        Uri uri = Uri.parse(hrefValue);
                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(it);
                    } else if (hrefValue.startsWith("mailto")) {
                        Uri uri = Uri.parse(url);
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        startActivity(it);
                    } else if(hrefValue.startsWith("tel")){

                    }
                } else {
                    String id="";
                    int index = hrefValue.indexOf("#");
                    if( index != -1 ) {
                        // ID 있음
//                        hrefValue = mViewer.getCurrentSpineInfo().getSpinePath();
                        //#앞에 파일명이 없을 시 현재 파일로 처리
                        id = hrefValue.substring(index+1);
                        if( !hrefValue.substring(0, index).equals("") ){
                            hrefValue = hrefValue.substring(0, index);
                        }
                    }
                    mViewer.goPageByLink(hrefValue, id);
                }
            }
        });

        mViewer.setOnCurrentPageInfo(new ViewerContainer.OnCurrentPageInfo() {

            @Override
            public void onGet(Bookmark bm) {
                Log.d("SSIN", "BookActivity ##################### onGet : " + bm.chapterFile + "|" + bm.text);

                if(isMoveSeekbar){
                    isMoveSeekbar = false;
                    mViewer.goPage((double)(__currentProgress));
                }

                if(isSearching){
                    isSearching=false;
                    // TODO : after moving by searching data
                }
            }
        });

        for (int i = 0; i < viewerContainer.getChildCount(); i++) {
            mViewList.add(viewerContainer.getChildAt(i));
        }

        ttsControlPopup = new TTSControlPopup(BaseBookActivity.this, mViewer, mViewer.getWidth(), mViewer.getHeight());
    }

    void initViews() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        viewerContainer = (RelativeLayout) findViewById(R.id.viewerContainer);
        topNavLinear = (LinearLayout) findViewById(R.id.topNavLinear);
        bottomNavLinear = (LinearLayout) findViewById(R.id.bottomNavLinear);

        controlbox = (LinearLayout) findViewById(R.id.ly_audio);

//		Button fontSelect = (Button) findViewById(R.id.selectFont);
        Button audioSelect = (Button) findViewById(R.id.selectAudio);
        Button chapterSelect = (Button) findViewById(R.id.selectChapter);
        Button bookMark = (Button) findViewById(R.id.bookMark);
        Button addBookMark = (Button) findViewById(R.id.btn_bookmark);
        Button searchKeyword = (Button) findViewById(R.id.btn_search);
//		Button settings2 = (Button) findViewById(R.id.settings2);

        //		[scroll] s
        Button viewModeNone = (Button) findViewById(R.id.viewmode_none);
        viewModeNone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isVeilViewShow = true;
                BookHelper.animationType = 1;

                lyScrollPrev.setVisibility(View.GONE);
                lyScrollNext.setVisibility(View.GONE);

                mViewer.reLoadBook();
            }
        });
        Button viewModeScroll = (Button) findViewById(R.id.viewmode_scroll);
        viewModeScroll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isVeilViewShow = true;
                BookHelper.animationType = 3;
                mViewer.reLoadBook();
            }
        });

        //		[audio sample] s
        Button play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //				audio 재생 - 해당 audio 정보 파라미터로 전달
                if(currentPlayAudioContent==null)
                    mViewer.playAudioContent(audioContents.get(audioContentsOnCurrentPage.get(0)));
                else
                    mViewer.playAudioContent(audioContents.get(currentPlayAudioContent.getXPath()));
            }
        });
        Button pause = (Button) findViewById(R.id.pause);
        pause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //				audio 일시정지 - 해당 audio 정보 파라미터로 전달
                mViewer.pauseAudioContent(currentPlayAudioContent);
            }
        });
        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //				audio 정지 - 해당 audio 정보 파라미터로 전달
                mViewer.stopAudioContent(currentPlayAudioContent);
            }
        });
        Button prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //				이전 재생 - 기존 재생 중지 및 이전 audio 재생
                //				mViewer.stopAudioContent(audioContents.get(playIndex));
                //				playIndex--;
                //				if(audioContents.size()>0){
                //					if(playIndex<0)
                //						playIndex++;
                //					else
                //						mViewer.playAudioContent(audioContents.get(playIndex));
                //				}

                //				mViewer.moveAudioPlayingPosition(currentPlayAudioContent.getXPath(), -5);
                //				mViewer.setMediaOverlayJavascriptInterface();
            }
        });
        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //				다음 재생 - 기존 재생 중지 및 다음 audio 재생
                //				mViewer.stopAudioContent(audioContents.get(playIndex));
                //
                //				playIndex++;
                //
                //				if(audioContents.size()>0){
                //					if(playIndex>audioContents.size())
                //						playIndex--;
                //					else
                //						mViewer.playAudioContent(audioContents.get(playIndex));
                //				}
                mViewer.moveAudioPlayingPosition(currentPlayAudioContent.getXPath(), 5);

            }
        });


        Button capture = (Button) findViewById(R.id.btn_capture);
        capture.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//				layoutVisible = true;
//				toggleLayoutVisible();
//				Bitmap bitmap = viewerScreenShot(viewerContainer);
//				saveBitmapToFile(addWaterMark(bitmap), Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+System.currentTimeMillis()+".png");
//                if(BookHelper.animationType==0){
//                    mViewer.setPageEffect(1);
//                    Log.d("DEBUG"," setPageEffect 1");
//                }else if(BookHelper.animationType==1){
//                    mViewer.setPageEffect(0);
//                    Log.d("DEBUG"," setPageEffect 0");
//                }
                Toast.makeText(BaseBookActivity.this, mViewer.getCurrentUserAgent(), Toast.LENGTH_SHORT).show();
            }
        });

        //		loop = (Button) findViewById(R.id.loop);
//		loop.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				//			    반복 설정 - 해당 audio 정보 및 루프 여부 파라미터로 전달
//				mViewer.loopAudioContent(audioContents.get(audioContentsOnCurrentPage.get(0)), true);
//			}
//		});
        //		[audio sample] e

//		fontSelect.setOnClickListener(new OnClickCallback());
        audioSelect.setOnClickListener(new OnClickCallback());
        chapterSelect.setOnClickListener(new OnClickCallback());
        bookMark.setOnClickListener(new OnClickCallback());
        addBookMark.setOnClickListener(new OnClickCallback());
        searchKeyword.setOnClickListener(new OnClickCallback());
//		settings2.setOnClickListener(new OnClickCallback());

        //		Button goPage = (Button)findViewById(R.id.go_page);
        //		goPage.setOnClickListener(new OnClickListener() {
        //
        //			@Override
        //			public void onClick(View v) {
        //				EditText percent = (EditText)findViewById(R.id.move_percent);
        //				String per = percent.getText().toString();
        //				if( !per.equals("") ){
        //
        //					InputMethodManager imm = (InputMethodManager)mThis.getSystemService(Context.INPUT_METHOD_SERVICE);
        //					imm.hideSoftInputFromWindow(percent.getWindowToken(), 0);
        //
        //					percent.setText("");
        //
        //					mViewer.goPage(Double.parseDouble(per));
        //				}
        //			}
        //		});


        mTitle = (TextView) findViewById(R.id.titleText);

        veilView = (ImageView) findViewById(R.id.veilView);
        veilView.setImageResource(R.drawable.blank_white_shape);
        veilView.setVisibility(View.VISIBLE);
        veilView.bringToFront();

        loadProgress = (ProgressBar) findViewById(R.id.progressBar1);

        mIndicator = (TextView) findViewById(R.id.bookIndicator);

        mBookmark = (ImageView) findViewById(R.id.bookMarkSelector);
        mBookmarkDefault = (ImageView) findViewById(R.id.bookMarkDefault);
        mBookmarkDefault.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewer.doBookmark();
            }
        });

        mNotes = (Button) findViewById(R.id.noteSelector);
        mNotes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                AnnotationDialog ad = new AnnotationDialog(mThis, mViewer.getHighlights(), mViewer.getDocumentVersion(), mViewer);
                ad.show();

                ad.setOnAnnotationItem(new AnnotationDialog.OnAnnotationItem() {
                    @Override
                    public void onSelect(Highlight high) {
                        isVeilViewShow = true;
                        Toast.makeText(mThis, high.text, Toast.LENGTH_LONG).show();
                        mViewer.goPage(high);
                    }

                    @Override
                    public void onShow() {
                        DebugSet.d(TAG, "Annotation onShow");
                        mViewer.restoreHighlights();
                        mViewer.reLoadBook();
                    }
                });

            }
        });

        Button settings = (Button)findViewById(R.id.reading_style);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if( layoutVisible )
                    toggleLayoutVisible();

                mViewer.setPreviewMode(true);

                mViewer.getCurrentTopPath();

                mSettingsDialog = new SettingsDialog(BaseBookActivity.this);
                mSettingsDialog.show(viewerContainer, 0, 0);
                mSettingsDialog.setCurrentVolumeState(mVolumeUse);
                mSettingsDialog.setOnSettingsListener(new SettingsDialog.OnSettingsListener() {

                    @Override
                    public void onCancel() {
                        mViewer.setPreviewMode(false);
                    }

                    @Override
                    public void onClose(boolean changed) {

                        mViewer.setPreviewMode(false);

                        if( changed ) {
                            mViewer.reLoadBook();
                        }
                    }

                    @Override
                    public void onFontBigger(int value) {
                        mViewer.changeFontSizeDirect(""+value+"%");
                    }

                    @Override
                    public void onFontSmaller(int value) {
                        mViewer.changeFontSizeDirect(""+value+"%");
                    }

                    @Override
                    public void onFontChange(String fontName, String faceName, String fontPath) {
                        mViewer.changeFontDirect(fontName, faceName, fontPath);
                    }

                    @Override
                    public void onBackgroundColorChange(int color, boolean nightMode) {
                        if(nightMode)
                            BookHelper.nightMode=1;
                        else
                            BookHelper.nightMode=0;

                        mViewer.changeBackgroundColorDirect(color, nightMode);

                        viewerContainer.setBackgroundColor(color);
                        veilView.setBackgroundColor(color);
                    }

                    @Override
                    public void onLineSpacing(int value) {
                        mViewer.changeLineHeightDirect(""+value+"%");
                    }

                    @Override
                    public void onParagraphSpacing(int value) {
                        mViewer.changeParaHeightDirect(""+value+"%");
                    }

                    @Override
                    public void onMargin(int left, int top, int right, int bottom) {
                        mViewer.changeMarginDirect(left, top, right, bottom);
                    }

                    @Override
                    public void onIndent(boolean onoff) {
                        if(onoff)
                            BookHelper.setIndent(1);
                        else
                            BookHelper.setIndent(0);
                        mViewer.changeIndentDirect(onoff);
                    }

                    @Override
                    public void onInitBook() {

                        mViewer.setDefaultReadingStyle();

                        mSettingsDialog.hide();

                        mViewer.setPreviewMode(false);

                        mViewer.reLoadBook();
                    }

                    @Override
                    public void onVolume(boolean useVolumeKey) {
                        mVolumeUse = useVolumeKey;
//                        mViewer.setUseVolumeKey(mVolumeUse);
                    }

                });
            }
        });

        Button ttsBtn = (Button)findViewById(R.id.tts);
        ttsBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//				ttsControlPopup = new TTSControlPopup(BaseBookActivity.this, mViewer, mViewer.getWidth(), mViewer.getHeight());
                if(ttsDataInfoArrayList.isEmpty())
                    ttsDataInfoArrayList = mViewer.makeTTSDataInfo();
                ttsControlPopup.setTTSData(ttsDataInfoArrayList);
                ttsControlPopup.showPopup(mViewer);
                mViewer.preventPageMove(true);
                //				[mediacontrol sample] s
                // TTS 재생 시 미디어 컨트롤 방지 여부 설정
                // true : 미디어 컨트롤 불가
                // false : 미디어 컨트롤 가능
                // 시나리오에 따라 설정 시점이 변경될 수 있으며 미디어 제어를 원하는 시점에는 false로 재설정 필요함
                mViewer.setPreventMediaControl(true);
                //				[mediacontrol sample] e
            }
        });

        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        mSeekbar.setMax(100);
        mSeekbar.setProgress(0);

        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if( fromUser ) {
                    ChapterInfo cp = mViewer.getChapterFromSpineIndex(mViewer.getSpineIndexFromPercent(progress));
                    String text = "" + (progress) + "%" + "  " + cp.getChapterName();
                    mToast.setText(text + " - " +(mViewer.getSpineIndexFromPercent(progress)+1));
                    mToast.show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                __currentProgress = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isVeilViewShow = true;
                int progress = seekBar.getProgress();
                if(  progress != __currentProgress ) {
                    isMoveSeekbar = true;
                    __currentProgress = progress;
                    mViewer.goPageByJump();
                }
            }

        });

        mLightSeekBar = (SeekBar) findViewById(R.id.light_bar);
        mLightSeekBar.setMax(255);
        mLightSeekBar.setProgress(255);
        mLightSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    WindowManager.LayoutParams laypam = getWindow().getAttributes();
                    laypam.screenBrightness = seekBar.getProgress() / (float) 255;
                    getWindow().setAttributes(laypam);
                }
            }
        });

        // ////////////////////////////////////////////////////////
        //
        // EPubViewer events & setup
        //
        // ////////////////////////////////////////////////////////

        mViewer = (ViewerContainer) findViewById(R.id.ePubViewer1);
        mViewer.setBackgroundColor(Color.WHITE);
        mViewer.setUseEPUB3Viewer(true);
        mViewer.setIgnoreDrm(true);
        mViewer.setOnDecodeContent(new ViewerContainer.OnDecodeContent() {

            @Override
            public String onDecode(String drmKey, String decodeFile) {
                DebugSet.d(TAG, "Viewer decode : " + drmKey + " | " + decodeFile);
                Log.d("SSIN","onDecode in decodeFile : "+decodeFile);
                return null;
            }

//			@Override
//			public String onDecryptImage(String filePath, String key, String algorithm) {		//TODO ::: comic
//				return decryptImage(getImage(filePath));
//			}

        });

        lyScrollNext = (RelativeLayout) findViewById(R.id.ly_scroll_next);
        lyScrollPrev = (RelativeLayout) findViewById(R.id.ly_scroll_prev);
        btnScrollNext  = (Button) findViewById(R.id.btn_scroll_next);
        bntScrollPrev  = (Button) findViewById(R.id.btn_scroll_prev);
        bntScrollPrev.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewer.scrollPrior();
                lyScrollPrev.setVisibility(View.GONE);
            }
        });
        btnScrollNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewer.scrollNext();
                lyScrollNext.setVisibility(View.GONE);
            }
        });

        mViewer.setPreload(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (resultCode == RESULT_OK) {

            switch(requestCode){

                case REQUEST_MEMO_ACTIVITY: {
//                    mViewer.doMemo();
                    hideSelectionMenu(mContextMenu);
                    break;
                }

                case 2: { //SHARE
                    Toast.makeText(mThis, "Share", Toast.LENGTH_SHORT).show();
                    break;
                }

                case 3: {
                    break;
                }

                case REQUEST_CODE_SEARCH :
                    //	[ssin-search] s
                    SearchResult search_data = (SearchResult) data.getSerializableExtra("SEARCH_DATA");
                    if(search_data!=null){
                        isSearching = true;
                        mViewer.focusText(search_data);
                    } else{
                        isSearching = false;
                    }
                    break;
                //	[ssin-search] e
            }
        }
    }
    boolean isSearching = false;

    void toggleBookmarkVisible(boolean bShow) {

        if (!bShow && !mBookmark.isShown() && !(mBookmark.getVisibility() == View.VISIBLE))
            return;

        if (!bShow) {
            // off
            AlphaAnimation alphaanimation = new AlphaAnimation(1F, 0.1F);
            alphaanimation.setDuration(200L);
            mBookmark.startAnimation(alphaanimation);

            mBookmark.setVisibility(View.INVISIBLE);
        } else {
            // on
            int i = -mBookmark.getHeight();
            int j = mBookmark.getPaddingTop();
            int k = i - j;
            // int l = 25;
            // float f = k - l;
            float f1 = mBookmark.getPaddingTop();

            TranslateAnimation translateanimation = new TranslateAnimation(0F,
                    0F, k, f1);
            translateanimation.setDuration(500L);
            mBookmark.startAnimation(translateanimation);

            mBookmark.setVisibility(View.VISIBLE);
        }
    }

    void toggleLayoutVisible() {

        if (!layoutVisible) {

            topNavLinear.setVisibility(View.VISIBLE);
            bottomNavLinear.setVisibility(View.VISIBLE);

            layoutVisible = true;
        } else {
            topNavLinear.setVisibility(View.INVISIBLE);
            bottomNavLinear.setVisibility(View.INVISIBLE);

            layoutVisible = false;
        }
    }
    @Override
    public void run() {
        try {

            String bookName = mBookName;  //BookHelper.getOnlyFilename(mBookName);
            String fullName = mPath + bookName;

            File file = new File(fullName);
            if( file.isDirectory() ) {
                mViewer.startEpubParse(fullName);
                mMainHandler.sendEmptyMessage(EPUB_PARSE_FINISH);
            }
            else {

                switch (BookHelper.getBookType(bookName)) {
                    case 3: // .zip extension
                    case 2: {
                        Log.e(TAG, "unZipFile >>>>>>>>>>> ");
                        String res = MyZip.unZipFile(fullName, mPath, true, null);
                        if (res == null) {
                            fullName = mPath + BookHelper.getOnlyFilename(bookName);

                            mViewer.startEpubParse(fullName);

                            mMainHandler.sendEmptyMessage(EPUB_PARSE_FINISH);

                        } else {
                            veilView.setVisibility(View.INVISIBLE);
                            toggleProgress(false);
                        }
                        break;
                    }

                    default: { // txt file
                        mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
                    }
                }
            }


        } catch(XmlPackageException e) {
            mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
            e.printStackTrace();
        } catch (XmlContainerException e) {
            mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
            e.printStackTrace();
        } catch (EpubFileSystemException e) {
            mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
            e.printStackTrace();
        } catch (XmlNavigationException e) {
            mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
            e.printStackTrace();
        }
    }

    Bitmap viewerScreenShot(View view) {

        try {

            int width = view.getWidth();
            int height = view.getHeight();

            Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            view.layout(0, 0, width, height);
            view.draw(c);

            return b;
        }
        catch (Exception e) {
        }
        catch( OutOfMemoryError e ) {
        }

        System.gc();

        Bitmap b = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
        return b;
    }

    private void printMemory(String tag) {
        DebugSet.i("Memory",
                tag + " : "
                        + "---------------------------------------------------------- ");
        DebugSet.i("Memory",
                tag + " : " + " max " + ((Runtime.getRuntime().maxMemory())));
        DebugSet.i("Memory",
                tag + " : " + " total " + (Runtime.getRuntime().totalMemory()));
        DebugSet.i("Memory",
                tag + " : " + " free " + (Runtime.getRuntime().freeMemory()));
        DebugSet.i("Memory", tag
                + " : "
                + " total - free "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                .freeMemory()));
        DebugSet.i("Memory",
                tag
                        + " : "
                        + "---------------------------------------------------------- ");
        DebugSet.i("Memory", tag + " : " + " getNativeHeapSize "
                + Debug.getNativeHeapSize());
        DebugSet.i("Memory", tag + " : " + " getNativeHeapFreeSize "
                + Debug.getNativeHeapFreeSize());
        DebugSet.i(
                "Memory",
                tag + " : " + " getNativeHeapAllocatedSize "
                        + Debug.getNativeHeapAllocatedSize());
        DebugSet.i("Memory",
                tag
                        + " : "
                        + " --------------------------------------------------------- ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewer.destroy();
        System.gc();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DEBUG","onPause()");
        mViewer.saveAllViewerData();
        if(ttsControlPopup!=null){
            ttsControlPopup.isPaused = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG","onResume()");
//        mViewer.reloadPoster();
        if(ttsControlPopup!=null && ttsControlPopup.isShowing()){
            ttsControlPopup.isPaused=false;
            if(ttsControlPopup.getChapterChanged()) {
                ttsControlPopup.moveByTTSData();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DEBUG","SSIN onStart()");
    }

    private class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case LOAD_BOOK_EPUB: {

                    ReadpositionData readPositionData = mViewer.getReadpositionData(BookHelper.getRootPath());
                    if(readPositionData!=null)
                        Toast.makeText(BaseBookActivity.this, readPositionData.getTotalPercent()+" 읽음", Toast.LENGTH_SHORT).show();

                    String title = mViewer.getBookTitle();
                    String author = mViewer.getBookCreator();

                    if (author.length() > 0) {
                        author = " - " + author;
                    }

                    mTitle.setText(title + author);

                    if(BookHelper.backgroundColor==null && BookHelper.nightMode==1)
                        BookHelper.nightMode=0;

                    mViewer.showBook();

                    break;
                }

                case EPUB_PARSE_FINISH : {

                    try {
                        mViewer.initView();
                    } catch (XmlPackageException e) {
                        e.printStackTrace();
                        mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
                    } catch (XmlContainerException e) {
                        e.printStackTrace();
                        mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
                    } catch (EpubFileSystemException e) {
                        e.printStackTrace();
                        mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
                    }

                    settingViewer();

                    // [ssin] s:temp - for test
                    ReadingStyle readingStyle = new ReadingStyle(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            18,
                            35,
                            18,
                            18);

                    mViewer.setDefaultReadingStyle(readingStyle);

                    String bookName = mBookName;
                    String fullName = mPath + bookName;
                    if( !mViewer.loadBook(fullName)) {
                        mMainHandler.sendEmptyMessage(EPUB_LOAD_ERROR);
                    }
                    else {
                        mMainHandler.sendEmptyMessage(LOAD_BOOK_EPUB);
                    }
                    break;
                }

//                case LOAD_FORCE_CHANGING: {
//                    mViewer.forceChapterChanging();
//                    break;
//                }

                case EPUB_LOAD_ERROR: {

                    Toast.makeText(mThis, "load failure : " + mBookName, Toast.LENGTH_LONG).show();

                    finish();
                    break;
                }

                case MSG_SCREEN_HIDE: {
                    veilView.setImageResource(R.drawable.blank_white_shape);
                    veilView.setVisibility(View.VISIBLE);
                    toggleProgress(true);
                    break;
                }

            }
        }
    }
    private final int REQUEST_MEMO_ACTIVITY 	= 1;
    private class SelectionItemHandler implements OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.textHighlight: {
//                    do_highlight();
                    mViewer.addAnnotation();    // TODO :: new custom selection modified
                    break;
                }

                case R.id.textMemo: {
//                    do_memo();
                    // TODO :: new custom selection modified
                    Intent intent = new Intent(getApplicationContext(), MemoWriteActivity.class);
                    intent.putExtra("SELECTED_ANNOTATION","");
                    intent.putExtra("MEMO_CONTENT","");
                    startActivityForResult(intent, REQUEST_MEMO_ACTIVITY);
                    break;
                }

                case R.id.textShare: {
                    Toast.makeText(mThis, "공유 : "+mViewer.getSelectedText(), Toast.LENGTH_SHORT).show();
                    mViewer.finishTextSelectionMode();
                    break;
                }

                case R.id.textSpeech: {

                    if(ttsDataInfoArrayList.isEmpty())
                        ttsDataInfoArrayList = mViewer.makeTTSDataInfo();

                    if(ttsControlPopup!=null && !ttsDataInfoArrayList.isEmpty()){
                        mViewer.setPreventMediaControl(true);
                        ttsControlPopup.setTTSDataFromSelection(ttsDataInfoArrayList);
                        mViewer.requestTTSDataFromSelection();
                    }

                    ttsControlPopup.showPopup(mViewer);
                    mViewer.preventPageMove(true);

//                if(ttsControlPopup!=null)
//				    mViewer.requestTTSDataFromSelection();
//                else
//                    Toast.makeText(mThis, "tts mode off", Toast.LENGTH_SHORT).show();
                    break;
                }

                case R.id.deleteAnnotation: {
//                    do_highlight();
                    mViewer.deleteAnnotation(); // TODO :: new custom selection modified
                    break;
                }

                case R.id.backColor1:
                case R.id.backColor2:
                case R.id.backColor3:
                case R.id.backColor4:
                case R.id.backColor5: {
                    int clrIndex = v.getId() - R.id.backColor1;
//                    mViewer.changeHighlightColorDirect(clrIndex);
                    mViewer.modifyAnnotationColorAndRange(clrIndex);    // TODO :: new custom selection modified
                    break;
                }

                case R.id.addMemo: {
                    Toast.makeText(mThis, "메모추가", Toast.LENGTH_SHORT).show();
//                    do_memo();
                    // TODO :: new custom selection modified
                    mViewer.requestAllMemoText();

                    break;
                }

                case R.id.text_next_page: {
                    // TODO :: new custom selection added
                    mViewer.selectionContinue(false);
                    break;
                }
                case R.id.text_continue_highlight: {
                    // TODO :: new custom selection added
                    mViewer.selectionContinue(true);
                    break;
                }
            }
        }
    }


    PopupWindow createPopup(int resId, BookHelper.ContextMenuType contextMenuType) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ctxView = inflater.inflate(resId, null, false);
        ctxView.findViewById(R.id.textHighlight).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.textMemo).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.textShare).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.textSpeech).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.text_search).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.text_next_page).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.text_continue_highlight).setOnClickListener( new SelectionItemHandler());

        if(contextMenuType == BookHelper.ContextMenuType.TYPE_NEW ) {
            ctxView.findViewById(R.id.text_next_page).setVisibility(View.GONE);
            ctxView.findViewById(R.id.text_continue_highlight).setVisibility(View.GONE);
        } else if(contextMenuType == BookHelper.ContextMenuType.TYPE_NEW_CONTINUE){
            ctxView.findViewById(R.id.text_continue_highlight).setVisibility(View.GONE);
        } else if(contextMenuType == BookHelper.ContextMenuType.TYPE_CONTINUE){
            ctxView.findViewById(R.id.textHighlight).setVisibility(View.GONE);
            ctxView.findViewById(R.id.textMemo).setVisibility(View.GONE);
            ctxView.findViewById(R.id.textShare).setVisibility(View.GONE);
            ctxView.findViewById(R.id.textSpeech).setVisibility(View.GONE);
            ctxView.findViewById(R.id.text_next_page).setVisibility(View.GONE);
            ctxView.findViewById(R.id.text_search).setVisibility(View.GONE);
        } else if(contextMenuType == BookHelper.ContextMenuType.TYPE_EXTRA){
            ctxView.findViewById(R.id.textHighlight).setVisibility(View.GONE);
            ctxView.findViewById(R.id.textMemo).setVisibility(View.GONE);
            ctxView.findViewById(R.id.text_next_page).setVisibility(View.GONE);
            ctxView.findViewById(R.id.text_continue_highlight).setVisibility(View.GONE);
        }

        ctxView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int cx = ctxView.getMeasuredWidth();
        int cy = ctxView.getMeasuredHeight();

        PopupWindow pw = new PopupWindow(ctxView, cx, cy, false);
        pw.setOutsideTouchable(true);

        return pw;
    }

    PopupWindow createPopup2(int resId, BookHelper.ContextMenuType contextMenuType) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ctxView = inflater.inflate(resId, null, false);
        ctxView.findViewById(R.id.backColor1).setOnClickListener( new SelectionItemHandler() );
        ctxView.findViewById(R.id.backColor2).setOnClickListener( new SelectionItemHandler() );
        ctxView.findViewById(R.id.backColor3).setOnClickListener( new SelectionItemHandler() );
        ctxView.findViewById(R.id.backColor4).setOnClickListener( new SelectionItemHandler() );
        ctxView.findViewById(R.id.backColor5).setOnClickListener( new SelectionItemHandler() );
        ctxView.findViewById(R.id.addMemo).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.textSpeech).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.deleteAnnotation).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.textShare).setOnClickListener( new SelectionItemHandler());
        ctxView.findViewById(R.id.text_next_page).setOnClickListener( new SelectionItemHandler());

        if(contextMenuType != BookHelper.ContextMenuType.TYPE_MODIFY_CONTINUE ) {
            ctxView.findViewById(R.id.text_next_page).setVisibility(View.GONE);
        }

        ctxView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int cx = ctxView.getMeasuredWidth();
        int cy = ctxView.getMeasuredHeight();

        PopupWindow pw = new PopupWindow(ctxView, cx, cy, false);
        pw.setOutsideTouchable(true);
        return pw;
    }

    PopupWindow showMemoPopup(int resId, String memo, int colorIndex) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        View ctxView = inflater.inflate(resId, null, false);

        ctxView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int cx = windowManager.getDefaultDisplay().getWidth();
        int cy = ctxView.getMeasuredHeight();

        PopupWindow pw = new PopupWindow(ctxView, cx, cy, false);
        pw.setOutsideTouchable(true);

        ((TextView) ctxView.findViewById(R.id.tv_memo)).setText(Uri.decode(memo));

        ((ScrollView) ctxView.findViewById(R.id.sv_memo)).setBackgroundColor(Color.parseColor(BookHelper.Colors[colorIndex]));

        ctxView.findViewById(R.id.btn_yellow).setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
//                mViewer.changeHighlightColorDirect(0);
                mViewer.modifyAnnotationColorAndRange(2);    // TODO :: new custom selection modified
            }
        });

        ctxView.findViewById(R.id.btn_green).setOnClickListener(  new OnClickListener() {

            @Override
            public void onClick(View v) {
//                mViewer.changeHighlightColorDirect(1);
                mViewer.modifyAnnotationColorAndRange(3);    // TODO :: new custom selection modified
            }
        });

        ctxView.findViewById(R.id.btn_violet).setOnClickListener(  new OnClickListener() {

            @Override
            public void onClick(View v) {
//                mViewer.changeHighlightColorDirect(2);
                mViewer.modifyAnnotationColorAndRange(4);    // TODO :: new custom selection modified
            }
        });

        ctxView.findViewById(R.id.btn_orange).setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
//                mViewer.changeHighlightColorDirect(3);
                mViewer.modifyAnnotationColorAndRange(0);    // TODO :: new custom selection modified
            }
        });

        ctxView.findViewById(R.id.btn_pink).setOnClickListener(  new OnClickListener() {

            @Override
            public void onClick(View v) {
//                mViewer.changeHighlightColorDirect(4);
                mViewer.modifyAnnotationColorAndRange(1);    // TODO :: new custom selection modified
            }
        });

        ctxView.findViewById(R.id.btn_edit).setOnClickListener(  new OnClickListener() {

            @Override
            public void onClick(View v) {
                mContextMenu.dismiss();

                mViewer.requestAllMemoText();   // TODO :: new custom selection - modified
//                MemoDialog md = new MemoDialog(mThis, mCurrentHighlight, R.style.myDialog, mViewer);
//                md.show();
//
//                md.setOnMemoClose(new MemoDialog.OnMemoClose() {
//                    @Override
//                    public void onClose(Highlight high, String memo, boolean isEdit) {
//                        mViewer.requestShowMemo(high, isEdit);
//                    }
//                });
            }
        });

        ctxView.findViewById(R.id.btn_delete).setOnClickListener(  new OnClickListener() {

            @Override
            public void onClick(View v) {
                mContextMenu.dismiss();
//                mViewer.doHighlight();
                mViewer.deleteAnnotation();  // TODO :: new custom selection - modified
            }
        });

        return pw;
    }

    int[] coords = null;
    void showSelectionMenu(PopupWindow pw, int x, int y, boolean hasMemo) {

        if( pw == null ) return;

        View ctxView = pw.getContentView();
        int cx = ctxView.getMeasuredWidth();
        int cy = ctxView.getMeasuredHeight();

        if( coords == null )
            coords = new int[2];
        mViewer.getLocationInWindow(coords);

        // TODO :: new custom selection
        // TODO :: FRONT - x,y,를 기준으로 메뉴가 가운데 놓이도록 구현 필요

//        int screenY = y - (cy + 50);
//        screenY += coords[1];
//
//        if (y - (cy + 50) < 0) {
//            screenY = y + cy + 50 + coords[1];
//        }
//        if (y + cy + cy + 50 > mViewer.getHeight()) {
//            screenY = y - cy - 50 + coords[1];
//        }
//
//        if (screenY < 0) {
//            screenY = 0;
//        }

        if(hasMemo)
            pw.showAtLocation(mViewer, Gravity.BOTTOM, 0, 0);
        else
            pw.showAtLocation(findViewById(R.id.ePubViewer1), Gravity.NO_GRAVITY, x, y);
    }

    void hideSelectionMenu(PopupWindow pw) {
        if( pw == null ) return;
        pw.dismiss();
    }

    void toggleProgress(boolean visible) {
        loadProgress.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        if( visible ) {
            loadProgress.bringToFront();
        }
    }

    private void showDialog(final String xPath){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("test").setCancelable(
                false).setPositiveButton("tts",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setNegativeButton("audio",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mViewer.setPreventMediaControl(false);
                        mViewer.playAudioContent(audioContents.get(xPath));
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("test");
        alert.show();
    }

    private void saveBitmapToFile(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try{
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }catch (Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                out.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private Bitmap addWaterMark(Bitmap src) {
        int w = viewerContainer.getWidth();
        int h = viewerContainer.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(BookHelper.textSelectionColor);

        Bitmap waterMark = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.logo);
        canvas.drawBitmap(waterMark, 0, 0, null);
        paint.setTextSize(200);
        canvas.drawText("TEST", 300, 300, paint);

        return result;
    }


//	private class BroadcastsHandler extends BroadcastReceiver {
//		
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			
//			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
//				
//				int state = intent.getIntExtra("state", -1);
//				
//				switch(state) {
//				case 0: // 헤드셋 해제
//					mViewer.unpluggedHeadSet(true);
//					break;
//				case 1: // 헤드셋 연결
////					mViewer.changeHeadsetState(true);
//					break;
//				}
//			}
//		}
//	}

//    	[ssin-comic] s
//    private String strKey = "AAAAAAAAAAAAAAAA";
//    private byte[] imageToEncrypted;
//    private byte[] imageToDecrypted;
//    private byte[] imagePlain;
//    private transient byte[] imageDecrypted;
//    private transient byte[] imageEncrypted;
//    private static final int BYTES_TO_ENCRYPT = 128;
//    private static final int BYTES_TO_DECRYPT = 144;
//    private byte[] getImage(String filePath){ //TODO ::: comic
//        File file = new File(filePath);
//        byte[] imageArr = new byte[(int) file.length()];
//        try {
//            FileInputStream fileInputStream = new FileInputStream(file);
//            fileInputStream.read(imageArr);
//            fileInputStream.close();
//        }  catch (Exception e){
//            e.printStackTrace();
//        }
//        return imageArr;
//    }
//
//    private String decryptImage(byte[] encryptedImage){	//TODO ::: comic
//
//        int bytesLeftOver = (int) (encryptedImage.length - BYTES_TO_DECRYPT);
//
//        imageToDecrypted = new byte[BYTES_TO_DECRYPT];
//        imagePlain = new byte[bytesLeftOver];
//
//        System.arraycopy(encryptedImage, 0, imageToDecrypted, 0, BYTES_TO_DECRYPT);
//        if (bytesLeftOver > 0)
//            System.arraycopy(encryptedImage, BYTES_TO_DECRYPT, imagePlain, 0, bytesLeftOver);
//
//        try {
//            imageDecrypted = decryptAES(imageToDecrypted, strKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        byte[] decryptedImage = new byte[imageDecrypted.length + imagePlain.length];
//        System.arraycopy(imageDecrypted, 0, decryptedImage, 0, imageDecrypted.length);
//        if (imagePlain != null && imagePlain.length > 0)
//            System.arraycopy(imagePlain, 0, decryptedImage, imageDecrypted.length, imagePlain.length);
//
//        String strDecode = new String(decryptedImage);
//
//        return strDecode;
//    }
//
//
//    private byte[] decryptAES(byte[] byteToDecrypt, String key) throws Exception {
//        try {
//            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//            return cipher.doFinal(byteToDecrypt);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}